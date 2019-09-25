package com.trustcommerce.ipa.dal.device.interfaces;

import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;
import com.trustcommerce.ipa.dal.exceptions.TerminalInfoException;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.model.Terminal;

public interface TerminalInfoProcessor {

	/**
	 * Disables the current setting in the MSR without closing it.
	 */
	void releaseDevice();

	Terminal getTerminalInfo();
	
	/**
	 * Sends an jPOS event requesting the version of the forms package currently
	 * installed in the device connected to the workstation.
	 */
	void setEventListener(DeviceEventListener newListener);

	void requestTerminalInformation() throws TerminalInfoException, IngenicoDeviceException;
	
	void setTerminalEmvKey(final String key);
	
	void setEncryptionConfiguration(final int value);
	
	/**
	 * 
	 * @param kernelVersion
	 */
	void setKernelVersion(final String kernelVersion);
	
	/**
	 * 
	 * @param value
	 */
	void setDukptKeyStatus(final String value);
	
	/**
	 * Request serial number
	 * @throws TerminalInfoException
	 * @throws IngenicoDeviceException
	 */
	void requestSerialNumber() throws TerminalInfoException, IngenicoDeviceException;
	
	String getSerialNumber();
	
	void setSerialNumber(String val);
	
	void requestEncryptionConfiguration();

}
