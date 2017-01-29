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
package org.syncany.plugins.transfer.plugin;

import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.syncany.config.Cache;
import org.syncany.config.Config;
import org.syncany.config.ConfigException;
import org.syncany.plugins.Plugin;
import org.syncany.plugins.transfer.StorageException;
import org.syncany.plugins.transfer.TransferManager;
import org.syncany.plugins.transfer.files.RemoteFile;
import org.syncany.util.ReflectionUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The transfer plugin is a special plugin responsible for transferring files
 * to the remote storage. Implementations must provide implementations for
 * {@link TransferPlugin} (this class), {@link TransferSettings} (connection
 * details) and {@link TransferManager} (transfer methods).<br/><br/>
 *
 * <p>Plugins have to follow a naming convention:
 * <ul>
 *   <li>Package names have to be lower snaked cased</li>
 *   <li>Class names have to be camel cased</li>
 *   <li>Package names will be converted to class names by replacing underscores ('_') and uppercasing the
 *      subsequent character.</li>
 * </ul>
 *
 * <p>Example:</b> 
 * A plugin is called DummyPlugin, hence <i>org.syncany.plugins.dummy_plugin.DummyPluginTransferPlugin</i> is the
 * plugin's {@link TransferPlugin} class and <i>org.syncany.plugins.dummy_plugin.DummyPluginTransferSettings</i> is the
 * corresponding {@link TransferSettings} implementation.
 *
 * @author Philipp C. Heckel <philipp.heckel@gmail.com>
 * @author Christian Roth <christian.roth@port17.de>
 */
public abstract class TransferPlugin extends Plugin {
	public TransferPlugin(String pluginId) {
		super(pluginId);
	}

	/**
	 * Creates an empty plugin-specific {@link org.syncany.plugins.transfer.plugin.TransferSettings} instance.
	 *
	 * @return Empty plugin-specific {@link org.syncany.plugins.transfer.plugin.TransferSettings} instance.
	 * @throws StorageException Thrown if no {@link org.syncany.plugins.transfer.plugin.TransferSettings} are attached to a
	 *         plugin using {@link org.syncany.plugins.transfer.PluginSettings}
	 */
	@SuppressWarnings("unchecked")
	public final <T extends TransferSettings> T createEmptySettings() throws ConfigException {
		final Class<? extends TransferSettings> transferSettings = TransferPluginUtil.getTransferSettingsClass(this.getClass());

		if (transferSettings == null) {
			throw new ConfigException("TransferPlugin does not have any settings attached!");
		}

		try {
			return (T) transferSettings.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Unable to create TransferSettings: " + e.getMessage());
		}
	}

	 /**
	 * Creates an initialized, plugin-specific {@link org.syncany.plugins.transfer.TransferManager} object using the given
	 * connection details.
	 *
	 * <p>The created instance can be used to upload/download/delete {@link RemoteFile}s
	 * and query the remote storage for a file list.
	 *
	 * @param list A valid {@link org.syncany.plugins.transfer.plugin.TransferSettings} instance.
	 * @param config A valid {@link org.syncany.config.Config} instance.
	 * @return A initialized, plugin-specific {@link org.syncany.plugins.transfer.TransferManager} instance.
	 * @throws StorageException Thrown if no (valid) {@link org.syncany.plugins.transfer.TransferManager} are attached to
	*  a plugin using {@link org.syncany.plugins.transfer.PluginManager}
	 */
	@SuppressWarnings("unchecked")
	public final <T extends TransferManager> T createTransferManager(List<Element> list, Config config) throws StorageException {
//		if (!list.isValid()) {
//			throw new StorageException("Unable to create transfer manager: connection isn't valid (perhaps missing some mandatory fields?)");
//		}
		Cache cache = (config==null) ? null : config.getCache();
		
		final Class<? extends TransferSettings> transferSettingsClass = TransferPluginUtil.getTransferSettingsClass(this.getClass());

		if (transferSettingsClass == null) {
			throw new RuntimeException("Unable to create transfer manager: No settings class attached");
		}

		try {
			TransferSettings value = deserializeToTransferSettings(transferSettingsClass, list);

			final Class<? extends TransferManager> transferManagerClass = TransferPluginUtil.getTransferManagerClass(this.getClass());

			if (transferManagerClass == null) {
				throw new RuntimeException("Unable to create transfer manager: No manager class attached");
			}

			Constructor<?> potentialConstructor = ReflectionUtil.getMatchingConstructorForClass(transferManagerClass, TransferSettings.class,
					Cache.class);

			if (potentialConstructor == null) {
				throw new RuntimeException("Invalid arguments for constructor in pluginclass -- must be 2 and subclass of " + TransferSettings.class
						+ " and " + Config.class);
			}

			return (T) potentialConstructor.newInstance(transferSettingsClass.cast(value), cache);
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to create transfer settings: " + e.getMessage(), e);
		}
		catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Unable to create transfer settings: " + e.getMessage(), e);
		}
		catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Unable to create transfer settings: " + e.getMessage(), e);
		}
		catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Unable to create transfer settings: " + e.getMessage(), e);
		}
	}

	public TransferSettings deserializeToTransferSettings(final Class<? extends TransferSettings> transferSettingsClass, List<Element> list)
			throws ParserConfigurationException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException,
			JAXBException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();
		
		Element root = doc.createElement("root");
		doc.appendChild(root);
		
		root.setAttribute("type", "ftp");
		
		for (Element element : list) {
			Node elementCopy = doc.importNode(element, true);
			root.appendChild(elementCopy);
		}
		
		// See what's in the dom
		StreamResult xmlOutput = new StreamResult(new StringWriter());
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(doc), xmlOutput);
		String y = xmlOutput.getWriter().toString();
		
		JAXBContext context = JAXBContext.newInstance(transferSettingsClass);
		Unmarshaller jaxbUnmarshaller = context.createUnmarshaller();

		JAXBElement<? extends TransferSettings> settings = jaxbUnmarshaller.unmarshal(new DOMSource(doc), transferSettingsClass);

		TransferSettings value = settings.getValue();
		return value;
	}
}
