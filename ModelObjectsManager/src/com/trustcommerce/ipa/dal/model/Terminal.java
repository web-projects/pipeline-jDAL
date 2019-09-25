package com.trustcommerce.ipa.dal.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.configuration.app.AppConfiguration;
import com.trustcommerce.ipa.dal.constants.device.TerminalModel;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.constants.global.JdalVersion;
import com.trustcommerce.ipa.dal.model.health.DeviceHealth;


/**
 * Information about a terminal. Most of these information comes from the Health Status.
 * @author luisa.lamore
 *
 */
public class Terminal {

	/** log4j logger.*/
	private static final transient Logger LOGGER = LoggerFactory.getLogger(Terminal.class);
	
	/** Ingenico. */
	private static final String MANUFACTURER_DESCRIPTION = "Ingenico";

	/** The model name of the payment device. */
	private TerminalModel modelName;

	/** The model reference of the payment device. */
	private String modelReference;

	/** The serial number of the device we are saving data for. */
	private String serialNumber;

	/** The serial number of the device we are saving data for. */
	private String rawSerialNumber;

	/** The version of forms the device is using. */
	private String formsVersion;

	/** The version of the firmware the device is using.  */
	private String firmwareVersion;

	// /** Identifying number for the device. */
	// private String assetNumber;
	
	// /** Id of the application recording the information. */
	// private int appID;
	
	/** The version of the device's operating system. */
	private String osVersion;
	
	///** Date on which the record was created in the database table. */
	//private Date createdDate;
	
	///** User who was running the application when the data record was created. */
	//private String createdBy;
	
	///** Date on which the record was updated in the database table. */
	//private Date updatedDate;
	
	///** // User who was running the application when the data record was . */
	//private String updatedBy;

	/** The version of the jDAL application at the time the data for the rec. */
	private String jdalVersion;
	
	/** The version of the jDAL application at the time the data for the rec. */
	private String jdalInternalVersion;

	/** Indicates whether the payment device is capable of handling EMV transactions. TerminalIsEMVCapable.*/
	private boolean isEMVCapable;
	
	/** Indicates whether or not the device is capable of capturing signatures.*/
	private boolean signatureCapable;
	
	// /** Id of the company submitting the data for processing. */
	// private int companyID;

	/** Primary Id of the record in the database. */
	private int deviceID;

	/** Indicates whether the device is capable of processing Debit cards. */
	private boolean debitKey;

	/** Not to be included in the jSon. */
	private boolean isEmvFirmware;
	/** Not to be included in the jSon. */
	private boolean containsEmvForms;
	/** .*/
	private String issues;
	/** as it is obtain from the terminal. */
	private DeviceHealth deviceHealth;
	/** as it is obtain from the terminal. */
	private String emvKernelVersion;
	/** .*/
	private String manufacturerDescription;
	/** .*/
	private String manufacturerDate;
	/** .*/
	private String hostConfig;
	/** .*/
	private String totalSwipes;
	/** .*/
	private String encryptionConfiguration;
	/** .*/
	private boolean panEncryption;


	
	/**
	 * constructor.
	 */
	public Terminal() {
		this.modelName = TerminalModel.UNKOWN;
		this.modelReference = "";
		this.jdalVersion = JdalVersion.JDAL_VERSION;
		this.jdalInternalVersion = JdalVersion.JDAL_INTERNAL_VERSION;
		this.manufacturerDescription = MANUFACTURER_DESCRIPTION;
	}
		
	
	public String getManufacturerDescription() {
		return manufacturerDescription;
	}
	
	public String getManufacturerDate() {
		return manufacturerDate;
	}
	
	public String getHostConfig() {
		return hostConfig;
	}
	
	/**
	 * 
	 * @return String with the reason why EMV can not be processed, otherwise null.
	 */
	public final String getIssues() {
		return issues;
	}


	/**
	 * 
	 * @return String
	 */
	public final String getSerialNumber() {
		return serialNumber;
	}
	
    /**
     * 
     * @param value serialNumber String
     */
	@JsonProperty("SerialNumber")
	public final void setSerialNumber(final String value) {
		this.serialNumber = value;
	}
	
	/**
     * 
     * @param value modelReference String
     */
	@JsonProperty("modelReference")
	public final void setModelReference(final String value) {
		
		if(value != null && value.length() > 0) {
			String [] worker = value.split("-");
			if(worker.length > 1) {
				this.modelReference += "-" + worker[1];
			}
		}
	}
	
    /**
     * 
     * @return String
     */
	public final String getFormsVersion() {
		return formsVersion;
	}
	
	/**
	 * 
	 * @param value formsVersion String
	 */
	@JsonProperty("FormsVersion")
	public final void setFormsVersion(final String value) {
		this.formsVersion = value;
	}
	
	/**
	 * 
	 * @return String
	 */
	public final String getFirmwareVersion() {
		return firmwareVersion;
	}
	
	/**
	 * 
	 * @param value firmwareVersion String
	 */
	@JsonProperty("FirmwareVersion")
	public final void setFirmwareVersion(final String value) {
		this.firmwareVersion = value;
	}
	
	
	/**
	 * 
	 * @return String
	 */
	public final String getOsVersion() {
		return osVersion;
	}
	
