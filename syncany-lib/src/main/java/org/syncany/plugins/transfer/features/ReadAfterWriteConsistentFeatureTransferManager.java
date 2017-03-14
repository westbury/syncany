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
package org.syncany.plugins.transfer.features;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.syncany.api.transfer.RemoteFile;
import org.syncany.api.transfer.RemoteFileFactory;
import org.syncany.api.transfer.StorageException;
import org.syncany.api.transfer.TransferManager;
import org.syncany.api.transfer.features.PathAwareRemoteFileType;
import org.syncany.api.transfer.features.ReadAfterWriteConsistent;
import org.syncany.api.transfer.features.ReadAfterWriteConsistentFeatureExtension;
import org.syncany.config.Config;
import org.syncany.plugins.transfer.files.AbstractRemoteFile;
import org.syncany.util.ReflectionUtil;

/**
 * <p>The ReadWriteConsistentFeatureTransferManager waits specific amount of time after
 * {@link #upload(File, AbstractRemoteFile)} and {@link #move(AbstractRemoteFile, AbstractRemoteFile)} operations
 * because some storage backends do no guarantee that a file immediately exists after
 * creation.
 *
 * <p>It throttles existence check using a simple exponential method:<br/>
 *
 * <code>throttle(n) = 3 ^ n * 100 ms, with n being the current iteration
 *
 * @author Christian Roth <christian.roth@port17.de>
 */
public class ReadAfterWriteConsistentFeatureTransferManager implements FeatureTransferManager {
	private static final Logger logger = Logger.getLogger(ReadAfterWriteConsistentFeatureTransferManager.class.getSimpleName());

	private final TransferManager underlyingTransferManager;
	private final Throttler throttler;
	private final ReadAfterWriteConsistentFeatureExtension readAfterWriteConsistentFeatureExtension;

	public ReadAfterWriteConsistentFeatureTransferManager(TransferManager originalTransferManager, TransferManager underlyingTransferManager, Config config, ReadAfterWriteConsistent readAfterWriteConsistentAnnotation) {
		this.underlyingTransferManager = underlyingTransferManager;
		this.throttler = new Throttler(readAfterWriteConsistentAnnotation.maxRetries(), readAfterWriteConsistentAnnotation.maxWaitTime());
		this.readAfterWriteConsistentFeatureExtension = getReadAfterWriteConsistentFeatureExtension(originalTransferManager, readAfterWriteConsistentAnnotation);
	}

	@SuppressWarnings("unchecked")
	private ReadAfterWriteConsistentFeatureExtension getReadAfterWriteConsistentFeatureExtension(TransferManager originalTransferManager, ReadAfterWriteConsistent readAfterWriteConsistentAnnotation) {
		Class<? extends TransferManager> originalTransferManagerClass = originalTransferManager.getClass();
		Class<ReadAfterWriteConsistentFeatureExtension> readAfterWriteConsistentFeatureExtensionClass = (Class<ReadAfterWriteConsistentFeatureExtension>) readAfterWriteConsistentAnnotation.extension();

		try {
			Constructor<?> constructor = ReflectionUtil.getMatchingConstructorForClass(readAfterWriteConsistentFeatureExtensionClass, originalTransferManagerClass);

			if (constructor != null) {
				return (ReadAfterWriteConsistentFeatureExtension) constructor.newInstance(originalTransferManager);
			}

			return readAfterWriteConsistentFeatureExtensionClass.newInstance();
		}
		catch (InvocationTargetException | InstantiationException | IllegalAccessException | NullPointerException e) {
			throw new RuntimeException("Cannot instantiate ReadWriteConsistentFeatureExtension (perhaps " + readAfterWriteConsistentFeatureExtensionClass + " does not exist?)", e);
		}
	}

	@Override
	public void connect() throws StorageException {
		underlyingTransferManager.connect();
	}

	@Override
	public void disconnect() throws StorageException {
		underlyingTransferManager.disconnect();
	}

	@Override
	public void init(boolean createIfRequired, RemoteFile syncanyRemoteFile) throws StorageException {
		underlyingTransferManager.init(createIfRequired, syncanyRemoteFile);
	}

	@Override
	public void download(final RemoteFile remoteFile, final File localFile) throws StorageException {
		underlyingTransferManager.download(remoteFile, localFile);
	}

	@Override
	public void move(final RemoteFile sourceFile, final RemoteFile targetFile) throws StorageException {
		underlyingTransferManager.move(sourceFile, targetFile);
		waitForFile(targetFile);
	}

	@Override
	public void upload(final File localFile, final RemoteFile remoteFile) throws StorageException {
		underlyingTransferManager.upload(localFile, remoteFile);
		waitForFile(remoteFile);
	}

	@Override
	public boolean delete(final RemoteFile remoteFile) throws StorageException {
		return underlyingTransferManager.delete(remoteFile);
	}

	@Override
	public <T extends RemoteFile> Collection<T> list(PathAwareRemoteFileType remoteFileType, RemoteFileFactory<T> factory) throws StorageException {
		return underlyingTransferManager.list(remoteFileType, factory);
	}

	@Override
	public String getRemoteFilePath(PathAwareRemoteFileType remoteFileType) {
		return underlyingTransferManager.getRemoteFilePath(remoteFileType);
	}

	@Override
	public boolean testTargetExists() throws StorageException {
		return underlyingTransferManager.testTargetExists();
	}

	@Override
	public boolean testTargetCanWrite() throws StorageException {
		return underlyingTransferManager.testTargetCanWrite();
	}

	@Override
	public boolean testTargetCanCreate() throws StorageException {
		return underlyingTransferManager.testTargetCanCreate();
	}

	@Override
	public boolean testRepoFileExists(RemoteFile repoFile) throws StorageException {
		return underlyingTransferManager.testRepoFileExists(repoFile);
	}

	private void waitForFile(RemoteFile remoteFile) throws StorageException {
		while (true) {
			if (readAfterWriteConsistentFeatureExtension.exists(remoteFile)) {
				logger.log(Level.FINER, remoteFile + " exists on the remote side");
				throttler.reset();
				break;
			}

			try {
				long waitForMs = throttler.next();
				logger.log(Level.FINER, "File not found on the remote side, perhaps its in transit, waiting " + waitForMs + "ms ...");
				Thread.sleep(waitForMs);
			}
			catch (InterruptedException e) {
				throw new StorageException("Unable to wait anymore", e);
			}
		}
	}

	private class Throttler {
		private final int maxRetries;
		private final int maxWait;
		private int currentIteration = 0;

		public Throttler(int maxRetries, int maxWait) {
			this.maxRetries = maxRetries;
			this.maxWait = maxWait;
		}

		public long next() throws InterruptedException {
			long waitFor = (long) Math.pow(3, currentIteration++) * 100;

			if (waitFor > maxWait || currentIteration > maxRetries) {
				throw new InterruptedException("Unable to wait anymore, because ending criteria reached");
			}

			return waitFor;
		}

		public void reset() {
			currentIteration = 0;
		}
	}
}
