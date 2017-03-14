package org.syncany.api.transfer;

/**
 * 
 * 
 * 
 * 
 * @author Nigel Westbury
 *
 * @param <T> the base class (usually an interface) for the types
	 * of settings that this may take
 */
public interface TransferSettingsSetter<T extends TransferSettings> {

	/**
	 * Gets the base class (usually an interface) for the types
	 * of settings that this may take.  
	 */
	Class<T> getSettingsClass();
	
	T getValue();
	
	void setValue(T value);
}