	/**
	 * 
	 * @param value String
	 */
	@JsonProperty("OSVersion")
	public final void setOsVersion(final String value) {
		this.osVersion = value;
	}
	
	
	/**
	 * 
	 * @return int
	 */
	public final int getDeviceID() {
		return deviceID;
	}
	
	/**
	 * 
	 * @param deviceID int
	 */
	@JsonProperty("DeviceID")
	public final void setDeviceID(final int value) {
		this.deviceID = value;
	}
	
	/**
	 * 
	 * @return String
	 */
	public final String getJdalVersion() {
		return jdalVersion;
	}
	
	/**
	 * 
	 * @param jdalVersion String
	 */
	@JsonProperty("JdalVersion")
	public final void setJdalVersion(final String value) {
		this.jdalVersion = value;
	}
	
	/**
	 * 
	 * @return boolean
	 */
	public final boolean isEMVBitOn() {
		return isEMVCapable;
	}
	
	
	public void setDeviceHealth(DeviceHealth value) {
		deviceHealth = value;
		final int temp = Integer.parseInt(deviceHealth.getSignatureReads());
		setSignatureCapable(temp);
		rawSerialNumber = deviceHealth.getOriginalSerialValue();
		manufacturerDate = deviceHealth.getManufDate();
		hostConfig = deviceHealth.getInterfaceType();
		totalSwipes = deviceHealth.getMsrSwipes();
	}
	
	
	public boolean isSignatureCapable() {
		return signatureCapable;
	}

	@JsonProperty("SignatureCapable")
	public void setSignatureCapable(final boolean value) {
		this.signatureCapable = value;
	}
	
	public void setSignatureCapable(int isSignatureCapable) {
		if (isSignatureCapable == 1) {
			this.signatureCapable = true;
		} else {
			this.signatureCapable = false;
		}
	}
	
	/**
	 * 
	 * @param value boolean
	 */
	@JsonProperty("IsEMVCapable")
	public final void setTerminalIsEMVCapable(final boolean value) {
		this.isEMVCapable = value;
	}

	@Override
	public final String toString() {
		return "{" 
				+ "\"DeviceID\" : \"" + deviceID + "\"," 
				+ "\"ModelDescription\" : \"" + modelName.name() + modelReference + "\","
		        + "\"SerialNumber\" : \"" + serialNumber + "\"," 
				+ "\"FormsVersion\" : \"" + formsVersion + "\","
		        + "\"FirmwareVersion\" : \"" + firmwareVersion + "\"," 
		        + "\"CompanyID\" : \"" + 0 + "\","
		        + "\"ManufacturerDescription\" : \"" + MANUFACTURER_DESCRIPTION + "\"," 
		        + "\"AssetNumber\" : \"" + "" + "\","
		        + "\"AppID\" : \"" + 0 + "\"," 
		        + "\"OSVersion\" : \"" + osVersion + "\"," 
		        + "\"Active\" : \"" + true + "\"," 
		        + "\"CreatedDate\" : \"" + "" + "\"," 
		        + "\"CreatedBy\" : \"" + "" + "\","
		        + "\"DebitKey\" : \"" + debitKey + "\"," 
		        + "\"UpdatedDate\" : \"" + "" + "\"," 
		        + "\"UpdatedBy\" : \"" + "" + "\"," 
		        + "\"IsEMVCapable\" : \"" + isEMVCapable + "\","
		        + "\"JdalVersion\" : \"" + JdalVersion.JDAL_VERSION 
		        + "\"}";
	}

