package com.trustcommerce.ipa.dal.model;

import java.util.EventObject;

import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;


/**
 * Events thrown by the IngenicoDevice class after receiving events from the
 * device.
 * 
 * old DeviceEventObject
 * @author tc
 *
 */
public class LocalActionEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -461246308127485491L;
	
	
	public DeviceState state;
	public TerminalEvent event;
	/**
	 * 
	 * @param source Object
	 * @param state DeviceState
	 * @param event TerminalEvent
	 */
	public LocalActionEvent(final Object source, final DeviceState state, final TerminalEvent event) {
		super(source);
		this.state = state;
		this.event = event;
	}

    @Override
	public final String toString() {
        return "LocalActionEvent [state=" + state + ", event=" + event + "]";
    }
}
