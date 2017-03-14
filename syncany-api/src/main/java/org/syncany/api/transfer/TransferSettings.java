package org.syncany.api.transfer;

public interface TransferSettings {

	void visitProperties(PropertyVisitor visitor);
	
	/**
	 * @param cache a place where temporary files can be created
	 * @throws StorageException 
	 */
	TransferManager createTransferManager(LocalDiskCache cache) throws StorageException;

	String getType();

	boolean isValid();

	String getReasonForLastValidationFail();

}
