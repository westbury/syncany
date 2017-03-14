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
package org.syncany.plugins.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.syncany.api.transfer.LocalDiskCache;

/**
 * The cache class represents the local disk cache. It is used for storing multichunks
 * or other metadata files before upload, and as a download location for the same
 * files. 
 * 
 * <p>The cache implements an LRU strategy based on the last modified date of the 
 * cached files. When files are accessed using the respective getters, the last modified
 * date is updated. Using the {@link #clear()}/{@link #clear(long)} method, the cache
 * can be cleaned.
 * 
 * @author Philipp C. Heckel <philipp.heckel@gmail.com>
 */
public class SimpleCache implements LocalDiskCache {

    private File cacheDir;
    
    public SimpleCache() throws IOException {
    	this.cacheDir = Files.createTempDirectory("cache").toFile();
    }
    
	/**
	 * Creates temporary file in the local directory cache, typically located at
	 * .syncany/cache. If not deleted by the application, the returned file is automatically
	 * deleted on exit by the JVM.
	 * 
	 * @return Temporary file in local directory cache
	 */
    public File createTempFile(String name) throws IOException {
       File tempFile = File.createTempFile(String.format("temp-%s-", name), ".tmp", cacheDir);
       tempFile.deleteOnExit();
       
       return tempFile;
    }
    
}
