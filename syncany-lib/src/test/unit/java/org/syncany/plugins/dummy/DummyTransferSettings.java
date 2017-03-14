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
package org.syncany.plugins.dummy;

import org.simpleframework.xml.core.Validate;
import org.syncany.api.transfer.LocalDiskCache;
import org.syncany.api.transfer.PropertyVisitor;
import org.syncany.api.transfer.Setter;
import org.syncany.api.transfer.StorageException;
import org.syncany.api.transfer.TransferManager;
import org.syncany.api.transfer.TransferSettings;
import org.syncany.api.transfer.TransferSettingsSetter;
import org.syncany.plugins.local.LocalTransferSettings;

/**
 * @author Christian Roth <christian.roth@port17.de>
 */
public class DummyTransferSettings implements TransferSettings {
	public enum DummyEnum {
		A, B
	}

	public String foo;

	public String baz;

	public int number;

	public TransferSettings subsettings;

	public LocalTransferSettings subsettings2;

	public DummyEnum enumField;

	private String getFoo() {
		return foo;
	}
	
	private void setFoo(String foo) {
		this.foo = foo;
	}
	
	private int getNumber() {
		return number;
	}
	
	private void setNumber(int number) {
		this.number = number;
	}
	
	private String getBaz() {
		return baz;
	}
	
	private void setBaz(String baz) {
		this.baz = baz;
	}
	
	@Validate
	public void validate() throws StorageException {
		// 'foo' is a required field
		if (foo == null) {
			throw new StorageException("Missing mandatory field DummyTransferSettings#foo");
		}

		if (baz != null && !baz.equalsIgnoreCase("baz")) {
			throw new StorageException("Only allowed value for baz field is 'baz'");
		}
	}

	@Override
	public void visitProperties(PropertyVisitor visitor) {
		visitor.stringProperty("foo", "foo", true, true, true, false, true, this::getFoo, this::setFoo);
		
		visitor.integerProperty("number", "number", false, false, false, false, true, this::getNumber, this::setNumber);
		
		visitor.stringProperty("baz", "baz", false, false, false, false, true, this::getBaz, this::setBaz);
		
		visitor.nestedSettingsProperty("nest", "Some generic nested settings", false, new TransferSettingsSetter<TransferSettings>(){
			@Override
			public TransferSettings getValue() {
				return subsettings;
			}
			@Override
			public void setValue(TransferSettings value) {
				subsettings = value;
			}
			@Override
			public Class<TransferSettings> getSettingsClass() {
				return TransferSettings.class;
			}
		});
		
		visitor.nestedSettingsProperty("nest2", "Some nested settings", false, new TransferSettingsSetter<LocalTransferSettings>(){
			@Override
			public LocalTransferSettings getValue() {
				return subsettings2;
			}
			@Override
			public void setValue(LocalTransferSettings value) {
				subsettings2 = value;
			}
			@Override
			public Class<LocalTransferSettings> getSettingsClass() {
				return LocalTransferSettings.class;
			}
		});

		visitor.enumProperty("enumField", "A enum field", false, DummyEnum.values(), enumField, new Setter<DummyEnum>(){
			@Override
			public String validateValue(DummyEnum value) {
				return null;
			}
			@Override
			public void setValue(DummyEnum value) {
				enumField = value;
			}
		});

	}

	@Override
	public TransferManager createTransferManager(LocalDiskCache cache) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getReasonForLastValidationFail() {
		// TODO Auto-generated method stub
		return null;
	}
}
