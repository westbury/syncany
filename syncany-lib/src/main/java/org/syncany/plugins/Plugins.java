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
package org.syncany.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

import org.syncany.api.transfer.Plugin;
import org.syncany.api.transfer.TransferPlugin;
import org.syncany.plugins.web.WebInterfacePlugin;

/**
 * This class loads and manages all the {@link Plugin}s loaded in the classpath.
 * It provides two public methods:
 *
 * <ul>
 *  <li>{@link #transferPlugins()} returns a list of all loaded plugins (as per classpath)</li>
 *  <li>{@link #get(String) get()} returns a specific plugin, defined by a name</li>
 * </ul>
 *
 * @see Plugin
 * @author Philipp C. Heckel <philipp.heckel@gmail.com>
 */
public class Plugins {
	private static Map<String, TransferPlugin> transferPlugins = null;

	private static Map<String, WebInterfacePlugin> webInterfacePlugins = null;

	/**
	 * Loads and returns a list of all available
	 * {@link TransferPlugin}s.
	 */
	public static List<TransferPlugin> transferPlugins() {
		loadPlugins();
		return new ArrayList<TransferPlugin>(transferPlugins.values());
	}

	public static List<WebInterfacePlugin> webInterfacePlugins() {
		loadPlugins();
		return new ArrayList<WebInterfacePlugin>(webInterfacePlugins.values());
	}

	private static void loadPlugins() {
		if (transferPlugins == null) {
			transferPlugins = new TreeMap<>();
			
			ClassLoader classLoader = Plugins.class.getClassLoader();
			 ServiceLoader<TransferPlugin> x = ServiceLoader.load(TransferPlugin.class, classLoader);
			for (TransferPlugin plugin : x) {
				transferPlugins.put(plugin.getId(), plugin);
			}
		}
	}

	public static TransferPlugin getTransferPlugin(String pluginId) {
		loadPlugins();
		return transferPlugins.get(pluginId);
	}

	public static void refresh() {
		transferPlugins = null;
	}

	public static Plugin get(String pluginId) {
		loadPlugins();

		// All plugins are currently transfer plugins.
		List<Map<String, ? extends Plugin>> allPluginMaps = Collections.singletonList(transferPlugins);

		for (Map<String, ? extends Plugin> pluginMap : allPluginMaps) {
			if (pluginMap.containsKey(pluginId)) {
				return pluginMap.get(pluginId);
			}
		}
	
		return null;
	}

}
