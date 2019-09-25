package com.trustcommerce.ipa.dal.logger;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;

import com.trustcommerce.ipa.dal.logger.utils.ConfigurationUtil;
import com.trustcommerce.ipa.dal.logger.utils.Environment;



public class LoggerConfig {

	private static final String LOG4J_DEV = "log4j_dev.xml";
	private static final String LOG4J_PROD = "log4j_prod.xml";

	public static void startLogger() {
		configureLogDOM();
	}

	public static void startLogger(String log4jxmlFilePath) {
		configureLogDOM(log4jxmlFilePath);
	}

	/**
	 * 
	 * @param mode
	 * @param hostType
	 * @return String type
	 */
	private static String getLog4jFileName() {
		 
		String environmentType = null;
		
		Environment environment = ConfigurationUtil.getConfiguration();
		if (environment == null || environment.getEnvironmentType() == null) {
			environmentType = "prod";
		} else {
			environmentType = environment.getEnvironmentType();
		}
		
 
		// default configuration should be prod
		if (environmentType != null && environmentType.equalsIgnoreCase("dev")) {
			return Paths.getLog4jFilesPath() + File.separatorChar + LOG4J_DEV;
		} else {
			return Paths.getLog4jFilesPath() + File.separatorChar + LOG4J_PROD;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static List<String> getAvailableLogFilesList() {
		final String logFolderName = Paths.getLogsPath();
		final File logDir = new File(logFolderName);
		final List<String> filesList = new ArrayList<String>();
		if (logDir.exists()) {
			// Need to build the list of files required
			final FilenameFilter logFilter = new FilenameFilter() {

				public boolean accept(final File dir, final String name) {
					return name.endsWith(Paths.LOG_FILE_NAME);
				}
			};
			// Get files in this directory
			final File[] entries = logDir.listFiles(logFilter);
			if (null != entries) {
				// Go over entries
				for (File entry : entries) {
					filesList.add(entry.getAbsolutePath());
				}
			}
		}
		return filesList;
	}

	/**
	 * Selects the log file to use for all plugins.
	 * 
	 * @param mode
	 *            ApplicationMode
	 * @param hostType
	 *            HardwareHostType
	 */
	private static void configureLogDOM() {

		final String logName = getLog4jFileName();
		final File confFile = new File(logName);

		if (confFile.exists()) {
			// Configure the logger
			try {
				DOMConfigurator.configure(logName);
				DefaultLoggerInitializer.initSyslog();
			} catch (Exception e) {
				// LOGGER.warn("Failed to initialize the logger");
			}
		}
	}

	/**
	 * Selects the log file to use for all plugins.
	 * 
	 * @param mode
	 *            ApplicationMode
	 * @param hostType
	 *            HardwareHostType
	 */
	private static void configureLogDOM(String logName) {

		final File confFile = new File(logName);

		if (confFile.exists()) {
			// Configure the logger
			try {
				DOMConfigurator.configure(logName);
				DefaultLoggerInitializer.initSyslog();
			} catch (Exception e) {
				// LOGGER.warn("Failed to initialize the logger");
			}
		}
	}

}