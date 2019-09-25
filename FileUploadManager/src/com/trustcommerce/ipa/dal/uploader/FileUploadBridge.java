package com.trustcommerce.ipa.dal.uploader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.configuration.app.AppConfiguration;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.device.firmware.Firmware;
import com.trustcommerce.ipa.dal.device.forms.FormsPackage;
import com.trustcommerce.ipa.dal.device.interfaces.FirmwareProcessor;
import com.trustcommerce.ipa.dal.device.interfaces.FormsPackageProcessor;
import com.trustcommerce.ipa.dal.device.interfaces.SingleUploadProcessor;
import com.trustcommerce.ipa.dal.exceptions.FileUploadException;
import com.trustcommerce.ipa.dal.exceptions.FirmwareUploadException;
import com.trustcommerce.ipa.dal.exceptions.FormsPackageUploadException;
import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.logger.Paths;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;
import com.trustcommerce.ipa.dal.model.fileUpload.UploadInputData;
import com.trustcommerce.ipa.dal.uploader.constants.UploaderConstants;
import com.trustcommerce.ipa.dal.uploader.files.SingleFileUpdateImpl;

/**
 * 
 * @author luisa.lamore
 *
 */
public class FileUploadBridge {
	/** log4j logger. */

	private static final transient Logger LOGGER = LoggerFactory.getLogger(FileUploadBridge.class);
	/** Callback class. */

	private FormsPackageProcessor formsProcessor;
	/** upload progress counter. */

	private int progressCounter;

	private UploadInputData upload;
	private static boolean pendingFormUpdate;
	private UploaderGui gui;



	public FileUploadBridge(final UploadInputData upload, UploaderGui uploaderGui) {
		this.upload = upload;
		this.gui = uploaderGui;
	}

