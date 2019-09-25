package com.trustcommerce.ipa.dal.logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

public final class FileIOUtils {

	public static void saveDataToTempFile(final String message, final String fileName) {

		PrintWriter out = null;
		final String formsPath = Paths.getLogsPath() + "\\" + fileName;
		try {
			out = new PrintWriter(formsPath);
			out.println(message);
		} catch (Exception e) {
			// logger.trace("********** saveCcDataToTempFile() Error creating
			// temp file: " + e.getMessage());
			return;
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}

	public static void appendToFile(final String message, final String fileName) {
		PrintWriter out = null;
		final String formsPath = Paths.getLogsPath() + "\\" + fileName;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(formsPath, true)));
			out.println(message);
		} catch (Exception e) {
			// logger.trace("********** saveCcDataToTempFile() Error creating
			// temp file: " + e.getMessage());
			return;
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}

	public static void saveDataToTempFile(final int message, final String fileName) {

		PrintWriter out = null;
		final String formsPath = Paths.getLogsPath() + "\\" + fileName;
		try {
			out = new PrintWriter(formsPath);
		} catch (Exception e) {
			// logger.trace("********** saveCcDataToTempFile() Error creating
			// temp file: " + e.getMessage());
			return;
		}

		out.println(message);

		out.flush();
		out.close();
	}

}
