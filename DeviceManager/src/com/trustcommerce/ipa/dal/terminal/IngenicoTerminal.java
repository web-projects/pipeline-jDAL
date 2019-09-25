package com.trustcommerce.ipa.dal.terminal;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.api.jpos.IngenicoPINPad;
import com.ingenico.jpos.IngenicoConst;
import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;
import com.trustcommerce.ipa.dal.constants.device.TerminalModel;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.device.LocalMsr;
import com.trustcommerce.ipa.dal.device.forms.FormsPackage;
import com.trustcommerce.ipa.dal.device.interfaces.FormsPackageProcessor;
import com.trustcommerce.ipa.dal.device.interfaces.TerminalInfoProcessor;
import com.trustcommerce.ipa.dal.device.observers.DeviceHealthDIOListener;
import com.trustcommerce.ipa.dal.device.observers.DeviceVariablesDIOListener;
import com.trustcommerce.ipa.dal.device.types.DeviceConsts;
import com.trustcommerce.ipa.dal.emvconstants.EmvTagsConst;
import com.trustcommerce.ipa.dal.exceptions.FormsPackageUploadException;
import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;
import com.trustcommerce.ipa.dal.exceptions.TerminalInfoException;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;
import com.trustcommerce.ipa.dal.model.Terminal;
import com.trustcommerce.ipa.dal.model.health.DeviceHealth;

import jpos.JposConst;
import jpos.JposException;
import jpos.MSR;
import jpos.MSRConst;
import jpos.events.DirectIOListener;
import jpos.events.ErrorEvent;
import jpos.events.ErrorListener;

public class IngenicoTerminal implements TerminalInfoProcessor, ErrorListener {
	/** slf4j. */
	private static final Logger LOGGER = LoggerFactory.getLogger(IngenicoTerminal.class);
	
	private static final int CLAIM_TIMEOUT = 30000;

	private static final int TIMEOUT = 30000;

	private TerminalModel deviceName;

	// Keep last listener set
	private DirectIOListener lastIOListener;


	private DeviceEventListener deviceEventListener;
	private FormsPackageProcessor formsPackage;
 

	private TerminalEvent ingenicoDeviceEvent;

	private Terminal terminalInfo;
	
	private String serialNumber;
	
	private LocalMsr localMsr;

	/**
	 * 
	 * @param deviceModel type TerminalModel
	 * @throws TerminalInfoException
	 */
	public IngenicoTerminal(final TerminalModel deviceModel) throws TerminalInfoException {
		LOGGER.debug("-> Constructor {} ", deviceModel);
		deviceName = deviceModel;
		localMsr = new LocalMsr(deviceName);
		initialize(deviceModel);
	}

	
	/**
	 * 
	 * @param deviceModel type TerminalModel
	 */
	private void initialize(final TerminalModel deviceModel) {
		this.deviceName = deviceModel;
		terminalInfo = new Terminal();
		terminalInfo.setModelName(deviceModel);
	}

	public final TerminalModel getLocalMsrModel() {
		return localMsr.getLocalMsrModel();
	}
	/**
	 * @throws IngenicoDeviceException 
	 * 
	 */
	@Override
	public final void requestTerminalInformation() throws TerminalInfoException, IngenicoDeviceException {
		LOGGER.debug("-> requestTerminalInformation");

		ingenicoDeviceEvent = TerminalEvent.DEVICE;

		localMsr.initMsr(this);
		requestDeviceVariable(DeviceConsts.DFS_EMV_KEY, 4);
		requestDeviceVariable(DeviceConsts.EMV_KERNEL_VER, 4);
		requestEncryptionConfiguration();
 		requestFormVersion();
 		if(deviceName.name().equalsIgnoreCase("iPP350") || deviceName.name().equalsIgnoreCase("iPP320")) {
 			TerminalModel localMsrModel = localMsr.getLocalMsrModel();
 			if(localMsrModel != deviceName) {
 				deviceName = localMsrModel;
 				terminalInfo.setModelName(deviceName);
 			}
 		}
		requestDeviceHealth();

		notifyCallerOfStateChange(DeviceState.DONE);

		LOGGER.trace("<- requestHealthStatus()");
	}
	
	
	@Override
	public final void requestSerialNumber() throws TerminalInfoException, IngenicoDeviceException {
		LOGGER.debug("-> requestSerialNumber");

		ingenicoDeviceEvent = TerminalEvent.DEVICE;
		localMsr.initMsr(this);
		
		requesSerialNumberValue();
		notifyCallerOfStateChange(DeviceState.GOT_SERIAL);

		LOGGER.trace("<- requestSerialNumber()");
	}

	
	@Override
	public void requestEncryptionConfiguration() {
		requestDeviceVariable(DeviceConsts.DFS_ENCRYPTION_CONFIG, 4);
		requestKeyStatus();
	}
	
