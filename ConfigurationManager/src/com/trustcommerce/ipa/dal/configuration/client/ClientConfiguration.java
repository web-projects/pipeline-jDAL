package com.trustcommerce.ipa.dal.configuration.client;

import org.codehaus.jackson.annotate.JsonProperty;

import com.trustcommerce.ipa.dal.constants.device.StartMode;
import com.trustcommerce.ipa.dal.constants.global.IinSource;
import com.trustcommerce.ipa.dal.constants.global.ProcessorPlatforms;
 

/**
 * The client configuration file clientConfig.json is located in
 * [TC_HOME]/config.
 * 
 * @author luisa.lamore
 *
 */
public class ClientConfiguration {

 	/**
	 * The timeout adjustment is to make sure client bridge does not time out
	 * before DAL. Time is seconds.
	 */
	private static final int TIME_ADJUST = 5;

	/** Timeout time use for the MSR for form display. */
	private int msrTimeout;
	/** Timeout time use for the complete Signature process. */
	private int signatureCaptureTimeout;
	private boolean showCvvForm;
	/** Swipe, Manual, Both. */
	private String startMode;
	private String lastConnectedDevice;
	
	private String formsVersionIsc480;
	private String formsVersionIsc250;
	private String formsVersionIpp350;
	private String formsVersionIup250;
	/** Udp Socket communication port. */
	private String paymentPort;
	private String environmentType;
	private String signaturePort;
	private String formsPath;

	private int emvRetryAttempts;
	private boolean contactlessEnabled;
	private boolean dev;
	private int timeoutPedal;
	/** External connection type, use for the debit card transactions. */
	private IinSource iinSource;

	private boolean allowPartialPayment;
	private String thresholds;
	private ProcessorPlatforms processingPlatform;

	public ClientConfiguration() {
		startMode = "swipe";
		showCvvForm = true;
		msrTimeout = 60000;
		signatureCaptureTimeout = 100000;
		environmentType = "prod";
		iinSource = IinSource.RapidConnect;
		processingPlatform = ProcessorPlatforms.FDRC;
		lastConnectedDevice = "";
	}

	public boolean isDev() {
		return dev;
	}

	public String getLastConnectedDevice() {
		return lastConnectedDevice;
	}

	@JsonProperty("lastConnectedDevice")
	public void setLastConnectedDevice(String lastConnectedDevice) {
		this.lastConnectedDevice = lastConnectedDevice;
	}
	
	public int getMsrTimeout() {
		return msrTimeout;
	}

	@JsonProperty("MsrTimeout")
	public void setMsrTimeout(int msrTimeout) {
		this.msrTimeout = msrTimeout;
	}

	public int getSignatureCaptureTimeout() {
		return signatureCaptureTimeout;
	}

	@JsonProperty("SignatureCaptureTimeout")
	public void setSignatureCaptureTimeout(final int signatureCaptureTimeout) {
		this.signatureCaptureTimeout = signatureCaptureTimeout;
	}

	public boolean isShowCvvForm() {
		return showCvvForm;
	}

	@JsonProperty("ShowCvvForm")
	public void setShowCvvForm(final boolean showCvvForm) {
		this.showCvvForm = showCvvForm;
	}

	public StartMode getStartMode() {

		if (startMode != null) {
			if (startMode.equalsIgnoreCase("manual")) {
				return StartMode.MANUAL;
			} else if (startMode.equalsIgnoreCase("both")) {
				return StartMode.SWIPE_MANUAL;
			} else {
				return StartMode.SWIPE;
			}
		} else {
			// Default
			return StartMode.SWIPE;
		}
	}

	@JsonProperty("StartMode")
	public void setStartMode(final String startMode) {
		this.startMode = startMode;
	}

	public String getFormsVersionIsc480() {
		return formsVersionIsc480;
	}

	@JsonProperty("FormsVersionIsc480")
	public void setFormsVersionIsc480(final String formsVersionIsc480) {
		this.formsVersionIsc480 = formsVersionIsc480;
	}

	public String getFormsVersionIsc250() {
		return formsVersionIsc250;
	}

	/**
	 * Returns the version that corresponds to the current device.
	 * 
	 * @param device
	 * @return
	 */
	public String getSimpleVersion(String device) {
		String temp = null;
		if (device.equalsIgnoreCase("iSC250")) {
			temp = formsVersionIsc250;
		} else if (device.equalsIgnoreCase("iSC480")) {
			temp = formsVersionIpp350;
		} else if (device.equalsIgnoreCase("iPP350")) {
			temp = formsVersionIsc480;
		}

		if (temp != null && temp.startsWith("tc_")) {
			temp = temp.substring(3);
		}
		return temp;
	}

