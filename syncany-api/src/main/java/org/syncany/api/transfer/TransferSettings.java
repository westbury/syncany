package org.syncany.api.transfer;

public interface TransferSettings {

	void visitProperties(PropertyVisitor visitor);
	
	TransferManager createTransferManager();

}
