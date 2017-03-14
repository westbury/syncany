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
package org.syncany.plugins.tests;

import org.syncany.api.transfer.StorageException;

/**
 * @author Nigel
 *
 */
public class RemoteFileFactories {

	public static MasterRemoteFile createMasterFile(String name) throws StorageException {
		if (!name.equals("master")) {
			throw new StorageException("master file must be called 'master'.");
		}
		return new MasterRemoteFile();
	}

	public static SyncanyRemoteFile createSyncanyFile(String name) throws StorageException {
		if (!name.equals("syncany")) {
			throw new StorageException("syncany file must be called 'syncany'.");
		}
		return new SyncanyRemoteFile();
	}

	public static MultichunkRemoteFile createMultichunkFile(String name) throws StorageException {
		return new MultichunkRemoteFile(name);
	}

	public static DatabaseRemoteFile createDatabaseFile(String name) throws StorageException {
		return new DatabaseRemoteFile(name);
	}

}
