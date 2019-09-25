package com.trustcommerce.ipa.dal.uploader.files;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.api.jpos.IngenicoMSR;
import com.ingenico.jpos.IngenicoConst;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.device.interfaces.SingleUploadProcessor;
import com.trustcommerce.ipa.dal.device.observers.FileUploadListener;
import com.trustcommerce.ipa.dal.exceptions.FileUploadException;
import com.trustcommerce.ipa.dal.exceptions.FormsPackageUploadException;
import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.logger.FileIOUtils;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;
import com.trustcommerce.ipa.dal.model.fileUpload.UploadInputData;

import jpos.JposConst;
import jpos.JposException;
import jpos.MSR;
import jpos.MSRConst;
import jpos.events.DirectIOListener;
import jpos.events.ErrorEvent;
import jpos.events.ErrorListener;

public class SingleFileUpdateImpl implements SingleUploadProcessor, ErrorListener {

	private static final Logger logger = LoggerFactory.getLogger(SingleFileUpdateImpl.class);

	private static final int TIMEOUT = 1200000;

	// Data state information
	private IngenicoMSR msr;

	private UploadInputData uploadInputData;

	// Keep last listener set
	private DirectIOListener lastIOListener;
	private String singleFile;
	//private JPosDirectIoUtil jPosDirectIoUtil;
	
	private DeviceEventListener deviceEventListener;

	public SingleFileUpdateImpl(final UploadInputData upload) throws FileUploadException {
		this.uploadInputData = upload;
		getSingleFilePath();
		initMSR();
	}


	public static void terminate(final String message) {
		logger.info("-> terminate ()");
		// javax.swing.JOptionPane.showMessageDialog(null, message);
		
		if (ClientConfigurationUtil.get().isDev()) {
			FileIOUtils.saveDataToTempFile(EntryModeStatusID.FormsPackageError.getCode(), "formsMessage.txt");
		}
	}


	/**
	 * Open and claim MSR device. For any ingenico family device MSR must be
	 * there.
	 * 
	 * @throws IngenicoDeviceException
	 * @throws FormsPackageUploadException
	 */
	private void initMSR() throws FileUploadException {
		logger.info("-> initMSR()");
		msr = new IngenicoMSR();
		// Load the default listeners
		msr.addErrorListener(this);
		try {
			logger.info("open() " + "MSR_" + uploadInputData.getModel().name());
			msr.open("MSR_" + uploadInputData.getModel().name());
			logger.trace("claim() " + TIMEOUT + "");
			msr.claim(TIMEOUT);
			//msr.setTracksToRead(jpos.MSRConst.MSR_TR_1_2_3);
 			//msr.setErrorReportingType(jpos.MSRConst.MSR_ERT_CARD);
 			//msr.setParseDecodeData(true);
		} catch (JposException e) {
			if (e.getErrorCode() == 104) {
				logger.error(
				        "MSR initialization error: jpos.xml contains a mismatch COM port number for this device."
				+ "Please update the file." + e.getErrorCode());
			} else if (e.getErrorCode() == 109) {
				logger.error("MSR Error" + e.getErrorCode() + ": JPOS_E_NOEXIST: Possible problems with the jpos.xml");
			}
			if (e.getErrorCode() == 111) {
				logger.error("MSR Error" + e.getErrorCode() + ": JPOS_ERROR");
			}
			logger.error("Error initializing MSR: ", e);
			System.exit(0);
		}
		logger.info("<- initMSR() MSR initialization complete");
	}


