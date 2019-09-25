package com.trustcommerce.ipa.dal.device.emv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LogEMV {

	private static final Logger logger = LoggerFactory.getLogger(LogEMV.class);

	private static String emvDeviceStatus;

	/**
	 * i.e. 00ISE-----------SC--O------------
	 */
	public static void logEmvDeviceStatus(String status) {
		logger.debug("-> logEmvDeviceStatus() ");
		final int offset = 3;
		for (int i = 0; i < 31; i++) {
			boolean modified = false;
			int flag = i + 1;
			int index = offset + i;
			char value = status.charAt(index);
			if (index >= status.length())
				break;
			if (value == '-')
				continue;
			if (emvDeviceStatus != null && emvDeviceStatus.length() > index && emvDeviceStatus.charAt(index) != '-') {
				if (emvDeviceStatus.charAt(index) == value) {
					continue;
				} else {
					modified = true;
				}
			}
			String flagDesc = "";
			switch (flag) {
			case 9:
				if (value == 'R')
					flagDesc = "Amount confirmation rejected";
				break;
			case 10:
				if (value == 'L') {
					flagDesc = "last PIN try";
				}
				break;
			case 11:
				flagDesc = value == 'B' ? "PIN bypassed" : "PIN entered";
				break;
			case 16:
				if (value == 'C') {
					flagDesc = "transaction canceled";
				}
				break;
			case 18:
				if (value == 'B') {
					flagDesc = "card blocked";
				}
				break;
			case 20:
				if (value == 'R') {
					flagDesc = "card unexpectedly removed";
				}
				break;
			case 21:
				if (value == 'N') {
					flagDesc = "card not supported";
				}
				break;
			}

			if (!flagDesc.isEmpty()) {
				flagDesc = " (" + flagDesc + ")";
			}
			logger.info("flagDesc: " + flagDesc);

			logger.info((modified ? "Modified " : "") + "Flag " + flag + ": " + value + flagDesc);
		}
		emvDeviceStatus = new String(status);
		logger.debug("<- logEmvDeviceStatus() " + emvDeviceStatus);
	}

}
