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
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.syncany.config.ConfigException;
import org.syncany.crypto.SaltedSecretKey;
import org.syncany.crypto.SaltedSecretKeyConverter;

/**
 * The user config transfer object is a helper data structure that allows storing
 * a user's global system settings such as system properties.
 *
 * <p>It uses the Simple framework for XML serialization, and its corresponding
 * annotation-based configuration.
 *
 * @see <a href="http://simple.sourceforge.net/">Simple framework</a>
 * @author Philipp C. Heckel <philipp.heckel@gmail.com>
 */
@XmlRootElement(name = "userConfig")
public class UserConfigTO {
//	@ElementMap(name = "systemProperties", entry = "property", key = "name", required = false, attribute = true)
	private TreeMap<String, String> systemProperties;

	private boolean preventStandby;

	private SaltedSecretKey configEncryptionKey;
	
	public UserConfigTO() {
		this.systemProperties = new TreeMap<String, String>();
		this.preventStandby = false;
	}

	public Map<String, String> getSystemProperties() {
		return systemProperties;
	}

	public boolean isPreventStandby() {
		return preventStandby;
	}
	
	public void setPreventStandby(boolean preventStandby) {
		this.preventStandby = preventStandby;
	}

	public SaltedSecretKey getConfigEncryptionKey() {
		return configEncryptionKey;
	}

	@XmlJavaTypeAdapter( SaltedSecretKeyConverter.class )
	public void setConfigEncryptionKey(SaltedSecretKey configEncryptionKey) {
		this.configEncryptionKey = configEncryptionKey;
	}


	public static UserConfigTO load(File file) throws ConfigException {
		try {
			JAXBContext context = JAXBContext.newInstance(UserConfigTO.class);
			Unmarshaller jaxbUnmarshaller = context.createUnmarshaller();
			return (UserConfigTO) jaxbUnmarshaller.unmarshal(file);
		}
		catch (Exception e) {
			throw new ConfigException("User config file cannot be read or is invalid: " + file, e);
		}
	}

	public void save(File file) throws ConfigException {
		try {
			JAXBContext context = JAXBContext.newInstance();
			Marshaller jaxbMarshaller = context.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(this, file);
		}
		catch (Exception e) {
			throw new ConfigException("Cannot write user config to file " + file, e);
		}
	}
}
