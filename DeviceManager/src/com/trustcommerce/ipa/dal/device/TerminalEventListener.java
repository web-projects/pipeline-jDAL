package com.trustcommerce.ipa.dal.device;

import java.util.EventListener;

import com.trustcommerce.ipa.dal.model.LocalActionEvent;


/**
 * 
 * @author luisa.lamore
 *
 */
public interface TerminalEventListener extends EventListener {
	

	/**
	 * handle events with objects.
	 * @param deviceEvent LocalActionEvent
	 * @param object Object
	 */
	void handleEvent(LocalActionEvent deviceEvent, Object object);
}
