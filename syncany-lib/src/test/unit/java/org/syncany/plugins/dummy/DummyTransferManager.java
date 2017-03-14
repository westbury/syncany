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
package org.syncany.plugins.dummy;

import java.io.File;
import java.util.Collection;

import org.syncany.api.transfer.LocalDiskCache;
import org.syncany.api.transfer.RemoteFile;
import org.syncany.api.transfer.RemoteFileFactory;
import org.syncany.api.transfer.StorageException;
import org.syncany.api.transfer.TransferManager;
import org.syncany.api.transfer.features.PathAwareRemoteFileType;

/**
 * @author Christian Roth <christian.roth@port17.de>
 */

public class DummyTransferManager implements TransferManager {

	public DummyTransferManager(DummyTransferSettings settings, LocalDiskCache cache) {
// Nigel		super(settings, cache);
	}

	@Override
	public void connect() throws StorageException {

	}

	@Override
	public void disconnect() throws StorageException {

	}

	@Override
	public void init(boolean createIfRequired, RemoteFile syncanyRemoteFile) throws StorageException {

	}

	@Override
	public void download(RemoteFile remoteFile, File localFile) throws StorageException {

	}

	@Override
	public void upload(File localFile, RemoteFile remoteFile) throws StorageException {

	}

	@Override
	public void move(RemoteFile sourceFile, RemoteFile targetFile) throws StorageException {

	}

	@Override
	public boolean delete(RemoteFile remoteFile) throws StorageException {
		return false;
	}

	@Override
	public <T extends RemoteFile> Collection<T> list(PathAwareRemoteFileType remoteFileType, RemoteFileFactory<T> factory) throws StorageException {
		return null;
	}

	@Override
	public boolean testTargetExists() throws StorageException {
		return false;
	}

	@Override
	public boolean testTargetCanWrite() throws StorageException {
		return false;
	}

	@Override
	public boolean testTargetCanCreate() throws StorageException {
		return false;
	}

	@Override
	public boolean testRepoFileExists(RemoteFile syncanyRemoteFile) throws StorageException {
		return false;
	}

	@Override
	public String getRemoteFilePath(PathAwareRemoteFileType remoteFileType) {
		return "";
	}
}
