package com.trustcommerce.ipa.dal.gui.interfaces;

import java.util.EventListener;


/**
 * This is a listener to communicate the Transaction ingenico bridge that changes have occurred.
 * @author luisa.lamore
 *
 */
public abstract interface BridgeEventListener extends EventListener {
	
	
	
	/**
	 * clicked on manual button.
	 */
	void goToManual();
	/**
	 * clicked on swipe button.
	 */
	
	void goToSwipe();
	/**
	 * closed jdal from taskbar.
	 */
	
	void windowClosed();
	
	void okPartialClicked();
	
	void voidPartialClicked();
	/**
	 * after completing the card holder information section, need to go to verify amount.
	 */
	
	void cardHolderCompleted();
}