	@JsonProperty("FormsVersionIsc250")
	public void setFormsVersionIsc250(final String formsVersionIsc250) {
		this.formsVersionIsc250 = formsVersionIsc250;
	}

	public String getFormsVersionIpp350() {
		return formsVersionIpp350;
	}

	@JsonProperty("FormsVersionIpp350")
	public void setFormsVersionIpp350(String formsVersionIpp350) {
		this.formsVersionIpp350 = formsVersionIpp350;
	}

	public String getPaymentPort() {
		return paymentPort;
	}

	@JsonProperty("PaymentPort")
	public void setPaymentPort(String paymentPort) {
		this.paymentPort = paymentPort;
	}

	public String getEnvironmentType() {
		return environmentType;
	}

	@JsonProperty("EnvironmentType")
	public void setEnvironmentType(String environmentType) {
		this.environmentType = environmentType;
		if (environmentType.equalsIgnoreCase("dev")) {
			dev = true;
		} else {
			dev = false;
		}
	}

	public String getFormsVersionIup250() {
		return formsVersionIup250;
	}

	@JsonProperty("FormsVersionIup250")
	public void setFormsVersionIup250(String formsVersionIup250) {
		this.formsVersionIup250 = formsVersionIup250;
	}

	public String getSignaturePort() {
		return signaturePort;
	}

	@JsonProperty("SignaturePort")
	public void setSignaturePort(String signaturePort) {
		this.signaturePort = signaturePort;
	}

	public String getJdalWebServiceAddress() {
		final StringBuilder sb = new StringBuilder();
		sb.append("http://0.0.0.0:");
		sb.append(signaturePort); // 3822
		sb.append("/jdalWebService");
		return sb.toString();
	}

	public String getFormsPath() {
		return formsPath;
	}

	@JsonProperty("FormsPath")
	public void setFormsPath(String formsPath) {
		this.formsPath = formsPath;
	}

	public int getEmvRetryAttempts() {
		if (emvRetryAttempts == 0) {
			return 20;
		} else
			return emvRetryAttempts;
	}

	@JsonProperty("EmvRetryAttempts")
	public void setEmvRetryAttempts(int emvRetryAttempts) {
		this.emvRetryAttempts = emvRetryAttempts;
	}

	public boolean isContactlessEnabled() {
		return contactlessEnabled;
	}

	@JsonProperty("ContactlessEnabled")
	public void setContactlessEnabled(boolean contactlessEnabled) {
		this.contactlessEnabled = contactlessEnabled;
	}

	public int getTimeoutPedal() {
		return timeoutPedal;
	}

	@JsonProperty("TimeoutPedal")
	public void setTimeoutPedal(int timeoutPedal) {
		this.timeoutPedal = timeoutPedal;
	}

	/**
	 * Returns jDal total timeout, this the time allowed from the beginning
	 * until the end of the transaction.
	 * 
	 * @return integer
	 */
	public final int getJdalTimeout() {
		return (timeoutPedal - TIME_ADJUST) * 1000;
	}

	public IinSource getIinSource() {
		return iinSource;
	}

	public void setIinSource(final String value) {
		this.iinSource = IinSource.valueOf(value);
	}

	public boolean isAllowPartialPayment() {
		return allowPartialPayment;
	}

	@JsonProperty("AllowPartialPayment")
	public void setAllowPartialPayment(final boolean value) {
		this.allowPartialPayment = value;
	}

	public String getThresholds() {
		return thresholds;
	}

	@JsonProperty("Thresholds")
	public void setThresholds(final String threasholds) {
		this.thresholds = threasholds;
	}
    
	public ProcessorPlatforms getProcessingPlatform() {
		return processingPlatform;
	}

	@JsonProperty("ProcessingPlatform")
	public void setProcessingPlatform(final String value) {
		try {
			processingPlatform = ProcessorPlatforms.valueOf(value);
		} catch (IllegalArgumentException e) {
			processingPlatform = ProcessorPlatforms.FDRC;
		}
	}
	
    @Override
    public String toString() {
	return "{" + 
			"\"MsrTimeout\" : \"" + msrTimeout + "\"," + 
			"\"SignatureCaptureTimeout\" : \"" + signatureCaptureTimeout + "\"," + 
			"\"ShowCvvForm\" : \"" + showCvvForm + "\"," + 
			"\"StartMode\" : \"" + startMode + "\"," + 
			"\"SignaturePort\" : \"" + signaturePort + "\"," + 
			"\"PaymentPort\" : \"" + paymentPort + "\"," + 
			"\"FormsPath\" : \"" + formsPath + "\"," + 
			"\"EnvironmentType\" : \"" + environmentType + "\"," + 
			"\"ContactlessEnabled\" : \"" + contactlessEnabled + "\"," + 
			"\"EmvRetryAttempts\" : \"" + emvRetryAttempts + "\""

			+ "}";
    }



}
