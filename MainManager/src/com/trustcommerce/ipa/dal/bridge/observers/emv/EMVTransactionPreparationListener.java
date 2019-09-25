package com.trustcommerce.ipa.dal.bridge.observers.emv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.bridge.PaymentBridge;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;


/**
* End EMV Step 1.
*/
public class EMVTransactionPreparationListener implements DeviceEventListener {
	/**
	 * log4j logger.
	 */
   private static  final Logger LOGGER = LoggerFactory.getLogger(EMVTransactionPreparationListener.class);
   /**Callback class.*/

    private PaymentBridge deviceBridge;

    /**
     * constructor.
     * @param bridge PaymentBridge
     */
    public EMVTransactionPreparationListener(final PaymentBridge bridge) {

        this.deviceBridge = bridge;

    }

    @Override
	public final void handleEvent(final LocalActionEvent deviceEvent) {
        LOGGER.debug("-> handleEvent() Value of device event: {}", deviceEvent.event.name());

        if (deviceEvent.event != TerminalEvent.EMV) {
            LOGGER.debug("Ignoring event that's not an EMV event");
            return;
        }
        LOGGER.debug("Value of state: {}", deviceEvent.state.name());
        
        switch (deviceEvent.state) {
            case EMV_TRANS_PREP_READY:
                ////
                // Inititialize EMV step 2
                ////
                deviceBridge.handleEmvTransactionPreparationReady();
                break;
                
            case ERROR:
            	LOGGER.error("Error Event !!!");
            	deviceBridge.baseEMVStateHandler(deviceEvent.state);
                break;
                
            default:
                deviceBridge.baseEMVStateHandler(deviceEvent.state);
                break;
            }
    }
}