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
package org.syncany.plugins.transfer;

import java.util.List;

import org.syncany.api.transfer.TransferSettings;
import org.syncany.api.transfer.TransferSettingsSetter;
import org.w3c.dom.Element;

/**
 * A nested plugin option is a special {@link TransferPluginOption} -- namely an
 * option that contains a complex object rather than just a simple value.
 *
 * <p>Nested plugin options are typically used to represent/use sub-plugins
 * within a certain plugin, e.g. to allow building a RAID0/1 plugin.
 *
 * @author Christian Roth <christian.roth@port17.de>
 */
public class NestedTransferPluginOption<T extends TransferSettings> extends TransferPluginOption {
	private TransferSettingsSetter<T> setter;
	private int level;

	public NestedTransferPluginOption(String id, String displayName,
			boolean required,
			TransferSettingsSetter<T> setter, Class<? extends TransferPluginOptionCallback> callback, int level) {

		super(id, displayName, true, required, callback);
		this.setter = setter;
		this.level = level;
	}

	public List<TransferPluginOption> getOptions() {
		T nestedSettings = setter.getValue();
		List<TransferPluginOption> nestedOptions = TransferPluginOptions.getOrderedOptions(nestedSettings, level+1);
		return nestedOptions;
	}

	@Override
	public void setValueFromXml(Element element) {
		// TODO Auto-generated method stub
		
	}

	public Class<T> getTransferSettingsClass() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setNestedSettings(TransferSettings childSettings) {
		// TODO Auto-generated method stub
		
	}

}
