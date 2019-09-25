package com.trustcommerce.ipa.dal.bridge.observers.emv;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.bridge.PaymentBridge;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;
import com.trustcommerce.ipa.dal.emvconstants.TransactionAction;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;
/** 
 * Listener called when the card is removed after the transaction is complete.
 *
 */
public class EMVFinalRemoveListener implements DeviceEventListener {
	
	/** log4j logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(EMVFinalRemoveListener.class);
    // private String Tag8aDefaultValue;
    
    /** Callback class. */
    private PaymentBridge deviceBridge;
    
    /** Either "emv_refund_confirmation" or "emv_auth_confirmation". */
    private TransactionAction transactionType;

    /**
     * Constructor.
     * @param transactionType TransactionAction
     * @param bridge PaymentBridge
     */
    public EMVFinalRemoveListener(final TransactionAction transactionType, final PaymentBridge bridge) {
        this.deviceBridge = bridge;
        this.transactionType = transactionType;
        // Tag8aDefaultValue = "";
    }

 
    @Override
	public final void handleEvent(final LocalActionEvent deviceEvent) {
        LOGGER.debug("-> handleEvent() {}", deviceEvent);
        if (deviceEvent.event != TerminalEvent.EMV) {
            LOGGER.debug("Ignoring non-EMV event");
            return;
        }

        switch (deviceEvent.state) {
        case EMV_CARD_REMOVED: 
            LOGGER.info("Card removed, submit final EMV data");
            deviceBridge.processEmvCardRemoved(transactionType);
            break;
            
        default:
            LOGGER.warn("Unexpected event during final removal phase:{} ", deviceEvent.state.toString());
            break;
        }
        LOGGER.debug("<- handleEvent()");
    }

}