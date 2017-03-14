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
package org.syncany.tests.unit.plugins.transfer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.simpleframework.xml.Element;
import org.syncany.api.transfer.LocalDiskCache;
import org.syncany.api.transfer.PropertyVisitor;
import org.syncany.api.transfer.TransferManager;
import org.syncany.api.transfer.TransferSettings;
import org.syncany.plugins.dummy.DummyTransferSettings;
import org.syncany.plugins.transfer.Setup;
import org.syncany.plugins.transfer.TransferPluginOption;
import org.syncany.plugins.transfer.TransferPluginOptionCallback;
import org.syncany.plugins.transfer.TransferPluginOptions;

public class TransferPluginOptionsTest {
	@Test
	public void testGetOrderedOptionsWithDummyPlugin() {
		List<TransferPluginOption> orderedOptions = TransferPluginOptions.getOrderedOptions(new DummyTransferSettings());
		
		assertEquals(6, orderedOptions.size());
		assertEquals("foo", orderedOptions.get(0).getId());
		assertEquals("number", orderedOptions.get(1).getId());
		assertEquals("baz", orderedOptions.get(2).getId());
		assertEquals("nest", orderedOptions.get(3).getId());
		assertEquals("nest2", orderedOptions.get(4).getId());
		assertEquals("enumField", orderedOptions.get(5).getId());
	}
	
	@Test
	public void testGetOrderedOptionsWithAnotherDummyPlugin() {
		List<TransferPluginOption> orderedOptions = TransferPluginOptions.getOrderedOptions(new AnotherDummyTransferSettings());
		
		assertEquals(3, orderedOptions.size());
		assertEquals("noSetupAnnotation", orderedOptions.get(0).getId());
		assertEquals("singularNonVisible", orderedOptions.get(1).getId());
		assertEquals("someCallback", orderedOptions.get(2).getId());		
	}
	
	public static class AnotherDummyTransferSettings implements TransferSettings {
		@Element(required = true)
		public String noSetupAnnotation;
		
		@Element(required = true)
		@Setup(singular = true, visible = false)
		public String singularNonVisible;
		
		@Element(required = true)
		@Setup(callback = TransferPluginOptionCallback.class)
		public String someCallback;

		private String getNoSetupAnnotation() {
			return noSetupAnnotation;
		}
		
		private void setNoSetupAnnotation(String value) {
			noSetupAnnotation = value;
		}

		private String getSingularNonVisible() {
			return singularNonVisible;
		}
		
		private void setSingularNonVisible(String value) {
			singularNonVisible = value;
		}
		
		private String getSomeCallback() {
			return someCallback;
		}
		
		private void setSomeCallback(String value) {
			someCallback = value;
		}

		@Override
		public void visitProperties(PropertyVisitor visitor) {
			visitor.stringProperty("noSetupAnnotation", "noSetupAnnotation", true, true, true, false, true, this::getNoSetupAnnotation, this::setNoSetupAnnotation);
			visitor.stringProperty("singularNonVisible", "singularNonVisible", true, true, true, false, true, this::getSingularNonVisible, this::setSingularNonVisible);
			visitor.stringProperty("someCallback", "someCallback", true, true, true, false, true, this::getSomeCallback, this::setSomeCallback);
		}

		@Override
		public TransferManager createTransferManager(LocalDiskCache cache) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getType() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isValid() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getReasonForLastValidationFail() {
			throw new UnsupportedOperationException();
		}
	}

	public static class AnotherDummyTransferPluginOptionCallback implements TransferPluginOptionCallback {
		@Override
		public String preQueryCallback() {
			return "hi there";
		}

		@Override
		public String postQueryCallback(String optionValue) {
			return "bye there";
		}		
	}
}