	@Override
	public final void errorOccurred(final ErrorEvent ee) {
		logger.error("-> errorOccurred() ");
		DeviceState state = DeviceState.UNKNOWN;
		final Object source = ee.getSource();

		try {
			if (source instanceof MSR) {
				final MSR oMSR = (MSR) ee.getSource();
				if (oMSR.getErrorReportingType() == MSRConst.MSR_ERT_CARD) {
					// Also here when the user cancels the "Enter Card Number"
					// form in the device ?
					logger.info("ERROR REPORTING BY CARD");
					logger.info("ErrorCode: {}", ee.getErrorCode()); // 111=JPOS_E_FAILURE
					logger.info("ErrorCodeExtended: {}", ee.getErrorCodeExtended()); // 0
					logger.info("ErrorResponse: {}", ee.getErrorResponse()); // 12=JPOS_ER_CLEAR
				}
			}

			if (ee.getErrorCode() == JposConst.JPOS_SUCCESS) {
				logger.debug("Received success code: 0");

			} else if (ee.getErrorLocus() == JposConst.JPOS_EL_INPUT
			        || ee.getErrorLocus() == JposConst.JPOS_EL_INPUT_DATA) {
				logger.warn("Error encountered reading input data");

				// 1 second sleep to give the driver time to get its bearings
				Thread.sleep(GlobalConstants.MESSAGE_WARN_SLEEP_TIMER);
				if (source.equals(msr)) {
					if (!msr.getClaimed()) {
						msr.claim(TIMEOUT);
					}

					msr.clearInput();
					msr.setDeviceEnabled(true);

				} else {
					logger.warn("Input error source unknown");
				}
			} else {
				state = DeviceState.ERROR;
			}
		} catch (JposException e) {
			logger.error("JposException: Failed to clear input for swipe retry");
			state = DeviceState.ERROR;
		} catch (InterruptedException e) {
			logger.error("JposException: Post-input error sleep interrupted");
			state = DeviceState.ERROR;
		}
		logger.debug("<- errorOccurred()" + state);
	}

	@Override
	public final void uploadFile() throws FileUploadException {
		logger.debug("-> uploadFile() {} ", singleFile);

		try {
			lastIOListener = new FileUploadListener(this, msr);
			if (!msr.getDeviceEnabled()) {
				msr.setDeviceEnabled(true);
			}
			logger.debug("Transfer {} to ingenico terminal", singleFile);
			msr.addDirectIOListener(lastIOListener);
			// ING_DIO_SAVE_FILE = 145
			msr.directIO(IngenicoConst.ING_DIO_SAVE_FILE, new int[] { 1 }, singleFile.getBytes());

		} catch (JposException e) {
			logger.error("Failed to update form: " + e.getMessage());
			throw new FileUploadException(e.getMessage());
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
		if (state == DeviceState.UNKNOWN) {
			// Nothing to notify
			return;
		}
		if (deviceEventListener != null) {
			// IOUtils.sendMessageThroughSocket(temp2);
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
	
	/**
	 * Returns the path and file to upload.
	 * @return String type
	 */
	private String getSingleFilePath() {
		logger.debug("-> getSingleFilePath()");

		String path = null;

		// This is for internal testing, so for firmware is use for the single
		// file name;
		path = uploadInputData.getSingleFileRootPath() + uploadInputData.getFirmwareName();
		File filePath = new File(path);
		if (!filePath.exists()) {
			logger.error("FILE NOT FOUND in " + path);
			System.exit(0);
		}

		return path;
	}

	
	@Override
	public final void updateSingleFile() throws FileUploadException {
		logger.info("-> updateSingleFile()");
		final String singleFile = getSingleFilePath();
		try {
			lastIOListener = new FileUploadListener(this, msr);
			if (!msr.getDeviceEnabled()) {
				msr.setDeviceEnabled(true);
			}
			logger.debug("Transfer " + singleFile + " to ingenico device");
			msr.addDirectIOListener(lastIOListener);
			// ING_DIO_SAVE_FILE = 145
			msr.directIO(IngenicoConst.ING_DIO_SAVE_FILE, new int[] { 1 }, singleFile.getBytes());

		} catch (JposException e) {
			logger.error("Failed to update form: " + e.getMessage());
			throw new FileUploadException(e.getMessage());
		}
	}
}
