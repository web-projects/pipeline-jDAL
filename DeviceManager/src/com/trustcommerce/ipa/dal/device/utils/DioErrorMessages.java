package com.trustcommerce.ipa.dal.device.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.configuration.app.AppConfiguration;

import jpos.JposException;

public class DioErrorMessages {

	private static final Logger LOGGER = LoggerFactory.getLogger(DioRunFile.class);

	public static String getErrorMessage(final String action, final JposException e) {
		LOGGER.info("{}  Code: {} ", e.getMessage(), e.getErrorCode());
		final StringBuilder sb = new StringBuilder();
		String temp = null;

		switch (e.getErrorCode()) {

		case 113:
			LOGGER.debug("Device is busy");
			temp = AppConfiguration.getLanguage().getString("JPOS_ERROR_E_BUSY_113");
			break;
		case 101:
			LOGGER.debug("Device is already closed");
			temp = AppConfiguration.getLanguage().getString("JPOS_ERROR_E_CLOSED_101");
			break;
		case 102:
			LOGGER.debug("Device is already claimed");
			temp = AppConfiguration.getLanguage().getString("JPOS_ERROR_E_CLAIMED_102");
			break;
		case 115:
			LOGGER.debug("Deprecated operation");
			temp = AppConfiguration.getLanguage().getString("JPOS_ERROR_E_DEPRECATED_115");
			break;
		case 105:
			LOGGER.debug("Device is disabled");
			temp = AppConfiguration.getLanguage().getString("JPOS_ERROR_E_DISABLED_105");
			break;
		case 110:
			LOGGER.debug("File alreday exists");
			temp = AppConfiguration.getLanguage().getString("JPOS_ERROR_E_EXISTS_110");
			break;
		case 111:
			LOGGER.debug("Device may be rebooting ");
			temp = AppConfiguration.getLanguage().getString("JPOS_ERROR_E_FAILURE_111");
			break;
		case 106:
			LOGGER.debug("Illegal operation performed");
			temp = AppConfiguration.getLanguage().getString("JPOS_ERROR_E_ILLEGAL_106");
			break;
		case 109:
			LOGGER.debug("jpos.xml file does not contain entry for signature capture");
			temp = AppConfiguration.getLanguage().getString("JPOS_ERROR_E_NOEXIST_109");
			break;
		case 107:
			LOGGER.debug("Device not connected ");
			temp = AppConfiguration.getLanguage().getString("JPOS_ERROR_E_NOHARDWARE_107");
			break;
		case 104:
			LOGGER.debug("Configuration Error");
			temp = AppConfiguration.getLanguage().getString("JPOS_ERROR_E_NOSERVICE_104");
			break;
		case 103:
			LOGGER.debug("Claim the device before using");
			temp = AppConfiguration.getLanguage().getString("JPOS_ERROR_E_NOTCLAIMED_103");
			break;
		case 108:
			LOGGER.debug("Device is offline");
			temp = AppConfiguration.getLanguage().getString("JPOS_ERROR_E_OFFLINE_108");
			break;
		case 112:
			LOGGER.debug("Timeout Occured");
			temp = AppConfiguration.getLanguage().getString("JPOS_E_ERROR_TIMEOUT_112");
			break;

		default:
			temp = " Code: " + e.getErrorCode()
			        + AppConfiguration.getLanguage().getString("JPOS_ERROR_E_NOSERVICE_104");

		}
		temp = String.format(temp, action + ":" + e.getErrorCode());
		return temp;

	}

}
