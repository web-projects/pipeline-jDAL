package com.trustcommerce.ipa.dal.uploader;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.logger.Paths;
import com.trustcommerce.ipa.dal.model.fileUpload.Upload;
import com.trustcommerce.ipa.dal.model.fileUpload.UploadInputData;
import com.trustcommerce.ipa.dal.uploader.constants.UploaderConstants;
/** */
public class FileUploadUtil {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadUtil.class);
	
	/**
	 * 1 for form, 2 for firmware.
	 * @param packageTypeId int
	 * @return Upload
	 */
	public static Upload getFormsUploadObject(final UploadInputData inputData) {
		
		
		final Upload p = new Upload();
		
		p.setFileName(inputData.getFirmwareName());
		
		p.setPackageTypeID(inputData.getProcessId());

		return p;
	}
	
	public static void createFileUploadErrorFile(final EntryModeStatusID statusId, final String message) {

		PrintWriter out = null;
		final String errorPath = Paths.getLogsPath() + "\\" + UploaderConstants.FIRMWARE_UPLOAD_ERROR;
		try {
			out = new PrintWriter(errorPath);
			out.println(statusId.getCode());
			out.println(getTodaysDate());
			out.println(message);
		} catch (Exception e) {
			LOGGER.error("  Error creating error file" );
			// temp file: " + e.getMessage());
			return;
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}
	
	
	private static String getTodaysDate() {
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);  
	}
	
	

	
}
