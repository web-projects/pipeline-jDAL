package com.trustcommerce.ipa.dal.bridge.observers.emv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.bridge.PaymentBridge;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;

////
// End EMV step 3
////
/** Listenerthat process the payment request and sends ARQC.
 *
 */
public class EMVFirstACListener implements DeviceEventListener {
	
	/** Log4j Logger.*/
    private static final Logger LOGGER = LoggerFactory.getLogger(EMVFirstACListener.class);
    /** Callback class. */
    private PaymentBridge deviceBridge;
    
    /**
     * Constructor.
     * @param bridge PaymentBridge
     */
    public EMVFirstACListener(final PaymentBridge bridge) {
        this.deviceBridge = bridge;
    }

    @Override
	public final void handleEvent(final LocalActionEvent deviceEvent) {
    	/* when we hit cancel on the pin form for an online pin transaction- only pin form is getting cancelled.
    	 * But for non- online pin (credit ) if we cancel the pin form the transaction is getting cancelled.
    	 */
    	
        LOGGER.debug("EMVFirstACListener -> handleEvent() {}", deviceEvent);

        if (deviceEvent.event != TerminalEvent.EMV) {
            LOGGER.debug("Ignoring non-EMV event");
            return;
        }

        switch (deviceEvent.state) {
        case EMV_ONLINE_PIN:
            LOGGER.info("Received Online PIN signal");
            deviceBridge.processEMVOnlinePin();
            break;

        case EMV_ARQC:
            LOGGER.debug("Received ARQC signal");

            deviceBridge.processFirstApplicationCryptogram();
            break;

        case EMV_TC:
        case EMV_AAC:
            LOGGER.debug("Received state {} ", deviceEvent.state.toString());
            deviceBridge.processOfflineStatusResponse(deviceEvent.state);
            break;
        default:
            deviceBridge.baseEMVStateHandler(deviceEvent.state);
            break;
        }
    }
}