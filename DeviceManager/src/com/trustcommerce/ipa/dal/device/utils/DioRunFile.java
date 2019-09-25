package com.trustcommerce.ipa.dal.device.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.api.jpos.IngenicoMSR;
import com.ingenico.jpos.IngenicoConst;
import com.trustcommerce.ipa.dal.configuration.types.ConfigurationException;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.constants.forms.FormTypes;
import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;
import com.trustcommerce.ipa.dal.model.types.ManualEntryState;

import jpos.BaseControl;
import jpos.JposException;


/**
 * 
 * @author luisa.lamore
 * 
 * This class stores ING_DIO_RUN_FILE methods.
 * This command is use to display a form in the PIN pad.
 * 
 * The first parameter Command ING_DIO_RUN_FILE = 146.
 * Command ING_DIO_RUN_FILE pData description: 
 * 	0 = form has no buttons or text boxes.
 *  1 = form has buttons or text boxes.
 *  2 = for is used for clearing text entry
 *  
 *  JPOS object: File name
 */
public class DioRunFile extends DioBase {

	/** Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DioRunFile.class);

	

	/**
	 * Display form specified in formName to device.
	 * 
	 * @param device
	 *            Device to display form to i.e msr
	 * @param formName
	 *            Filename of form to display
	 * @param mode 0 if form does not contain any user input, edit boxes 
	 * @throws JposException
	 * @throws IngenicoDeviceException 
	 * @throws ConfigurationException
	 */
	public static void showDeviceForm(final BaseControl device, final String formName, final int mode) 
			throws JposException, IngenicoDeviceException {
		LOGGER.debug("-> showDeviceForm() mode {} formName {} ", mode, formName);
		getMsrReady(device);
		device.directIO(IngenicoConst.ING_DIO_RUN_FILE, new int[] { mode }, formName.getBytes());
	}

	
	/**
	 * Display form specified in formName to device.
	 * 
	 * @param device IngenicoMSR
	 * @param formName
	 *            Filename of form to display
	 * @param buffer
	 *            Additional setting for this call
	 * @param mode
	 * @throws JposException
	 * @throws ConfigurationException
	 */
	public static void showDeviceForm(final BaseControl device, final String formName, final String buffer,
			final int mode, int timeout) throws JposException, IngenicoDeviceException {
		LOGGER.trace("-> showDeviceForm() mode {} formName {} ", mode, formName);
		getMsrReady(device);
		device.directIO(IngenicoConst.ING_DIO_SETVAR, new int[] { 1 }, buffer.getBytes());
		// The first parameter ING_DIO_RUN_FILE = 146. RUN_FILE (see p55)
		device.directIO(IngenicoConst.ING_DIO_RUN_FILE, new int[] { mode }, formName.getBytes());
	}
	
	
	/**
	 * Displays the CVV form in the Ingenico Device. After sending the request.
	 * to the device the Device State changes to MANUAL_CREATE_TRACKS.
	 * @param msr IngenicoMSR
	 *             
	 * @throws JposException
	 * @throws IngenicoDeviceException 
	 */
	public static ManualEntryState displayManualCVVForm(IngenicoMSR msr) throws JposException, IngenicoDeviceException {
		LOGGER.info("-> displayManualCVVForm() ");
		msr.directIO(IngenicoConst.JPOS_FC_SET_VARIABLE, 0, DirectIOCommands.MANUAL_ENTRY_CVV2);
		showDeviceForm(msr, FormTypes.CVV.getName(), 1);
		return ManualEntryState.MANUAL_CREATE_TRACKS;
	}

