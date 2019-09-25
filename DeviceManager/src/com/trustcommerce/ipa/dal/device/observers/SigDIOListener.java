package com.trustcommerce.ipa.dal.device.observers;

import org.apache.log4j.Logger;

import com.ingenico.api.jpos.IngenicoSignatureCapture;
import com.ingenico.jpos.IngenicoConst;
import com.ingenico.jpos.UIAConst;
import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.device.IngenicoSignature;

import jpos.events.DirectIOEvent;
import jpos.events.DirectIOListener;

public class SigDIOListener implements DirectIOListener {

    private static final Logger logger = Logger.getLogger(SigDIOListener.class);
    private IngenicoSignatureCapture sigCap;
    private IngenicoSignature signature;

    public SigDIOListener(IngenicoSignature ingenicoSignature, IngenicoSignatureCapture sigCap) {

	this.sigCap = sigCap;
	this.signature = ingenicoSignature;
    }

    @Override
    public void directIOOccurred(final DirectIOEvent arg0) {
	if (arg0.getSource().equals(sigCap)) {
	    final int eventNumber = arg0.getEventNumber();
	    final int data = arg0.getData();

	    if (eventNumber == IngenicoConst.ING_DIO_RUN_FILE) {
		logger.trace("Data value: " + data);

		switch (data) {
		case IngenicoConst.ING_DIO_RESPONSE_SUCCESS:
		    logger.info("Signature form displayed");
		    break;
		case IngenicoConst.ING_DIO_RESPONSE_ERROR:
		    logger.error("Error encountered displaying sigcap form");
		    signature.notifyGuiOfStateChange(DeviceState.ERROR);
		    break;
		case UIAConst.UIA_SBC_CANCEL:
		    logger.info("Cancel button pressed");
		    signature.notifyGuiOfStateChange(DeviceState.CANCELLED_BY_USER);
		    break;
		case UIAConst.UIA_SBC_ENTER:
		    logger.debug("Enter button pressed");
		    signature.notifyGuiOfStateChange(DeviceState.DONE);
		    break;
		}
	    } else if (eventNumber == IngenicoConst.ING_DIO_SETVAR) {
		if (data != IngenicoConst.ING_DIO_RESPONSE_SUCCESS) {
		    logger.fatal("Failed to set variable to device");
		    signature.notifyGuiOfStateChange(DeviceState.ERROR);
		} else {
			logger.debug("Variable set successfully");
		}
	    } else {
			logger.warn("Unhandled direct IO event: " + Integer.toString(eventNumber) + " data: "
				+ Integer.toString(data));
		}
	}
    }
}
