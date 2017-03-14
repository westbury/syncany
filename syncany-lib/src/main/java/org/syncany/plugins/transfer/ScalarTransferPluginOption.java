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

import org.syncany.api.FileType;
import org.syncany.api.transfer.StorageException;
import org.w3c.dom.Element;

/**
 * @author Nigel Westbury
 *
 */
public abstract class ScalarTransferPluginOption extends TransferPluginOption {

	private final FileType fileType;
	private final boolean encrypted;
	private final boolean sensitive;
	private final boolean singular;
	private final Class<? extends TransferPluginOptionConverter> converter;
	
	protected String stringValue;

	/**
	 * @param displayName
	 * @param fileType
	 * @param encrypted
	 * @param sensitive
	 * @param singular
	 * @param visible
	 * @param required
	 * @param callback
	 * @param converter
	 * @param stringValue
	 */
	public ScalarTransferPluginOption(String id, String displayName, FileType fileType, boolean encrypted, boolean sensitive, boolean singular,
			boolean visible, boolean required, Class<? extends TransferPluginOptionCallback> callback, Class<? extends TransferPluginOptionConverter> converter,
			String stringValue) {
		super(id, displayName, visible, required, callback);
		
		this.fileType = fileType;
		this.encrypted = encrypted;
		this.sensitive = sensitive;
		this.singular = singular;
		this.converter = converter;
		
		this.stringValue = stringValue;
	}

	public FileType getFileType() {
		return fileType;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public boolean isSensitive() {
		return sensitive;
	}

	public boolean isSingular() {
		return singular;
	}

	@Override
	public void setValueFromXml(Element element) {
		// TODO Auto-generated method stub

	}

	public String getStringValue() {
		return stringValue;
	}

	public Class<? extends TransferPluginOptionConverter> getConverter() {
		return converter;
	}

	public ValidationResult isValid(String value) {
		if (!validateInputMandatory(value)) {
			return ValidationResult.INVALID_NOT_SET;
		}

		if (!validateInputType(value)) {
			return ValidationResult.INVALID_TYPE;
		}

		return ValidationResult.VALID;
	}

	private boolean validateInputMandatory(String value) {
		return !isRequired() || (value != null && !value.equals(""));
	}

	public abstract boolean validateInputType(String value);

	public abstract void setStringValue(String stringValue) throws StorageException;
}
