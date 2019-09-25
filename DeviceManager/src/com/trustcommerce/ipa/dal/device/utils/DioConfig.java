package com.trustcommerce.ipa.dal.device.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.api.jpos.IngenicoMSR;
import com.ingenico.jpos.IngenicoConst;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;

import jpos.JposException;


/**
 * Use for the command ING_DIO_CONFIG
 * @author luisa.lamore
 *
 */
public class DioConfig extends DioBase{

	/** Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DioRunFile.class);

	
	/**
	 * Sends a DirectIO to command 11.
	 * 
	 * @param value
	 * @throws JposException
	 * @throws IngenicoDeviceException when the MSR happens to be null (rare situation)
	 */
	public static void injectEmvKey(final IngenicoMSR msr, int value) throws JposException, IngenicoDeviceException {
		LOGGER.info("-> injectEmvKey() " + value);
		if (msr == null) {
			throw new IngenicoDeviceException(MSR_NULL, EntryModeStatusID.MSRIsCloseOrNull.getCode());
		}
		if (msr.getDeviceEnabled()) {
			msr.setDeviceEnabled(false);
		}
		if (!msr.getClaimed()) {
			msr.claim(ClientConfigurationUtil.getMsrTimeout());
		}
		final String temp = "emv:Integer:" + value;
		msr.directIO(IngenicoConst.ING_DIO_CONFIG, new int[] {0}, temp);
		if (!msr.getDeviceEnabled()) {
			msr.setDeviceEnabled(true);
		}
	}
	
	
	
}