	/**
	 * Display the request for the zipcode in the device.
	 * @param msr IngenicoMSR
	 *            Device to display form to i.e msr
	 * @throws JposException
	 * @throws IngenicoDeviceException 
	 */
	public static ManualEntryState displayZipCodeForm(IngenicoMSR msr) throws JposException, IngenicoDeviceException {
		LOGGER.info("-> displayZipCodeForm() ");
		showDeviceForm(msr, FormTypes.ZIPCODE.getName(), 1);
		return ManualEntryState.MANUAL_ZIPCODE_ENTRY;
	}

	
	/**
	 * 
	 * @param msr IngenicoMSR
	 * @return
	 * @throws JposException
	 * @throws IngenicoDeviceException 
	 */
	public static ManualEntryState displayManualExpDate(IngenicoMSR msr) throws JposException, IngenicoDeviceException {
		LOGGER.info("-> displayManualExpDate() ");
		msr.directIO(IngenicoConst.JPOS_FC_SET_VARIABLE, 0, DirectIOCommands.MANUAL_ENTRY_CREATE_EXP);
		showDeviceForm(msr, FormTypes.EXPIRATION_DATE.getName(), 1);
		return ManualEntryState.MANUAL_EXP_ENTRY;
	}

	
	/**
	 * Displays the firmware upload form. 
	 * @param msr IngenicoMSR
	 * @throws JposException
	 * @throws IngenicoDeviceException 
	 */
	public static void displayFirmwareUpdateForm(final IngenicoMSR msr) throws JposException, IngenicoDeviceException {
		showDeviceForm(msr, FormTypes.FIRMWARE_UPLOAD.getName(), 1);
	}

	
	/**
	 * Display Account Number Entry Form in the Ingenico Device.
	 * 
	 * @param msr IngenicoMSR
	 * @throws JposException
	 * @throws IngenicoDeviceException when the MSR happens to be null (rare situation)
	 */
	public static void displayAccountNumberEntry(final IngenicoMSR msr) throws JposException, IngenicoDeviceException {
		if (msr == null) {
			throw new IngenicoDeviceException(MSR_NULL, EntryModeStatusID.MSRIsCloseOrNull.getCode());
		}

		// JPOS_FC_SET_VARIABLE = 0xA1 : Set UIA variables
		// Turned off the beeping otherwise it will beep when typing acct number, exp date and cvv ...
		msr.directIO(IngenicoConst.JPOS_FC_SET_VARIABLE, new int[] { 0 },
		        new String(DirectIOCommands.KEYBEEP_ZERO).getBytes());

		// ManualEntry=ON begins the manual entry session
		msr.directIO(IngenicoConst.JPOS_FC_SET_VARIABLE, new int[] { 1 },
		        new String(DirectIOCommands.MANUAL_ENTRY_ON).getBytes());
		msr.directIO(IngenicoConst.JPOS_FC_SET_VARIABLE, new int[] { 1 }, DirectIOCommands.MANUAL_ENTRY_PAN.getBytes());
		showDeviceForm(msr, FormTypes.CARD_NUMBER.getName(), 1);
	}
	
	
	/**
	 * 
	 * @param msr IngenicoMSR
	 * @param amount as String
	 * @throws JposException
	 * @throws IngenicoDeviceException
	 */
	public static void displayAmountVerificationForm(final IngenicoMSR msr, final String amount)
	        throws JposException, IngenicoDeviceException {
		LOGGER.info("-> displayAmountVerificationForm() Setting {}", amount);
		
		if (msr == null) {
			throw new IngenicoDeviceException(MSR_NULL, EntryModeStatusID.MSRIsCloseOrNull.getCode());
		}
		// Re-enable device for amount verification
		if (!msr.getDeviceEnabled()) {
			msr.setDeviceEnabled(true);
		}
		// Load amount to variable
		final String strBuf = "VAR=" + amount;
		msr.directIO(IngenicoConst.ING_DIO_SETVAR, new int[] { 1 }, strBuf.getBytes());
		// msr.setDeviceEnabled(false);
		showDeviceForm(msr, FormTypes.AMOUNT_VERIFICATION.getName(), 1);
	}
	
	
	/**
	 * Use for iUP250 to display messages in the terminal.
	 * 
	 * @param msr
	 * @param errorCode
	 * @param message String
	 * @throws JposException
	 * @throws IngenicoDeviceException when the MSR happens to be null (rare situation)
	 */
	public static void displayMessageInTerminal(final IngenicoMSR msr, final EntryModeStatusID errorCode,
	        final String message) throws JposException , IngenicoDeviceException{
		LOGGER.info("-> displayMessageInTerminal() Setting {}", message);
		getMsrReady(msr);

		final String dispError;
		if (errorCode == EntryModeStatusID.Success) {
			dispError = "VAR1=" + message;
		} else {
			dispError = "VAR1=" + message + "-" + errorCode.getCode();
		}
		msr.directIO(IngenicoConst.ING_DIO_SETVAR, new int[] { 1 }, dispError.getBytes());
		// msr.setDeviceEnabled(false);
		showDeviceForm(msr, FormTypes.MESSAGE.getName(), 1);
	}
	

}
