package com.trustcommerce.ipa.dal.configuration.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import com.trustcommerce.ipa.dal.configuration.types.ConfigConsts;
import com.trustcommerce.ipa.dal.configuration.types.ConfigurationException;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;

/**
 * Obtain the values from the clientConfig.json file.
 * 
 * @author luisa.lamore
 *
 */
public class ClientConfigurationUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientConfigurationUtil.class);

	private static final int DEFAUlT_AMOUNT_IN_CENTS = 2500;

	private static final String DELIMITER = ":";

	private static ClientConfiguration configuration;

	private static String tcipaHome;
	/** map with AID and threshold amount in cents. */
	private static Map<String, String> thresholds;

	public static ClientConfiguration get() {
		return configuration;
	}

	/**
	 * 
	 * @return Type ClientConfiguration.
	 * @throws ConfigurationException
	 */
	public static ClientConfiguration getConfiguration() throws ConfigurationException {
		LOGGER.debug("-> getClientConfiguration()");
		if (configuration != null) {
			return configuration;
		}
		thresholds = new HashMap<String, String>();
		InputStream fis = null;
		try {
			fis = new FileInputStream(getTCIPAHome() + ConfigConsts.CLIENT_CONFIG_FILE_LOCATION);
		} catch (FileNotFoundException e) {
			throw new ConfigurationException("Configuration file does not exist");
		}

		final ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS, false);

		try {
			configuration = mapper.readValue(fis, ClientConfiguration.class);
			setThresholds();
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			throw new ConfigurationException("Problems reading client Configuration file");
		}
		LOGGER.debug("<- getClientConfiguration() {} ", configuration.toString());
		return configuration;
	}
	
	/**
	 * Resets the configuration so fresh values can be taken out fo the configuration file.
	 */
	public static void resetConfiguration() {
		configuration = null;
	}

	/**
	 * Reads the AIDs from the configuration file and updates the threshold Map.
	 */
	private static void setThresholds() {
		final String temp = configuration.getThresholds();
		if (temp == null || temp.isEmpty()) {
			LOGGER.warn("Threasholds not found in the config file. Default value will be used");
			return;
		}
		final String[] tempArray = temp.split(",");

		for (String aid : tempArray) {
			final String[] keyVal = aid.split(DELIMITER);
			// add to the map the threshold amount in cents
			final String amount = keyVal[1].replace(".", "");
			thresholds.put(keyVal[0], amount);
		}
	}

	/**
	 * Returns the value of the environment variable TCIPA_HOME.
	 * 
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public static String getTCIPAHome() {
		if (tcipaHome == null) {
			setTCIPAHome();
		}
		return tcipaHome;
	}

	/**
	 * Returns the value of the environment variable TCIPA_HOME.
	 * 
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	private static void setTCIPAHome() {

		final String temp = System.getenv(GlobalConstants.TC_HOME);
		if (temp == null) {
			LOGGER.error("TC_HOME Environment Variable not set!!!!!!!");
			tcipaHome = GlobalConstants.DEFAULT_TC_HOME;
		} else {
			tcipaHome = temp;
		}
	}

	/**
	 * Returns the threshold amount for the corresponding application AID.
	 * 
	 * @param issuer
	 *            AID
	 * @return type String
	 */
	public static int getThresholdForAID(final String aid) {

		final int defaultAmount = DEFAUlT_AMOUNT_IN_CENTS;
		if (thresholds == null || thresholds.isEmpty()) {
			return defaultAmount;
		}
		if (thresholds.containsKey(aid)) {
			return Integer.parseInt(thresholds.get(aid));
		} else {
			return defaultAmount;
		}
	}

	public static int getMsrTimeout() {
		int timeout = ConfigConsts.DEFAULT_MSR_TIMEOUT;
		try {
			timeout = getConfiguration().getMsrTimeout();
		} catch (ConfigurationException e) {
		}
		return timeout;
	}

}
