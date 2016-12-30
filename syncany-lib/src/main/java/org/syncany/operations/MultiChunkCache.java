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
package org.syncany.operations;

import java.io.File;
import java.io.IOException;

import org.syncany.chunk2.MultiChunkEntry.MultiChunkId;
import org.syncany.config.Cache;

/**
 * @author Nigel
 *
 */
public class MultiChunkCache {

	private static String FILE_FORMAT_MULTICHUNK_ENCRYPTED = "multichunk-%s";
	private static String FILE_FORMAT_MULTICHUNK_DECRYPTED = "multichunk-%s-decrypted";

	/**
	 * The underlying file cache used for this implementation.
	 */
	private final Cache cache;
	
	/**
	 * @param cache
	 */
	public MultiChunkCache(Cache cache) {
		this.cache = cache;
	}

    /**
     * Returns a file path of a decrypted multichunk file, 
     * given the identifier of a multichunk.
     */
    public File getDecryptedMultiChunkFile(MultiChunkId multiChunkId) {
    	return cache.getFileInCache(FILE_FORMAT_MULTICHUNK_DECRYPTED, multiChunkId.toString());
    }    

    /**
     * Returns a file path of a encrypted multichunk file, 
     * given the identifier of a multichunk.
     */
    public File getEncryptedMultiChunkFile(MultiChunkId multiChunkId) {
    	return cache.getFileInCache(FILE_FORMAT_MULTICHUNK_ENCRYPTED, multiChunkId.toString());
    }

	public File createTempFile(String name) throws IOException {
		return cache.createTempFile(name);
	}    

}
