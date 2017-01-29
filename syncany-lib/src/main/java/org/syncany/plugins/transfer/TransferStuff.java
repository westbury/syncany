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
package org.syncany.plugins.transfer;

import org.syncany.config.Config;
import org.syncany.config.ConfigException;
import org.syncany.config.to.ConfigTO;
import org.syncany.config.to.Connection;
import org.syncany.plugins.Plugins;
import org.syncany.plugins.transfer.plugin.TransferPlugin;

/**
 * @author Nigel
 *
 */
public class TransferStuff {

	private Config config;
	private TransferPlugin plugin;
	private Connection transferSettings;

	public TransferStuff(Config config) throws ConfigException {
		this.config = config;
		initConnection(config.getConfigTO());
	}

	private void initConnection(ConfigTO configTO) throws ConfigException {
		if (configTO.getConnection() != null) {
			plugin = Plugins.get(configTO.getConnection().getType(), TransferPlugin.class);

			if (plugin == null) {
				throw new ConfigException("Plugin not supported: " + configTO.getConnection().getType());
			}

			try {
				transferSettings = configTO.getConnection();
			}
			catch (Exception e) {
				throw new ConfigException("Cannot initialize storage: " + e.getMessage(), e);
			}
		}
	}

	public TransferManager getTransferManager() throws StorageException {
		return plugin.createTransferManager(transferSettings.getSettings(), config);
	}

	
}
