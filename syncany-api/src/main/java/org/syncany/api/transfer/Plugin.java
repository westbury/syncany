package org.syncany.api.transfer;

public interface Plugin {

	/**
	 * Returns a unique plugin identifier.
	 * 
	 * <p>This identifier must be globally unique.  Uniqueness is typically achieved by
	 * starting the id with a reverse DNS name. 
	 */
	String getId();

	/**
	 * Returns a short name of the plugin
	 */
	String getName();

	/**
	 * Returns the version of the plugin
	 */
	String getVersion();
	
}
