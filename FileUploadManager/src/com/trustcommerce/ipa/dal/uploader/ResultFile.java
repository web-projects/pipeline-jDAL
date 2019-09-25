package com.trustcommerce.ipa.dal.uploader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.trustcommerce.ipa.dal.uploader.constants.UploaderConstants;


/**
 * File generated after a firmware upload has completed.
 * This file is currently located under [TC_HOME]/logs.
 * @author luisa.lamore
 *
 */
public class ResultFile {


	/** The file class. */
	private File resultFile;
	/** The file class. */
	private String filePath;
	/** The Process type can be either firmware or forms. */
	private final int processType;
	
	
	/**
	 * Constructor.
	 * @param processtype Firmware or forms.
	 */
	public ResultFile(final int processtype) {
		this.processType = processtype;
		if (processType == 7 || processType == 9 ) {
			filePath = UploaderConstants.FILEUPLOADER_RESULTS_PATH;
			resultFile = new File(UploaderConstants.FILEUPLOADER_RESULTS_PATH);
		} else {
			filePath = UploaderConstants.SIMPLE_FILEUPLOADER_RESULTS_PATH;
			resultFile = new File(UploaderConstants.SIMPLE_FILEUPLOADER_RESULTS_PATH);
		}
		// write the date in the first line
		writeHeader();
	}
	
	public void deleteFile() {
		
		File file = null;
		if (processType == 7) {
			file = new File(UploaderConstants.FILEUPLOADER_RESULTS_PATH);
		} else {
			file = new File(UploaderConstants.SIMPLE_FILEUPLOADER_RESULTS_PATH);
		}
		// delete the output file
		if (file.exists()) {
			file.delete();
		}
		writeHeader();
	}
	
	
	private void writeHeader() {
 
		if (!resultFile.exists()) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			// ie. 2016/11/16 12:08:43
			writeToFile(dateFormat.format(date));
		}
	}
	
	public void append(String message) {

		appendToFile(message);
	}

	
	private void writeToFile(final String message) {

		PrintWriter out = null;
	 
		try {
			out = new PrintWriter(filePath);
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
	
	
	private void appendToFile(final String message) {
		PrintWriter out = null;

		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
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

}
