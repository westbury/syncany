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
package org.syncany.plugins.merge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.syncany.plugins.transfer.StorageException;

/**
 * This interface is designed so that implementations can delay download
 * of a file version's contents until a stream or file is requested through
 * the interface.  This avoids unnecessary downloads when the merge strategy
 * does not require all three versions of the file.
 * 
 * @author Nigel Westbury
 *
 */
public interface FileVersionContent {
	File getFile() throws StorageException, IOException;
	
	InputStream openInputStream() throws FileNotFoundException, StorageException, IOException;
	
}
