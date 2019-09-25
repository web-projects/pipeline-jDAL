package com.trustcommerce.ipa.dal.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.api.jpos.IngenicoMSR;
import com.ingenico.jpos.IngenicoConst;
import com.trustcommerce.ipa.dal.configuration.app.AppConfiguration;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.constants.device.TerminalModel;
import com.trustcommerce.ipa.dal.constants.forms.FormTypes;
import com.trustcommerce.ipa.dal.device.utils.DioErrorMessages;
import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;
import com.trustcommerce.ipa.dal.terminal.IngenicoTerminal;

import jpos.JposException;
import jpos.events.DirectIOListener;
import jpos.events.ErrorEvent;
import jpos.events.ErrorListener;


/**
 * Single class use to obtained an initialize instance of the MSR.
 * @author luisa.lamore
 *
 */
public class LocalMsr implements ErrorListener {

	/** slf4j . */
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalMsr.class);
	

	private IngenicoMSR msr;
	
	private ErrorListener currentErrorListener;
	
	private TerminalModel model;
	
	private int deviceTimeout;
	
	private boolean msrIsOpen;
	
	
	public LocalMsr(TerminalModel deviceName) {
		model = deviceName;
	}
	
	public TerminalModel getLocalMsrModel() {
		return model;
	}
	
	public boolean IsMsrIsOpen() {
		return msrIsOpen;
	}
	
 	
	public void initMsr(ErrorListener errorListener) throws IngenicoDeviceException {
		LOGGER.debug("-> initMsr()");
		setTimeout(errorListener);
		msr = new IngenicoMSR();
		msr.addErrorListener(this);
		try {
			initMSR(errorListener);
		} catch (IngenicoDeviceException e) {
			
			if (e.getErrorCode() == 111 && (model.name().equalsIgnoreCase("iPP350") || model.name().equalsIgnoreCase("iPP320"))) {
				try {
					if(model.name().equalsIgnoreCase("iPP350")) {
						model = TerminalModel.iPP320;
					} else {
						model = TerminalModel.iPP350;
					}
					setTimeout(errorListener);
					msr = new IngenicoMSR();
					msr.addErrorListener(this);
					initMSR(errorListener);
				} catch (IngenicoDeviceException e1) {
					LOGGER.error(e1.getMessage());
					throw new IngenicoDeviceException(e1.getMessage(), EntryModeStatusID.MSRIsCloseOrNull.getCode());
				}
			} else {
				LOGGER.error(e.getMessage());
				throw new IngenicoDeviceException(e.getMessage(), EntryModeStatusID.MSRIsCloseOrNull.getCode());
			}
		}
	}
	
	public IngenicoMSR getMsr() {
		return msr;
	}
	
	/**
	 * 
	 * @param listener DirectIOListener type
	 */
	public final void releaseAndCloseMsr(final DirectIOListener listener) {
		LOGGER.debug("-> releaseAndCloseMsr()");
		if (msr == null) {
			LOGGER.debug("No need to release, MSR is null");
			return;
		}

		if (listener != null) {
			msr.removeDirectIOListener(listener);
		}
		try {
			showPostBootForm();
			if (msr.getDeviceEnabled()) {
				msr.setDeviceEnabled(false);
			}
			if (msr.getClaimed()) {
				msr.release();
			}
			msrIsOpen = false;
			msr.close();
		} catch (JposException e) {
			final String error = DioErrorMessages.getErrorMessage("releaseMsr", e);
			LOGGER.error(error);
			 
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				LOGGER.debug("InterruptedException releasing MSR");
			} else {
				LOGGER.warn("Unkown error while releasing MSR: " + e.getMessage());
			}
		}
		msr = null;
	}
	
	/**
	 * Displays the postboot form on the terminal.
	 * After the form is displayed the MSR is disabled and claimed.
	 * @throws JposException 
	 * @throws IngenicoDeviceException
	 * @device Device to display post boot form on
	 */
	private void showPostBootForm() throws JposException, IngenicoDeviceException {
		LOGGER.info("-> showPostBootForm()");
		if (msr == null) {
			LOGGER.error("Cannot display postboot");
			// this should never happen, but for sanity sake ....
			throw new IngenicoDeviceException("Error displaying postboot", EntryModeStatusID.MSRIsCloseOrNull.getCode());
		}
		if (!msr.getClaimed()) {
			final int timeout = ClientConfigurationUtil.getMsrTimeout();
			msr.claim(timeout);
		}
		if (!msr.getDeviceEnabled()) {
			msr.setDeviceEnabled(true);
		}
		LOGGER.debug("Calling ING_DIO_RUN_FILE");
		msr.directIO(IngenicoConst.ING_DIO_RUN_FILE, new int[] { 0 }, FormTypes.POST_BOOT.getName().getBytes());
	}
	
	/**
	 * 
	 * @param state DeviceState
	 */
	private void redirectError(final DeviceState state) {
		if (currentErrorListener instanceof IngenicoMsr) {
			((IngenicoMsr) currentErrorListener).notifyCallerOfStateChange(state);
		} else if (currentErrorListener instanceof IngenicoTerminal) {
			((IngenicoTerminal) currentErrorListener).notifyCallerOfStateChange(state);
		}
	}
	
	
	private void setTimeout(final ErrorListener errorListener) {
		if (errorListener instanceof IngenicoMsr) {
			deviceTimeout = ClientConfigurationUtil.getMsrTimeout();
		} else if (errorListener instanceof IngenicoTerminal) {
			deviceTimeout = ClientConfigurationUtil.getMsrTimeout();
		}
	}
	
	
	public void updateErrorListener(final ErrorListener listener) {
		if (currentErrorListener != null) {
			msr.removeErrorListener(currentErrorListener);
		}
		msr.addErrorListener(listener);
		currentErrorListener = listener;
	}
	

	/**
	 * 
	 */
	public void enableMSR() {
		try {
			if (!msr.getDeviceEnabled()) {
				msr.setDeviceEnabled(true);
			}
		} catch (JposException e) {
			LOGGER.error("Failed to enable MSR. Cause: " + e.getMessage() + " Code: " + e.getErrorCode()
			        + " Extended code: " + e.getErrorCodeExtended());
			redirectError(DeviceState.ERROR);
		}
	}

	/**
	 * Open and claim MSR device. For any ingenico family device MSR must be
	 * there.
	 * @param ErrorListener listener
	 * @throws IngenicoDeviceException
	 */
	private void initMSR(final ErrorListener listener) throws IngenicoDeviceException {
		
		LOGGER.info("-> initMSR() : {} ", model);
		updateErrorListener(listener);
		
		try {
			msr.open("MSR_" + model);
			msrIsOpen = true;
			LOGGER.info("MSR OPEN SUCCESFULLY ");
		} catch (JposException e) {
			LOGGER.error("*** Can Not Open MSR: JposException: {}, {}", e.getErrorCode(), e.getMessage());
			if (e.getErrorCode() == 111 && !model.name().equalsIgnoreCase("iPP350")) {
				redirectError(DeviceState.DEVICE_NOT_CONNECTED);
			}
			final String temp = AppConfiguration.getLanguage().getString("INITIALIZING_ERROR");
			throw new IngenicoDeviceException(DioErrorMessages.getErrorMessage(temp, e), e.getErrorCode());
		}
		claimMSR(listener);
		LOGGER.info("<- initMSR() MSR initialization complete");
	}
	
	
	private void claimMSR(final ErrorListener listener) throws IngenicoDeviceException {
		LOGGER.info("-> claimMSR()  device timeout ", deviceTimeout);
		if (msr == null) {
			// This check has been included here for cases where user terminates DAL [x] before the device is open
			throw new IngenicoDeviceException("MSR has been released and can not be initialized", 666);
		}
		try {
			msr.claim(deviceTimeout);
			
			if (listener instanceof IngenicoMsr) {
				msr.setTracksToRead(jpos.MSRConst.MSR_TR_1_2_3);
				msr.setErrorReportingType(jpos.MSRConst.MSR_ERT_CARD);
				msr.setParseDecodeData(true);
			}
		} catch (JposException e) {
			LOGGER.error("JposException: " + e.getMessage());
			String temp = AppConfiguration.getLanguage().getString("INITIALIZING_ERROR");
			final String error = DioErrorMessages.getErrorMessage(temp, e);
			LOGGER.error(e.getMessage());
			throw new IngenicoDeviceException(error);
		}
	}

	
	@Override
	public final void errorOccurred(final ErrorEvent error) {
 
		LOGGER.error("-> errorOccurred()");
		LOGGER.warn("ERROR REPORTING BY CARD");
		LOGGER.info("ErrorCode: {}", error.getErrorCode()); // 111=JPOS_E_FAILURE
		LOGGER.info("ErrorCodeExtended: {}", error.getErrorCodeExtended()); // 0
		LOGGER.info("ErrorResponse: {}", error.getErrorResponse()); // 12=JPOS_ER_CLEAR

	}
	
	
	public void reInitializeMsr() throws JposException {
		// 1 second sleep to give the driver time to get its bearings
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		if (msr != null) {
			if (!msr.getClaimed()) {
				msr.claim(deviceTimeout);
			}
			msr.clearInput();
			enableMSR();
		}
	}

}
