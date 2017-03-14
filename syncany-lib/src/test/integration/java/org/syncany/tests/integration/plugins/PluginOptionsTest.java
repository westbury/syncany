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
package org.syncany.tests.integration.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.syncany.api.transfer.TransferSettings;
import org.syncany.plugins.dummy.DummyTransferSettings;
import org.syncany.plugins.transfer.NestedTransferPluginOption;
import org.syncany.plugins.transfer.TransferPluginOption;
import org.syncany.plugins.transfer.TransferPluginOptions;
import org.syncany.util.ReflectionUtil;

public class PluginOptionsTest {

	@Test
	@Ignore
	// TODO rewrite to make this test work again with generic nested transfer settings
	public void nestedSettingsTest() throws Exception {
		DummyTransferSettings dts = new DummyTransferSettings();

		for (TransferPluginOption option : TransferPluginOptions.getOrderedOptions(dts)) {
			NestedTransferPluginOption<?> nestedOptions = (NestedTransferPluginOption<?>) option;
			askNestedPluginSettings(dts, nestedOptions, 0);
		}

		assertNotNull(dts.baz);
		assertNotNull(dts.foo);
		assertNotNull(dts.number);
		assertNotNull(dts.subsettings);
	}

	private <T extends TransferSettings> void askNestedPluginSettings(TransferSettings settings, NestedTransferPluginOption<T> option, int wrap) throws Exception {

		if (option instanceof NestedTransferPluginOption) {
			NestedTransferPluginOption<?> nestedOptions = (NestedTransferPluginOption) option;
			assertNotNull(ReflectionUtil.getClassFromType(nestedOptions.getTransferSettingsClass()));
			System.out.println(new String(new char[wrap]).replace("\0", "\t") + ReflectionUtil.getClassFromType(nestedOptions.getTransferSettingsClass()) + "#"
					+ option.getId() + " (nested)");
			TransferSettings nestedSettings = (TransferSettings) ReflectionUtil.getClassFromType(nestedOptions.getTransferSettingsClass()).newInstance();
			nestedOptions.setNestedSettings(nestedSettings);

			for (TransferPluginOption nItem : nestedOptions.getOptions()) {
				NestedTransferPluginOption<?> nestedItem = (NestedTransferPluginOption<?>) option;
				askNestedPluginSettings(nestedSettings, nestedItem, ++wrap);
			}
		}
		else {
			System.out.println(new String(new char[wrap]).replace("\0", "\t") + settings.getClass() + "#" + option.getId());
			// TODO what's this about? Nigel
//			settings.setField(option.getField().getName(), String.valueOf(settings.hashCode()));
		}
	}

	@Test
	@Ignore
	public void testOrderingOfOptions() throws Exception {
		final String[] expectedOrder = new String[] { "foo", "number", "baz", "nest" };

		DummyTransferSettings dts = new DummyTransferSettings();
		List<TransferPluginOption> items = TransferPluginOptions.getOrderedOptions(dts);

		int i = 0;
		for (TransferPluginOption item : items) {
			assertEquals(expectedOrder[i++], item.getId());
		}
	}

}
