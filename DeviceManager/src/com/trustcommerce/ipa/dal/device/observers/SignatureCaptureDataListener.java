package com.trustcommerce.ipa.dal.device.observers;

import java.awt.Point;

import org.apache.log4j.Logger;

import com.ingenico.api.jpos.IngenicoSignatureCapture;
import com.ingenico.jpos.UIAConst;
import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.constants.forms.FormTypes;
import com.trustcommerce.ipa.dal.device.IngenicoSignature;

import jpos.JposException;
import jpos.events.DataEvent;
import jpos.events.DataListener;

public class SignatureCaptureDataListener implements DataListener {

    private static final Logger logger = Logger.getLogger(SignatureCaptureDataListener.class);
    private IngenicoSignature ingenicoSignature;
    private IngenicoSignatureCapture sigCap;
    // Signature capture
    private Point[] sigPoints;

    public SignatureCaptureDataListener(final IngenicoSignature ingenicoSignature, IngenicoSignatureCapture sigCap) {
	this.ingenicoSignature = ingenicoSignature;
	this.sigCap = sigCap;
    }
    
    
    @Override
    public void dataOccurred(final DataEvent event) {
	logger.trace("Got sigcap data event");
	if (!event.getSource().equals(sigCap)) {
	    logger.warn("SigCapDataListener ignoring event");
	    return;
	}

	final int status = event.getStatus();
	logger.trace("Value of status: " + status);

	if (status == UIAConst.UIA_SBC_CANCEL) {
	    logger.debug("Cancel button pressed on device");
	    ingenicoSignature.notifyGuiOfStateChange(DeviceState.CANCELLED_BY_USER);

	} else if (status == UIAConst.UIA_SBC_ENTER) {
	    try {
		logger.debug("Getting points");

		sigPoints = sigCap.getPointArray();
		logger.trace("Number of points: " + sigPoints.length);
		if (sigPoints.length == 0) {
		    logger.debug("No signature points found");
		    sigCap.setDataEventEnabled(true);

		    sigCap.beginCapture(FormTypes.SIGNATURE.getName());
		} else {
		    logger.debug("Received signature from device");
		    ingenicoSignature.notifyGuiOfStateChange(DeviceState.DONE);
		}
	    } catch (JposException e) {
		logger.warn("Failed to get points. Code: " + e.getErrorCode() + " Message: " + e.getMessage());
		return;
	    }
	}
    }
}