	/**
	 * 
	 * @return boolean
	 */
	public final boolean isDebitKey() {
		return debitKey;
	}
	/**
	 * 
	 * @param debitKey boolean
	 */
	@JsonProperty("DebitKey")
	public final void setDebitKey(final boolean debitKey) {
		this.debitKey = debitKey;
	}

	
	/**
	 * 
	 */
	public final void verifyLoadedFirmwareVersion() {

		LOGGER.info("-> verifyLoadedFirmwareVersion()  ");

		if (firmwareVersion == null) {
			LOGGER.warn("Loaded version not available; skip firmware verification process.");
			isEmvFirmware = true;
		}

		LOGGER.info("Expected minimum firmware version: ' {}' ", GlobalConstants.MIN_EMV_APP_VERSION);
		LOGGER.info("Loaded firmware version: {}", firmwareVersion);

		final String[] loaded = firmwareVersion.split("\\.");
		final String[] emvVersion = GlobalConstants.MIN_EMV_APP_VERSION.split("\\.");

		// Check the major
		if (Integer.parseInt(loaded[0]) > Integer.parseInt(emvVersion[0])) {
			// Stop, this is an EMV app
			isEmvFirmware = true;
		} else if (Integer.parseInt(loaded[0]) == Integer.parseInt(emvVersion[0])) {
			// check the minor
			if (Integer.parseInt(loaded[1]) > Integer.parseInt(emvVersion[1])) {
				isEmvFirmware = true;
			} else if (Integer.parseInt(loaded[1]) == Integer.parseInt(emvVersion[1])) {
				// check the patch
				if (Integer.parseInt(loaded[2]) >= Integer.parseInt(emvVersion[2])) {
					isEmvFirmware = true;
				} else {
					isEmvFirmware = false;
				}
			} else {
				isEmvFirmware = false;
			}
		} else {
			isEmvFirmware = false;
		}
		LOGGER.info("<- verifyLoadedFirmwareVersion() returned {}", isEmvFirmware);
	}
	
	
    /**
     * The EMV Vault package version includes the suffix EMV. 
     * 
     * @param loadedFormVersion
     * @param expectedVersion
     * @return boolean
     */
    public final boolean verifyLoadedFormVersion() {
        LOGGER.info("-> verifyLoadedFormVersion()");

        // logger.debug("Expected form version: \"" + expectedVersion + "\"");
        LOGGER.debug("Loaded form version: {} ", formsVersion);
        if (formsVersion == null) {
        	return false;
        }
        final boolean matched = formsVersion.contains(GlobalConstants.FORM_PACKAGE_SUFFIX);

        if (!matched) {
            LOGGER.info("Loaded form package does not contain EMV forms");
            containsEmvForms = false;
        } else {
            LOGGER.info("Loaded form package contain EMV forms");
            containsEmvForms = true;
        }
        return matched;
    }
    
    
    /**
     * The 3 conditions to process EMV transactions are true: Firmware, forms and emv bit.
     * @return String with the reason why EMV can not be processed, otherwise null.
     */
	public final boolean isEmvCapable() {
		final boolean result = false;
		if (!isEMVCapable) {
			issues = "Connected device is not EMV Enabled";
			return result;
		} else if (!isEmvFirmware) {
			issues = String.format(AppConfiguration.getLanguage().getString("BAD_FIRMWARE_VERSION"),
					firmwareVersion.toString(),
					GlobalConstants.MIN_EMV_APP_VERSION.toString());
			return result;
		} else if (!containsEmvForms) {
			issues = AppConfiguration.getLanguage().getString("BAD_FORMS_VERSION");
			return result;
		}
		return true;
	}

	/**
	 * 
	 * @return boolean
	 */
	public final boolean isEmvFirmware() {
		return isEmvFirmware;
	}
	/**
	 * 
	 * @return boolean
	 */
	public final boolean isContainsEmvForms() {
		return containsEmvForms;
	}
	/**
	 * 
	 * @param containsEmvForms boolean
	 */
	public final void setContainsEmvForms(final boolean value) {
		this.containsEmvForms = value;
	}
	/**
	 * 
	 * @return TerminalModel
	 */
	public final TerminalModel getModelName() {
		return modelName;
	}
	
	/**
	 * The model name is updated during initialization of jdal.
	 * @param value modelName TerminalModel type
	 */
	public final void setModelName(final TerminalModel value) {
		this.modelName = value;
	}
	
	/**
	 * 
	 * @param value String
	 */
	@JsonProperty("ModelDescription")
	public final void setModelDescription(final String value) {
		if (value == null || value.isEmpty()) {
			return;
		}
		this.modelName = TerminalModel.valueOf(value);
	}

	public String getEmvKernelVersion() {
		return emvKernelVersion;
	}

	public void setEmvKernelVersion(String emvKernelVersion) {
		this.emvKernelVersion = emvKernelVersion;
	}

	public String getRawSerialNumber() {
		return rawSerialNumber;
	}

	@JsonProperty("RawSerialNumber")
	public void setRawSerialNumber(String rawSerialNumber) {
		this.rawSerialNumber = rawSerialNumber;
	}

	public String getJdalInternalVersion() {
		return jdalInternalVersion;
	}


	public void setHostConfig(String hostConfig) {
		this.hostConfig = hostConfig;
	}


	public String getTotalSwipes() {
		return totalSwipes;
	}


	public String getEncryptionConfiguration() {
		return encryptionConfiguration;
	}
	
	public void setEncryptionConfiguration(int value) {
		if (value == 0) {
			encryptionConfiguration = "no encryption";
		} else if (value == 11) {
			encryptionConfiguration = "Generic TDES DUKPT encryption";
		} else {
			encryptionConfiguration = "Not supported";
		}
	}

	@JsonProperty("EncryptionConfiguration")
	public void setEncryptionConfiguration(String value) {
		this.encryptionConfiguration = value;
	}


	public boolean isPanEncryption() {
		return panEncryption;
	}

	@JsonProperty("PanEncryption")
	public void setPanEncryption(boolean value) {
		this.panEncryption = value;
	}
	
	
	/**
	 * 
	 * @param data type String
	 */
	public final void processDukptKeys(final String data) {
		final String[] values = data.split("\n");
		for (String s: values) {
			if (s.contains("KSN_4")) {
				panEncryption = true;
			} else if (s.contains("KSN_0")) {
				debitKey = true;
			} else {
				LOGGER.debug("Found another key");
			}
		}
	}

}
