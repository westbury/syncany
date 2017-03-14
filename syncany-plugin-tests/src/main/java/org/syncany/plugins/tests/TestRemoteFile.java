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

import org.syncany.api.transfer.RemoteFileAttributes;
import org.syncany.api.transfer.features.PathAwareRemoteFileType;

public class TestRemoteFile implements org.syncany.api.transfer.RemoteFile {

	private String name;
	private PathAwareRemoteFileType type;

	public TestRemoteFile(String fileName, PathAwareRemoteFileType type) {
		this.name = fileName;
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public <T extends RemoteFileAttributes> void setAttributes(T remoteFileAttributes) {
		// TODO Auto-generated method stub
	}

	@Override
	public <T extends RemoteFileAttributes> T getAttributes(Class<T> remoteFileAttributesClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PathAwareRemoteFileType getPathAwareType() {
		return type;
	}
}