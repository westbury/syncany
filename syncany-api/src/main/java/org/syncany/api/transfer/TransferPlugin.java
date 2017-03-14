package org.syncany.api.transfer;

import java.util.List;

import org.w3c.dom.Element;

/**
 * A transfer plugin can be used to store Syncany's repository files on any remote location. 
 * Implementations of the <tt>TransferPlugin</tt> interfaces identify a storage/transfer plugin.
 * 
 * The transfer plugin is responsible for transferring files
 * to the remote storage. Implementations must provide implementations for
 * {@link TransferPlugin} (this class), {@link TransferSettings} (connection
 * details) and {@link TransferManager} (transfer methods).<br/><br/>
 *
 * @author Philipp C. Heckel <philipp.heckel@gmail.com>
 * @author Christian Roth <christian.roth@port17.de>
 */
public interface TransferPlugin extends Plugin {

	/**
	 * Creates an empty plugin-specific {@link TransferSettings} instance.
	 * 
	 * Create transfer settings object.  None of the fields will be
	 * set initially.  One must visit and set each field value.
	 * 
	 * @return
	 */
	TransferSettings createEmptySettings();
		
}
