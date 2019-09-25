package com.trustcommerce.ipa.dal.listeners;

import java.util.EventListener;

import com.trustcommerce.ipa.dal.model.LocalActionEvent;


/**
 * 
 * @author tc
 *
 */
public abstract interface DeviceEventListener extends EventListener {
	
	/** handle events for the Payment bridge.*/
	void handleEvent(LocalActionEvent deviceEvent);
}
