package org.syncany.config.to;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;

import org.w3c.dom.Element;

public class Connection {

	private String type;

	private List<Element> settings;

	@XmlAttribute
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlAnyElement(lax=true)
	public List<Element> getSettings() {
		return settings;
	}

	public void setSettings(List<Element> body) {
		this.settings = body;
	}
}
