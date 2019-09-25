package com.trustcommerce.ipa.dal.commport.ports;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.commport.exceptions.PortConnectionException;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfiguration;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.constants.device.TerminalModel;

public class PortCommunicator {
	

	private static final Logger LOGGER = LoggerFactory.getLogger(PortCommunicator.class);

	private static PortUtils portUtil;

	/**
	 * Returns the model of the Ingenico terminal connected to the workstation.
	 * @return type TerminalModel i.e. iSC250, iSC480 ...
	 * @throws PortConnectionException if multiple ingenico devices are connected to this workstation
	 */
	public static TerminalModel getConnectedDevice() throws PortConnectionException {
		// get the device name from the port.
		portUtil = new PortUtils();
		TerminalModel model = null;

		String deviceName = portUtil.findConnectedIngenicoDevice();
		if (deviceName == null) {
			return null;
		}
		model = TerminalModel.valueOf(deviceName);
		
		if(model.name().equalsIgnoreCase("iPP350")) {
			try {
				ClientConfiguration configuration = ClientConfigurationUtil.getConfiguration();
				String lastConnectedDevice = configuration.getLastConnectedDevice();
				if(lastConnectedDevice.length() > 0) {
					TerminalModel localMsrmodel = TerminalModel.valueOf(lastConnectedDevice); 
					if(localMsrmodel.name().equalsIgnoreCase("iPP320")) {
						model = TerminalModel.valueOf(lastConnectedDevice);
					}
				}
			} catch(Exception e) {
				LOGGER.error("findConnectedIngenicoDevice exception=", e.getMessage());
			}
		}
		
		LOGGER.info("deviceName: {} ", model.name());
		return model;
	}
	
	public static TerminalModel getConnectedDevice(String device) throws PortConnectionException {
		
		// get the device name from the given name.
		String deviceName = device;
		if (deviceName == null) {
			return null;
		}
		
		TerminalModel model = TerminalModel.valueOf(deviceName);
		LOGGER.info("deviceName: {} ", model.name());
		
		return model;
	}
	
	public static void disconnect() {
//		portUtil.disconnect();
	}

}
