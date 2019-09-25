package com.trustcommerce.ipa.dal.model.fileUpload;

import java.io.File;

import com.trustcommerce.ipa.dal.configuration.app.AppConfiguration;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.constants.device.TerminalModel;
import com.trustcommerce.ipa.dal.constants.paths.TcipaFiles;


/**
 * 
 * @author luisa.lamore
 *
 */
public class UploadInputData {

	
	private static final String FORMS_NAME = "JPOSFORMS.TGZ";
	
	private int processId;
	
	private String firmwareName;
	
	private String deviceHardwareVer;
	
	private String firmwarePath;
	
	private String formsPath;
	
	private boolean showGui;
	
	private TerminalModel model;
	
	/**
	 * 
	 * @param inputProcessId type int 1 for forms, 2 for firmware
	 */
	public UploadInputData(final int inputProcessId) {
		processId = inputProcessId;
		showGui = true;
		deviceHardwareVer = "V3";
	}
	

	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

	public String getFirmwareName() {
		return firmwareName;
	}

	public void setFirmwareName(String firmwareName) {
		this.firmwareName = firmwareName;
	}

	public void setFirmwareVersion(String deviceHardwareVer) {
		this.deviceHardwareVer = deviceHardwareVer;
	}
	
	public String getFirmwareVersion() {
		return this.deviceHardwareVer;
	}
	
	public String getFirmwarePath(boolean useAlternate) {
		if (firmwareName == null) {
			return null;
		} else {
			return getFirmwareRootPath(useAlternate) + firmwareName;
		}
	}

	public String getFormsPath() {
		return getFormsRootPath() + FORMS_NAME;
	}

 
	public boolean isShowGui() {
		return showGui;
	}

	public void setShowGui(boolean showGui) {
		this.showGui = showGui;
	}
	
	public TerminalModel getModel() {
		return model;
	}

	public void setModel(TerminalModel model) {
		this.model = model;
	}
	
	/**
	 * Validates the existence of the file to Upload.
	 * @param model
	 * @param fileName
	 * @param processId
	 * @return
	 * @throws Exception 
	 */
	public final void validateFilesToUpload() throws Exception {
		// build the forms path, forms are required for both firmware and forms
		final File f = new File(getFormsPath());
		// Check if the forms package exist
		if (!f.exists()) {
			final String fm = String.format(
			        AppConfiguration.getLanguage().getString("MESSAGE_FORMS_NO_UPDATE_REQUIRED"), model.name());
			throw new Exception(fm);
		}
		// Firmware
		if (firmwareName == null) {
			// For now it is acceptable that the firmware name is null. It will
			// use the default.
			return;
		}
		if (processId == 2 || processId == 7) {

			File fwFile = new File(getFirmwarePath(false));
			if (!fwFile.exists()) {
				// try alternate directory
				fwFile = new File(getFirmwarePath(true));
				if (fwFile.exists()) {
    				final String fw = String.format(
    				        AppConfiguration.getLanguage().getString("FIRMWARE_WRONG_VERSION"), firmwareName);
    				throw new Exception(fw);
				}
				else {
    				final String fw = String.format(
    				        AppConfiguration.getLanguage().getString("ERROR__FILE_NOT_FOUND"), firmwareName);
    				throw new Exception(fw);
				}
			}
		}
	}
	
	/**
	 * Returns the path of the firmware in the workstation.
	 * @return firmware path
	 */
	private String getFirmwareRootPath(boolean useAlternateVersion) {
		final StringBuilder sb = new StringBuilder();
		sb.append(ClientConfigurationUtil.getTCIPAHome());
		sb.append(TcipaFiles.INGENICO_FOLDER);
		sb.append(TcipaFiles.FIRMWARE_FOLDER);
		sb.append(File.separator);
		sb.append(model.name());
		sb.append(File.separator);
		if(useAlternateVersion) {
			if(this.deviceHardwareVer.contains("V3")) {
				sb.append("V4");
			}
			else {
				sb.append("V3");
			}
		}
		else {
			sb.append(this.deviceHardwareVer);
		}
		sb.append(File.separator);
		return sb.toString();
	}
	
	/**
	 * Returns the path of the firmware in the workstation.
	 * @return firmware path
	 */
	private String getFormsRootPath() {
		final StringBuilder sb = new StringBuilder();
		sb.append(ClientConfigurationUtil.getTCIPAHome());
		sb.append(TcipaFiles.INGENICO_FOLDER);
		sb.append(TcipaFiles.FORMS_FOLDER);
		sb.append(File.separator);
		sb.append(model.name());
		sb.append(File.separator);
		return sb.toString();
	}
	
	
	/**
	 * Returns the path of the firmware in the workstation.
	 * @return firmware path
	 */
	public String getSingleFileRootPath() {
		final StringBuilder sb = new StringBuilder();
		sb.append(ClientConfigurationUtil.getTCIPAHome());
		sb.append(TcipaFiles.INGENICO_FOLDER);
		sb.append("custom");
		sb.append(File.separator);
		sb.append(model.name());
		sb.append(File.separator);
		return sb.toString();
	}


	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("UploadInputData [processId=");
		builder.append(processId);
		builder.append(", firmwareName=");
		builder.append(firmwareName);
		builder.append(", firmwarePath=");
		builder.append(firmwarePath);
		builder.append(", formsPath=");
		builder.append(formsPath);
		builder.append(", showGui=");
		builder.append(showGui);
		builder.append(", model=");
		builder.append(model);
		builder.append("]");
		return builder.toString();
	}
}
