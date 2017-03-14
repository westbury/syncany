package org.syncany.api.transfer;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.syncany.api.FileType;


public interface PropertyVisitor {

	void stringProperty(String id, String displayName, boolean isRequired, boolean storeEncrypted, boolean sensitive, boolean singular,
			boolean visible, Supplier<String> value, Consumer<String> setter);

	void integerProperty(String id, String displayName, boolean isRequired, boolean storeEncrypted, boolean sensitive, boolean singular, boolean visible, Supplier<Integer> value, Consumer<Integer> setter);

	void booleanProperty(String id, String displayName, boolean isRequired, boolean singular, boolean visible, boolean value, Setter<Boolean> setter);

	void fileProperty(String id, String displayName, boolean isRequired, boolean singular, boolean visible, FileType folder, File value, Setter<File> setter);

	<T extends Enum<T>> void enumProperty(String id, String displayName, boolean isRequired, T[] options, T value, Setter<T> setter);

	void nestedSettingsProperty(String id, String displayName, boolean isRequired, TransferSettingsSetter<?> setter);

}
