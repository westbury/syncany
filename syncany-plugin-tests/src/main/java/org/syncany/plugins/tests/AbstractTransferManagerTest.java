/*
 * Syncany, www.syncany.org
 * Copyright (C) 2011-2016 Philipp C. Heckel <philipp.heckel@gmail.com>
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
package org.syncany.plugins.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.xml.bind.DatatypeConverter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.syncany.api.FileType;
import org.syncany.api.transfer.LocalDiskCache;
import org.syncany.api.transfer.PropertyVisitor;
import org.syncany.api.transfer.RemoteFile;
import org.syncany.api.transfer.RemoteFileFactory;
import org.syncany.api.transfer.Setter;
import org.syncany.api.transfer.StorageException;
import org.syncany.api.transfer.TransferManager;
import org.syncany.api.transfer.TransferPlugin;
import org.syncany.api.transfer.TransferSettings;
import org.syncany.api.transfer.TransferSettingsSetter;
import org.syncany.api.transfer.features.PathAwareRemoteFileType;

public abstract class AbstractTransferManagerTest {

	private static Random nonRandomGen = new Random(123456789L); // fixed seed!

	private File tempLocalSourceDir;

	private File localRootDir;

	public abstract Map<String, String> createPluginSettings();

	public abstract String getPluginId();

	@Before
	public void setUp() throws Exception {
		tempLocalSourceDir = new File(localRootDir + "/local");
		tempLocalSourceDir.mkdir();
	}

	@After
	public void tearDown() {
		deleteDirectory(tempLocalSourceDir);
		deleteDirectory(localRootDir);
	}

	@Test
	public void testLoadPluginAndCreateTransferManager() throws StorageException, IOException {
		loadPluginAndCreateTransferManager();
	}

	@Test
	public void testLocalPluginInfo() throws StorageException {
		String pluginId = getPluginId();
		TransferPlugin plugin = lookupTransferPlugin(pluginId);

		assertNotNull("PluginInfo should not be null.", plugin);
		assertEquals("Plugin ID should different.", pluginId, plugin.getId());
		assertNotNull("Plugin version should not be null.", plugin.getVersion());
		assertNotNull("Plugin name should not be null.", plugin.getName());
	}

	@Test(expected = StorageException.class)
	public void testConnectWithInvalidSettings() throws StorageException, IOException {
		TransferPlugin plugin = lookupTransferPlugin(getPluginId());

		TransferSettings connection = plugin.createEmptySettings();

		// This should cause a Storage exception, because the path does not exist
		LocalDiskCache cache = new SimpleCache();
		TransferManager transferManager = connection.createTransferManager(cache);

		transferManager.connect();
	}

	@Test
	public void testUploadListDownloadAndDelete() throws Exception {
		// Setup
		File tempFromDir = Files.createTempDirectory("syncanytest").toFile();
		File tempToDir = Files.createTempDirectory("syncanytest").toFile();

		// Create connection, upload, list, download
		TransferManager transferManager = loadPluginAndCreateTransferManager();

		transferManager.init(true, new SyncanyRemoteFile());
		transferManager.connect();

		// Clear up previous test (if test location is reused)
		cleanTestLocation(transferManager);

		// Run!
		uploadDownloadListDelete(transferManager, tempFromDir, tempToDir, PathAwareRemoteFileType.Syncany, RemoteFileFactories::createSyncanyFile,
				new SyncanyRemoteFile[] { new SyncanyRemoteFile() });

		uploadDownloadListDelete(transferManager, tempFromDir, tempToDir, PathAwareRemoteFileType.Master, RemoteFileFactories::createMasterFile, new MasterRemoteFile[] { new MasterRemoteFile() });

		uploadDownloadListDelete(transferManager, tempFromDir, tempToDir, PathAwareRemoteFileType.Database, RemoteFileFactories::createDatabaseFile, new DatabaseRemoteFile[] {
				new DatabaseRemoteFile("database-A-0001"), new DatabaseRemoteFile("database-B-0002") });

		uploadDownloadListDelete(transferManager, tempFromDir, tempToDir, PathAwareRemoteFileType.Multichunk, RemoteFileFactories::createMultichunkFile, new MultichunkRemoteFile[] {
				new MultichunkRemoteFile("multichunk-84f7e2b31440aaef9b73de3cadcf4e449aeb55a1"),
				new MultichunkRemoteFile("multichunk-beefbeefbeefbeefbeefbeefbeefbeefbeefbeef"),
				new MultichunkRemoteFile("multichunk-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa") });

		// Clear up previous test (if test location is reused)
		cleanTestLocation(transferManager);

		// Clean local location
		deleteDirectory(tempFromDir);
		deleteDirectory(tempToDir);
	}

	private <T extends RemoteFile> void uploadDownloadListDelete(TransferManager transferManager, File tempFromDir, File tempToDir,
			PathAwareRemoteFileType remoteFileType, RemoteFileFactory<T> factory, T[] remoteFiles) throws Exception {
		for (RemoteFile remoteFile : remoteFiles) {
			File originalLocalFile = new File(tempFromDir, remoteFile.getName());
			File downloadedLocalFile = new File(tempToDir, remoteFile.getName());

			createFile(originalLocalFile, 5 * 1024, nonRandomGen);

			transferManager.upload(originalLocalFile, remoteFile);
			transferManager.download(remoteFile, downloadedLocalFile);

			String checksumOriginalFile = createChecksum(originalLocalFile);
			String checksumDownloadedFile = createChecksum(downloadedLocalFile);

			assertEquals("Uploaded file differs from original file, for file " + originalLocalFile, checksumOriginalFile, checksumDownloadedFile);
		}

		Collection<T> listLocalFilesAfterUpload = transferManager.list(remoteFileType, factory);
		assertEquals(remoteFiles.length, listLocalFilesAfterUpload.size());

		for (RemoteFile remoteFile : remoteFiles) {
			transferManager.delete(remoteFile);
		}

		Collection<T> listLocalFileAfterDelete = transferManager.list(remoteFileType, factory);
		assertEquals(0, listLocalFileAfterDelete.size());
	}

	private void cleanTestLocation(TransferManager transferManager) throws StorageException {
		// Nigel - probably not right...
		Collection<MultichunkRemoteFile> normalFiles = transferManager.list(PathAwareRemoteFileType.Multichunk, RemoteFileFactories::createMultichunkFile);
		Collection<DatabaseRemoteFile> databaseFiles = transferManager.list(PathAwareRemoteFileType.Database, RemoteFileFactories::createDatabaseFile);
		Collection<MultichunkRemoteFile> multiChunkFiles = transferManager.list(PathAwareRemoteFileType.Multichunk, RemoteFileFactories::createMultichunkFile);

		for (RemoteFile remoteFile : normalFiles) {
			transferManager.delete(remoteFile);
		}

		for (RemoteFile remoteFile : databaseFiles) {
			transferManager.delete(remoteFile);
		}

		for (RemoteFile remoteFile : multiChunkFiles) {
			transferManager.delete(remoteFile);
		}
	}

	@Test
	public void testDeleteNonExistentFile() throws StorageException, IOException {
		TransferManager transferManager = loadPluginAndCreateTransferManager();
		transferManager.connect();

		boolean deleteSuccess = transferManager.delete(new MultichunkRemoteFile("multichunk-dddddddddddddddddddddddddddddddddddddddd")); // does not
																																			// exist
		assertTrue(deleteSuccess);
	}

	private TransferManager loadPluginAndCreateTransferManager() throws StorageException, IOException {
		TransferPlugin pluginInfo = lookupTransferPlugin(getPluginId());

		TransferSettings connection = pluginInfo.createEmptySettings();
		final Map<String, String> propertyMap = createPluginSettings();
		connection.visitProperties(new PropertyVisitor() {

			@Override
			public void stringProperty(String id, String displayName, boolean isRequired, boolean storeEncrypted, boolean sensitive, boolean singular,
					boolean visible, Supplier<String> value, Consumer<String> setter) {
				setter.accept(propertyMap.get(id));
			}

			@Override
			public void integerProperty(String id, String displayName, boolean isRequired, boolean storeEncrypted, boolean sensitive,
					boolean singular, boolean visible, Supplier<Integer> value, Consumer<Integer> setter) {
				String stringValue = propertyMap.get(id);
				if (stringValue != null) {
					setter.accept(Integer.valueOf(stringValue));
				}
			}

			@Override
			public void booleanProperty(String id, String displayName, boolean isRequired, boolean singular, boolean visible, boolean value,
					Setter<Boolean> setter) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void fileProperty(String id, String displayName, boolean isRequired, boolean singular, boolean visible, FileType folder,
					File value, Setter<File> setter) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public <T extends Enum<T>> void enumProperty(String id, String displayName, boolean isRequired, T[] options, T value, Setter<T> setter) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void nestedSettingsProperty(String id, String displayName, boolean isRequired, TransferSettingsSetter<?> setter) {
				// TODO Auto-generated method stub
				
			}
		});

		LocalDiskCache cache = new SimpleCache();
		TransferManager originalTransferManager = connection.createTransferManager(cache);
//		return new TransactionAwareFeatureTransferManager(originalTransferManager, originalTransferManager, null, null);
		return originalTransferManager;
	}

	private static TransferPlugin lookupTransferPlugin(String pluginId) throws StorageException {
		 ServiceLoader<TransferPlugin> x = ServiceLoader.load(TransferPlugin.class);
		 TransferPlugin result = null;
		for (TransferPlugin plugin : x) {
			if (plugin.getId().equals(pluginId)) {
				if (result != null) {
					throw new StorageException("There is more than one plugin with the given id");
				}
				result = plugin;
			}
		}

		if (result == null) {
			throw new StorageException("There are no plugins with the given id");
		}
		
		return result;
	}

	protected static boolean deleteDirectory(File path) {
		if (path != null && path.exists() && path.isDirectory()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				}
				else {
					files[i].delete();
				}
			}
		}
		else
			return false;
		return (path.delete());
	}

	public static String createChecksum(File filename) throws NoSuchAlgorithmException, IOException {
		FileInputStream fis = new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("SHA1");
		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();
		byte[] bytes = complete.digest();
		return DatatypeConverter.printHexBinary(bytes).toLowerCase();

		
	}

	private static void createFile(File fileToCreate, long sizeInBytes, Random randomGen) throws IOException {
		if (fileToCreate != null && fileToCreate.exists()) {
			throw new IOException("File already exists");
		}

		FileOutputStream fos = new FileOutputStream(fileToCreate);
		int bufSize = 4096;
		long cycles = sizeInBytes / (long) bufSize;

		for (int i = 0; i < cycles; i++) {
			byte[] randomByteArray = createArray(bufSize, randomGen);
			fos.write(randomByteArray);
		}

		// create last one
		// modulo cannot exceed integer range, so cast should be ok
		byte[] arr = createArray((int) (sizeInBytes % bufSize), randomGen);
		fos.write(arr);

		fos.close();
	}

	public static byte[] createArray(int size, Random randomGen) {
		byte[] ret = new byte[size];
		randomGen.nextBytes(ret);
		return ret;
	}

}
