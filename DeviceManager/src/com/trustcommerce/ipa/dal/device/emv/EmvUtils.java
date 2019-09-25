package com.trustcommerce.ipa.dal.device.emv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmvUtils {

    private static final Logger logger = LoggerFactory.getLogger(EmvUtils.class);

    public boolean processEmvTransaction(Emv emv) {
	boolean result = verifyLoadedFormVersion(emv.getLoadedFormVersion(), emv.getEmvFormsVersion());

	if (result) {
	    result = verifyLoadedFirmwareVersion(emv.getLoadedFirmwareVersion(), emv.getEmvFirmwareVersion());
	}
	return result;
    }

    private boolean verifyLoadedFormVersion(String loadedFormVersion, String expectedVersion) {
	logger.info("Verify form version");

	logger.debug("Expected form version: \"" + expectedVersion + "\"");
	logger.debug("Loaded form version: \"" + loadedFormVersion + "\"");

	boolean matched = expectedVersion.equals(loadedFormVersion);

	if (!matched) {
	    logger.debug("Loaded form doesn't match expected form version");

	} else {
	    logger.info("Latest form already loaded to device");
	}
	return matched;
    }

    private boolean verifyLoadedFirmwareVersion(String firmwareVersion, String loadedVersion) {

	if (firmwareVersion.isEmpty()) {
	    logger.warn("Firmware version or URL is not specified; skip firmware verification process.");
	    return true;
	}

	logger.info("Verify firmware version");

	String expectedVersion = firmwareVersion;

	if (loadedVersion == null) {
	    logger.warn("Loaded version not available; skip firmware verification process.");
	    return true;
	}

	logger.info("Expected firmware version: '" + expectedVersion + "'");
	logger.info("Loaded firmware version: '" + loadedVersion + "'");

	boolean matched = expectedVersion.equals(loadedVersion);

	if (!matched) {
	    logger.info("Loaded firmware doesn't match expected version");
	    final String text = "<html><center>Your POS firmware requires an update.<br><br>Loaded version: "
		    + loadedVersion + "<br>Required version: " + expectedVersion + "</center></html>";
	} else {
	    logger.info("Latest firmware already loaded to device");
	}

	return matched;
    }

}
