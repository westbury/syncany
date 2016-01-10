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

/**
 * Interface to be implemented by all mergers that
 * know how to merge files.  There will generally
 * be an implementation of this interface for each
 * file type that can be merged.
 * 
 * @author Nigel Westbury
 *
 */
public interface FileMerger {

	void merge(FileVersionContent latestRemoteFile, File localFile, FileVersionContent commonAncestorFile);

}
