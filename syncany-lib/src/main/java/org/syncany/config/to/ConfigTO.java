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
package org.syncany.config.to;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.syncany.config.ConfigException;
import org.syncany.crypto.SaltedSecretKey;
import org.syncany.crypto.SaltedSecretKeyConverter;

/**
 * The config transfer object is used to create and load the local config
 * file from/to XML. The config file contains local config settings of a client,
 * namely the machine and display name, the master key as well as connection
 * information (for the connection plugin).
 *
 * <p>It uses the Simple framework for XML serialization, and its corresponding
 * annotation-based configuration.
 *
 * @see <a href="http://simple.sourceforge.net/">Simple framework</a>
 * @author Philipp C. Heckel <philipp.heckel@gmail.com>
 */
@XmlRootElement(name="config")
public class ConfigTO {

	private String machineName;

	private String displayName;

	private SaltedSecretKey masterKey;

	// TODO [high] Workaround for 'connect' via GUI and syncany://link; field not needed when link is supplied
	private Connection connection;

	private Long cacheKeepBytes;

	public static ConfigTO load(File file) throws ConfigException {
		try {
			// pluginSettings.toArray(new Class[0])
			JAXBContext context = JAXBContext.newInstance(ConfigTO.class);
			Unmarshaller jaxbUnmarshaller = context.createUnmarshaller();
	
			ConfigTO result = (ConfigTO) jaxbUnmarshaller.unmarshal(file);

			return result;
			
//			registry.bind(SaltedSecretKey.class, new SaltedSecretKeyConverter());
//			registry.bind(String.class, new EncryptedTransferSettingsConverter());
		}
		catch (Exception ex) {
			throw new ConfigException("Config file does not exist or is invalid: " + file, ex);
		}
	}

	public void save(File file) throws ConfigException {
		try {
			JAXBContext context = JAXBContext.newInstance(ConfigTO.class);
			
			Marshaller jaxbMarshaller = context.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(this, file);

//			registry.bind(SaltedSecretKey.class, new SaltedSecretKeyConverter());
//			registry.bind(String.class, new EncryptedTransferSettingsConverter(connection.getClass()));
		}
		catch (Exception e) {
			throw new ConfigException("Cannot write config to file " + file, e);
		}
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public SaltedSecretKey getMasterKey() {
		return masterKey;
	}

	@XmlJavaTypeAdapter( SaltedSecretKeyConverter.class )
	public void setMasterKey(SaltedSecretKey masterKey) {
		this.masterKey = masterKey;
	}

	public Long getCacheKeepBytes() {
		return cacheKeepBytes;
	}

	public void setCacheKeepBytes(Long cacheKeepBytes) {
		this.cacheKeepBytes = cacheKeepBytes;
	}

}
