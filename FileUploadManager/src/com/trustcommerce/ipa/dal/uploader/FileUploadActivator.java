package com.trustcommerce.ipa.dal.uploader;

import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.commport.exceptions.PortConnectionException;
import com.trustcommerce.ipa.dal.commport.ports.PortCommunicator;
import com.trustcommerce.ipa.dal.configuration.app.AppConfiguration;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.constants.device.TerminalModel;
import com.trustcommerce.ipa.dal.logger.LoggerConfig;
import com.trustcommerce.ipa.dal.logger.Utils;
import com.trustcommerce.ipa.dal.model.Terminal;
import com.trustcommerce.ipa.dal.model.fileUpload.UploadInputData;
import com.trustcommerce.ipa.dal.uploader.constants.UploaderConstants;
import com.trustcommerce.ipa.dal.uploader.utils.DeviceHealthStatus;
import com.trustcommerce.ipa.dal.uploader.utils.UploaderUtils;
import com.ingenico.api.jpos.IngenicoMSR;

/**
 * A server program which accepts requests from clients to capitalize strings.
 * When clients connect, a new thread is started to handle an interactive dialog
 * in which the client sends in a string and the server thread sends back the
 * capitalized version of the string.
 *
 * The program is runs in an infinite loop, so shutdown in platform dependent.
 * If you ran it from a console window with the "java" interpreter, Ctrl+C
 * generally will shut it down.
 */

