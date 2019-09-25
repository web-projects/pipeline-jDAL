package com.trustcommerce.ipa.dal.device;

import java.awt.Point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.api.jpos.IngenicoSignatureCapture;
import com.ingenico.jpos.IngenicoConst;
import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;
import com.trustcommerce.ipa.dal.constants.device.TerminalModel;
import com.trustcommerce.ipa.dal.constants.forms.FormTypes;
import com.trustcommerce.ipa.dal.device.interfaces.SignatureProcessor;
import com.trustcommerce.ipa.dal.device.observers.SigDIOListener;
import com.trustcommerce.ipa.dal.device.observers.SignatureCaptureDataListener;
import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;

import jpos.JposConst;
import jpos.JposException;
import jpos.MSR;
import jpos.MSRConst;
import jpos.events.ErrorEvent;
import jpos.events.ErrorListener;

/**
 * 
 * 
 */
public class IngenicoSignature implements SignatureProcessor, ErrorListener {

	/** slf4j. */
	private static final Logger LOGGER = LoggerFactory.getLogger(IngenicoSignature.class);


	private TerminalModel deviceName;

	private IngenicoSignatureCapture ingenicoSignature;

	private int deviceTimeout;

	// Data state information
	private DeviceEventListener deviceEventListener;
	private TerminalEvent ingenicoDeviceEvent;

	/**
	 * This constructor is use when this running as an application.
	 * 
	 * @param deviceName TerminalModel
	 * @param deviceTimeout
	 * @throws IngenicoDeviceException
	 */
	public IngenicoSignature(final TerminalModel deviceName, final int deviceTimeout) {
		LOGGER.debug("Constructor() deviceTimeout: " + deviceTimeout);
		this.deviceName = deviceName;
		this.deviceTimeout = deviceTimeout;
	}

	/**
	 * Open and claim the signature capture. we need to distinguish between a
	 * device not configured for sigcap and an actual error initializing the
	 * sigcap module. DeviceState.SIGCAP_NOT_ENABLED
	 * 
	 * @throws IngenicoDeviceException on jpos exceptions
	 */
	private void initSigCap() throws IngenicoDeviceException {
		LOGGER.info("-> initSigCap()");
		if (deviceName == TerminalModel.iPP350 || deviceName == TerminalModel.iUP250) {
			return;
		}
		ingenicoSignature = new IngenicoSignatureCapture();
		ingenicoSignature.addErrorListener(this);

		try {
			ingenicoSignature.open("SigCap_" + deviceName);
			ingenicoSignature.claim(deviceTimeout);
		} catch (JposException e) {
			throw new IngenicoDeviceException("Can not initialize Signature capture");
		}

		LOGGER.info("SigCap initialization complete");
	}


	/**
	 * Starts the process to capture the signature.
	 * @throws IngenicoDeviceException if jpos errors are encountered.
	 */
	public final void captureSignature() throws IngenicoDeviceException {
		LOGGER.debug("-> captureSignature()");
		initSigCap();
		ingenicoDeviceEvent = TerminalEvent.SIGNATURE;

		if (ingenicoSignature == null) {
			// This should not happened Skip this process
			LOGGER.error("INTERNAL ERROR: Signature Captured is null");
			throw new IngenicoDeviceException(" Signature Captured can not be initialized, please handle this ...");
		}
		String buf = "VAR=";
		try {

			buf = buf + "Approved";
			ingenicoSignature.setDeviceEnabled(true);
			ingenicoSignature.addDirectIOListener(new SigDIOListener(this, ingenicoSignature));
			ingenicoSignature.directIO(IngenicoConst.ING_DIO_SETVAR, new int[] { 1 }, buf.getBytes());

			LOGGER.debug("Enabling signature capture listeners");
			ingenicoSignature.addDataListener(new SignatureCaptureDataListener(this, ingenicoSignature));
			ingenicoSignature.setDataEventEnabled(true);
			ingenicoSignature.beginCapture(FormTypes.SIGNATURE.getName());

		} catch (JposException e) {
			LOGGER.error(
			        "Error initializing signature capture. Code: " + e.getErrorCode() + " Message: " + e.getMessage());
			notifyGuiOfStateChange(DeviceState.ERROR);
		}
		LOGGER.debug("<- captureSignature()");
	}

	@Override
	public final void errorOccurred(final ErrorEvent ee) {
		LOGGER.warn("-> errorOccurred() ");
		DeviceState state = DeviceState.UNKNOWN;
		final Object source = ee.getSource();

		try {
			if (source instanceof MSR) {
				final MSR oMSR = (MSR) ee.getSource();
				if (oMSR.getErrorReportingType() == MSRConst.MSR_ERT_CARD) {
					LOGGER.info("ERROR REPORTING BY CARD");
					LOGGER.info("ErrorCode:" + ee.getErrorCode()); // 111=JPOS_E_FAILURE
					LOGGER.info("ErrorCodeExtended: " + ee.getErrorCodeExtended()); // 0
					LOGGER.info("ErrorResponse: " + ee.getErrorResponse()); // 12=JPOS_ER_CLEAR
				}
			}

			if (ee.getErrorCode() == JposConst.JPOS_SUCCESS) {
				LOGGER.debug("Received success code: 0");

			} else if (ee.getErrorLocus() == JposConst.JPOS_EL_INPUT
			        || ee.getErrorLocus() == JposConst.JPOS_EL_INPUT_DATA) {
				LOGGER.warn("Error encountered reading input data");
				state = DeviceState.INPUT_ERROR;

				// // 1 second sleep to give the driver time to get its bearings
				// Thread.sleep(1000);
				// if (source.equals(msr)) {
				// if (!msr.getClaimed())
				// msr.claim(deviceTimeout);
				//
				// msr.clearInput();
				// msr.setDeviceEnabled(true);
				// } else if (source.equals(pinPad)) {
				// logger.warn("Input error caused by pin pad");
				// } else
				// logger.warn("Input error source unknown");
			} else {
				state = DeviceState.ERROR;
			}
		} catch (JposException e) {
			LOGGER.error("JposException: Failed to clear input for swipe retry");
			state = DeviceState.ERROR;
		}
		notifyGuiOfStateChange(state);
	}

	/**
	 * Helper class to send state message to DeviceEventListener instance old.
	 * name: generateState
	 * 
	 * @param state
	 *            State to send to listener
	 */
	public final void notifyGuiOfStateChange(final DeviceState state) {
		if (state == DeviceState.UNKNOWN) {
			// Nothing to notify
			return;
		}
		if (deviceEventListener != null) {
			deviceEventListener.handleEvent(new LocalActionEvent(this, state, ingenicoDeviceEvent));
		}
	}

	
	/**
	 * Closes the IngenicoSignatureCaptured instance.
	 */
	public final void releaseDevice() {
		LOGGER.info("-> Releasing signature capture");
		if (ingenicoSignature != null) {
			try {
				ingenicoSignature.close();
				// A bug has been observed at this level when jDAL is close while the Signature pad form is displayed.
				LOGGER.debug("Signature capture is closed");
			} catch (JposException e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
			}
			ingenicoSignature = null;
		}
	}

	/**
	 * returns the array of points in the coordinate x,y. This value is use to
	 * draw the signature in the frame;
	 */
	@Override
	public final Point[] getSigPoints() {
		try {
			final Point[] points = ingenicoSignature.getPointArray();
			return points;
		} catch (JposException e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public final void setEventListener(final DeviceEventListener newListener) {
		deviceEventListener = newListener;
	}



}
