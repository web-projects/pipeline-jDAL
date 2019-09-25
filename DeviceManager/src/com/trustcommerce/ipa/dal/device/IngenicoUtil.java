package com.trustcommerce.ipa.dal.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * serial.
 * 
 * @author tc
 *
 */
public final class IngenicoUtil {

	/** SLF4J. */
	private static final Logger LOGGER = LoggerFactory.getLogger(IngenicoUtil.class);

	/**
	 * 
	 * @param bytes
	 * @return
	 */
	public static String asciiBytesToString(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return "";
		}
		final char[] result = new char[bytes.length];
		for (int i = 0; i < bytes.length; ++i) {
			result[i] = (char) bytes[i];
		}

		return new String(result);

	}

	/**
	 * 
	 * @param response
	 * @return String
	 */
	public static String extractGetResult(String response) {
		if (response == null || response.length() == 0) {
			return new String("");
		}
		final int beginIndex = response.indexOf("=");
		if (beginIndex != -1) {
			final String result = response.substring(beginIndex + 1, response.length());
			return result;
		} else {
			return "";
		}
	}

	/**
	 *  
	 * @param encryptedPIN String encrypted PIN
	 * @return String
	 */
	public static String getPinBlock(final String encryptedPIN) {
		if (encryptedPIN.startsWith("FFFF")) {
			return encryptedPIN.substring(4);
		} else {
			return encryptedPIN;
		}

	}

	// TODO: Modify this to just take the binary data
	public static String hexToAscii(final String hexValue) {
		LOGGER.debug("-> hexToAscii()");
		final StringBuilder output = new StringBuilder();
		for (int i = 0; i < hexValue.length(); i += 2) {
			final String str = hexValue.substring(i, i + 2);
			output.append((char) Integer.parseInt(str, 16));
		}
		return output.toString();
	}

}
