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
package org.syncany.plugins.merge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.syncany.util.FileUtil;

/**
 * This class implements a merger that 'merges' conflicting
 * files by keeping the remote version and saving away the
 * local version in a file with a different name.
 * <P>
 * This implementation was the only implementation before
 * file merging was implemented.
 * 
 * @author Nigel Westbury
 *
 */
public class KeepRemoteDefaultMerger implements Merger {
	protected static final Logger logger = Logger.getLogger(KeepRemoteDefaultMerger.class.getSimpleName());

	String userName;
	
	public KeepRemoteDefaultMerger(String userName) {
		this.userName = userName;
	}
	
	@Override
	public boolean merge(FileVersionContent latestRemoteFile, File localFile, FileVersionContent commonAncestorFile) {
		try {
			moveToConflictFile(localFile);
		}
		catch (IOException e) {
			// If we can't save it, just ignore and lose it????
		}

		try {
			FileUtils.moveFile(latestRemoteFile.getFile(), localFile);
		}
		catch (FileExistsException e) {
			logger.log(Level.FINE, "File already existed", e);
			throw new RuntimeException("What to do here?!");
		}
		catch (Exception e) {
			throw new RuntimeException("What to do here?!");
		}

		// This merger can always do the merge (unless an exception is throw)
		return true;
	}

	protected void moveToConflictFile(File conflictingPath) throws IOException {
		if (!FileUtil.exists(conflictingPath)) {
			logger.log(Level.INFO, "     - Creation of conflict file not necessary. Locally conflicting file vanished from " + conflictingPath);
			return;
		}

		int attempts = 0;

		while (attempts++ < 10) {
			File conflictedCopyPath = null;

			try {
				conflictedCopyPath = findConflictFilename(conflictingPath);
				logger.log(Level.INFO, "     - Local version conflicts, moving local file " + conflictingPath + " to " + conflictedCopyPath + " ...");

				FileUtils.moveFile(conflictingPath, conflictedCopyPath);

				// Success!
				break;
			}
			catch (FileExistsException e) {
				logger.log(Level.SEVERE, "     - Cannot create conflict file; attempt = " + attempts + " for file: " + conflictedCopyPath, e);
			}
			catch (FileNotFoundException e) {
				logger.log(Level.INFO, "     - Conflict file vanished. Don't care!", e);
			}
			catch (Exception e) {
				throw new RuntimeException("What to do here?", e);
			}
		}
	}

	private File findConflictFilename(File conflictingPath) throws Exception {
		boolean conflictUserNameEndsWithS = userName.endsWith("s");
		String conflictDate = new SimpleDateFormat("d MMM yy, h-mm a").format(new Date());

		String conflictFilenameSuffix;

		if (conflictUserNameEndsWithS) {
			conflictFilenameSuffix = String.format("%s' conflicted copy, %s", userName, conflictDate);
		}
		else {
			conflictFilenameSuffix = String.format("%s's conflicted copy, %s", userName, conflictDate);
		}

		return withSuffix(conflictingPath, conflictFilenameSuffix);
	}

	public File withSuffix(File conflictingPath, String filenameSuffix) throws Exception {
			File creatableNormalizedPath = null;
			int attempt = 0;
			
			do {
				String aFilenameSuffix = (attempt > 0) ? filenameSuffix + " " + attempt : filenameSuffix;
				creatableNormalizedPath = new File(addFilenameConflictSuffix(conflictingPath.getCanonicalPath(), aFilenameSuffix));
				boolean exists = FileUtil.exists(creatableNormalizedPath);
				
				if (!exists) {
					return creatableNormalizedPath;
				}
			} while (attempt++ < 200);
			
			throw new Exception("Cannot create path with suffix; "+attempt+" attempts: "+creatableNormalizedPath);
	}
	
	private String addFilenameConflictSuffix(String pathPart, String filenameSuffix) {
		String conflictFileExtension = getExtension(pathPart, false);		
		boolean originalFileHasExtension = conflictFileExtension != null && !"".equals(conflictFileExtension);

		if (originalFileHasExtension) {
			String conflictFileBasename = getPathWithoutExtension(pathPart);
			return String.format("%s (%s).%s", conflictFileBasename, filenameSuffix, conflictFileExtension);						
		}
		else {
			return String.format("%s (%s)", pathPart, filenameSuffix);
		}
	}

	private String getExtension(String filename, boolean includeDot) {
		int lastDot = filename.lastIndexOf(".");
		int lastSlash = filename.lastIndexOf("/");

		if (lastDot == -1 || lastSlash > lastDot) {
			return "";
		}

		String extension = filename.substring(lastDot + 1, filename.length());
		return (includeDot) ? "." + extension : extension;
	}
	
	private String getPathWithoutExtension(String filename) {
		String extension = getExtension(filename, true); // .txt
		
		if ("".equals(extension)) {
			return filename;
		}
		else {
			return filename.substring(0, filename.length() - extension.length());
		}
	}

	@Override
	public String getMimeType() {
		// Indicate that this merger is not restricted to
		// any particular mime type.
		return null;
	}

	@Override
	public String getExtension() {
		// Indicate that this merger is not restricted to
		// any particular extension.
		return null;
	}
	
}
