package com.trustcommerce.ipa.dal.logger;

import java.io.File;

import com.trustcommerce.ipa.dal.constants.paths.TcipaFiles;

public class Paths {


	private static final String TC_LOGS = "C:\\TrustCommerce\\logs";

	static final String LOG_FILE_NAME = "jdal_telium.log";
	static final String CONFIG_FOLDER = "jDAL\\config";

	// "C:/ProgramData/TrustCommerce/TCIPA/logs/jpos_telium.log"
	public static final String getLogsPath() {
		return TC_LOGS;
	}

	// EpicConfiguration.getTCIPAHome() + File.separatorChar + LOG4J_DEV
	static final String getLog4jFilesPath() {
		final StringBuilder sb = new StringBuilder();
		sb.append(Utils.getTCIPAHome());
		sb.append(File.separatorChar);
		sb.append(CONFIG_FOLDER);
		return sb.toString();
	}

	// EpicConfiguration.getTCIPAHome() + File.separatorChar + LOG4J_DEV
	public static final String getJREPath() {
		final StringBuilder sb = new StringBuilder();
		sb.append(Utils.getTCIPAHome());
		sb.append(File.separatorChar);
		sb.append(TcipaFiles.INGENICO_FOLDER);
		sb.append(File.separatorChar);
		sb.append("jre7");
		sb.append(File.separatorChar);
		return sb.toString();
	}

}
