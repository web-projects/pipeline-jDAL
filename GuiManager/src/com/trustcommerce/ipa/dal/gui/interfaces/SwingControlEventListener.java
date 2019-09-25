package com.trustcommerce.ipa.dal.gui.interfaces;

import java.util.EventListener;
/**
 * Manual transaction userPanel eventlistener.
 * @author ayswarya.prashant
 *
 */
public abstract interface SwingControlEventListener extends EventListener {
	
	/**
	 * Event thrown when a transaction is complete.
	 * This eventis also thrown when an error has occurred and the current transaction has to
	 * be terminated.
	 */
	void userInfomationCompleted();
	/**
	 * Event thrown when a transaction is timeout on userpanel.
	 * 
	 */

	void userInfomationTimeout();
	

}