public class FileUploadActivator {
	/** log4j logger.*/

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadActivator.class);
	/** callback class.*/
	private static FileUploadBridge fileUploadBridge;
	
	public static ResultFile uploadResult;

	private static UploaderGui gui;
	
	private static Hashtable<String, String> deviceModel; 
	private static Hashtable<String, String> deviceFirmware; 
	
	/**
	 * Application method to run the server runs in an infinite loop listening
	 * on port 9898. When a connection is requested, it spawns a new thread to
	 * do the servicing and immediately returns to listening. The server keeps a
	 * unique client number for each client that connects just to show
	 * interesting logging messages. It is certainly not necessary to do this.
	 * @param args String[]
	 * @throws Exception occurred during fileupload
	 */
	public static void main(final String[] args) throws Exception {
		
		LOGGER.info("TrustCommerce File Uploader is running.");
		Utils.createTcipaLogFolder();
		LoggerConfig.startLogger(UploaderConstants.LOG4J_XML_PATH);
		//LoggerConfig.startLogger();
		Utils.logHeader(UploaderConstants.LOG_TITLE);

		gui = new UploaderGui();
		
		// Check we have at least 1 argument
		if (args.length == 0) {
			String temp = AppConfiguration.getLanguage().getString("ARG_MISSING");
			FileUploadUtil.createFileUploadErrorFile(EntryModeStatusID.FirmwarePackageError, "Missing required argument to perform file upload");
			gui.displayWarning(temp);
			System.exit(0);
		}
		
		// get the first argument 1 for forms, 2 for firmware
		final String arg1 = (String) args[0];
		 
		final int processId = Integer.parseInt(arg1);
		
		if (UploaderUtils.ingenicoJreIsRunning()) {
			if (processId == 1 || processId == 9 || processId == 2) {
				// The Forms upload process allows jDAL running ...
			} else {
				LOGGER.info("TCIPA JRE is running! Can not proceed with firmware upload");
				gui.displayWarning(AppConfiguration.getLanguage().getString("ANOTHER_JRE"));
				System.exit(0);
			}
		}

		uploadResult = new ResultFile(processId);
		UploadInputData inputData = new UploadInputData(processId);
		
		// Argument 2: The installation is silent by default: displayPopups 1
		if (args.length > 1) {
			final String arg2 = (String) args[1];
			boolean displayGUI = Boolean.parseBoolean(arg2);
			inputData.setShowGui(displayGUI);
		}
		
		// Argument 3: Filename
		if (args.length > 2) {
			String fileName = (String) args[2];
			if(!fileName.equalsIgnoreCase("NULL")) {
				inputData.setFirmwareName(fileName);
			}
		}
	
		// gui = new SwingController(terminalModel);

		// Argument 4: deviceName
		String deviceName = null;
		if (args.length > 3) {
			deviceName = (String) args[3];
		}
		
		// ===================
		
		TerminalModel terminalModel = null;
		try {
			if(deviceName != null) {
				terminalModel = PortCommunicator.getConnectedDevice(deviceName);
			}
			else {
				terminalModel = PortCommunicator.getConnectedDevice();
			}
			if (terminalModel == null) {
				LOGGER.info(AppConfiguration.getLanguage().getString("ERROR_IN_CONNECTION"));
				FileUploadUtil.createFileUploadErrorFile(EntryModeStatusID.BadConnection, AppConfiguration.getLanguage().getString("ERROR_IN_CONNECTION"));
				gui.displayWarning(AppConfiguration.getLanguage().getString("ERROR_IN_CONNECTION"));
				System.exit(0);
			}
		} catch (PortConnectionException e) {
			LOGGER.error(e.getMessage());
			FileUploadUtil.createFileUploadErrorFile(EntryModeStatusID.BadConnection, e.getMessage());
		}
		
		inputData.setModel(terminalModel);
		LOGGER.info("File Upload Input Data : {}",  inputData.toString());
		
		// Firmware version: MM.m.pp.ssss
		String firmwareVer = "";

		//TODO: define enum constants
		// firmware process flow: ID=2, firmware update only (DAL)
		//                        ID=7, form update, followed by firmware update (update_firmware.bat)
		if (processId == 2 || processId == 7)  {
			// validate form exists to avoid blank screen
			if (processId == 7)  {
				try {
					   inputData.validateFilesToUpload();
					} catch (Exception e) {
						LOGGER.error("File Upload failed.  Missing files required for the requested upload");
						FileUploadUtil.createFileUploadErrorFile(EntryModeStatusID.FormsPackageError, e.getMessage());
						gui.displayWarning(e.getMessage());
						System.exit(0);
					}
			}
			
			inputData.setFirmwareName("PLACEHOLDER.OGZ");
			String [] result = firmwareVersion(inputData);
			if(result == null) {
				System.exit(0);
			}
	
			firmwareVer = result[0];
			String deviceHardwareVer = result[1];
		
			inputData.setFirmwareVersion(deviceHardwareVer);
			
			// Set Device/Firmware Table
			SetDeviceTables();
			
			// Set Firmware filename
			SetFirmwareFileName(inputData);
		}
		
		try {
		   inputData.validateFilesToUpload();
		} catch (Exception e) {
			LOGGER.error("File Upload failed.  Missing files required for the requested upload");

			if (processId == 1 || processId == 9) {
				FileUploadUtil.createFileUploadErrorFile(EntryModeStatusID.FormsPackageError, e.getMessage());
			} else {
				FileUploadUtil.createFileUploadErrorFile(EntryModeStatusID.FirmwarePackageError, e.getMessage());
				// Reboot device
				Reboot(terminalModel.name());
			}
			gui.displayWarning(e.getMessage());
			System.exit(0);
		}
		
		// ===================
		final Terminal terminalInfo = new Terminal();
		terminalInfo.setModelName(terminalModel);

		boolean update = true;
		// Check if firmware update is required: for firmware update command only
		if(inputData.getProcessId() == 7 || inputData.getProcessId() == 8) {
			update = firmwareUpdate(inputData, firmwareVer);
		}
		
		if(update) {
			processUpload(inputData);
		}
		else {
			System.exit(2);
		}
	}
	
	private static void Reboot(String modelName) {
		
		try {
			LOGGER.debug("Attempting to reboot device..."); 
			IngenicoMSR msr = new IngenicoMSR();
			msr.open("MSR_" + modelName);
			msr.claim(10000);
			// Reboot device
			msr.directIO(106, new int[] { 1 }, "");
			LOGGER.debug("reboot: command issued.");
		} 
		catch (Exception e) {
			LOGGER.error("IngenicMSR(): " + e.getMessage());
		}
	}

	private static String [] firmwareVersion(UploadInputData inputData) {
		// Firmware version: MM.m.pp.ssss
		String firmwareVer = "";
		// Hardware version: V4, V3
		String deviceHardwareVer = "";
		
		// Obtain Firmware version
		try {
			DeviceHealthStatus health = new DeviceHealthStatus();
			firmwareVer = health.getFirmwareVersion(inputData);
			deviceHardwareVer = health.getHardwareVersion();
		} 
		catch (Exception e) {
			// Probably MSR initialization error
			LOGGER.error(e.getMessage());
			gui.displayWarning(e.getMessage());
			return null;
		}
		
		//TODO: return instead of assignment to array
		String [] result = { firmwareVer, deviceHardwareVer };
		return result;
	}
	
	private static void SetDeviceTables() {
		/* INGENICO FIRMWARE NAMING CONVENTION
		 * 
		 *  V3 hardware filename might start with UGEN (version 13.1.12)
		 *  V4 hardware filename starts with U
			iPP320 v3 starts with 04 - filename: U04190602B3.OGZ
			iPP320 v4 starts with 24 - filename: U24190602B3.OGZ 
			iPP350 v3 starts with 02 - filename: U02190602B3.OGZ
			iPP350 v4 starts with 22 - filename: U22190602B3.OGZ
			iSC250 v3 starts with 03 - filename: U03190602B3.OGZ
			iSC250 v4 starts with 0B - filename: U0B190602B3.OGZ
			iSC480 v3 starts with 31 - filename: U31190602B3.OGZ
			iSC480 v4 starts with 0A - filename: U0A190602B3.OGZ
		*/
		
		deviceModel = new Hashtable(); 
		deviceModel.put("iPP320V3", "04");
		deviceModel.put("iPP320V4", "24");
		deviceModel.put("iPP350V3", "02");
		deviceModel.put("iPP350V4", "22");
		deviceModel.put("iSC250V3", "03");
		deviceModel.put("iSC250V4", "0B");
		deviceModel.put("iSC480V3", "31");
		deviceModel.put("iSC480V4", "0A");
		
		deviceFirmware = new Hashtable();
		deviceFirmware.put("iPP320V3", "U04190602B3.OGZ");
		deviceFirmware.put("iPP320V4", "U24190602B3.OGZ");
		deviceFirmware.put("iPP350V3", "U02190602B3.OGZ");
		deviceFirmware.put("iPP350V4", "U22190602B3.OGZ");
		deviceFirmware.put("iSC250V3", "UGEN0313112.OGZ");
		deviceFirmware.put("iSC250V4", "U0B190602B3.OGZ");
		deviceFirmware.put("iSC480V3", "UGEN3113112.OGZ");
		deviceFirmware.put("iSC480V4", "U0A190602B3.OGZ");
	}
	
	private static void SetFirmwareFileName(UploadInputData inputData) {
		
		String model = inputData.getModel() + inputData.getFirmwareVersion();
		String firmwareName = deviceFirmware.get(model);
		inputData.setFirmwareName(firmwareName);
		LOGGER.debug("\nUpdating firmware on device {} with filename: {}", model, firmwareName);
	}
	
	public static boolean firmwareUpdate(UploadInputData inputData, String firmwareVer) {

		
		String filefirmwareVer = "";
		String fileHardwareVer = "";
		String updateVersion[] = null;
		String firmwareFile = inputData.getFirmwareName();
		
		if(firmwareFile.contains("UGEN")) {
			fileHardwareVer = inputData.getFirmwareName().substring(4, 6);
			filefirmwareVer = firmwareFile.substring(6, 11);
			updateVersion = new String[] { filefirmwareVer.substring(0, 2), filefirmwareVer.substring(2, 3), filefirmwareVer.substring(3, 5) };
		}
		else {
			fileHardwareVer = inputData.getFirmwareName().substring(1, 3);
			filefirmwareVer = firmwareFile.substring(3, 9);
			updateVersion = new String[] { filefirmwareVer.substring(0, 2), filefirmwareVer.substring(2, 4), filefirmwareVer.substring(4, 6) };
		}

		String deviceHardwareVer = inputData.getFirmwareVersion();
		String model = inputData.getModel() + inputData.getFirmwareVersion();
		
		if(!deviceModel.containsKey(model)) {
			String warning = String.format(AppConfiguration.getLanguage().getString("BAD_FIRMWARE_FILE"),
                                           deviceHardwareVer);
			gui.displayWarning(warning);
			warning = warning.replaceAll("<.+?>", "");
			LOGGER.error(warning);			
			return false;
		}

		String modelVersion = deviceModel.get(model);
		
		// Validate firmware source file
		if(!modelVersion.equals(fileHardwareVer)) {
			String warning = String.format(AppConfiguration.getLanguage().getString("BAD_FIRMWARE_FILE"),
					                       deviceHardwareVer);
			gui.displayWarning(warning);
			warning = warning.replaceAll("<.+?>", "");
			LOGGER.error(warning);			
            return false;
		}
		
		// Validate firmware version comparing it to the filename:  UGEN03MMmss
		if(firmwareVer.length() > 0)
		{
			// should match: GlobalConstants.MIN_EMV_APP_VERSION
			String deviceVersion[] = firmwareVer.split("\\.");
			
			if(Integer.parseInt(deviceVersion[0]) > Integer.parseInt(updateVersion[0]) ||
			   Integer.parseInt(deviceVersion[0]) == Integer.parseInt(updateVersion[0]) &&
			   Integer.parseInt(deviceVersion[1]) > Integer.parseInt(updateVersion[1])) 
			{
				String warning = String.format(AppConfiguration.getLanguage().getString("FIRMWARE_UPTODATE"),
						firmwareVer.toString());
				gui.displayWarning(warning);
				warning = warning.replaceAll("<.+?>", "");
				LOGGER.error(warning);
				return false;
			}
		}
		
		return true;
	}
	
	public static void processUpload(UploadInputData upload) {
		LOGGER.info("-> processUpload() ");
		
		fileUploadBridge = new FileUploadBridge(upload, gui);
		
		if (upload.getProcessId() == 2) {
			fileUploadBridge.updateFirmware(upload, true);
			uploadResult.append("installing Firmware");
			
		} else if (upload.getProcessId() == 1) {
			// Use uniquely for forms upload
			uploadResult.deleteFile();
			fileUploadBridge.updateFormsPackage(upload, true);
			uploadResult.append("installing forms");
			
		} else if (upload.getProcessId() == 3) {
			fileUploadBridge.updateSingleFile(upload);
			
		} else if (upload.getProcessId() == 7) {
			// two passes are required here
			uploadResult.deleteFile();
			fileUploadBridge.updateFormsPackage(upload, false);
			uploadResult.append("installing forms");

		} else if (upload.getProcessId() == 8) {
			// two passes are required here
			fileUploadBridge.updateFirmware(upload, false);

		} else if (upload.getProcessId() == 9) {
			fileUploadBridge.updateFormsPackage(upload, true);
			uploadResult.append("installing final forms");
			
		} else {
			LOGGER.info(" Unknown upload option!!!");
		}
	}
}