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
package org.syncany.plugins.transfer;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.simpleframework.xml.Element;
import org.syncany.api.FileType;
import org.syncany.api.transfer.PropertyVisitor;
import org.syncany.api.transfer.Setter;
import org.syncany.api.transfer.TransferSettings;
import org.syncany.api.transfer.TransferSettingsSetter;

import com.google.common.collect.ImmutableList;

/**
 * Helper class to read the options of a {@link TransferSettings} using the
 * {@link Setup} and {@link Element} annotations.
 *
 * @author Christian Roth <christian.roth@port17.de>
 */
public class TransferPluginOptions {
	private static final int MAX_NESTED_LEVELS = 3;

	/**
	 * Get an ordered list of {@link TransferPluginOption}s, given class a {@link TransferSettings} class.
	 *
	 * <p>This method uses the {@link Setup} and {@link Element} annotation, and their attributes
	 * to sort the options. If no annotation is given or no order attribute is provided, the
	 * option will be listed last.
	 */
	public static List<TransferPluginOption> getOrderedOptions(TransferSettings transferSettings) {
		return getOrderedOptions(transferSettings, 0);
	}

	protected static List<TransferPluginOption> getOrderedOptions(TransferSettings transferSettings, int level) {
		ImmutableList.Builder<TransferPluginOption> options = ImmutableList.builder();

		transferSettings.visitProperties(new PropertyVisitor() {

			@Override
			public void stringProperty(String id, String displayName, boolean isRequired, boolean storeEncrypted, boolean sensitive,
					boolean singular, boolean visible, Supplier<String> value, Consumer<String> setter) {
				// TODO Nigel
				Class<? extends TransferPluginOptionCallback> callback = null;
				// TODO Nigel
				Class<? extends TransferPluginOptionConverter> converter = null;
				TransferPluginOption option = new ScalarTransferPluginOption(id, displayName, null, storeEncrypted, sensitive, singular, visible, isRequired, callback, converter, "todo") {
				
					@Override
					public void setStringValue(String stringValue) {
						this.stringValue = stringValue;
						setter.accept(stringValue);
					}

					@Override
					public boolean validateInputType(String value) {
						return true;
					}
				};
				options.add(option);
			}

			@Override
			public void integerProperty(String id, String displayName,  boolean isRequired, boolean storeEncrypted, boolean sensitive, boolean singular, boolean visible, Supplier<Integer> value, Consumer<Integer> setter) {
				// TODO Nigel
				Class<? extends TransferPluginOptionCallback> callback = null;
				// TODO Nigel
				Class<? extends TransferPluginOptionConverter> converter = null;
				TransferPluginOption option = new ScalarTransferPluginOption(id, displayName, null, storeEncrypted, sensitive, singular, visible, isRequired, callback, converter, "todo") {
				
					@Override
					public void setStringValue(String stringValue) throws IllegalArgumentException {
						int intValue = Integer.getInteger(stringValue);
						this.stringValue = stringValue;
						setter.accept(intValue);
					}

					@Override
					public boolean validateInputType(String value) {
						try {
							Integer.toString(Integer.parseInt(value));
							return true;
						}
						catch (NumberFormatException e) {
							return false;
						}
					}
				};
				options.add(option);
			}

			@Override
			public void booleanProperty(String id, String displayName, boolean isRequired, boolean singular, boolean visible, boolean value,
					Setter<Boolean> setter) {
				// TODO Nigel
				Class<? extends TransferPluginOptionCallback> callback = null;
				// TODO Nigel
				Class<? extends TransferPluginOptionConverter> converter = null;
				TransferPluginOption option = new ScalarTransferPluginOption(id, displayName, null, false, false, singular, visible, isRequired, callback, converter, "todo") {
				
					@Override
					public void setStringValue(String stringValue) {
						boolean booleanValue = Boolean.getBoolean(stringValue);
						this.stringValue = stringValue;
						setter.setValue(booleanValue);
					}

					@Override
					public boolean validateInputType(String value) {
						return true;
					}
				};
				options.add(option);
			}

			@Override
			public void fileProperty(String id, String displayName, boolean isRequired, boolean singular, boolean visible, FileType fileType, File value,
					Setter<File> setter) {
				TransferPluginOption option = new ScalarTransferPluginOption(id, displayName, fileType, false, false, singular, visible, isRequired, null, null, "todo") {
				
					@Override
					public void setStringValue(String stringValue) {
						File fileValue = new File(stringValue);
						this.stringValue = stringValue;
						setter.setValue(fileValue);
					}

					@Override
					public boolean validateInputType(String value) {
						if (isRequired()) {
							if (value != null) {
								return true;
							}
							return false;
						}
						else {
							return true;
						}
					}
				};
				options.add(option);
			}

			@Override
			public <T extends Enum<T>> void enumProperty(String id, String displayName, boolean isRequired, T[] enumOptions, T value, Setter<T> setter) {
				TransferPluginOption option = new EnumTransferPluginOption<T>(id, displayName, null, false, false, false, true, isRequired, null, null, enumOptions, value, setter);
				options.add(option);
			}

			@Override
			public void nestedSettingsProperty(String id, String displayName, boolean isRequired,
					TransferSettingsSetter<?> setter) {
				TransferPluginOption option = createNestedOption(transferSettings, level, id, displayName, isRequired, setter);
				options.add(option);
			}
		});

		return options.build();
	}

//	private static TransferPluginOption getOptionFromField(Field field, Class<? extends TransferSettings> transferSettingsClass, int level) {
//		XmlElement elementAnnotation = field.getAnnotation(XmlElement.class);
//		Setup setupAnnotation = field.getAnnotation(Setup.class);
//
//		boolean hasName = !elementAnnotation.name().equalsIgnoreCase("");
//		boolean hasDescription = setupAnnotation != null && !setupAnnotation.description().equals("");
//		boolean hasCallback = setupAnnotation != null && !setupAnnotation.callback().isInterface();
//		boolean hasConverter = setupAnnotation != null && !setupAnnotation.converter().isInterface();
//		boolean hasFileType = setupAnnotation != null && setupAnnotation.fileType() != null;
//
//		String name = (hasName) ? elementAnnotation.name() : field.getName();
//		String description = (hasDescription) ? setupAnnotation.description() : field.getName();
//		FileType fileType = (hasFileType) ? setupAnnotation.fileType() : null;
//		boolean required = elementAnnotation.required();
//		boolean sensitive = setupAnnotation != null && setupAnnotation.sensitive();
//		boolean singular = setupAnnotation != null && setupAnnotation.singular();
//		boolean visible = setupAnnotation != null && setupAnnotation.visible();
//		boolean encrypted = field.getAnnotation(Encrypted.class) != null;
//		Class<? extends TransferPluginOptionCallback> callback = (hasCallback) ? setupAnnotation.callback() : null;
//		Class<? extends TransferPluginOptionConverter> converter = (hasConverter) ? setupAnnotation.converter() : null;
//
//		boolean isNestedOption = TransferSettings.class.isAssignableFrom(field.getType());
//
//		if (isNestedOption) {
//			return createNestedOption(field, level, name, description, fileType, encrypted, sensitive, singular, visible, required, callback, converter);
//		}
//		else {
//			return createNormalOption(field, transferSettingsClass, name, description, fileType, encrypted, sensitive, singular, visible, required, callback, converter);
//		}
//	}

	private static <T extends TransferSettings> TransferPluginOption createNestedOption(TransferSettings transferSettings, int level, String id, String displayName,
			boolean required, TransferSettingsSetter<T> setter) {

		if (++level > MAX_NESTED_LEVELS) {
			throw new RuntimeException("Plugin uses too many nested transfer settings (max allowed value: " + MAX_NESTED_LEVELS + ")");
		}

		Class<? extends TransferPluginOptionCallback> callback = null;
		
		return new NestedTransferPluginOption<T>(id, displayName, required, setter, callback, level);
	}
}
