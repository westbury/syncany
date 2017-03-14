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
package org.syncany.plugins.unreliable_local;

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.syncany.api.transfer.LocalDiskCache;
import org.syncany.api.transfer.RemoteFile;
import org.syncany.api.transfer.RemoteFileFactory;
import org.syncany.api.transfer.StorageException;
import org.syncany.api.transfer.features.PathAwareRemoteFileType;
import org.syncany.plugins.local.LocalTransferManager;

public class UnreliableLocalTransferManager extends LocalTransferManager {
	private static final Logger logger = Logger.getLogger(UnreliableLocalTransferManager.class.getSimpleName());
	private UnreliableLocalTransferSettings connection;

	public UnreliableLocalTransferManager(LocalDiskCache cache, UnreliableLocalTransferSettings connection) throws StorageException {
		super(cache, connection.getPath());
		this.connection = connection;
	}

	private boolean isNextOperationSuccessful(String operationType, String operationDescription) {
		// Increase absolute/overall operation counter
		connection.increaseTotalOperationCounter();

		// Increase type-relative operation counter
		Integer typeOperationCounter = connection.getTypeOperationCounters().get(operationType);

		typeOperationCounter = (typeOperationCounter != null) ? typeOperationCounter + 1 : 1;
		connection.getTypeOperationCounters().put(operationType, typeOperationCounter);

		// Construct operation line
		String operationLine = String.format("abs=%d rel=%d op=%s %s", connection.getTotalOperationCounter(), typeOperationCounter, operationType,
				operationDescription);

		// Check if it fails
		for (String failingOperationPattern : connection.getFailingOperationPatterns()) {
			if (operationLine.matches(".*" + failingOperationPattern + ".*")) {
				logger.log(Level.INFO, "Operation NOT successful: " + operationLine);
				return false;
			}
		}

		logger.log(Level.INFO, "Operation successful:     " + operationLine);
		return true;
	}

	@Override
	public void connect() throws StorageException {
		String operationType = "connect";
		String operationDescription = "connect";

		if (isNextOperationSuccessful(operationType, operationDescription)) {
			super.connect();
		}
		else {
			throw new StorageException("Operation failed: " + operationDescription);
		}
	}

	@Override
	public void disconnect() throws StorageException {
		String operationType = "disconnect";
		String operationDescription = "disconnect";

		if (isNextOperationSuccessful(operationType, operationDescription)) {
			super.disconnect();
		}
		else {
			throw new StorageException("Operation failed: " + operationDescription);
		}
	}

	@Override
	public void init(boolean createIfRequired, RemoteFile syncanyRemoteFile) throws StorageException {
		String operationType = "init";
		String operationDescription = "init";

		if (isNextOperationSuccessful(operationType, operationDescription)) {
			super.init(createIfRequired, syncanyRemoteFile);
		}
		else {
			throw new StorageException("Operation failed: " + operationDescription);
		}
	}

	@Override
	public void download(RemoteFile remoteFile, File localFile) throws StorageException {
		String operationType = "download";
		String operationDescription = "download(" + remoteFile.getName() + ", " + localFile.getAbsolutePath() + ")";

		if (isNextOperationSuccessful(operationType, operationDescription)) {
			super.download(remoteFile, localFile);
		}
		else {
			throw new StorageException("Operation failed: " + operationDescription);
		}
	}

	@Override
	public void upload(File localFile, RemoteFile remoteFile) throws StorageException {
		String operationType = "upload";
		String operationDescription = "upload(" + localFile.getAbsolutePath() + ", " + remoteFile.getName() + ")";

		if (isNextOperationSuccessful(operationType, operationDescription)) {
			super.upload(localFile, remoteFile);
		}
		else {
			throw new StorageException("Operation failed: " + operationDescription);
		}
	}

	@Override
	public void move(RemoteFile sourceFile, RemoteFile targetFile) throws StorageException {
		String operationType = "move";
		String operationDescription = "move(" + sourceFile.getName() + "," + targetFile.getName() + ")";

		if (isNextOperationSuccessful(operationType, operationDescription)) {
			super.move(sourceFile, targetFile);
		}
		else {
			throw new StorageException("Operation failed: " + operationDescription);
		}
	}

	@Override
	public boolean delete(RemoteFile remoteFile) throws StorageException {
		String operationType = "delete";
		String operationDescription = "delete(" + remoteFile.getName() + ")";

		if (isNextOperationSuccessful(operationType, operationDescription)) {
			return super.delete(remoteFile);
		}
		else {
			throw new StorageException("Operation failed: " + operationDescription);
		}
	}

	@Override
	public <T extends RemoteFile> Collection<T> list(PathAwareRemoteFileType remoteFileType, RemoteFileFactory<T> factory) throws StorageException {
		String operationType = "list";
		String operationDescription = "list(" + remoteFileType + ")";

		if (isNextOperationSuccessful(operationType, operationDescription)) {
			return super.list(remoteFileType, factory);
		}
		else {
			throw new StorageException("Operation failed: " + operationDescription);
		}
	}
}
