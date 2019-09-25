package com.trustcommerce.ipa.dal.bridge.observers.emv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.bridge.PaymentBridge;
import com.trustcommerce.ipa.dal.bridge.socket.SocketUtil;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;

/**
 *	Listener for card removal after EMV failure.
 *
 */

public class EMVRemoveForRetry implements DeviceEventListener {
	/**
	 * log4j logger.
	 */
    private static final Logger LOGGER = LoggerFactory.getLogger(EMVRemoveForRetry.class);
    
    /** Callback class.*/
    private PaymentBridge deviceBridge;
    /**
     * constructor.
     * @param bridge PaymentBridge
     */

    public EMVRemoveForRetry(final PaymentBridge bridge) {

        this.deviceBridge = bridge;

    }

    @Override
	public final void handleEvent(final LocalActionEvent deviceEvent) {

        LOGGER.debug("Value of device event: {}", deviceEvent.event.toString());
        if (deviceEvent.event != TerminalEvent.EMV) {
            LOGGER.debug("Ignoring event that's not an EMV event");
            return;
        }

        LOGGER.debug("Value of state: {}", deviceEvent.state.toString());
        switch (deviceEvent.state) {
        case EMV_CARD_REMOVED:
            LOGGER.debug("Remove signal received. Re-start MSR transaction");
            deviceBridge.swipeOrInsertCardStep();
            break;
        case EMV_TRANS_CANCELED:
            LOGGER.debug("Transaction Cancelled");
            SocketUtil.sendErrorMessageToCaller(EntryModeStatusID.Timeout);
			deviceBridge.deviceCleanup();
            break;
        default:
            LOGGER.debug("Unexpected state");
            break;
        }
    }
}
