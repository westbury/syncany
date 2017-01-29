package org.syncany.api.transfer;

public interface Setter<T> {

	String validateValue(T value);
	
	void setValue(T value);
}
