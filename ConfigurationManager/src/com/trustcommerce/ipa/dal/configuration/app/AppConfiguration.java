package com.trustcommerce.ipa.dal.configuration.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.configuration.types.ConfigConsts;
import com.trustcommerce.ipa.dal.configuration.types.ConfigurationException;

public class AppConfiguration {

	private static Properties props;

	private static void initialize() throws FileNotFoundException {

		/*
		 * if (props != null) { // properties are already set return; }
		 */
		props = new Properties();
		InputStream inputStream = null;
		final String propsFileName = ClientConfigurationUtil.getTCIPAHome() + ConfigConsts.JDAL_CONFIG_FILE;

		final File f = new File(propsFileName);
		if (!f.exists()) {
			throw new FileNotFoundException("property file '" + propsFileName + "' not found in the classpath");
		}

		try {
			// inputStream =
			// getClass().getClassLoader().getResourceAsStream(propsFileName);
			inputStream = new FileInputStream(f);

			if (inputStream != null) {
				props.load(inputStream);
			}
			if (inputStream != null) {
				inputStream.close();
			}

		} catch (IOException e) {
			System.out.println("Exception: " + e);
			throw new FileNotFoundException("property file '" + propsFileName + "' not found in the classpath");
		} finally {

		}
	}

	public static String getProperty(AppPropertyKeys key) {
		try {
			initialize();
		} catch (FileNotFoundException e) {
			return null;
		}
		return props.getProperty(key.name());

	}

	public static String getProperty(String keyName) {
		try {
			initialize();
		} catch (FileNotFoundException e) {
			return null;
		}
		return props.getProperty(keyName);

	}

	public static String getProperty(AppPropertyKeys key, boolean isRequired) throws ConfigurationException {
		try {
			initialize();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final String temp = props.getProperty(key.name());
		if (temp == null || temp.isEmpty()) {
			throw new ConfigurationException(" Bad configuration file. Missing property " + key.name());
		}
		return temp;

	}
	
	
	/**
	 * 
	 * @param keyName 
	 * @return Integer
	 */
	public static Integer getPropertyasInteger(String keyName) {
		try {
			initialize();
		} catch (FileNotFoundException e) {
			return null;
		}
		final String temp = props.getProperty(keyName);
		int val = 0;
		if (temp != null) {
			try {
				Integer.parseInt(temp);
			} catch (Exception e) {
				
			}
		}
		return val;
	}

	/**
	 * Returns the value of the configuration DownloadForms.
	 * 
	 * @throws FileNotFoundException
	 * 
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public static boolean getDownloadForms() {
		final boolean result = false;
		try {
			initialize();
			final String temp = props.getProperty(AppPropertyKeys.DownloadForms.name());
			if (temp == null || temp.isEmpty()) {
				return result;
			}
			return Boolean.parseBoolean(temp);
		} catch (Exception e1) {
			// Ignore it. Just use prod as default
			return result;
		}
	}

	/**
	 * Returns the value of the configuration DownloadForms.
	 * 
	 * @throws FileNotFoundException
	 * 
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public static boolean performSignatureCapture() {
		final boolean result = false;
		try {
			initialize();
			final String temp = props.getProperty(AppPropertyKeys.UseSignatureCapture.name());
			if (temp == null || temp.isEmpty()) {
				return result;
			}
			return Boolean.parseBoolean(temp);
		} catch (Exception e1) {
			// Ignore it. Just use prod as default
			return result;
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
	public static Map<String, String> getTrustCommerceIngenicoPorts() throws FileNotFoundException {

		initialize();
		final Map<String, String> ports = new HashMap<String, String>();
		final List<String> devices = new ArrayList<String>();
		final String deviceList = props.getProperty("DeviceList");
		if (deviceList == null) {
			// Config problems, config file needs to be updated!!!
			return null;
		}
		devices.addAll(Arrays.asList(props.getProperty("DeviceList").split(",")));

		for (String device : devices) {
			final String temp = props.getProperty(device);
			ports.put(device, temp);
		}
		return ports;
	}

	/**
	 * Get the language and country from the App configuration file.
	 * 
	 * @return currentLocale
	 */
	public static ResourceBundle getLanguage() {
		try {
			initialize();
			Locale currentLocale = null;

			final String language = AppConfiguration.getProperty(AppPropertyKeys.Language.name());
			final String country = AppConfiguration.getProperty(AppPropertyKeys.Country.name());

			if (language == null || language.isEmpty() || country == null || country.isEmpty()) {
				currentLocale = AppConfigConstants.A_LOCALE;
			} else {
				currentLocale = new Locale(language, country);
			}
			return ResourceBundle.getBundle("MessagesBundle", currentLocale);
		} catch (Exception e1) {
			// Ignore it. Just use prod as default
			return AppConfigConstants.MESSAGES;
		}
	}
}
