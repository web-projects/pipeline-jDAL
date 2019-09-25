package com.trustcommerce.ipa.dal.device.forms;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.api.jpos.IngenicoMSR;
import com.ingenico.jpos.IngenicoConst;
import com.trustcommerce.ipa.dal.configuration.app.AppConfiguration;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.configuration.types.ConfigurationException;
import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;
import com.trustcommerce.ipa.dal.constants.device.TerminalModel;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;
import com.trustcommerce.ipa.dal.device.interfaces.FormsPackageProcessor;
import com.trustcommerce.ipa.dal.device.observers.FileUploadListener;
import com.trustcommerce.ipa.dal.device.observers.FormVersionDIOListener;
import com.trustcommerce.ipa.dal.device.types.DeviceConsts;
import com.trustcommerce.ipa.dal.device.utils.DioRunFile;
import com.trustcommerce.ipa.dal.device.utils.DioSetGetVar;
import com.trustcommerce.ipa.dal.exceptions.FirmwareUploadException;
import com.trustcommerce.ipa.dal.exceptions.FormsPackageUploadException;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;
import com.trustcommerce.ipa.dal.model.fileUpload.UploadInputData;

import jpos.JposConst;
import jpos.JposException;
import jpos.MSR;
import jpos.MSRConst;
import jpos.events.DirectIOListener;
import jpos.events.ErrorEvent;
import jpos.events.ErrorListener;

