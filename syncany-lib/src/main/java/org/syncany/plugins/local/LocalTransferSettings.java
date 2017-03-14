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
package org.syncany.plugins.local;

import java.io.File;

import org.syncany.api.FileType;
import org.syncany.api.transfer.LocalDiskCache;
import org.syncany.api.transfer.PropertyVisitor;
import org.syncany.api.transfer.Setter;
import org.syncany.api.transfer.StorageException;
import org.syncany.api.transfer.TransferManager;
import org.syncany.api.transfer.TransferSettings;

/**
 * The local connection represents the settings required to create to a
 * backend based on a local (or mounted network) folder. It can be used to
 * initialize/create a {@link LocalTransferManager} and is part of
 * the {@link LocalTransferPlugin}.
 *
 * @author Philipp C. Heckel
 */
public class LocalTransferSettings implements TransferSettings {
	public File path;

	public String getType() {
		return "local";
	}

	public File getPath() {
		return path;
	}

	public void setPath(File path) {
		this.path = path;
	}

	@Override
	public void visitProperties(PropertyVisitor visitor) {
		Setter<File> pathSetter = new Setter<File>() {

			@Override
			public String validateValue(File value) {
				if (value == null) {
					return "Path must be specified";
				}
				return null;
			}

			@Override
			public void setValue(File value) {
				path = value;
			}
		};
		
		visitor.fileProperty("path", "Path to local repository", true, false, true, FileType.FOLDER, path, pathSetter );
	}

	@Override
	public TransferManager createTransferManager(LocalDiskCache cache) throws StorageException {
		return new LocalTransferManager(cache, path);
	}

	@Override
	public boolean isValid() {
		// TODO More validation here???
		return path != null;
	}

	@Override
	public String getReasonForLastValidationFail() {
		// TODO Auto-generated method stub
		return "todo";
	}
}
