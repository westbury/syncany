/*
 * Syncany, www.syncany.org
 * Copyright (C) 2011-2017 Philipp C. Heckel <philipp.heckel@gmail.com> 
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

import java.util.Arrays;
import java.util.Optional;

import org.syncany.api.FileType;
import org.syncany.api.transfer.Setter;
import org.syncany.api.transfer.StorageException;
import org.syncany.util.StringUtil;

/**
 * @author Nigel Westbury
 *
 */
public class EnumTransferPluginOption<T extends Enum<T>> extends ScalarTransferPluginOption {

	private T[] enumOptions;
	private Setter<T> setter;

	/**
	 * @param name
	 * @param description
	 * @param fileType
	 * @param encrypted
	 * @param sensitive
	 * @param singular
	 * @param visible
	 * @param required
	 * @param callback
	 * @param converter
	 * @param value
	 */
	public EnumTransferPluginOption(String id, String displayName, FileType fileType, boolean encrypted, boolean sensitive, boolean singular,
			boolean visible, boolean required, Class<? extends TransferPluginOptionCallback> callback,
			Class<? extends TransferPluginOptionConverter> converter, T[] enumOptions, T value, Setter<T> setter) {
		// Bit of a hack here as we convert T to String
		super(id, displayName, fileType, encrypted, sensitive, singular, visible, required, callback, converter, value == null ? null : value.toString());
		this.enumOptions = enumOptions;
		this.setter = setter;
	}

	@Override
	public void setStringValue(String stringValue) throws StorageException {
		this.stringValue = stringValue;
		
		Optional<T> first = Arrays.stream(enumOptions)
	            .filter(x -> x.name().equalsIgnoreCase(stringValue.trim()))
	            .findFirst();
		if (!first.isPresent()) {
			throw new StorageException(stringValue + " is not a valid option for " + getId());
		}
		T value = first.get();		
		setter.setValue(value);
	}

	@Override
	public boolean validateInputType(String stringValue) {
		return Arrays.stream(enumOptions)
	            .anyMatch(x -> x.name().equalsIgnoreCase(stringValue.trim()));
	}

	@Override
	public String getDescription() {
		return String.format("%s, choose from %s", super.getDescription(), StringUtil.join(enumOptions, ", "));
	}
}
