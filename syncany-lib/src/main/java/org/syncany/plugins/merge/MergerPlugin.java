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

import org.syncany.config.Config;
import org.syncany.plugins.Plugin;
import org.syncany.plugins.transfer.files.RemoteFile;

/**
 * The merger plugin is a special plugin that knows how to merge particular
 * files or directories.
 * 
 * The plugin may be configured to process files using any of the
 * following criteria:
 * <UL>
 * <LI>Process files of a given extension.
 * </LI>
 * <LI>Process files of a given mime type.  Many of the implementations
 * that determine a file's mime type do so by looking at the file extension.
 * However some, such as the Apache POI implementation, do look at the content
 * of the files too so the mime type can be a better criteria to use than
 * the file extension.
 * </LI>
 * <LI>Process files in a list of given file paths.  This option may be of limited
 * value but is included to give full flexibility.  For example if a user had a merger
 * that knew how to sensibly merge pom.xml files (using knowledge of pom files to ensure
 * the merged result was a valid pom file) then using the extension would not be adequate.
 * </LI>
 * <LI>Process a directory with a given path.  This option allows a merge to take responsibility
 * to merge an entire directory and its sub-directories.  An example where this might be
 * needed if it one keeps, say, a database, in syncany's directory.  A database is generally kept
 * as a directory with a number of files and sub-directories.  Merging two such directories on a
 * file-by-file basis may result in an invalid database even if there as no file conflicts.
 * </LI>
 * </UL> 
 *
 * Implementations must provide implementations for
 * {@link MergerPlugin} (this class), {@link MergerSettings} (configuration
 * details for what is to be merged and how) and {@link Merger} (merge methods).<br/><br/>
 *
 * @author Nigel Westbury <???>
 */
public abstract class MergerPlugin extends Plugin {
	public MergerPlugin(String pluginId) {
		super(pluginId);
	}

	/**
	 * Creates an empty plugin-specific {@link org.syncany.plugins.merge.MergerSettings} instance.
	 *
	 * @return Empty plugin-specific {@link org.syncany.plugins.merge.MergerSettings} instance.
	 * @throws MergingException Thrown if no {@link org.syncany.plugins.merge.MergerSettings} are attached to a
	 *         plugin using {@link org.syncany.plugins.merge.PluginSettings}
	 */
	public abstract <T extends MergerSettings> T createEmptySettings() throws MergingException;
//		final Class<? extends MergerSettings> transferSettings = MergerPluginUtil.getTransferSettingsClass(this.getClass());
//
//		if (transferSettings == null) {
//			throw new MergingException("TransferPlugin does not have any settings attached!");
//		}
//
//		try {
//			return (T) transferSettings.newInstance();
//		}
//		catch (InstantiationException | IllegalAccessException e) {
//			throw new RuntimeException("Unable to create TransferSettings: " + e.getMessage());
//		}
//	}

	 /**
	 * Creates an initialized, plugin-specific {@link org.syncany.plugins.transfer.TransferManager} object using the given
	 * connection details.
	 *
	 * <p>The created instance can be used to upload/download/delete {@link RemoteFile}s
	 * and query the remote storage for a file list.
	 *
	 * @param transferSettings A valid {@link org.syncany.plugins.transfer.TransferSettings} instance.
	 * @param config A valid {@link org.syncany.config.Config} instance.
	 * @return A initialized, plugin-specific {@link org.syncany.plugins.transfer.TransferManager} instance.
	 * @throws MergingException Thrown if no (valid) {@link org.syncany.plugins.transfer.TransferManager} are attached to
	*  a plugin using {@link org.syncany.plugins.transfer.PluginManager}
	 */
	public abstract <T extends Merger> T createMerger(MergerSettings transferSettings, Config config) throws MergingException;
//		if (!transferSettings.isValid()) {
//			throw new MergingException("Unable to create transfer manager: connection isn't valid (perhaps missing some mandatory fields?)");
//		}
//
//		final Class<? extends MergerSettings> transferSettingsClass = MergerPluginUtil.getTransferSettingsClass(this.getClass());
//		final Class<? extends Merger> transferManagerClass = MergerPluginUtil.getTransferManagerClass(this.getClass());
//
//		if (transferSettingsClass == null) {
//			throw new RuntimeException("Unable to create transfer manager: No settings class attached");
//		}
//
//		if (transferManagerClass == null) {
//			throw new RuntimeException("Unable to create transfer manager: No manager class attached");
//		}
//
//		try {
//			Constructor<?> potentialConstructor = ReflectionUtil.getMatchingConstructorForClass(transferManagerClass, MergerSettings.class,
//					Config.class);
//
//			if (potentialConstructor == null) {
//				throw new RuntimeException("Invalid arguments for constructor in pluginclass -- must be 2 and subclass of " + MergerSettings.class
//						+ " and " + Config.class);
//			}
//
//			return (T) potentialConstructor.newInstance(transferSettingsClass.cast(transferSettings), config);
//		}
//		catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
//			throw new RuntimeException("Unable to create transfer settings: " + e.getMessage(), e);
//		}
//	}
}
