package com.trustcommerce.ipa.dal.uploader.constants;

import com.trustcommerce.ipa.dal.logger.Paths;

public class UploaderConstants {

	private static final String LOG4J = "log4j_fileUpload.xml";

	public static final String LOG4J_XML_PATH = "C:\\TrustCommerce\\jDAL\\config\\" + LOG4J;

	public static final String FIRMWARE_UPLOAD_ERROR = "upload_firmware_error.txt";
	
	public static final String FORMS_UPLOAD_ERROR = "upload_forms_error.txt";

	static final String LOG_FILE_NAME = "file_uploader.log";
	
	public static final String LOG_TITLE = "TrustCommerce File Uploader tool";
	/** Firmware upload results file name. */
	public static final String FILEUPLOADER_RESULTS_NAME = "fileUploaderResults.txt";
	/** Firmware upload results file path. */
	public static final String FILEUPLOADER_RESULTS_PATH = Paths.getLogsPath() + "\\" + FILEUPLOADER_RESULTS_NAME;
	/** Firmware upload results file name. */
	public static final String SIMPLE_FILEUPLOADER_RESULTS_NAME = "formsUploadResults.txt";
	/** Firmware upload results file path. */
	public static final String SIMPLE_FILEUPLOADER_RESULTS_PATH = Paths.getLogsPath() + "\\" + SIMPLE_FILEUPLOADER_RESULTS_NAME;

	
 


}
