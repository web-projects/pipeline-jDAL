package com.trustcommerce.ipa.dal.common.scripts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Logger;

public class ExternalScriptsUtil {

	private static final Logger logger = Logger.getLogger(ExternalScriptsUtil.class);
	private static final String EXCEPTION_EXTERNALSCRIPT = "Failed to run script {0}";
	private static final String EXCEPTION_FILEDONOTEXISTS = "Script file {0} do not exists";

	/**
	 * 
	 * @param script
	 * @param map
	 * @param timeout
	 * @param runInBackground
	 * @param rootRequired
	 * @param args
	 * @return ResultOutput
	 * @throws ExternalScriptException
	 * @throws ToolkitFileDoNotExistException
	 */
	public static ResultOutput runScript(final String script, Map map, int timeout, boolean runInBackground,
	        final boolean rootRequired, String... args) throws ExternalScriptException {
		logger.debug("-> runScript  " + script);
		String fullcmd;
		if ((null == script) || (!doesFilesExist(script))) {
			logger.error(EXCEPTION_FILEDONOTEXISTS);
			throw new ExternalScriptException(EXCEPTION_FILEDONOTEXISTS + script);
		}
		// The installation package can only be installed by someone with root priviledges
		if (rootRequired) {
			fullcmd = "su " + script;
		} else {
			fullcmd = script;
		}
		for (String parameter : args) {
			fullcmd = fullcmd + " " + parameter;
		}
		logger.debug(" runScript() command " + fullcmd);
		if (null != map) {
			return executeCommand(CommandLine.parse(fullcmd, map), timeout, runInBackground);
		} else {
			return executeCommand(CommandLine.parse(fullcmd), timeout, runInBackground);
		}
	}


	/**
	 * 
	 * @param script
	 * @param map
	 * @param timeout
	 * @param runInBackground
	 * @param rootRequired
	 * @param args
	 * @return ResultOutput
	 * @throws ExternalScriptException
	 * @throws ToolkitFileDoNotExistException
	 */
	public static ResultOutput runScript(final String script, String... args) throws ExternalScriptException {
		logger.debug("-> runScript  " + script);
		String fullcmd;
		if ((null == script) || (!doesFilesExist(script))) {
			logger.error(EXCEPTION_FILEDONOTEXISTS);
			throw new ExternalScriptException(EXCEPTION_FILEDONOTEXISTS + script);
		}
		fullcmd = script;
		
		for (String parameter : args) {
			fullcmd = fullcmd + " " + parameter;
		}
		logger.debug(" runScript() command " + fullcmd);
		return executeCommand(CommandLine.parse(fullcmd), 0, false);
		
	}
	
	/**
	 * 
	 * @param cmdLine
	 *            CommandLine
	 * @param timeout
	 *            int
	 * @param runInBackground
	 *            boolean
	 * @return ResultOutput
	 * @throws ExternalScriptException
	 */
	private static ResultOutput executeCommand(final CommandLine cmdLine, final int timeout,
	        final boolean runInBackground) throws ExternalScriptException {
		logger.debug("-> executeCommand():" + cmdLine);
		final ResultOutput output = new ResultOutput();
		final ResultHandler resultHandler;
		ExecuteWatchdog watchdog = null;
		final ByteArrayOutputStream outputStream;
		final PumpStreamHandler streamHandler;
		outputStream = new ByteArrayOutputStream();
		streamHandler = new PumpStreamHandler(outputStream);

		final DefaultExecutor executor = new DefaultExecutor();
		if (0 != timeout) {
			watchdog = new ExecuteWatchdog(timeout * 1000);
			executor.setWatchdog(watchdog);
		}
		try {
			if (runInBackground) {
				resultHandler = new ResultHandler(watchdog);
				executor.setStreamHandler(streamHandler);
				executor.execute(cmdLine, resultHandler);
			} else {
				executor.setStreamHandler(streamHandler);
				output.setExitResult(executor.execute(cmdLine));
				output.setStrResult(outputStream.toString());
			}
		} catch (ExecuteException e) {
			logger.error("ExecuteException  " + e.getMessage());
			throw new ExternalScriptException(e.getMessage() + EXCEPTION_EXTERNALSCRIPT 
					+ cmdLine.toString());
		} catch (IOException e) {
			logger.error("IOException  " + e.getMessage());
			throw new ExternalScriptException(e.getMessage() + EXCEPTION_EXTERNALSCRIPT 
					+ cmdLine.toString());
		}
		return output;
	}


	/**
	 * 
	 * @param filePath
	 *            String
	 * @return boolean
	 */
	private static boolean doesFilesExist(final String filePath) {
		return new File(filePath).exists();
	}
}
