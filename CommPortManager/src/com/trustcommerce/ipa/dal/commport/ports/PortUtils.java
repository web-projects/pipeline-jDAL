package com.trustcommerce.ipa.dal.commport.ports;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.comm.CommPortIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.commport.exceptions.PortConnectionException;
import com.trustcommerce.ipa.dal.commport.model.Port;
import com.trustcommerce.ipa.dal.commport.model.PortList;
import com.trustcommerce.ipa.dal.configuration.app.AppConfiguration;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfiguration;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.logger.Paths;
import com.trustcommerce.ipa.dal.commport.ports.PortReader;

/**
 * This utils need the comm.jar.
 * 
 * @author luisa.lamore
 *
 */
public class PortUtils  {


	 private static final Logger LOGGER = LoggerFactory.getLogger(PortUtils.class);

	/**
	 * Gets a list of all the COM ports currently connected to this workstation.
	 * reloading the driver for reflecting the port change
	 * 
	 * @return PortList
	 * @throws Exception
	 */
	public final PortList getCommPorts() throws PortConnectionException {
		LOGGER.debug("-> getCommPorts()");

		final PortList ports = new PortList();

		// reloading the driver for reflecting the port change
		try {
			final Field masterIdListField = CommPortIdentifier.class.getDeclaredField("masterIdList");
			masterIdListField.setAccessible(true);
			masterIdListField.set(null, null);

			final String temp = Paths.getJREPath() + "lib" + File.separator + "javax.comm.properties";
			final Method loadDriverMethod = CommPortIdentifier.class.getDeclaredMethod("loadDriver",
			        new Class[] { String.class });
			loadDriverMethod.setAccessible(true); // unprotect it
			loadDriverMethod.invoke(null, new Object[] { temp });
		} catch (Exception e) {
			LOGGER.debug("Exception Occurred {} " + e.getMessage());
		} finally {
			 
		}
		
		@SuppressWarnings("unchecked")
		final
		Enumeration<CommPortIdentifier> allPorts = CommPortIdentifier.getPortIdentifiers();

		while (allPorts.hasMoreElements()) {
			final CommPortIdentifier currentPort = (CommPortIdentifier) allPorts.nextElement();
			if (currentPort.getPortType() != CommPortIdentifier.PORT_SERIAL) {
				continue;
			}
			
			final String portName = currentPort.getName();
			if (currentPort.isCurrentlyOwned()) {
				LOGGER.debug("portName isCurrentlyOwned by {}", currentPort.getCurrentOwner());
			}
			LOGGER.debug("portName {}", portName);
			final int number = currentPort.getPortType();
			// The port sometimes shows an owner but sometimes is does not ...
			// TODO
			final String owner = currentPort.getCurrentOwner();
			LOGGER.debug("port owner {} ", owner);
			if (portName.startsWith("COM")) {
				ports.add(new Port(portName, owner, number));
			}

		}

		LOGGER.warn("<- getCommPorts() found the following COM ports: " + ports);
		return ports;
	}

	
	/**
	 * 
	 * @return
	 * @throws PortConnectionException if multiple Ingenico Devices are connected to the workstation.
	 */
	public String findConnectedIngenicoDevice() throws PortConnectionException {
		final List<String> device = new ArrayList<String>();
		final PortList usePorts = getCommPorts();

        String devicePID = PortReader.StreamReader.GetDevicePID();
		if(devicePID.startsWith("Exception")) {
			return null;
		}
		
		if (usePorts.isEmpty()) {
			return null;
		}
		final PortConfig portConfig = new PortConfig();
		final PortList configPorts = portConfig.getDefaultIngenicoPorts();

		for (Port port : configPorts.list()) {
			for (Port usePort : usePorts.list()) {
				if (port.getName().equalsIgnoreCase(usePort.getName()) && port.getPid().equalsIgnoreCase(devicePID)) {
					device.add(port.getDeviceName());
				}
			}
		}
		if (device.size() > 1) {
			throw new PortConnectionException(AppConfiguration.getLanguage().getString("MULTIPLE_DEVICES_DETECTED"));
		} else if (device.isEmpty()) {
			return null;
		}
		
		return device.get(0);
	}
	
	
//	/**
//	 * CommPortOwnershipListener
//	 * @param arg0
//	 */
//	@Override
//	public void ownershipChange(int arg0) {
//		// TODO Auto-generated method stub
//		if (PORT_OWNED == arg0) {
//			
//			String currentOwner = selectedPortIdentifier.getCurrentOwner();
//			logger.warn(" PORT_OWNED by " + currentOwner);
//		} else if (PORT_UNOWNED == arg0) {
//			logger.warn(" PORT_UNOWNED ");
//		} else if (PORT_OWNERSHIP_REQUESTED  == arg0) {
//			logger.warn(" PORT_OWNERSHIP_REQUESTED ");
//		}
//	}

//	@Override
//	public void serialEvent(SerialPortEvent arg0) {
//		// TODO Auto-generated method stub
//		
//	}

}
