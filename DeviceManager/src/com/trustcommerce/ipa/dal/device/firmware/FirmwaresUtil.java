package com.trustcommerce.ipa.dal.device.firmware;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FirmwaresUtil {

	private static Properties props;

	private static void initialize(File f) throws FileNotFoundException {

		if (props != null) { // properties are already set return;

		}

		props = new Properties();

		try {
			InputStream inputStream = null;
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
			throw new FileNotFoundException("property file not found in the classpath");
		} finally {

		}
	}

	public static String getProperty(String keyName, File f) {
		try {
			initialize(f);
		} catch (FileNotFoundException e) {
			return null;
		}
		return props.getProperty(keyName);

	}

}
