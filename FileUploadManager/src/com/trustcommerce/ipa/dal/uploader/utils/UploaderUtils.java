package com.trustcommerce.ipa.dal.uploader.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploaderUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(UploaderUtils.class);
	private static final String JRE_NAME = "TCIPAjDAL.exe";

	public static boolean ingenicoJreIsRunning() {

		LOGGER.info("-> ingenicoJreIsRunning()");
		int jres = 0;
		String line = null;
		String pidInfo = "";

		Process p = null;
		try {
			p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}

		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

		try {
			while ((line = input.readLine()) != null) {
				if (line.contains(JRE_NAME)) {
					LOGGER.info("Found JRE");
					jres++;
				}
			}
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (jres > 1) {
			return true;
		} else {
			return false;
		}
	}

}
