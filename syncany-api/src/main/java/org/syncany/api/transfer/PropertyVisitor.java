package org.syncany.api.transfer;

import java.io.File;

public interface PropertyVisitor {

	void stringProperty(String id, String displayName, boolean isRequired, boolean storeEncrypted, String value, Setter<String> setter);

	void integerProperty(String id, String displayName, boolean isRequired, boolean storeEncrypted, int value, Setter<Integer> setter);

	void directoryProperty(String id, String displayName, boolean isRequired, boolean storeEncrypted, File value, Setter<File> setter);
}
