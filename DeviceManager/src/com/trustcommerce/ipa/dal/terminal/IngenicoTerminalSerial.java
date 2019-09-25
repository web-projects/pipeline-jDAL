package com.trustcommerce.ipa.dal.terminal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.api.jpos.IngenicoMSR;
import com.ingenico.jpos.IngenicoConst;
import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.device.LocalMsr;
import com.trustcommerce.ipa.dal.device.TerminalEventListener;
import com.trustcommerce.ipa.dal.device.observers.DeviceHealthDIOListener;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;

import jpos.JposConst;
import jpos.JposException;
import jpos.MSR;
import jpos.MSRConst;
import jpos.events.DirectIOListener;
import jpos.events.ErrorEvent;
import jpos.events.ErrorListener;


public class IngenicoTerminalSerial implements ErrorListener {
	/** slf4j. */
	private static final Logger LOGGER = LoggerFactory.getLogger(IngenicoTerminalSerial.class);

	private static final int TIMEOUT = 30000;

 
	// Keep last listener set
	private DirectIOListener lastIOListener;


	private TerminalEventListener deviceEventListener;
  

	private TerminalEvent ingenicoDeviceEvent;

 	
	private String serialNumber;
	
	/** Magnetic Stripe Reader. */
	private IngenicoMSR msr;

	/**
	 * 
	 * @param transactionMsr type MyMsr the MSR received here has to be initialized
	 * @throws TerminalInfoException
	 */
	public IngenicoTerminalSerial(final LocalMsr transactionMsr) {
		LOGGER.debug("-> Constructor()");
		msr = transactionMsr.getMsr();
	}

	
 
	public void requesSerialNumberValue() {
		LOGGER.info("-> requesSerialNumberValue()");
		ingenicoDeviceEvent = TerminalEvent.DEVICE;
		lastIOListener = new DeviceHealthDIOListener(this);
		try {
			msr.addDirectIOListener(lastIOListener);
			msr.setDeviceEnabled(true);
			Object object = "TERM_SERIAL#,MANUF_DATE,MODEL";
			msr.directIO(IngenicoConst.JPOS_FC_HEALTH_STATS, null, object);
		} catch (JposException e) {
			msr.removeDirectIOListener(lastIOListener);
			lastIOListener = null;

			LOGGER.warn("Error getting Serial Number: " + e.getMessage() + " Code: " + e.getErrorCode());
			notifyCallerOfStateChange(DeviceState.HEALTH_STATUS_ERROR, null);
		}

		LOGGER.trace("<- requesSerialNumberValue()");
	}
	
	
	@Override
	public final void errorOccurred(final ErrorEvent ee) {
		LOGGER.error("-> errorOccurred() ");
		LOGGER.info("ErrorCode: {}", ee.getErrorCode()); // 111=JPOS_E_FAILURE
		LOGGER.info("ErrorCodeExtended: {}, ", ee.getErrorCodeExtended()); // 0
		LOGGER.info("ErrorResponse: {}", ee.getErrorResponse()); // 12=JPOS_ER_CLEAR

		DeviceState state = DeviceState.UNKNOWN;
		final Object source = ee.getSource();

		try {
			if (source instanceof MSR) {
				final MSR oMSR = (MSR) ee.getSource();
				if (oMSR.getErrorReportingType() == MSRConst.MSR_ERT_CARD) {
					// Also here when the user cancels the "Enter Card Number"
					// form in the device ?
					LOGGER.info("ERROR REPORTING BY CARD");
				}
			}

			if (ee.getErrorCode() == JposConst.JPOS_SUCCESS) {
				LOGGER.debug("Received success code: 0");
				
			} else if (ee.getErrorCode() == JposConst.JPOS_E_OFFLINE) {
				// 108, ErrorCodeExtended: 11, ErrorResponse: 12
				LOGGER.debug("Received JPOS_E_OFFLINE: Device disconnected");
				notifyCallerOfStateChange(DeviceState.DEVICE_NOT_CONNECTED, null);
				
			} else if (ee.getErrorLocus() == JposConst.JPOS_EL_INPUT
			        || ee.getErrorLocus() == JposConst.JPOS_EL_INPUT_DATA) {
				LOGGER.warn("Error encountered reading input data");

				// 1 second sleep to give the driver time to get its bearings
				Thread.sleep(GlobalConstants.MESSAGE_WARN_SLEEP_TIMER);
				if (source.equals(msr)) {
					if (!msr.getClaimed()) {
						msr.claim(TIMEOUT);
					}

					msr.clearInput();
					msr.setDeviceEnabled(true);
	

				} else {
					LOGGER.warn("Input error source unknown");
				}
			} else {
				state = DeviceState.ERROR;
			}
		} catch (JposException e) {
			LOGGER.error("JposException: Failed to clear input for swipe retry");
			state = DeviceState.ERROR;
		} catch (InterruptedException e) {
			LOGGER.error("JposException: Post-input error sleep interrupted");
			state = DeviceState.ERROR;
		}
		LOGGER.debug("<- errorOccurred()" + state);
	}


	public void setEventListener(final TerminalEventListener newListener) {
		deviceEventListener = newListener;
	}

	/**
	 * Helper class to send state message to DeviceEventListener instance old
	 * name: generateState
	 * 
	 * @param state
	 *            State to send to listener
	 */
	public final void notifyCallerOfStateChange(final DeviceState state, String value) {
		if (state == DeviceState.UNKNOWN) {
			// Nothing to notify
			return;
		}
		if (deviceEventListener != null) {
			// IOUtils.sendMessageThroughSocket(temp2);
			deviceEventListener.handleEvent(new LocalActionEvent(this, state, TerminalEvent.TERMINAL_INFO), value);
		}
	}


	public final String getSerialNumber() {
		return serialNumber;
	}
	
	 
	public final void setSerialNumber(String value) {
		serialNumber = value;
		notifyCallerOfStateChange(DeviceState.GOT_SERIAL, serialNumber);
		msr.removeDirectIOListener(lastIOListener);
	}

}
