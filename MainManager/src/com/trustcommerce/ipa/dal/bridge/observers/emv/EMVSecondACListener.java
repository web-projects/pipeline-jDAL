package com.trustcommerce.ipa.dal.bridge.observers.emv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.bridge.PaymentBridge;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;

/**
 * End EMV step 5 This listener is activated after the application has received
 * the ARPC from the client bridge.
 * 
 */
public class EMVSecondACListener implements DeviceEventListener {
	
	/** Log4j. */
	private static final Logger LOGGER = LoggerFactory.getLogger(EMVSecondACListener.class);
	
	/** callback class. */
	private PaymentBridge deviceBridge;

	/**
	 * Constructor.
	 * @param bridge type PaymentBridge
	 */
	public EMVSecondACListener(final PaymentBridge bridge) {
		this.deviceBridge = bridge;
	}

 
	@Override
	public final void handleEvent(final LocalActionEvent deviceEvent) {
		LOGGER.debug("*** EMVSecondACListener  *** -> handleEvent(): {}", deviceEvent.state.toString());
		if (deviceEvent.event != TerminalEvent.EMV) {
			LOGGER.debug("Ignoring non-EMV event");
			return;
		}

		switch (deviceEvent.state) {
		case EMV_TC:
		case EMV_AAC:
			deviceBridge.handleTcAndAcc(deviceEvent.state);
			break;
			
		case EMV_INVALID_CARD_DATA:
			deviceBridge.handleEmvInvalidCardData();
			break;
			
		default:
			deviceBridge.baseEMVStateHandler(deviceEvent.state);
			break;
		}
	}
}