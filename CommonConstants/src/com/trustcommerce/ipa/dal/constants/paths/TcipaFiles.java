package com.trustcommerce.ipa.dal.constants.paths;

public class TcipaFiles {

	/** Location of the ingenico files. */
    public static final String INGENICO_FOLDER = "\\devices\\ingenico\\";
    /** Forms folder. */
    public static final String FORMS_FOLDER = "forms";
    /** Firmware folder. */
    public static final String FIRMWARE_FOLDER = "firmware";
    /** Card information file. Only generated in DEV mode.*/
    public static final String CC_INFO_FILENAME = "PaymentResponse.tmp";
    /** File containing information about the ARQC/ARPC. Only generated in DEV mode.*/
    public static final String APP_CRYPTOGRAM = "arqcarpc.tmp";
    /** File containing information about the ARQC/ARPC. Only generated in DEV mode.*/
    public static final String EMV_FINAL_DATA = "emvFinalData.tmp";
    /** Contains the status code of the fileUpload. Only generated in DEV mode.*/
    public static final String TERMINAL_INFO = "terminalInfo.tmp";
    /** Contains the status code of the fileUpload. Only generated in DEV mode.*/
    public static final String FORMS_UPLOAD_FILENAME = "formsMessage.txt";
	/** Location of the jDAL/jPOS logs.*/
	public static final String LOG_FOLDER = "C:\\TrustCommerce\\logs";
	
	
	// Development environment constants
	
	public static final String IMAGE_EXTENSION = "jpg";
	public static final String IMAGE_NAME = "sc.jpg";

	
	//"C:/ProgramData/TrustCommerce/TCIPA/logs/jpos_telium.log"
	public static final String getLogsPath() {
		return LOG_FOLDER;
	}
	
  


}
