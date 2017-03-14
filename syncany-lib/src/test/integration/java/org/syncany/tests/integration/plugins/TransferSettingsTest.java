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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.syncany.api.transfer.StorageException;
import org.syncany.api.transfer.TransferManager;
import org.syncany.api.transfer.TransferPlugin;
import org.syncany.api.transfer.TransferSettings;
import org.syncany.config.Config;
import org.syncany.config.to.ConfigTO;
import org.syncany.config.to.Connection;
import org.syncany.operations.init.InitOperationOptions;
import org.syncany.plugins.Plugins;
import org.syncany.plugins.dummy.DummyTransferSettings;
import org.syncany.plugins.local.LocalTransferPlugin;
import org.syncany.plugins.local.LocalTransferSettings;
import org.syncany.plugins.transfer.EnumTransferPluginOption;
import org.syncany.plugins.transfer.TransferPluginOption;
import org.syncany.plugins.transfer.TransferPluginOptions;
import org.syncany.tests.util.TestConfigUtil;

public class TransferSettingsTest {
	private File tmpFile;
	private Config config;
	private TransferPlugin localTransferPlugin = new LocalTransferPlugin();

	@Before
	public void before() throws Exception {
		tmpFile = File.createTempFile("syncany-transfer-settings-test", "tmp");
		config = TestConfigUtil.createTestLocalConfig();
		assertNotNull(Plugins.getTransferPlugin("dummy"));
		assertNotNull(config);
	}

	@After
	public void after() throws Exception {
		tmpFile.delete();
		FileUtils.deleteDirectory(((LocalTransferSettings) config.getConnection()).getPath());
		FileUtils.deleteDirectory(config.getLocalDir());
		config = null;
	}

	@Test
	public void testRestore() throws Exception {

		final String fooTest = "foo-test";
		final String bazTest = "baz";
		final int numberTest = 1234;

		final DummyTransferSettings ts = new DummyTransferSettings();
		final LocalTransferSettings lts = new LocalTransferSettings();
		final InitOperationOptions initOperationOptions = TestConfigUtil.createTestInitOperationOptions("syncanytest");
		final ConfigTO conf = initOperationOptions.getConfigTO();

		LocalTransferSettings settings = (LocalTransferSettings)localTransferPlugin.createEmptySettings();
		Connection connection = initOperationOptions.getConfigTO().getConnection();
		connection.fillTransferSettings(settings);
		File repoDir = settings.getPath();
		
		File localDir = initOperationOptions.getLocalDir();

		conf.setConnection(connection);

		ts.foo = fooTest;
		ts.baz = bazTest;
		ts.number = numberTest;
		lts.setPath(File.createTempFile("aaa", "bbb"));
		ts.subsettings = lts;

		assertTrue(ts.isValid());
		Serializer serializer = new Persister();
		serializer.write(conf, tmpFile);

		System.out.println(new String(Files.readAllBytes(Paths.get(tmpFile.toURI()))));

		ConfigTO confRestored = ConfigTO.load(tmpFile);
		TransferPlugin plugin = Plugins.getTransferPlugin(confRestored.getConnection().getType());
		assertNotNull(plugin);

		Connection connRestored = confRestored.getConnection();
		assertNotNull(connRestored);

		TransferSettings tsRestored = plugin.createEmptySettings();
		connRestored.fillTransferSettings(tsRestored);
		TransferManager transferManager = tsRestored.createTransferManager(null);
		assertNotNull(transferManager);

		// Tear down
		FileUtils.deleteDirectory(localDir);
		FileUtils.deleteDirectory(repoDir);
	}

	@Test
	public void createNewValidConnectionTO() throws Exception {
		TransferPlugin p = Plugins.getTransferPlugin("dummy");
		DummyTransferSettings ts = (DummyTransferSettings)p.createEmptySettings();
		ts.foo = "foo-value";
		ts.number = 5;

		assertTrue(ts.isValid());
	}

	@Test
	public void createNewInvalidConnectionTO() throws Exception {
		TransferPlugin p = Plugins.getTransferPlugin("dummy");
		DummyTransferSettings ts = (DummyTransferSettings)p.createEmptySettings();

		assertFalse(ts.isValid());
	}

	@Test
	public void testDeserializeCorrectClass() throws Exception {
		InitOperationOptions initOperationOptions = TestConfigUtil.createTestInitOperationOptions("syncanytest");
		// Always LocalTransferSettings
		initOperationOptions.getConfigTO().save(tmpFile);

		ConfigTO confRestored = ConfigTO.load(tmpFile);

		assertEquals("local", confRestored.getConnection().getType());

		// Tear down
		FileUtils.deleteDirectory(initOperationOptions.getLocalDir());
		
		LocalTransferSettings settings = (LocalTransferSettings)localTransferPlugin.createEmptySettings();
		Connection connection = initOperationOptions.getConfigTO().getConnection();
		connection.fillTransferSettings(settings);
		File repoDir = settings.getPath();

		FileUtils.deleteDirectory(repoDir);
	}

	@Test
	public void testGetSettingsAndManagerFromPlugin() throws Exception {
		Class<? extends TransferSettings> settingsClass = Plugins.getTransferPlugin("dummy").createEmptySettings().getClass();
		assertEquals(DummyTransferSettings.class, settingsClass);
	}

	@Test
	public void testEnumSettingValid() throws Exception {
		final String enumValue = "A";

		DummyTransferSettings testTransferSettings = new DummyTransferSettings();
		
		List<TransferPluginOption> orderedOptions = TransferPluginOptions.getOrderedOptions(testTransferSettings);
		assertEquals("enumField", orderedOptions.get(5).getId());
		EnumTransferPluginOption<?> enumField = (EnumTransferPluginOption<?>)orderedOptions.get(5);
		
		enumField.setStringValue(enumValue);
		assertEquals(DummyTransferSettings.DummyEnum.A, testTransferSettings.enumField);

		final String enumValueLower = "a";

		enumField.setStringValue(enumValueLower);
		assertEquals(DummyTransferSettings.DummyEnum.A, testTransferSettings.enumField);
	}

	@Test(expected = StorageException.class)
	public void testEnumSettingInvalid() throws Exception {
		final String enumValue = "C"; // does not exist

		DummyTransferSettings testTransferSettings = new DummyTransferSettings();

		List<TransferPluginOption> orderedOptions = TransferPluginOptions.getOrderedOptions(testTransferSettings);
		assertEquals("enumField", orderedOptions.get(5).getId());
		EnumTransferPluginOption<?> enumField = (EnumTransferPluginOption<?>)orderedOptions.get(5);
		
		enumField.setStringValue(enumValue);
	}
}
