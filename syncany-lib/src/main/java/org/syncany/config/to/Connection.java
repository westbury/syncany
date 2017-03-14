package org.syncany.config.to;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.syncany.api.FileType;
import org.syncany.api.transfer.PropertyVisitor;
import org.syncany.api.transfer.Setter;
import org.syncany.api.transfer.TransferSettings;
import org.syncany.api.transfer.TransferSettingsSetter;
import org.syncany.config.UserConfig;
import org.syncany.crypto.CipherException;
import org.syncany.crypto.CipherSpecs;
import org.syncany.crypto.CipherUtil;
import org.syncany.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Connection {

	/**
	 * @author Nigel Westbury
	 *
	 */
	private static final class PropertyVisitorWritingToXml implements PropertyVisitor {
		private final Document document;

		private final List<Element> elements;

		private final List<Exception> exceptions;
		
		/**
		 * @param document 
		 * @param elements
		 * @param exceptions 
		 */
		private PropertyVisitorWritingToXml(Document document, List<Element> elements, List<Exception> exceptions) {
			this.document = document;
			this.elements = elements;
			this.exceptions = exceptions;
		}

		@Override
		public void stringProperty(String id, String displayName, boolean isRequired, boolean storeEncrypted, boolean sensitive,
				boolean singular, boolean visible, Supplier<String> value, Consumer<String> setter) {
			try {
				Element element = document.createElement(id);
				if (storeEncrypted) {
					element.setTextContent(encrypt(value.get()));
					element.setAttribute("encrypted", Boolean.TRUE.toString());
				} else {
					element.setTextContent(value.get());
				}
				elements.add(element);
			} catch (CipherException e) {
				exceptions.add(e);
			}
		}

		@Override
		public void integerProperty(String id, String displayName, boolean isRequired, boolean storeEncrypted, boolean sensitive, boolean singular, boolean visible, Supplier<Integer> value, Consumer<Integer> setter) {
			try {
				Element element = document.createElement(id);
				if (storeEncrypted) {
					element.setTextContent(encrypt(value.get().toString()));
					element.setAttribute("encrypted", Boolean.TRUE.toString());
				} else {
					element.setTextContent(value.get().toString());
				}
				elements.add(element);
			} catch (CipherException e) {
				exceptions.add(e);
			}
		}

		@Override
		public void booleanProperty(String id, String displayName, boolean isRequired, boolean singular, boolean visible, boolean value,
				Setter<Boolean> setter) {
			Element element = document.createElement(id);
			element.setTextContent(Boolean.toString(value));
			elements.add(element);				
		}

		@Override
		public void fileProperty(String id, String displayName, boolean isRequired, boolean singular, boolean visible, FileType folder, File value,
				Setter<File> setter) {
			if (value != null) {
				Element element = document.createElement(id);
				element.setTextContent(value.getAbsolutePath());
				elements.add(element);
			}
		}

		@Override
		public <T extends Enum<T>> void enumProperty(String id, String displayName, boolean isRequired, T[] options, T value, Setter<T> setter) {
			if (value != null) {
				Element element = document.createElement(id);
				element.setTextContent(value.name());
				elements.add(element);
			}
		}

		@Override
		public void nestedSettingsProperty(String id, String displayName, boolean isRequired,
				TransferSettingsSetter<?> setter) {
			TransferSettings childSettings = setter.getValue();
			if (childSettings != null) {
				Element element = document.createElement(id);
				element.setAttribute("type", childSettings.getType());
				elements.add(element);

				List<Element> childElements = new ArrayList<>();
				PropertyVisitor childVisitor = new PropertyVisitorWritingToXml(document, childElements, exceptions);
				childSettings.visitProperties(childVisitor);
				
				for (Element childElement : childElements) {
					element.appendChild(childElement);
				}
			}
			
		}
	}

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

	public void setTransferSettings(TransferSettings transferSettings) {
		List<Element> elements = buildElementsFromSettings(transferSettings);
		setSettings(elements);
	}

	// TODO this should really be private but we have tests that call it directly.
	public static List<Element> buildElementsFromSettings(TransferSettings transferSettings) {
		final List<Element> elements = new ArrayList<>();

		DocumentBuilder documentBuilder;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			documentBuilder = dbf.newDocumentBuilder();

			Document document = documentBuilder.newDocument();

			List<Exception> exceptions = new ArrayList<>();
			
			transferSettings.visitProperties(new PropertyVisitorWritingToXml(document, elements, exceptions));

			return elements;
		}
		catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void fillTransferSettings(TransferSettings transferSettings) {
		Map<String, Element> values = new HashMap<>();
		for (Element setting : settings) {
			values.put(setting.getNodeName(), setting);
		}
		
		List<Exception> exceptions = new ArrayList<>();
		
		transferSettings.visitProperties(new PropertyVisitor() {

			@Override
			public void stringProperty(String id, String displayName, boolean isRequired, boolean storeEncrypted, boolean sensitive,
					boolean singular, boolean visible, Supplier<String> value, Consumer<String> setter) {
				try {
					Element element = values.get(id);
					String valueFromXml = element.getTextContent();

					String encryptedAttribute = element.getAttribute("encrypted");
					if (encryptedAttribute != null && encryptedAttribute.equals(Boolean.TRUE.toString())) {
						valueFromXml = decrypt(valueFromXml);
					}

					setter.accept(valueFromXml);
				} catch (CipherException e) {
					exceptions.add(e);
				}
			}

			@Override
			public void integerProperty(String id, String displayName, boolean isRequired, boolean storeEncrypted,
					boolean sensitive, boolean singular, boolean visible, Supplier<Integer> value, Consumer<Integer> setter) {
				try {
					Element element = values.get(id);
					String valueFromXml = element.getTextContent();

					String encryptedAttribute = element.getAttribute("encrypted");
					if (encryptedAttribute != null && encryptedAttribute.equals(Boolean.TRUE.toString())) {
						valueFromXml = decrypt(valueFromXml);
					}

					if (valueFromXml != null) {
						setter.accept(Integer.valueOf(valueFromXml));
					}
				} catch (CipherException e) {
					exceptions.add(e);
				}
			}

			@Override
			public void booleanProperty(String id, String displayName, boolean isRequired, boolean singular, boolean visible,
					boolean value, Setter<Boolean> setter) {
				Element element = values.get(id);
				String valueFromXml = element.getTextContent();
				if (valueFromXml != null) {
					setter.setValue(Boolean.valueOf(valueFromXml));
				}
			}

			@Override
			public void fileProperty(String id, String displayName, boolean isRequired, boolean singular, boolean visible,
					FileType folder, File value, Setter<File> setter) {
				Element element = values.get(id);
				String valueFromXml = element.getTextContent();
				if (valueFromXml != null) {
					setter.setValue(new File(valueFromXml));
				}
			}

			@Override
			public <T extends Enum<T>> void enumProperty(String id, String displayName, boolean isRequired, T[] options, T value, Setter<T> setter) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void nestedSettingsProperty(String id, String displayName, boolean isRequired,
					TransferSettingsSetter<?> setter) {
				// TODO Auto-generated method stub
				
			}
		});		
	}

	public static String decrypt(String encryptedHexString) throws CipherException {
		byte[] encryptedBytes = StringUtil.fromHex(encryptedHexString);
		byte[] decryptedBytes = CipherUtil.decrypt(new ByteArrayInputStream(encryptedBytes), UserConfig.getConfigEncryptionKey());

		return new String(decryptedBytes);
	}

	public static String encrypt(String decryptedPlainString) throws CipherException {
		InputStream plaintextInputStream = IOUtils.toInputStream(decryptedPlainString);
		byte[] encryptedBytes = CipherUtil.encrypt(plaintextInputStream, CipherSpecs.getDefaultCipherSpecs(), UserConfig.getConfigEncryptionKey());

		return StringUtil.toHex(encryptedBytes);
	}
}
