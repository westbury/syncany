/*
 * Syncany, www.syncany.org
 * Copyright (C) 2011-2015 Philipp C. Heckel <philipp.heckel@gmail.com> 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.syncany.operations.down.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.syncany.config.Config;
import org.syncany.database.FileContent;
import org.syncany.database.FileVersion;
import org.syncany.database.ChunkEntry.ChunkChecksum;
import org.syncany.database.FileVersion.FileStatus;
import org.syncany.database.MultiChunkEntry.MultiChunkId;
import org.syncany.database.MemoryDatabase;
import org.syncany.database.SqlDatabase;
import org.syncany.operations.Assembler;
import org.syncany.operations.Downloader;
import org.syncany.plugins.transfer.StorageException;
import org.syncany.plugins.transfer.TransferManager;
import org.syncany.plugins.transfer.TransferManagerFactory;
import org.syncany.plugins.transfer.features.PathAware;
import org.syncany.plugins.transfer.features.ReadAfterWriteConsistent;
import org.syncany.plugins.transfer.features.Retriable;
import org.syncany.plugins.transfer.features.TransactionAware;
import org.syncany.plugins.transfer.features.TransactionAwareFeatureTransferManager;

public class ChangeFileSystemAction extends FileCreatingFileSystemAction {

	private Downloader downloader;
	private SqlDatabase localDatabase;

	public ChangeFileSystemAction(Config config, MemoryDatabase winningDatabase, SqlDatabase localDatabase, Assembler assembler, FileVersion fromFileVersion,
			FileVersion toFileVersion) throws StorageException {
		super(config, winningDatabase, assembler, fromFileVersion, toFileVersion);
		
		this.localDatabase = localDatabase;
		
		TransactionAwareFeatureTransferManager regularFileTransferManager = TransferManagerFactory
				.build(config)
				.withFeature(ReadAfterWriteConsistent.class)
				.withFeature(Retriable.class)
				.withFeature(PathAware.class)
				.withFeature(TransactionAware.class)
				.as(TransactionAware.class);

		TransferManager transferManager = regularFileTransferManager;
		this.downloader = new Downloader(config, transferManager );
	}
	
	@Override
	public FileSystemActionResult execute() throws Exception {
		boolean fromFileExists = fileExists(fileVersion1);
		boolean fromFileMatches = fromFileExists && fileAsExpected(fileVersion1);
		
		boolean toFileExists = fileExists(fileVersion2);
		boolean toFileMatches = toFileExists && fileAsExpected(fileVersion2);
		
		boolean filesAtSameLocation = fileVersion1.getPath().equals(fileVersion2.getPath());

		if (fromFileMatches && !toFileMatches) { // Normal case	
			// Original file matches, so we can delete it
			// Create conflict file for winning file, if it exists
			if (!toFileExists) {
				logger.log(Level.INFO, "     - (1) Original file matches, target file does NOT match: deleting original file, creating target file at: "+fileVersion2);
				
				deleteFile(fileVersion1);	
				createFileFolderOrSymlink(fileVersion2);						
			}
			else {
				logger.log(Level.INFO, "     - (2) Original file matches, target file does NOT match (EXISTS!): deleting original file, creating conflict file and creating target file at: "+fileVersion2);
				
				deleteFile(fileVersion1);	
				moveToConflictFile(fileVersion2);
				createFileFolderOrSymlink(fileVersion2);										
			}				
		}
		else if (fromFileMatches && toFileMatches) {
			// Original file matches, so we can delete it
			// Nothing to do for winning file, matches
			
			if (!filesAtSameLocation) {
				logger.log(Level.INFO, "     - (3) Original file matches, target file matches, and they are not the same: deleting orig. file, nothing else!");
				deleteFile(fileVersion1);
			}
			else {
				logger.log(Level.INFO, "     - (4) Original file matches, target file matches, but they are in the same location (!!): Do nothing!");				
			}
		}
		else if (!fromFileMatches && toFileMatches) {
			// Leave original file untouched. Will be untracked from now on
			// Nothing to do for winning file, matches
			
			logger.log(Level.INFO, "     - (5) Original does NOT match, target file matches: Leaving orig. file untouched. Do nothing!");				
		}
		else if (!fromFileMatches && !toFileMatches) {
			// Leave original file untouched. Will be untracked from now on
			// Create conflict file for winning file, if it exists
			
			if (toFileExists) {
				logger.log(Level.INFO, "     - (6) Original does NOT match, target file does NOT match, but exists: Creating conflict file, and creating file at: "+fileVersion2);
				
				// attempt merge first
				File actualLocalFile = getAbsolutePathFile(fileVersion2.getPath());

				File commonAncestorFile = assembleFileToCache(fileVersion2);	
				
				
				
				Set<MultiChunkId> unknownMultiChunks = new HashSet<MultiChunkId>(determineMultiChunksToDownload(fileVersion1, winningDatabase));
				
				downloader.downloadAndDecryptMultiChunks(unknownMultiChunks);

				
				
				File latestRemoteFile = assembleFileToCache(fileVersion1);

				boolean canMerge = merge(latestRemoteFile, actualLocalFile, commonAncestorFile);
				if (!canMerge) {
				moveToConflictFile(fileVersion2);
				createFileFolderOrSymlink(fileVersion2);
				
				}
			}
			else {
				if (fileVersion2.getStatus() == FileStatus.DELETED) {
					logger.log(Level.INFO, "     - (7) Original does NOT match, target file does not exist (and SHOUDN'T): Nothing to do!");					
				}
				else {
					logger.log(Level.INFO, "     - (8) Original does NOT match, target file does not exist: Creating file at: "+fileVersion2);
					createFileFolderOrSymlink(fileVersion2);
				}
			}
		}
		
		return new FileSystemActionResult();
		
	}

	/**
	 * Finds the multichunks that need to be downloaded for the given file version -- using the local 
	 * database and given winners database. Returns a set of multichunk identifiers.
	 */
	private Collection<MultiChunkId> determineMultiChunksToDownload(FileVersion fileVersion, MemoryDatabase winnersDatabase) {
		Set<MultiChunkId> multiChunksToDownload = new HashSet<MultiChunkId>();

		// First: Check if we know this file locally!
		List<MultiChunkId> multiChunkIds = localDatabase.getMultiChunkIds(fileVersion.getChecksum());
		
		if (multiChunkIds.size() > 0) {
			multiChunksToDownload.addAll(multiChunkIds);
		}
		else {
			// Second: We don't know it locally; must be from the winners database
			FileContent winningFileContent = winnersDatabase.getContent(fileVersion.getChecksum());			
			boolean winningFileHasContent = winningFileContent != null;

			if (winningFileHasContent) { // File can be empty!
				List<ChunkChecksum> fileChunks = winningFileContent.getChunks(); 
				
				// TODO [medium] Instead of just looking for multichunks to download here, we should look for chunks in local files as well
				// and return the chunk positions in the local files ChunkPosition (chunk123 at file12, offset 200, size 250)
				
				Map<ChunkChecksum, MultiChunkId> checksumsWithMultiChunkIds = localDatabase.getMultiChunkIdsByChecksums(fileChunks);
				
				for (ChunkChecksum chunkChecksum : fileChunks) {
					MultiChunkId multiChunkIdForChunk = checksumsWithMultiChunkIds.get(chunkChecksum);
					if (multiChunkIdForChunk == null) {
						multiChunkIdForChunk = winnersDatabase.getMultiChunkIdForChunk(chunkChecksum);
						
						if (multiChunkIdForChunk == null) {
							throw new RuntimeException("Cannot find multichunk for chunk "+chunkChecksum);	
						}
					}
					
					if (!multiChunksToDownload.contains(multiChunkIdForChunk)) {
						logger.log(Level.INFO, "  + Adding multichunk " + multiChunkIdForChunk + " to download list ...");
						multiChunksToDownload.add(multiChunkIdForChunk);
					}
				}
			}
		}
		
		return multiChunksToDownload;
	}
	
	private boolean merge(File latestRemoteFile, File localFile, File commonAncestorFile) {
		try {
			InputStream in1 = new FileInputStream(latestRemoteFile);
			byte[] buffer = new byte[4000];
			in1.read(buffer);
			System.out.println("lastest remote file: \n" + new String(buffer));
			in1.close();

			InputStream in2 = new FileInputStream(localFile);
//			byte[] buffer = new byte[4000];
			in2.read(buffer);
			System.out.println("local file:\n" + new String(buffer));
			in2.close();

			InputStream in3 = new FileInputStream(commonAncestorFile);
//			byte[] buffer = new byte[4000];
			in3.read(buffer);
			System.out.println("common ancestor file:\n" + new String(buffer));
			in3.close();
		}
		catch (FileNotFoundException e) {
			// TODO Log this, then fall thru
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toString() {
		return "ChangeFileSystemAction [file1=" + fileVersion1 + ", file2=" + fileVersion2 + "]";
	}				
}	
