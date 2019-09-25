package com.trustcommerce.ipa.dal.common.scripts;

import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.log4j.Logger;

public class ResultHandler extends DefaultExecuteResultHandler {

	private static final Logger logger = Logger.getLogger(ResultHandler.class);
	private ExecuteWatchdog watchdog;

	public ResultHandler(ExecuteWatchdog watchdog) {
		this.watchdog = watchdog;
	}

	public ResultHandler(int exitValue) {
		super.onProcessComplete(exitValue);
	}

	public void onProcessComplete(int exitValue) {
		super.onProcessComplete(exitValue);
		logger.info("Process completed with return value " + exitValue);
	}

	public final void onProcessFailed(final ExecuteException e) {
		super.onProcessFailed(e);
		if (watchdog != null && watchdog.killedProcess()) {
			logger.warn("The process timed out");
		} else {
			logger.error("The process failed");
		}
	}
}
