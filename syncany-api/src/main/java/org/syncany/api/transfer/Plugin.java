package org.syncany.api.transfer;


/**
 * A plugin can be used to extend Syncany.  For example a plugin may extend the transfer
 * extension point to support the storage of Syncany's repository files on a particular remote location. 
 * Implementations of the <tt>Plugin</tt> class identify a plugin.
 * 
 * <p>Using the 'id' attribute, plugins can be loaded by the {@link Plugins} class. 
 * Once a plugin is loaded, an instance of the plugin's main class can be obtained.
 * From that instance, which implements one of the extension point interfaces, the various
 * features implemented by the plugin are available to Syncany.
 * 
 * @author Philipp C. Heckel <philipp.heckel@gmail.com>
 */
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
