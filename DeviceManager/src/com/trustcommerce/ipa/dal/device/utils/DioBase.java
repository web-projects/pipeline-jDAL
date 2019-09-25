package com.trustcommerce.ipa.dal.device.utils;

import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;

import jpos.BaseControl;
import jpos.JposException;

public class DioBase {

	
	protected static final String MSR_NULL = "MSR is not available";
	
	
	
	/**
	 * 
	 * @param baseControl BaseControl type
	 * @throws JposException when "Control not opened" = 101
	 * @throws IngenicoDeviceException when the MSR happens to be null (rare situation)
	 */
	protected static void getMsrReady(final BaseControl baseControl) throws JposException, IngenicoDeviceException {
		if (baseControl == null) {
			throw new IngenicoDeviceException(MSR_NULL, EntryModeStatusID.MSRIsCloseOrNull.getCode());
		}
		if (!baseControl.getClaimed()) {
			final int timeout = ClientConfigurationUtil.getMsrTimeout();
			baseControl.claim(timeout);
		}
		if (!baseControl.getDeviceEnabled()) {
			baseControl.setDeviceEnabled(true);
		}
	}

}
