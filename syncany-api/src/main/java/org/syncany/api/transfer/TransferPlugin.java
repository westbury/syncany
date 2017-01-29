package org.syncany.api.transfer;

import java.util.List;

import org.w3c.dom.Element;


public interface TransferPlugin extends Plugin {

	/**
	 * I'm not sure whether to keep this method.  Currently plugin and config
	 * dependencies were split by passing a list of Element objects in the API.
	 * However settings can be configured from config.xml by setting each
	 * field individually.
	 * 
	 * @param settings
	 * @return
	 */
	TransferSettings createTransferSettings(List<Element> settings);
	
	/**
	 * Create transfer settings object.  None of the fields will be
	 * set initially.  One must visit and set each field value.
	 * 
	 * @return
	 */
	TransferSettings createTransferSettings();
		
}
