package org.syncany.config.to;

import javax.xml.bind.annotation.XmlAttribute;

public class EncryptedKey {

	private String salt;

	private String key;

	@XmlAttribute
	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	@XmlAttribute
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
