package com.trustcommerce.ipa.dal.commport.ports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.ConfigurationException;

import com.trustcommerce.ipa.dal.commport.exceptions.PortConnectionException;
import com.trustcommerce.ipa.dal.commport.model.Port;
import com.trustcommerce.ipa.dal.commport.model.PortList;


public class PortConfig {

	private static final String PORT_CONFIG_NAME = "portConfig.properties";

	private static final String DEVICE_LIST = "DeviceList";

	private static Properties props;

	/**
	 * Gets the properties from the properties file.
	 * 
	 * @throws FileNotFoundException
	 */
	private void initialize() throws PortConnectionException {

		props = new Properties();
		InputStream inputStream = null;

		try {
			inputStream = getClass().getClassLoader().getResourceAsStream(PORT_CONFIG_NAME);

			if (inputStream != null) {
				props.load(inputStream);
			}
			if (inputStream != null) {
				inputStream.close();
			}

		} catch (IOException e) {
			System.out.println("Exception: " + e);
			throw new PortConnectionException("property file '" + PORT_CONFIG_NAME
			        + "' not found in the classpath");
		} finally {

		}
	}

	/**
	 * Returns the COM ports that will be setup for each Ingenico device.
	 * 
	 * @throws FileNotFoundException
	 * 
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public PortList getDefaultIngenicoPorts() throws PortConnectionException {

		final PortList ports = new PortList();
		final List<String> devices = getDeviceList();
		for (String device : devices) {
			final String temp = props.getProperty(device);
			ports.add(new Port(device, temp));
		}
		return ports;
	}

	/**
	 * Returns the COM ports that will be setup for each Ingenico device.
	 * 
	 * @throws FileNotFoundException
	 * 
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public final Map<String, String> getConfigurablePorts() throws PortConnectionException {

		final Map<String, String> ports = new HashMap<String, String>();
		final List<String> devices = getDeviceList();

		for (String device : devices) {
			final String temp = props.getProperty(device);
			ports.put(device, temp);
		}
		return ports;
	}

	private List<String> getDeviceList() throws PortConnectionException {
		initialize();

		final List<String> devices = new ArrayList<String>();
		final String deviceList = props.getProperty(DEVICE_LIST);
		if (deviceList == null) {
			// Config problems, config file needs to be updated!!!
			return null;
		}
		devices.addAll(Arrays.asList(props.getProperty(DEVICE_LIST).split(",")));
		return devices;
	}

	public void saveParamChanges(final List<Port> finalPortList) {

		final Properties props = new Properties();
		final File f = new File("ingenicoPorts2.properties");
		props.setProperty("DeviceList", "iSC250,iSC480,iPP350,iUP250");
		try {
			final OutputStream out = new FileOutputStream(f);
			for (Port p: finalPortList) {
				props.setProperty(p.getDeviceName(), p.getName());
			}
			props.store(out, "This is an optional header comment string");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
