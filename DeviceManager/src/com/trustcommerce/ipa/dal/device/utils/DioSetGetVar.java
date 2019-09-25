package com.trustcommerce.ipa.dal.device.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.api.jpos.IngenicoMSR;
import com.ingenico.jpos.IngenicoConst;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;

import jpos.BaseControl;
import jpos.JposException;

/**
 * Class use to send commands to the Device ManualEntryState currentState.
 * 
 * ING_DIO_SETVAR = 161
 * 
 * 
 * 
 * @author luisa.lamore
 *
 */
public class DioSetGetVar extends DioBase {

	/** Logger.*/
	private static final Logger LOGGER = LoggerFactory.getLogger(DioSetGetVar.class);
	
	private DioSetGetVar() {
		// defeat instantiation
	}
	
	public static void setManualEntryStatus(IngenicoMSR msr) throws JposException {
		LOGGER.info("-> displayManualEntryStatus() ");
		msr.directIO(IngenicoConst.JPOS_FC_GET_VARIABLE, new int[] { 0 }, 
				new String(DirectIOCommands.MANUAL_ENTRY_STATUS));
	}

	
	/**
	 * For EPIC we want to avoid displaying the CVV entry so we will pass a
	 * fictitious value.
	 * 
	 * @throws JposException
	 */
	public static void setManualCVVEntryValue(final IngenicoMSR msr) throws JposException {
		LOGGER.info("-> setManualCVVEntryValue() ");
		// Manually pass the value of the CVV to the device
		final String strBuf = DirectIOCommands.DIRECT_SETTING_CVV2;
		// ING_DIO_SETVAR = 161
		msr.directIO(IngenicoConst.ING_DIO_SETVAR, new int[] { 0 }, strBuf.getBytes());
	}


	/**
	 * Last step in the manual device entry process. In order to call this
	 * method, the cc number, exp date and cvv need to be stored in the device.
	 * 
	 * @param msr
	 * @return Status MANUAL_ENTRY_OFF
	 * @throws JposException
	 */
	public static void requestManualEntryCreationOfTracks(final IngenicoMSR msr) throws JposException {
		LOGGER.info("-> requestManualEntryCreationOfTracks() ");
		msr.directIO(IngenicoConst.JPOS_FC_GET_VARIABLE, new int[] { 0 },
		        new String(DirectIOCommands.MANUAL_ENTRY_STATUS));
		msr.directIO(IngenicoConst.JPOS_FC_SET_VARIABLE, new int[] { 0 },
		        new String(DirectIOCommands.MANUAL_ENTRY_CREATE_TRACKS));
		enableSound(msr);
	}
	


	/**
	 * 
	 * @param device BaseControl
	 */
	public static void enableSound(final BaseControl device) {
		// re-enable the beeping to on
		try {
			getMsrReady(device);
			device.directIO(IngenicoConst.JPOS_FC_SET_VARIABLE, new int[] { 0 },
			        new String(DirectIOCommands.KEYBEEP_ON).getBytes());
		} catch (JposException e) {
			// We don't need to report this, probably device was close
			LOGGER.error("Error found trying to enable sound {} ", e.getMessage());
		} catch (IngenicoDeviceException e) {
			LOGGER.error("Error trying to enable sound {} ", e.getMessage());
		}
	}

	

	/**
	 * EMV fall back is supported on the iUN. The fallback happens when chip detection fails and the card is taken out.
	 * When the card in inserted and it is unable to read the chip, after injecting this variable, the msr on the card
	 * will be read when the card is removed.
	 * ONLY USE ON FRAMEWORKS AFTER VERSION 16.
	 * @param msr type IngenicoMSR
	 * @param value either 1 or 0
	 * @throws JposException
	 * @throws IngenicoDeviceException when the MSR happens to be null (rare situation)
	 */
	@SuppressWarnings("unused")
	private static void injectEMVFallbackToiUN(final IngenicoMSR msr, final int value) throws JposException, 
		IngenicoDeviceException {
		LOGGER.info("-> injectEMVFallbackToiUN() " + value);
		if (msr == null) {
			throw new IngenicoDeviceException(MSR_NULL, EntryModeStatusID.MSRIsCloseOrNull.getCode());
		}
		if (msr.getDeviceEnabled()) {
			msr.setDeviceEnabled(false);
		}
		if (!msr.getClaimed()) {
			msr.claim(ClientConfigurationUtil.getMsrTimeout());
		}
		final String temp = "IUN_MSR_FALLBACK_SUPPORTED=" + value;
		msr.directIO(IngenicoConst.JPOS_FC_SET_VARIABLE, new int[] { 0 }, temp.getBytes());
		msr.setDeviceEnabled(true);
	}


	/**
	 * Sends a DirectIO to command 11 id="101" message="Please remove card".
	 * @param msr type IngenicoMSR
	 * @throws JposException
	 * @throws IngenicoDeviceException when the MSR happens to be null (rare situation)
	 */
	public static void suspendEmvFlow(final IngenicoMSR msr) throws IngenicoDeviceException {
		LOGGER.debug("-> suspendEmvFlow() ");
		if (msr == null) {
			throw new IngenicoDeviceException(MSR_NULL, EntryModeStatusID.MSRIsCloseOrNull.getCode());
		}
		
		try {
			getMsrReady(msr);
			final String temp = "EMV_AUTH_OFFLINE_PROMPT_INDEX=101";
			 
			// valid prompt index from PROMPT.XML EMV_CARD_REMOVED_PROMPT_INDEX
			msr.directIO(IngenicoConst.JPOS_FC_SET_VARIABLE, new int[] { 0 }, temp);

			msr.setDeviceEnabled(true);
		} catch (JposException e) {
			LOGGER.debug(e.getMessage());
		}
		LOGGER.debug("<- suspendEmvFlow() ");
	}
}