	/**
	 * 
	 * 
	 * @param upload
	 *            Upload
	 * @param terminal
	 *            Terminal
	 */
	public final void updateFormsPackage(final UploadInputData uploadData, final boolean reboot) {
		LOGGER.info(" -> updateFormsPackage() model {}, reboot {}", uploadData.toString(), reboot);
		progressCounter = 1;

		upload = uploadData;
		
		deleteErrorFile(upload.getProcessId());
		// verifyDeviceSelectedForUpdate(deviceName, deviceToUpdate,
		// UserMessages.FORM_PACKAGE_TITLE);
		try {
			formsProcessor = new FormsPackage(uploadData);
			formsProcessor.setEventListener(new FileUpload2Listener());
 			if (upload.isShowGui() && !reboot) {
				//showFirmwareUploadGui(AppConfiguration.getLanguage().getString("FIRMWARE_UPDATE_STATUS"), false);
			}
			formsProcessor.updateForms(reboot);
		} catch (FormsPackageUploadException e) {
			LOGGER.error(e.getMessage());
			if (reboot) {
				FileUploadUtil.createFileUploadErrorFile(EntryModeStatusID.FirmwarePackageError,
						e.getMessage());
				//javax.swing.JOptionPane.showMessageDialog(null, e.getMessage());
				System.exit(0);
			} 
		} catch (IngenicoDeviceException e) {
			LOGGER.warn(e.getMessage());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
			}
		}
	}

	/**
	 * 
	 * 
	 * @param upload
	 *            Upload
	 * @param terminal
	 *            Terminal
	 */
	public final void updateSingleFile(final UploadInputData upload) {
		LOGGER.info(" -> updateSingleFile() {}", upload.toString());
		progressCounter = 1;

		// verifyDeviceSelectedForUpdate(deviceName, deviceToUpdate,
		// UserMessages.FORM_PACKAGE_TITLE);
		SingleUploadProcessor processor = null;
		try {
			processor = new SingleFileUpdateImpl(upload);
			processor.setEventListener(new FileUpload2Listener());
			processor.updateSingleFile();

		} catch (FileUploadException e) {
			LOGGER.error(e.getMessage());
			SingleFileUpdateImpl.terminate(
					String.format(AppConfiguration.getLanguage().getString("MESSAGE_FORMS_NO_UPDATE_REQUIRED")
							+ upload.getModel()));
			// SocketUtil.sendMessageToCaller(SocketMessageType.FileUpload,
			// EntryModeStatusID.FormsPackageError);
		}
	}

	/**
	 * 
	 * 
	 * @param upload
	 *            Upload
	 * @param terminal
	 *            Terminal
	 */
	public final void updateFirmware(final UploadInputData upload, final boolean reboot) {
		LOGGER.info(" -> updateFirmware() ");

		deleteErrorFile(upload.getProcessId());
 
		if (upload.isShowGui()) {
			gui.updateLabel(AppConfiguration.getLanguage().getString("FIRMWARE_UPDATE_STATUS")); 
		}
		// verifyDeviceSelectedForUpdate(deviceName, deviceToUpdate,
		// UserMessages.FIRMWARE_TITLE);
		// Check the connected device ..

		FirmwareProcessor firmware = null;
		try { 
			firmware = new Firmware(upload);
			firmware.setEventListener(new FileUpload2Listener());
 
			firmware.displayUploadForms();
		} catch (FirmwareUploadException e) {
			// Probably MSR initialization error
			LOGGER.error(e.getMessage());
			FileUploadUtil.createFileUploadErrorFile(EntryModeStatusID.FirmwarePackageError, e.getMessage());
			gui.displayWarning(e.getMessage());
			System.exit(0);
			return;
		}

		try {
			firmware.updateFirmware2(reboot);
		} catch (FirmwareUploadException e) {
			LOGGER.error(e.getMessage());
			FormsPackage.terminate(
					String.format(AppConfiguration.getLanguage().getString("MESSAGE_FIRMWARE_NO_UPDATE_REQUIRED")
							+ upload.getModel()));

			FileUploadUtil.createFileUploadErrorFile(EntryModeStatusID.FirmwarePackageError, e.getMessage());
		}
	}

	private Map<Integer, String> getPropertiesFile(String model) {
		Map<Integer, String> result = new HashMap<Integer, String>();

		// Get file from resources folder
		ClassLoader classLoader = getClass().getClassLoader();
		URL url = classLoader.getResource("firmwares.properties");
		if (url == null) {
			return null;
		}
		File file = new File(url.getFile());

		try (Scanner scanner = new Scanner(file)) {

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.contains(model.toLowerCase())) {
					String[] temp = line.split("=");
					String[] temp2 = temp[0].split("_");
					int version = Integer.parseInt(temp2[1]);

					result.put(version, temp[1]);
				}
			}
			scanner.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 
	 *
	 */
	private class FileUpload2Listener implements DeviceEventListener {

		@Override
		public void handleEvent(final LocalActionEvent deviceEvent) {

			LOGGER.debug("DeviceEvent: " + deviceEvent.state);
			switch (deviceEvent.state) {

			case FORMS_UPDATED:
				LOGGER.info("Forms package saved to device successfully");
				// No reboot occurred here so proceed with a firmware update
				pendingFormUpdate = true;
				upload.setProcessId(2);
				FileUploadActivator.processUpload(upload);
				break;
				
			case FIRMWARE_UPDATED:
				LOGGER.debug("Firmware package saved to device successfully");
				// No reboot occurred here so proceed with firmware
				upload.setProcessId(1);
				FileUploadActivator.processUpload(upload);
				break;
				
			case INGENICO_REBOOT:
				// If the device reboot, it is very possible that everything
				// worked fine
				LOGGER.warn("Received signal: Terminal Reboot: pendingFormUpdate = {}", pendingFormUpdate);
				
				if (upload.getProcessId() == 2) {
					processFirmwareUploadCompletion();

				} else if (upload.getProcessId() == 1 || upload.getProcessId() == 9) {
					FileUploadActivator.uploadResult.append("Done");
					System.exit(0);
				}
				// javax.swing.JOptionPane.showMessageDialog(null,UserMessages.INFO_REBOOTING);
				// DalActivator.openGui(UserMessages.UPLOAD_COMPLETE);
				break;

			case PROGRESS_UPDATE:
				// SocketUtil.sendMessageToCaller(SocketMessageType.FileUpload,
				// "InProgess " + progressCounter * 10);
				progressCounter++;
				// openGui("Form package upload progress " + progressCounter *
				// 10 + "%" );
				break;

			case ERROR:
				// SocketUtil.sendMessageToCaller(SocketMessageType.FileUpload,
				// EntryModeStatusID.FormsPackageError);
				break;

			case REJECTED:
				upload.setProcessId(1);
				FileUploadActivator.processUpload(upload);
				break;
				
			default:
				LOGGER.warn("Got the following state during file upload: " + deviceEvent.state.toString());
				break;
			}
		}
	}



	/**
	 * Error files are generated if errors are found during an upload.
	 * 
	 * @param uploadType
	 */
	private void deleteErrorFile(int uploadType) {
		final StringBuilder sb = new StringBuilder();
		sb.append(Paths.getLogsPath());
		sb.append(File.separator);
		if (uploadType == 2) {
			sb.append(UploaderConstants.FIRMWARE_UPLOAD_ERROR);
		} else if (uploadType == 1 || uploadType == 9) {
			sb.append(UploaderConstants.FORMS_UPLOAD_ERROR);
		}
		sb.append(File.separator);

		File file = new File(sb.toString());
		if (file.exists()) {
			file.delete();
		}
	}
	
	/**
	 * In EMV is mandatory to update the forms package after a firmware update because each firmware inlcudes
	 * its own set of packages.
	 * @param pendingForms
	 */
	private void processFirmwareUploadCompletion() {
		LOGGER.debug("-> processFirmwareUploadCompletion()");
		FileUploadActivator.uploadResult.append("Firmware installation completed");
		if (upload.isShowGui()) {
			gui.showFirmwareUploadCompleteGui(AppConfiguration.getLanguage().getString("FIRMWARE_UPDATE_COMPLETED"));
			try {
				// 20181120: some devices take longer to cycle through a reboot after a firmware update.
				Thread.sleep(75000);
			} catch (InterruptedException e) {
			}
			gui.showFirmwareUploadCompleteGui(AppConfiguration.getLanguage().getString("FIRMWARE_FORMS_REBOOT"));
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}
		LOGGER.debug("terminating jDAL");
		System.exit(0);
	}
}
