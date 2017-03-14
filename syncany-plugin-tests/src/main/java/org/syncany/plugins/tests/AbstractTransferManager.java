/*
 * Syncany, www.syncany.org
 * Copyright (C) 2011-2016 Philipp C. Heckel <philipp.heckel@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.syncany.plugins.tests;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.syncany.api.transfer.StorageException;
import org.syncany.api.transfer.StorageTestResult;
import org.syncany.api.transfer.TransferManager;
import org.syncany.api.transfer.features.Retriable;
import org.syncany.api.transfer.features.TransactionAware;

/**
 * Implements basic functionality of a {@link TransferManager} which
 * can be implemented sub-classes.
 * 
 * <p>This transfer manager is enhanced with the {@link TransactionAware}
 * and {@link Retriable} annotations, thereby making it reliable.
 *
 * @author Philipp C. Heckel <philipp.heckel@gmail.com>
 * @author Christian Roth <christian.roth@port17.de>
 */
// TODO Nigel this has been transformed to a wrapper class that adds a class to
// do the tests.  Probably
// needs to be removed altogether.
// These two annotations moved to each transfer manager:
//@TransactionAware
//@Retriable(numberRetries = 3, sleepInterval = 3000)
public class AbstractTransferManager { // TODO [medium] Rename this to AbstractReliableTransferManager
	private static final Logger logger = Logger.getLogger(AbstractTransferManager.class.getSimpleName());

	protected TransferManager manager;
//	protected ICache cache;

	public AbstractTransferManager(TransferManager manager) {
		this.manager = manager;
	}

	/**
	 * Checks whether the settings given to this transfer manager can be
	 * used to create or connect to a remote repository.
	 *
	 * <p>Tests if the target exists, if it can be written to and if a
	 * repository can be created.
	 */
	public StorageTestResult test(boolean testCreateTarget) {
		logger.log(Level.INFO, "Performing storage test TM.test() ...");
		StorageTestResult result = new StorageTestResult();

		try {
			logger.log(Level.INFO, "- Running connect() ...");
			manager.connect();

			result.setTargetExists(manager.testTargetExists());
			result.setTargetCanWrite(manager.testTargetCanWrite());
			result.setRepoFileExists(manager.testRepoFileExists(new SyncanyRemoteFile()));

			if (result.isTargetExists()) {
				result.setTargetCanCreate(true);
			}
			else {
				if (testCreateTarget) {
					result.setTargetCanCreate(manager.testTargetCanCreate());
				}
				else {
					result.setTargetCanCreate(false);
				}
			}

			result.setTargetCanConnect(true);
		}
		catch (StorageException e) {
			result.setTargetCanConnect(false);
			result.setErrorMessage(getStackTrace(e));

			logger.log(Level.INFO, "-> Testing storage failed. Returning " + result, e);
		}
		finally {
			try {
				manager.disconnect();
			}
			catch (StorageException e) {
				logger.log(Level.FINE, "Could not disconnect", e);
			}
		}

		return result;
	}

	private static String getStackTrace(Exception exception) {
    	StringWriter stackTraceStringWriter = new StringWriter();
    	exception.printStackTrace(new PrintWriter(stackTraceStringWriter));
    	
    	return stackTraceStringWriter.toString();
    }
	
}
