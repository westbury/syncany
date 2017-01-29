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
package org.syncany.api.transfer;

/**
 * A remote file represents a file object on a remote storage. Its purpose is to
 * identify a file and allow {@link TransferManager}s to upload/download local files.
 *
 * <p>Transfer manager operations take either <tt>RemoteFile</tt> instances, or classes
 * that extend this class. Depending on the type of the sub-class, they might store the
 * files at a different location or in a different format to optimize performance.
 * 
 * <p>Remote files can be extended with {@link RemoteFileAttributes} in certain situations, 
 * e.g. to add additional information about the sub-path. The attributes can be added set
 * and read via {@link #setAttributes(RemoteFileAttributes)} and {@link #getAttributes(Class)}.
 *
 * <p><b>Important:</b> Sub-classes must offer a
 * {@link RemoteFile#RemoteFile(String) one-parameter constructor} that takes a
 * <tt>String</tt> argument. This constructor is required by the {@link RemoteFileFactory}.
 *
 * @author Philipp C. Heckel <philipp.heckel@gmail.com>
 */
public interface RemoteFile {

	/**
	 * Returns the name of the file (as it is identified by Syncany)
	 */
	String getName();
	
	/**
	 * Sets remote file attributes to this remote file class. Attributes 
	 * can extend the parameters of this class without actually having to extend it.
	 */
	<T extends RemoteFileAttributes> void setAttributes(T remoteFileAttributes);
	
	/**
	 * Returns a list of attributes for a given file, 
	 * or null if there is no attribute with the given class.
	 */
	<T extends RemoteFileAttributes> T getAttributes(Class<T> remoteFileAttributesClass);

}