public class FormsPackage implements FormsPackageProcessor, ErrorListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(FormsPackage.class);

	private static final int TIMEOUT = 1200000;

	// Data state information
	private IngenicoMSR msr;

	private String deviceFormVersion;
	// Keep last listener set
	private DirectIOListener lastIOListener;
	private String formFile;

	private TerminalModel model;
	
	private DeviceEventListener deviceEventListener;
	
	private UploadInputData uploadInputData;

	/**
	 * 
	 * @param deviceName
	 * @throws FormsPackageUploadException
	 * @throws IngenicoDeviceException
	 */
	public FormsPackage(final UploadInputData inputData) throws FormsPackageUploadException, IngenicoDeviceException {
		uploadInputData = inputData;
		formFile = uploadInputData.getFormsPath();
		model = inputData.getModel();
		initMSR();
		requestFormVersion();
	}

	public FormsPackage(final TerminalModel deviceName, IngenicoMSR ingenicoMSR) throws FormsPackageUploadException {

		this.msr = ingenicoMSR;
		this.model = deviceName;
	}

	public String getFormsPackageVersion() {
		return deviceFormVersion;
	}

	public static void terminate(final String message) {
		LOGGER.info("-> terminate ()");
		// javax.swing.JOptionPane.showMessageDialog(null, message);
	}


	/**
	 * Open and claim MSR device. For any ingenico family device MSR must be
	 * there.
	 * 
	 * @throws IngenicoDeviceException
	 * @throws FormsPackageUploadException
	 */
	private void initMSR() throws FormsPackageUploadException, IngenicoDeviceException {
		LOGGER.debug("-> initMSR()");
		msr = new IngenicoMSR();
		// Load the default listeners
		msr.addErrorListener(this);
		try {
			LOGGER.info("open() " + "MSR_" + uploadInputData.getModel().name());
			msr.open("MSR_" + uploadInputData.getModel().name());
			msr.claim(TIMEOUT);
 
		} catch (JposException e) {
			if (e.getErrorCode() == 104) {
				LOGGER.error(
				        "MSR initialization error: jpos.xml contains a mismatch COM port number for this device."
				+ "Please update the file." + e.getErrorCode());
			} else if (e.getErrorCode() == 109) {
				LOGGER.error("MSR Error" + e.getErrorCode() + ": JPOS_E_NOEXIST: Possible problems with the jpos.xml");
			}
			if (e.getErrorCode() == 111 && (model.name().equalsIgnoreCase("iPP350") || model.name().equalsIgnoreCase("iPP320"))) {
				try {
					if(model.name().equalsIgnoreCase("iPP350")) {
						model = TerminalModel.iPP320;
					} else {
						model = TerminalModel.iPP350;
					}
					uploadInputData.setModel(model);
					msr = new IngenicoMSR();
					msr.addErrorListener(this);
					LOGGER.info("open() " + "MSR_" + uploadInputData.getModel().name());
					msr.open("MSR_" + uploadInputData.getModel().name());
					msr.claim(TIMEOUT);
				} catch (JposException e1) {
    				LOGGER.error("MSR Error" + e.getErrorCode() + ": JPOS_ERROR");
    				notifyCallerOfStateChange(DeviceState.REJECTED);
    				throw new IngenicoDeviceException("Error initializing MSR: ", e);
				}
			} else {
				throw new FormsPackageUploadException("Error initializing MSR: ", e);
			}
		}
		LOGGER.info("<- initMSR() MSR initialization complete");
	}

	/**
	 * Request from the device the file FORMSVER.TXT. FORMSVER.TXT contains the
	 * version of the form package.
	 */
	public final void requestFormVersion() {
		LOGGER.debug("-> getFormVersion()");

		lastIOListener = new FormVersionDIOListener(this);
		try {
			msr.addDirectIOListener(lastIOListener);
			if (!msr.getDeviceEnabled()) {
				msr.setDeviceEnabled(true);
			}
			msr.directIO(IngenicoConst.ING_DIO_RETRIEVE_FILE, null, DeviceConsts.VERSION_FILE.getBytes());
		} catch (JposException e) {
			LOGGER.error("JposException in requestFormVersion(): " + e.getErrorCode());
			// We do not want to stop because of errors in the forms ...
			msr.removeDirectIOListener(lastIOListener);
			lastIOListener = null;

			if (e.getErrorCode() == JposConst.JPOS_E_NOEXIST) { // 109
				LOGGER.info("Connected device does not have TrustCommerce forms package.");
			} else if (e.getErrorCode() == 114) {
				// JPOS_E_EXTENDED
				LOGGER.error("ErrorCodeExtended = {}", e.getErrorCodeExtended());
			} else {
				LOGGER.warn("Error getting form version file: {} code: ", e.getMessage(), e.getErrorCode());
			}
		}

		LOGGER.trace("<- getFormVersion()");
	}

	public void setLoadedFormVersion(final String val, final DeviceState rsltState) {
		deviceFormVersion = val;
		msr.removeDirectIOListener(lastIOListener);
		lastIOListener = null;
	}

	@Override
	public final void errorOccurred(final ErrorEvent ee) {
		LOGGER.error("-> errorOccurred() ");
		DeviceState state = DeviceState.UNKNOWN;
		final Object source = ee.getSource();

		try {
			if (source instanceof MSR) {
				final MSR oMSR = (MSR) ee.getSource();
				if (oMSR.getErrorReportingType() == MSRConst.MSR_ERT_CARD) {
					// Also here when the user cancels the "Enter Card Number"
					// form in the device ?
					LOGGER.info("ERROR REPORTING BY CARD");
					LOGGER.info("ErrorCode: {}", ee.getErrorCode()); // 111=JPOS_E_FAILURE
					LOGGER.info("ErrorCodeExtended: {}", ee.getErrorCodeExtended()); // 0
					LOGGER.info("ErrorResponse: {}", ee.getErrorResponse()); // 12=JPOS_ER_CLEAR
				}
			}

			if (ee.getErrorCode() == JposConst.JPOS_SUCCESS) {
				LOGGER.debug("Received success code: 0");

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

	@Override
	public final void updateForms(final boolean reboot) throws FormsPackageUploadException {
		LOGGER.info("-> updateForms() {} ", formFile);

		try {
			lastIOListener = new FileUploadListener(this, msr);
			if (!msr.getDeviceEnabled()) {
				msr.setDeviceEnabled(true);
			}
			LOGGER.debug("Transfer {} to ingenico terminal", formFile);
			msr.addDirectIOListener(lastIOListener);
			// ING_DIO_SAVE_FILE = 145
			if (reboot) {
				msr.directIO(IngenicoConst.ING_DIO_SAVE_FILE, new int[] { 1 }, formFile.getBytes());
			} else {
				//JPosDirectIoUtil.displayFirmwareUpdateForm(msr);
				msr.directIO(IngenicoConst.ING_DIO_SAVE_FILE, new int[] { 0 }, formFile.getBytes());
			}

		} catch (JposException e) {
			LOGGER.error("Failed to update form: " + e.getMessage());
			throw new FormsPackageUploadException(e.getMessage());
		}
	}

	

	@SuppressWarnings("unused")
	private boolean needFormUpload(String v1, String tcVersion) {

		if (v1 == null || v1.isEmpty()) {
			return true;
		}

		v1 = v1.replaceAll("\\n", "");
		tcVersion = tcVersion.replaceAll("\\n", "");

		if (v1.equalsIgnoreCase(tcVersion)) {
			return false;
		}
		final String[] v1Array = v1.split("\\.");
		final String[] tcVersionArray = tcVersion.split("\\.");

		if (v1Array.length < 3 || v1Array.length >= 4) {
			// not our format
			return true;
		}

		if (v1Array[0].equalsIgnoreCase(tcVersionArray[0])) {
			if (v1Array[1].equalsIgnoreCase(tcVersionArray[1])) {
				if (v1Array[2].equalsIgnoreCase(tcVersionArray[2])) {
					return false;
				} else {
					return true;
				}
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

	/**
	 * Call on termination.
	 */
	@Override
	public final void releaseDevice() {
		LOGGER.debug("-> releaseDevice()");
		try {

			if (msr != null) {
				LOGGER.debug("Releasing MSR");

				//JPosDirectIoUtil.showPostBootForm(msr);

				msr.removeDirectIOListener(lastIOListener);
				lastIOListener = null;
				msr.close();
			}

		} catch (JposException e) {
			LOGGER.warn("Error encountered releasing MSR: " + e.getMessage());
		} catch (Exception ie) {
			if (ie instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			} else {
				LOGGER.warn("An unkown error happened while releasing MSR: " + ie.getMessage());
			}
		}
		msr = null;
	}

	
	public void setEventListener(DeviceEventListener newListener) {
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
		if (state == DeviceState.UNKNOWN || state == DeviceState.PROGRESS_UPDATE) {
			// Nothing to notify
			return;
		}
		if (deviceEventListener != null) {
			// IOUtils.sendMessageThroughSocket(temp2);
			releaseDevice();
			deviceEventListener.handleEvent(new LocalActionEvent(this, state, TerminalEvent.FORMUPDATE));
		}
	}
	
	
	/**
	 * Helper class to send state message to DeviceEventListener instance old.
	 * name: generateState
	 * 
	 * @param state
	 *            State to send to listener
	 */
	public final void notifyCallerOfStateChange(final DeviceState state, final String value) {
		if (state == DeviceState.UNKNOWN) {
			// Nothing to notify
			return;
		}
		if (deviceEventListener != null) {
			// IOUtils.sendMessageThroughSocket(temp2);
			deviceEventListener.handleEvent(new LocalActionEvent(this, state, TerminalEvent.FORMUPDATE));
		}
	}
	
	

	@Override
	public final void updateSingleFile(final String filename) throws FormsPackageUploadException {
		LOGGER.debug("-> updateSingleFile()");
		final String singleFile = uploadInputData.getSingleFileRootPath() + filename;
		try {
			lastIOListener = new FileUploadListener(this, msr);
			if (!msr.getDeviceEnabled()) {
				msr.setDeviceEnabled(true);
			}
			LOGGER.debug("Transfer " + singleFile + " to ingenico device");
			msr.addDirectIOListener(lastIOListener);
			// ING_DIO_SAVE_FILE = 145
			msr.directIO(IngenicoConst.ING_DIO_SAVE_FILE, new int[] { 1 }, singleFile.getBytes());

		} catch (JposException e) {
			LOGGER.error("Failed to update form: " + e.getMessage());
			throw new FormsPackageUploadException(e.getMessage());
		}
	}

	
	@Override
	public void displayUploadForms() throws FirmwareUploadException {
		try {
			DioRunFile.displayFirmwareUpdateForm(msr);
		} catch (JposException e) {
			LOGGER.error("Failed to update form: " + e.getMessage());
			throw new FirmwareUploadException(e);
		} catch (IngenicoDeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

 
}
