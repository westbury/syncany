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

import org.syncany.api.transfer.TransferSettings;
import org.w3c.dom.Element;

/**
 * A plugin option represents a single setting of a transfer plugin
 * within the corresponding {@link TransferSettings} class. A plugin option
 * is created during the initialization from the {@link Setup} annotation
 * to aid the guided repository setup (init and connect).
 *
 * @author Christian Roth <christian.roth@port17.de>
 */
public abstract class TransferPluginOption {
	public enum ValidationResult {
		VALID, INVALID_TYPE, INVALID_NOT_SET
	}

	private final String id;
	private final String displayName;
	private final boolean visible;
	private final boolean required;
	private final Class<? extends TransferPluginOptionCallback> callback;
	
	public TransferPluginOption(String id, String displayName, boolean visible, boolean required, Class<? extends TransferPluginOptionCallback> callback) {
		this.id = id;
		this.displayName = displayName;
		this.visible = visible;
		this.required = required;
		this.callback = callback;
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return displayName;
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isRequired() {
		return required;
	}

	public Class<? extends TransferPluginOptionCallback> getCallback() {
		return callback;
	}

	public abstract void setValueFromXml(Element element);
}
