package com.trustcommerce.ipa.dal.bridge.observers.emv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.bridge.PaymentBridge;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;
/**
 * Listener for Pin entry.
 *
 */
public class EMVPinListener implements DeviceEventListener {
	/**
	 * log4j logger.
	 */
   private static final Logger LOGGER = LoggerFactory.getLogger(EMVPinListener.class);
   /** Callback class.*/
    private PaymentBridge deviceBridge;

    /**
     * Constructor.
     * @param bridge PaymentBridge
     */
    public EMVPinListener(final PaymentBridge bridge) {

        this.deviceBridge = bridge;

    }

    @Override
	public final void handleEvent(final LocalActionEvent deviceEvent) {
        LOGGER.trace("EMVPinListener::handleEvent");

        if (deviceEvent.event != TerminalEvent.PIN) {
            LOGGER.debug("Ignoring event {} ", deviceEvent.event.toString());
            return;
        }

        LOGGER.debug("Got pin entry event");
        deviceBridge.pinEMVStateHandler(deviceEvent.state);
//        switch (deviceEvent.state) {
//        case DONE:
//            ////
//            // Customer provided pin number
//            ////
//            deviceBridge.handleStateDone();
//            break;
//        case ERROR:
//            deviceBridge.handleStateError();
//            break;
//        case INPUT_ERROR:
//            deviceBridge.handleStateInputError();
//            break;
//            
//        case REJECTED:
//            logger.debug("Pin entry cancelled; contiue with PIN bypass");
//            deviceBridge.handleStateRejected();
//            break;
//            
//        case TIMEOUT:
//            deviceBridge.handleStateInputError();
//            break;
//
//        default:
//            logger.warn("Got the following state during pin entry: " + deviceEvent.state.toString());
//            break;
//        }
    }
}