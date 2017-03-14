/*
 * Syncany, www.syncany.org
 * Copyright (C) 2017 Philipp C. Heckel <philipp.heckel@gmail.com> 
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

import org.syncany.api.transfer.StorageException;
import org.syncany.plugins.transfer.files.ActionRemoteFile;
import org.syncany.plugins.transfer.files.CleanupRemoteFile;
import org.syncany.plugins.transfer.files.DatabaseRemoteFile;
import org.syncany.plugins.transfer.files.MasterRemoteFile;
import org.syncany.plugins.transfer.files.MultichunkRemoteFile;
import org.syncany.plugins.transfer.files.SyncanyRemoteFile;
import org.syncany.plugins.transfer.files.TempRemoteFile;
import org.syncany.plugins.transfer.files.TransactionRemoteFile;

/**
 * @author Nigel Westbury
 *
 */
public class RemoteFileFactories {

	public static MasterRemoteFile createMasterFile(String name) throws StorageException {
		return new MasterRemoteFile(name);
	}

	public static SyncanyRemoteFile createSyncanyFile(String name) throws StorageException {
		return new SyncanyRemoteFile(name);
	}

	public static ActionRemoteFile createActionFile(String name) throws StorageException {
		return new ActionRemoteFile(name);
	}

	public static MultichunkRemoteFile createMultichunkFile(String name) throws StorageException {
		return new MultichunkRemoteFile(name);
	}

	public static DatabaseRemoteFile createDatabaseFile(String name) throws StorageException {
		return new DatabaseRemoteFile(name);
	}

	public static TransactionRemoteFile createTransactionFile(String name) throws StorageException {
		return new TransactionRemoteFile(name);
	}

	public static CleanupRemoteFile createCleanupFile(String name) throws StorageException {
		return new CleanupRemoteFile(name);
	}

	public static TempRemoteFile createTempFile(String name) throws StorageException {
		return new TempRemoteFile(name);
	}
}
