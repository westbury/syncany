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
package org.syncany.plugins.transfer.files;

import java.util.Map;

import org.syncany.api.transfer.RemoteFile;
import org.syncany.api.transfer.RemoteFileAttributes;
import org.syncany.api.transfer.StorageException;
import org.syncany.api.transfer.TransferManager;
import org.syncany.api.transfer.features.PathAwareRemoteFileType;
import org.syncany.plugins.transfer.features.RemoteFileFactories;

import com.google.common.collect.Maps;

/**
 * A remote file represents a file object on a remote storage. Its purpose is to
 * identify a file and allow {@link TransferManager}s to upload/download local files.
 *
 * <p>Transfer manager operations take either <tt>RemoteFile</tt> instances, or classes
 * that extend this class. Depending on the type of the sub-class, they might store the
 * files at a different location or in a different format to optimize performance.
 * 
 * <p>Remote files can be extended with {@link AbstractRemoteFileAttributes} in certain situations, 
 * e.g. to add additional information about the sub-path. The attributes can be added set
 * and read via {@link #setAttributes(AbstractRemoteFileAttributes)} and {@link #getAttributes(Class)}.
 *
 * <p><b>Important:</b> Sub-classes must offer a
 * {@link AbstractRemoteFile#RemoteFile(String) one-parameter constructor} that takes a
 * <tt>String</tt> argument. This constructor is required by the {@link RemoteFileFactories}.
 *
 * @author Philipp C. Heckel <philipp.heckel@gmail.com>
 */
public abstract class AbstractRemoteFile implements RemoteFile {
	private String name;
	private Map<Class<? extends RemoteFileAttributes>, RemoteFileAttributes> attributes;
	
	/**
	 * Creates a new remote file by its name. The name is used by {@link TransferManager}s
	 * to identify a file on the remote storage.
	 *
	 * <p>The constructor parses and validates the given name using the
	 * {@link #validateName(String) validateName()} method. While <tt>RemoteFile</tt> has no name
	 * pattern (and never throws an exception), sub-classes might.
	 *
	 * <p><b>Important:</b> Sub-classes must also implement a one-parameter constructor that takes a
	 * <tt>String</tt> argument. This constructor is required by the {@link RemoteFileFactories}.
	 *
	 * @param name The name of the file (as it is identified by Syncany)
	 * @throws StorageException If the name does not match the name pattern defined by the class.<br />
	 *         <b>Note:</b> <tt>RemoteFile</tt> does never throw this exceptions, however, subclasses might.
	 */
	public AbstractRemoteFile(String name) throws StorageException {
		this.name = validateName(name);
		this.attributes = Maps.newHashMap();
	}

	/**
	 * Returns the name of the file (as it is identified by Syncany)
	 */
	public final String getName() {
		return name;
	}
	
	/**
	 * Sets remote file attributes to this remote file class. Attributes 
	 * can extend the parameters of this class without actually having to extend it.
	 */
	@Override
	public final <T extends RemoteFileAttributes> void setAttributes(T remoteFileAttributes) {
		attributes.put(remoteFileAttributes.getClass(), remoteFileAttributes);
	}
	
	/**
	 * Returns a list of attributes for a given file, 
	 * or null if there is no attribute with the given class.
	 */
	@SuppressWarnings("unchecked")
	public final <T extends RemoteFileAttributes> T getAttributes(Class<T> remoteFileAttributesClass) {
		return (T) attributes.get(remoteFileAttributesClass);
	}

	/**
	 * Parses the name of the file and validates it against the classes name pattern. While
	 * <tt>RemoteFile</tt> has no name pattern (and never throws an exception), sub-classes might by
	 * overriding this method.
	 *
	 * @param name The name of the file (as it is identified by Syncany)
	 * @return Returns a (potentially changed) name, after validating the name
	 * @throws StorageException If the name does not match the name pattern defined by the class.<br />
	 *         <b>Note:</b> <tt>RemoteFile</tt> does never throw this exceptions, however, subclasses might.
	 */
	protected String validateName(String name) throws StorageException {
		return name;
	}

	/**
	 * Creates a remote file based on a name and a class name.
	 *
	 * <p>The name must match the corresponding name pattern, and the class name
	 * can either be <tt>RemoteFile</tt>, or a sub-class thereof.
	 *
	 * @param name The name of the remote file
	 * @param remoteFileType Class name of the object to instantiate, <tt>RemoteFile</tt> or a sub-class thereof
	 * @return Returns a new object of the given class
	 */
	public static <T extends RemoteFile> RemoteFile createRemoteFile(String name, PathAwareRemoteFileType remoteFileType) throws StorageException {
		switch (remoteFileType) {
		case Action:
			return new ActionRemoteFile(name);
		case Cleanup:
			return new CleanupRemoteFile(name);
		case Database:
			return new DatabaseRemoteFile(name);
		case Master:
			return new MasterRemoteFile(name);
		case Multichunk:
			return new MultichunkRemoteFile(name);
		case Syncany:
			return new SyncanyRemoteFile(name);
		case Temp:
			return new TempRemoteFile(name);
		case Transaction:
			return new TransactionRemoteFile(name);
		default:
			throw new RuntimeException("impossible enum");
		}
	}

	/**
	 * Creates a remote file based on a name and derives the class name using the
	 * file name.
	 *
	 * <p>The name must match the corresponding name pattern (nameprefix-...), and
	 * the derived class can either be <tt>RemoteFile</tt>, or a sub-class thereof.
	 *
	 * @param name The name of the remote file
	 * @return Returns a new object of the given class
	 */
	public static <T extends AbstractRemoteFile> AbstractRemoteFile createRemoteFile(String name) throws StorageException {
		String prefix = name.contains("-") ? name.substring(0, name.indexOf('-')) : name;
		
		switch (prefix.toLowerCase()) {
		case "action":
			return new ActionRemoteFile(name);
		case "cleanup":
			return new CleanupRemoteFile(name);
		case "database":
			return new DatabaseRemoteFile(name);
		case "master":
			return new MasterRemoteFile(name);
		case "multichunk": 
			return new MultichunkRemoteFile(name);
		case "syncany":
			return new SyncanyRemoteFile(name);
		case "temp":
			return new TempRemoteFile(name);
		case "transaction":
			return new TransactionRemoteFile(name);
		default:
			throw new RuntimeException("impossible enum");
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || !(obj instanceof AbstractRemoteFile)) {
			return false;
		}

		AbstractRemoteFile other = (AbstractRemoteFile) obj;

		if (name == null) {
			if (other.name != null) {
				return false;
			}
		}
		else if (!name.equals(other.name)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return AbstractRemoteFile.class.getSimpleName() + "[name=" + name + "]";
	}
}