	public void isTrack1AndTrack2Encrypted() {
		requestDeviceVariable(DeviceConsts.DFS_TRACK12_ENCRYPTED, 4);
	}
	
	
	private void requesSerialNumberValue() {
		LOGGER.info("-> requesSerialNumberValue()");

		lastIOListener = new DeviceHealthDIOListener(this);
		try {
			localMsr.getMsr().addDirectIOListener(lastIOListener);
			localMsr.enableMSR();
			Object object = "TERM_SERIAL#,MANUF_DATE,MODEL";
			localMsr.getMsr().directIO(IngenicoConst.JPOS_FC_HEALTH_STATS, null, object);
		} catch (JposException e) {
			localMsr.getMsr().removeDirectIOListener(lastIOListener);
			lastIOListener = null;

			LOGGER.warn("Error getting health information: " + e.getMessage() + " Code: " + e.getErrorCode());
			notifyCallerOfStateChange(DeviceState.HEALTH_STATUS_ERROR);
		}

		LOGGER.trace("<- requesSerialNumberValue()");
	}
	
	
	/**
	 * Request from the device the file FORMSVER.TXT. FORMSVER.TXT contains the
	 * version of the form package.
	 */
	public final void requestFormVersion() {
		LOGGER.info("-> requestFormVersion()");
		ingenicoDeviceEvent = TerminalEvent.DEVICE;
		try {
			formsPackage = new FormsPackage(deviceName, localMsr.getMsr());
			formsPackage.requestFormVersion();
			try {
				Thread.sleep(GlobalConstants.MESSAGE_WARN_SLEEP_TIMER);
				terminalInfo.setFormsVersion(formsPackage.getFormsPackageVersion());
				terminalInfo.verifyLoadedFormVersion();
			} catch (InterruptedException e) {
			}
		} catch (FormsPackageUploadException e) {
			// not fatal at the moment. TODO
			LOGGER.warn("FormsPackageUploadException " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void setLoadedFormVersion(final String val, final DeviceState rsltState) {
		// deviceFormVersion = val;
		localMsr.getMsr().removeDirectIOListener(lastIOListener);
		lastIOListener = null;
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
				notifyCallerOfStateChange(DeviceState.DEVICE_NOT_CONNECTED);
				
			} else if (ee.getErrorLocus() == JposConst.JPOS_EL_INPUT
			        || ee.getErrorLocus() == JposConst.JPOS_EL_INPUT_DATA) {
				LOGGER.warn("Error encountered reading input data");

				// 1 second sleep to give the driver time to get its bearings
				Thread.sleep(GlobalConstants.MESSAGE_WARN_SLEEP_TIMER);
				if (source.equals(localMsr.getMsr())) {
					if (!localMsr.getMsr().getClaimed()) {
						localMsr.getMsr().claim(TIMEOUT);
					}

					localMsr.getMsr().clearInput();
					localMsr.enableMSR();
	

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

	private void requestDeviceHealth() {
		LOGGER.info("-> requestDeviceHealth()");

		lastIOListener = new DeviceHealthDIOListener(this);
		try {
			localMsr.getMsr().addDirectIOListener(lastIOListener);
			localMsr.enableMSR();
			localMsr.getMsr().directIO(IngenicoConst.JPOS_FC_HEALTH_STATS, null, null);
		} catch (JposException e) {
			localMsr.getMsr().removeDirectIOListener(lastIOListener);
			lastIOListener = null;

			LOGGER.warn("Error getting health information: " + e.getMessage() + " Code: " + e.getErrorCode());
			notifyCallerOfStateChange(DeviceState.HEALTH_STATUS_ERROR);
		}

		LOGGER.trace("<- requestDeviceHealth()");
	}

	/**
	 * This function only retrieves the EMV value associated with the emvKey. If
	 * additional variables are needed, review the code and add the appropriate
	 * code in the right location.
	 * @param key 
	 */
	private void requestDeviceVariable(final String key, int data) {
		LOGGER.debug("-> requestDeviceVariable()");

		DeviceVariablesDIOListener dObject = null;

		lastIOListener = dObject = new DeviceVariablesDIOListener(this);
		try {
			localMsr.getMsr().addDirectIOListener(lastIOListener);
			localMsr.enableMSR();
 			// dObject.keyName = emvKey;
			getDirectIOVariable(key, data);
			dObject.permits.acquire();

		} catch (java.lang.InterruptedException e) {
			// If this happens, we actually want to bail at this point.
			LOGGER.warn("Unable to acquire semaphore used for retrieving device variables, "
					+ "interrupted from getDeviceVariables() {}", e);
			LOGGER.trace("<- requestDeviceVariable()");
			return;
		}

		LOGGER.debug("getDeviceVariables() Acquire");
		try {
			dObject.permits.acquire();
		} catch (java.lang.InterruptedException e) {
			LOGGER.warn("Unable to acquire semaphore used for retrieving device variables.", e);
		}
		if (lastIOListener != null) {
			localMsr.getMsr().removeDirectIOListener(lastIOListener);
		}

		LOGGER.trace("<- getDeviceVariables()");
	}

	
	/**
	 * getVariable(String s).
	 * 
	 * @param variableName String
	 */
	private void getDirectIOVariable(final String variableName, int data) {
		ingenicoDeviceEvent = TerminalEvent.MSR;
		try {
			final int[] nData = new int[] { data };
			localMsr.getMsr().directIO(IngenicoConst.ING_DIO_GET_UIA_VARIABLE, nData, variableName);

		} catch (JposException e) {
			LOGGER.error("Failed to retrieve EMV information. Cause: {}  Code: Extended code: ", e.getMessage(),
			        e.getErrorCode(), e.getErrorCodeExtended());
			notifyCallerOfStateChange(DeviceState.ERROR);
		}
	}
	
	
	/**
	 * 0x3 key slot number, this value could be 0x0 - 0x09
	 * 0x00 Master/Session Type
	 * 0x01 DUKPT Type
	 * 0xA3 Data Validity Check Value (this value will change when the key slot number changes.
	 * This is an exclusive OR of the previous 5 bytes of the message)
	 * 
	 * 
	 */
	private void requestKeyStatus() {
		LOGGER.debug("-> getKeyStatus()");
		lastIOListener = new DeviceVariablesDIOListener(this);
		ingenicoDeviceEvent = TerminalEvent.MSR;
		try {
			localMsr.getMsr().addDirectIOListener(lastIOListener);
		
			final int[] nData = new int[] { 4 };
			localMsr.enableMSR();
			localMsr.getMsr().directIO(IngenicoConst.ING_DIO_GET_UIA_VARIABLE, nData, "KEYSTATUS");

		} catch (JposException e) {
			LOGGER.error("Failed to retrieve EMV information. Cause: {}  Code: Extended code: ", e.getMessage(),
			        e.getErrorCode(), e.getErrorCodeExtended());
			notifyCallerOfStateChange(DeviceState.ERROR);
		}
	}
	
	
	@Override
	public final void releaseDevice() {
		LOGGER.debug("-> releaseDevice()");
		if (localMsr == null) {
			LOGGER.debug("Local MSR is null");
			return;
		}
		try {
			localMsr.releaseAndCloseMsr(lastIOListener);
		} catch (Exception ie) {
			LOGGER.warn("An unkown Exception happened while releasing MSR: {}", ie.getMessage());
			if (ie instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}  
		}
		 
	}
	

	public void setEventListener(final DeviceEventListener newListener) {
		deviceEventListener = newListener;
	}

	/**
	 * Helper class to send state message to DeviceEventListener instance old
	 * name: generateState
	 * 
	 * @param state
	 *            State to send to listener
	 */
	public final void notifyCallerOfStateChange(final DeviceState state) {
		if (state == DeviceState.UNKNOWN) {
			// Nothing to notify
			return;
		}
		if (deviceEventListener != null) {
			// IOUtils.sendMessageThroughSocket(temp2);
			deviceEventListener.handleEvent(new LocalActionEvent(this, state, TerminalEvent.TERMINAL_INFO));
		}
	}

 
	public String getLoadedFormVersion() {
		return formsPackage.getFormsPackageVersion();
	}

	/**
	 * 
	 * @param deviceHealth
	 *            DeviceHealth
	 * @param rsltState
	 *            rsltState
	 */
	public void setDeviceHealth(final DeviceHealth deviceHealth, final DeviceState rsltState) {

		LOGGER.info("-> setDeviceHealth() DeviceState:{} , {}", rsltState, ingenicoDeviceEvent);
		if (deviceHealth == null) {
			return;
		}
	 
		if (!deviceHealth.getModel().equalsIgnoreCase(terminalInfo.getModelName().name())) {
			// This happens, communication port did not get updated after swapping devices
			LOGGER.error("COM port error, jDAL is terminating");
			notifyCallerOfStateChange(DeviceState.DEVICE_NOT_CONNECTED);
			return;
		}
		if (rsltState == DeviceState.READY) {
			terminalInfo.setDeviceHealth(deviceHealth);
			terminalInfo.setSerialNumber(deviceHealth.getSerialNo());
			terminalInfo.setFirmwareVersion(deviceHealth.getApplicationVersion());
			terminalInfo.setOsVersion(deviceHealth.getOsVersion());
			
			// Ensure modelReference is not null before saving it
			if(deviceHealth.getModelReference() != null) {
				terminalInfo.setModelReference(deviceHealth.getModelReference());
			}
			
			if (deviceHealth.getApplicationVersion() != null && !deviceHealth.getApplicationVersion().isEmpty()) {
				terminalInfo.verifyLoadedFirmwareVersion();
			}

		} else {
			LOGGER.error("Can not communicate with the Ingenico device");
		}
		localMsr.getMsr().removeDirectIOListener(lastIOListener);
		lastIOListener = null;
	}


	@Override
	public final Terminal getTerminalInfo() {
 
		return terminalInfo;
	}
	
 
	@Override
	public final String getSerialNumber() {
		return serialNumber;
	}
	
	 
	@Override
	public final void setSerialNumber(String value) {
		serialNumber = value;
	}

	
	@Override
	public final void setTerminalEmvKey(final String key) {
		if (key == null) {
			terminalInfo.setTerminalIsEMVCapable(false);
			return;
		}
		if (key.equals("1")) {
			terminalInfo.setTerminalIsEMVCapable(true);
			LOGGER.info("Device is EMV Enabled.");
		} else {
			LOGGER.info("Device is not EMV Enabled.");
			terminalInfo.setTerminalIsEMVCapable(false);
		}
	}
	

	@Override
	public final void setKernelVersion(final String kernelVersion) {
		// Example of data received: EMVDC:0467EMVENGINE:0487
		final Map<String, String> map = new HashMap<String, String>();
		for (String item: kernelVersion.split(EmvTagsConst.EMV_DELIM)) {
			final String[] keyValue = item.split(":", 2);
			map.put(keyValue[0], keyValue[1]);
		}
		final String emvEngineVersion = map.get("EMVENGINE");
		terminalInfo.setEmvKernelVersion(emvEngineVersion);
	}


	@Override
	public final void setEncryptionConfiguration(final int value) {
		terminalInfo.setEncryptionConfiguration(value);
	}


	@Override
	public final void setDukptKeyStatus(final String value) {
		terminalInfo.processDukptKeys(value);
	}



}
