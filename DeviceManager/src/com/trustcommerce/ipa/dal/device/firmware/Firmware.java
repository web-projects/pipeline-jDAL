package com.trustcommerce.ipa.dal.device.firmware;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.api.jpos.IngenicoMSR;
import com.ingenico.jpos.IngenicoConst;
import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;
import com.trustcommerce.ipa.dal.constants.device.TerminalModel;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.device.interfaces.FirmwareProcessor;
import com.trustcommerce.ipa.dal.device.observers.DeviceHealthDIOListener;
import com.trustcommerce.ipa.dal.device.observers.FileUploadListener;
import com.trustcommerce.ipa.dal.device.utils.DioRunFile;
import com.trustcommerce.ipa.dal.device.utils.DioSetGetVar;
import com.trustcommerce.ipa.dal.exceptions.FirmwareUploadException;
import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;
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

public class Firmware implements FirmwareProcessor, ErrorListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(Firmware.class);
	
	
	/**
	 * Firmware updates last about 5.5 minutes, for security sake, time out to
	 * 10 minutes.
	 */
	private static final int TIMEOUT = 900000;

	// Data state information
	private IngenicoMSR msr;

	private static String firmwareVersion;
	// Keep last listener set
	private DirectIOListener lastIOListener;
	
	private DeviceEventListener deviceEventListener;

	/** Full path of the location of the firmware file including the ogz name. */
	private String filePath;

	private UploadInputData uploadInputData;

	private String modelReference = "";
	
	/**
	 * Constructor.
	 * @param inputData type UploadInputData
	 * @throws FirmwareUploadException
	 */
	public Firmware(final UploadInputData inputData) throws FirmwareUploadException {

		this.uploadInputData = inputData;

		if (inputData.getFirmwareName() == null) {
			getFirmwareFileInFolder(inputData.getFirmwareName());
		} else {
			filePath = inputData.getFirmwarePath(false);
		}

		initMSR();
		requestDeviceHealth();

	}


	@Override
	public final String getFirmwareVersion() {
		return firmwareVersion;
	}


	/**
	 * Returns all the file in a folder.
	 * 
	 * @param path String Path only
	 * @return
	 * @throws FirmwareUploadException
	 */
	private void getFirmwareFileInFolder(final String path) throws FirmwareUploadException {

		LOGGER.debug("-> getFirmwareFileInFolder() path: {}", path);

		final File rootfolder = new File(path);
		if (!rootfolder.exists()) {
			throw new FirmwareUploadException("Firmware can not be updated, path " + path + " does not exist ");
		}
		final List<String> ogzFiles = new ArrayList<String>();
		for (final File fileEntry : rootfolder.listFiles()) {
			if (fileEntry.isDirectory()) {
				continue;
			} else {
				if (fileEntry.getName().endsWith(FWConst.FIRMWARE_FILE_EXTENSION)) {
					ogzFiles.add(fileEntry.getPath());
				}
			}
		}

		if (ogzFiles.isEmpty()) {
			throw new FirmwareUploadException("Firmware can not be updated, firmware not found in " + path);
		} else if (ogzFiles.size() == 1) {
			if (ogzFiles.get(0).endsWith(".OGZ")) {
				filePath = ogzFiles.get(0);
			}
		} else if (ogzFiles.size() > 1) {
			throw new FirmwareUploadException("Firmware can not be updated, multiple firmware found in " + path);
		} 
	}
	
	
	/**
	 * Open and claim MSR device. For any ingenico family device MSR must be
	 * there.
	 * 
	 * @throws IngenicoDeviceException
	 * @throws FirmwareUploadException on MSR initialization errors. 
	 */
	private void initMSR() throws FirmwareUploadException {
		LOGGER.info("-> initMSR()");
		msr = new IngenicoMSR();
		// Load the default listeners
		msr.addErrorListener(this);
		try {
			LOGGER.info("open() MSR_{}", uploadInputData.getModel().name());
			msr.open("MSR_" + uploadInputData.getModel().name());
			msr.claim(TIMEOUT);
			
		} catch (JposException e) {
			if (e.getErrorCode() == 111 && (uploadInputData.getModel().name().equalsIgnoreCase("iPP350") || uploadInputData.getModel().name().equalsIgnoreCase("iPP320"))) {
				msr = new IngenicoMSR();
				msr.addErrorListener(this);
				try {
					if(uploadInputData.getModel().name().equalsIgnoreCase("iPP350")) {
						uploadInputData.setModel(TerminalModel.iPP320);
					} else {
						uploadInputData.setModel(TerminalModel.iPP350);
					}
					LOGGER.info("open() MSR_{}", uploadInputData.getModel().name());
					msr.open("MSR_" + uploadInputData.getModel().name());
					msr.claim(TIMEOUT);
				} catch (JposException e1) {
	    			LOGGER.error("MSR Error : {}, message: {}", e.getErrorCode(), e.getMessage());
	    			throw new FirmwareUploadException("<html><center>File Upload failed. <br><br>MSR initialization error "
	    			        + e.getErrorCode() + "</center></html>");
				}
			} else {			
    			LOGGER.error("MSR Error : {}, message: {}", e.getErrorCode(), e.getMessage());
    			throw new FirmwareUploadException("<html><center>File Upload failed. <br><br>MSR initialization error "
    			        + e.getErrorCode() + "</center></html>");
			}
		}
		LOGGER.info("<- initMSR() MSR initialization complete");
	}

	/**
	 * 
	 */
	private void requestDeviceHealth() {
		LOGGER.info("-> requestDeviceHealth()");

		lastIOListener = new DeviceHealthDIOListener(this);
		try {
			msr.addDirectIOListener(lastIOListener);
			msr.setDeviceEnabled(true);
			msr.directIO(IngenicoConst.JPOS_FC_HEALTH_STATS, null, null);
		} catch (JposException e) {
			msr.removeDirectIOListener(lastIOListener);
			lastIOListener = null;

			LOGGER.warn("Error getting health information: " + e.getMessage() + " Code: " + e.getErrorCode());
		}

		LOGGER.trace("Exiting getDeviceHealth()");
	}

	public void setLoadedFormVersion(final String val) {
		firmwareVersion = val;
		msr.removeDirectIOListener(lastIOListener);
		lastIOListener = null;
	}

	public void setModelReference(final String val) {
		modelReference = val;
	}
	
	public String getModelReference() {
		return modelReference;
	}

	@Override
	public void errorOccurred(final ErrorEvent ee) {
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
	 * This method did not work
	 */
	public final void updateFirmware() throws FirmwareUploadException {
		LOGGER.debug("-> updateFirmware()");

		try {
			if (!msr.getDeviceEnabled()) {
				msr.setDeviceEnabled(true);
			}
			LOGGER.debug("Transfering ogzFile {} to to ingenico device ", filePath);
			msr.updateFirmware(filePath);
		} catch (JposException e) {
			LOGGER.error("Failed to update form: " + e.getMessage());
			throw new FirmwareUploadException(e);
		}

		// msr.directIO(IngenicoConst.ING_DIO_SAVE_FILE, new int[] { 1 },
		// ogzFile.getBytes());
		LOGGER.debug("<- updateFirmware()");
	}

	/**
	 * This is a second way to update the firmware.
	 * 
	 * @throws FirmwareUploadException
	 */
	public final void updateFirmware2(boolean reboot) throws FirmwareUploadException {
		LOGGER.info("-> updateFirmware2(): {} ", filePath);

		try {
			lastIOListener = new FileUploadListener(this, msr);
			if (!msr.getDeviceEnabled()) {
				msr.setDeviceEnabled(true);
			}
			LOGGER.info("Transfer {} to ingenico terminal ", filePath);
			msr.addDirectIOListener(lastIOListener);
			// ING_DIO_SAVE_FILE = 145
			
			//JPosDirectIoUtil.displayFirmwareUpdateForm(msr);
	
			if (reboot) {
				msr.directIO(IngenicoConst.ING_DIO_SAVE_FILE, new int[] { 1 }, filePath.getBytes());
			} else {
				//JPosDirectIoUtil.displayFirmwareUpdateForm(msr);
				msr.directIO(IngenicoConst.ING_DIO_SAVE_FILE, new int[] { 0 }, filePath.getBytes());
			}

		} catch (JposException e) {
			LOGGER.error("Failed to update form: " + e.getMessage());
			throw new FirmwareUploadException(e);
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
		if (state == DeviceState.UNKNOWN || state == DeviceState.PROGRESS_UPDATE) {
			// Nothing to notify at this moment
			return;
		}

		if (deviceEventListener != null) {
			deviceEventListener.handleEvent(new LocalActionEvent(this, state, TerminalEvent.FIRMWARE_UPDATE));
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
