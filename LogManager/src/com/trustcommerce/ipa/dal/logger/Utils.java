package com.trustcommerce.ipa.dal.logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.constants.global.JdalVersion;
import com.trustcommerce.ipa.dal.constants.paths.TcipaFiles;

public final class Utils {

	private static final Logger LOGGER = Logger.getLogger(Utils.class);



	public static void logHeader(final String title) {

		LOGGER.info("******************************************************");
		LOGGER.info("              " + title + "                        ");
		LOGGER.info("******************************************************");
		LOGGER.info(" ");

		final String path = getTCIPAHome();
		LOGGER.info("       HOME_DRIVE: " + System.getenv("HOMEDRIVE"));
		LOGGER.info("       TC_HOME: " + System.getenv("TC_HOME"));
		LOGGER.info("       TC INSTALLATION: " + path);
		LOGGER.info("       JRE_PATH: " + System.getenv("JRE_PATH"));
		LOGGER.info("       TCIPA version : " + JdalVersion.JDAL_VERSION);
		LOGGER.info("       Internal version : " + JdalVersion.JDAL_INTERNAL_VERSION);
		// For trouble shooting purposes ...
		logInvironmentVariables();
	}

	private static void logInvironmentVariables() {
		// Local environment properties
		LOGGER.info("COMPUTERNAME=" + System.getenv("COMPUTERNAME"));
		LOGGER.info("USERDOMAIN=" + System.getenv("USERDOMAIN"));
		LOGGER.info("USERNAME=" + System.getenv("USERNAME"));
		LOGGER.info("OS=" + System.getenv("OS"));
		LOGGER.info("PROCESSOR_ARCHITECTURE=" + System.getenv("PROCESSOR_ARCHITECTURE"));
	}

	/**
	 * Creates the trustcommerce log path.
	 */
	public static final void createTcipaLogFolder() {
		final String logDir = TcipaFiles.getLogsPath();
		final Path confDir = Paths.get(logDir);

		if (Files.notExists(confDir)) {
			try {
				LOGGER.info("Creating " + logDir);
				Files.createDirectory(confDir);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Returns the value of the environment variable TCIPA_HOME.
	 * 
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public static String getTCIPAHome() {
		String tcipaHome = null;
		final String temp = System.getenv(GlobalConstants.TC_HOME);
		if (temp == null) {
			//logger.error("TC_HOME Environment Variable not set!!!!!!!");
			tcipaHome = GlobalConstants.DEFAULT_TC_HOME;
		} else {
			tcipaHome = temp;
		}
		return tcipaHome;
	}


}
