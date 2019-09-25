package com.trustcommerce.ipa.dal.model.payment;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.jackson.annotate.JsonProperty;

import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.emvconstants.EmvTags;
import com.trustcommerce.ipa.dal.emvconstants.TransactionType;
import com.trustcommerce.ipa.dal.model.emv.ArpcData;
import com.trustcommerce.ipa.dal.model.types.EntryModeTypeId;

/**
 * This class should map the PaymentRequest object in the C# code.
 * @author luisa.lamore
 *
 */
public class Transaction extends TerminalData {

	/** Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(Transaction.class);
	
	
	/** The payment amount requested. */
	private String amount;
	/** The companyid. */
	private int companyID;
	/** Device Error number. */
	private int entryModeStatusID;
	/** The IPA customer id. Not used for bridge/pedal PaymentSystemType. */
	private int customerID;
	/** See TenderType table. */
	private int tenderTypeID;
	/** The TCLINK custid. */
	private int tcCustID;
	/** The TCLINK username being passed for processing. In the future this will not be passed. */
	private String tcLinkUserName;
	/** The TCLINK password being passed for processing. In the future this will not be passed.. */
	private String tcLinkPassword;
	/** The PaymentID for this transaction. Returned by calling StartPayment in IPAService. */
	private int paymentID;
	/** The PaymentSystemID 1= BridgePedal, 2=IPA Website . */
	private int paymentSystemTypeID;
	/** Identifies this transaction as being processed from a EMV custId or EMV company. 
	 * THIS VALUE IS INMUTABLE AT THE JDAL LEVEL. JDAL CAN NOT CHANGE IT*/
	private boolean isEMV;
	/** true is the EMV transaction ends up in a fallback. */
	private boolean emvFallback;
	/** 1 = sale, 2 = refund, 3=preAuth, 4=postauth, 5=void. See Transaction type. */
	private int paymentTypeID;
	/**
	 * This is the amount of money, in cents, the merchant will return to the
	 * cardholder. It is the portion of the "amount" field indicating the cash
	 * back. Merchants who want to provide cash back to the cardholder must
	 * provide this information as required by the acquiring bank. This amount
	 * may be "0" and if this field is omitted altogether, the "cashback" amount
	 * is considered to be "0".
	 */
	private String cashBack;
	/** Internal. */
	private String transactionID;
	/** Internal. */
	private String offenders;
	/** "error", the "errortype" field will also be included, providing more detail regarding the reason for the error.
	 * (see StatusCode, related to TCLINK). */
	private String errorType;
	/** see paymentTypeID. */
	private TransactionType transactionType;

	private boolean masterCard;
	private String connectionIDForIPALink;
	
	/** Technical fallback or MSR fallback. */
	private int emvFallBackTypeId;
	private int statusCodeID;

	private String workflowType;

	private boolean loadTestMode;

	private String connectionIDForDAL;
	private boolean previousTransactionSucces;
	private boolean requestToken;

	private String start;
	private boolean verifyTransaction;

	private String token;

	private boolean demoMode;
	private boolean store;
	private boolean lastPaymentUnstore;
	private boolean authorizeNow;
	private int numberOfPayments;

	private double firstAmount;
	private double lastAmount;

	private int billingCycle;
	private int billingCycleCount;
	private String pedalDNS;
	private String pedalIPv6;

	private String pedalIPv4;
	private String messageID;
	private double surcharge;

	private double originalAmountAuthorized;

	private String offlineAuthCode;
	private boolean statusLoggingOn;

	private boolean saveCard;
	private boolean allowPartialAuth;

	private String[] customName;
	private String[] customValues;
	private String createdBy;
	private String updatedBy;

	private int errorCode;

	/** Not used by jDAL. */
	private List<String> errorMessages;
	private int errorTypeID;

	private int originalTenderTypeID;
	private String originalTCTransID;

	private boolean tcIsTestAccount;
	private int ordinal;
	private int originalOrdinal;

	private boolean AVS;
	private String AVSAddress1;
	private String AVSZip;
	
	private boolean signatureRequired;

	// private Date shipDate;

	// Refunds
	private boolean debitRefund;
	/** for debit refund. First 6 digits of card. */
	private String iin;
	/** for debit refund. Last 4 digits of card. */
	private String lastFour;
	/** Amount to refund. */
	private int cashbackAmt;
	
	private String customFields;

	public Transaction() {
		super();
	}

	/**
	 * Constructor use to submit an error code.
	 * 
	 * @param entryModeStatusID
	 */
	public Transaction(final int entryModeStatusID) {
		super(entryModeStatusID);
	}

	/**
	 * Updates the Final EMV data Map by adding the members of this class.
	 * 
	 * @param transactionFields
	 *            Map<String, String>
	 */
	@SuppressWarnings("unused")
	private void updateEmvMap(final Map<String, String> transactionFields) {

		transactionFields.put("EntryModeTypeID", Integer.toString(EntryModeTypeId.EMV_Chip_Read.ordinal()));
		transactionFields.put("DebitCard", Boolean.toString(debitCard));
		//transactionFields.put("SerialNumber", serialNumber);
		//transactionFields.put("FirmwareVersionOnPED", firmwareVersion);

		final String status = transactionFields.get(EmvTags.T8A.tagName());
		if (status.equals(status)) {
			transactionFields.put("EntryModeStatusID", Integer.toString(EntryModeStatusID.Success.getCode()));
		} else {
			transactionFields.put("EntryModeStatusID", Integer.toString(EntryModeStatusID.Error.getCode()));
		}
		// Do not use EmvTags.T57 because this tag is removed from the final
		// data (Applet)
		transactionFields.put("IsEMV", Boolean.toString(isEMV));
		transactionFields.put("Track2", track2Data);
		transactionFields.put("Track3", track3Data);
		transactionFields.put("TransID", transactionID);
		transactionFields.put("CCExpirationDate", transactionFields.get(EmvTags.T5F24.tagName()));
		transactionFields.put("CustomerName", transactionFields.get(EmvTags.T5F20.tagName()));
		if (track2Data != null) {
			transactionFields.put("CreditCardNumber", track2Data.split("D")[0]);
		}
	}



	public int getCompanyID() {
		return companyID;
	}

	@JsonProperty("CompanyID")
	public void setCompanyID(final int companyID) {
		this.companyID = companyID;
	}

	public int getCustomerID() {
		return customerID;
	}

	@JsonProperty("CustomerID")
	public void setCustomerID(final int customerID) {
		this.customerID = customerID;
	}

	public int getTenderTypeID() {
		return tenderTypeID;
	}

	@JsonProperty("TenderTypeID")
	public void setTenderTypeID(final int tenderTypeID) {
		this.tenderTypeID = tenderTypeID;
	}

	public int getTcCustID() {
		return tcCustID;
	}

	@JsonProperty("TCCustID")
	public void setTcCustID(final int tcCustID) {
		this.tcCustID = tcCustID;
	}

	public String getTcLinkUserName() {
		return tcLinkUserName;
	}

	@JsonProperty("TCLinkUserName")
	public void setTcLinkUserName(final String tcLinkUserName) {
		this.tcLinkUserName = tcLinkUserName;
	}

	public String getTcLinkPassword() {
		return tcLinkPassword;
	}

	@JsonProperty("TCLinkPassword")
	public void setTcLinkPassword(final String tcLinkPassword) {
		this.tcLinkPassword = tcLinkPassword;
	}

	public int getPaymentID() {
		return paymentID;
	}

	/**
	 * 
	 * @param paymentID
	 */
	@JsonProperty("PaymentID")
	public void setPaymentID(final int paymentID) {
		this.paymentID = paymentID;
	}

	public int getPaymentSystemTypeID() {
		return paymentSystemTypeID;
	}

	@JsonProperty("PaymentSystemTypeID")
	public void setPaymentSystemTypeID(final int paymentSystemTypeID) {
		this.paymentSystemTypeID = paymentSystemTypeID;
	}

	public int getEntryModeStatusID() {
		return entryModeStatusID;
	}

	@JsonProperty("EntryModeStatusID")
	public void setEntryModeStatusID(final int entryModeStatusID) {
		this.entryModeStatusID = entryModeStatusID;
	}

	public boolean getIsEMV() {
		return isEMV;
	}

	@JsonProperty("IsEMV")
	public void setIsEMV(final boolean isEMV) {
		this.isEMV = isEMV;
	}

	public String getTransactionID() {
		return transactionID;
	}

	@JsonProperty("TransactionID")
	public void setTransactionID(final String transId) {
		this.transactionID = transId;
	}

	public String getOffenders() {
		return offenders;
	}

	@JsonProperty("Offenders")
	public void setOffenders(final String offenders) {
		this.offenders = offenders;
	}

	public String getErrorType() {
		return errorType;
	}

	@JsonProperty("ErrorType")
	public void setErrorType(final String errorType) {
		this.errorType = errorType;
	}

	public String getAmount() {
		return amount;
	}

	@JsonProperty("Amount")
	public void setAmount(final String amount) {
		/* Verify the amount to check it has two decimal points. */
		final String tempamount;
		final String[] amountArray;
		if (amount.indexOf(".") != -1) {
			amountArray = amount.split("\\.");
			if (amountArray[1].length() == 2) {
				this.amount = amount;
			} else if (amountArray[1].length() == 1) {
				tempamount = new StringBuilder().append(amount).append("0").toString();
				this.amount = tempamount;
			} else if (amountArray[1].length() > 2) {
				LOGGER.error("Amount with more than two decimal point is not allowed");

			}
		} else {
			tempamount = new StringBuilder().append(amount).append(".00").toString();
			this.amount = tempamount;
		}

	}

	public String getConnectionIDForIPALink() {
		return connectionIDForIPALink;
	}

	@JsonProperty("ConnectionIDForIPALink")
	public void setConnectionIDForIPALink(final String val) {
		this.connectionIDForIPALink = val;
	}

	public int getEmvFallBackTypeId() {
		return emvFallBackTypeId;
	}

	@JsonProperty("EMVFallBackTypeID")
	public void setEmvFallBackTypeId(final int val) {
		this.emvFallBackTypeId = val;
	}

	public int getStatusCodeID() {
		return statusCodeID;
	}

	@JsonProperty("StatusCodeID")
	public void setStatusCodeID(final int statusCodeID) {
		this.statusCodeID = statusCodeID;
	}

	public String getWorkflowType() {
		return workflowType;
	}

	@JsonProperty("WorkflowType")
	public void setWorkflowType(final String workflowType) {
		this.workflowType = workflowType;
	}

	public boolean isLoadTestMode() {
		return loadTestMode;
	}

	@JsonProperty("LoadTestMode")
	public void setLoadTestMode(final boolean loadTestMode) {
		this.loadTestMode = loadTestMode;
	}

	public String getConnectionIDForDAL() {
		return connectionIDForDAL;
	}

	@JsonProperty("ConnectionIDForDAL")
	public void setConnectionIDForDAL(final String connectionIDForDAL) {
		this.connectionIDForDAL = connectionIDForDAL;
	}

	public boolean isPreviousTransactionSucces() {
		return previousTransactionSucces;
	}

	@JsonProperty("PreviousTransactionSucces")
	public void setPreviousTransactionSucces(final boolean previousTransactionSucces) {
		this.previousTransactionSucces = previousTransactionSucces;
	}

	public boolean isRequestToken() {
		return requestToken;
	}

	@JsonProperty("RequestToken")
	public void setRequestToken(final boolean requestToken) {
		this.requestToken = requestToken;
	}

	public String getStart() {
		return start;
	}

	@JsonProperty("Start")
	public void setStart(final String start) {
		this.start = start;
	}

	public boolean isVerifyTransaction() {
		return verifyTransaction;
	}

	@JsonProperty("VerifyTransaction")
	public void setVerifyTransaction(final boolean verifyTransaction) {
		this.verifyTransaction = verifyTransaction;
	}

	public String getToken() {
		return token;
	}

	@JsonProperty("Token")
	public void setToken(final String token) {
		this.token = token;
	}

	public boolean isDemoMode() {
		return demoMode;
	}

	@JsonProperty("DemoMode")
	public void setDemoMode(final boolean demoMode) {
		this.demoMode = demoMode;
	}

	public boolean isStore() {
		return store;
	}

	@JsonProperty("Store")
	public void setStore(final boolean store) {
		this.store = store;
	}

	public boolean isLastPaymentUnstore() {
		return lastPaymentUnstore;
	}

	@JsonProperty("LastPaymentUnstore")
	public void setLastPaymentUnstore(final boolean lastPaymentUnstore) {
		this.lastPaymentUnstore = lastPaymentUnstore;
	}

	public boolean isAuthorizeNow() {
		return authorizeNow;
	}

	@JsonProperty("AuthorizeNow")
	public void setAuthorizeNow(final boolean authorizeNow) {
		this.authorizeNow = authorizeNow;
	}

	public int getNumberOfPayments() {
		return numberOfPayments;
	}

	@JsonProperty("NumberOfPayments")
	public void setNumberOfPayments(final int numberOfPayments) {
		this.numberOfPayments = numberOfPayments;
	}

	public double getFirstAmount() {
		return firstAmount;
	}

	@JsonProperty("FirstAmount")
	public void setFirstAmount(final double firstAmount) {
		this.firstAmount = firstAmount;
	}

	public double getLastAmount() {
		return lastAmount;
	}

	@JsonProperty("LastAmount")
	public void setLastAmount(final double lastAmount) {
		this.lastAmount = lastAmount;
	}

	public int getBillingCycle() {
		return billingCycle;
	}

	@JsonProperty("BillingCycle")
	public void setBillingCycle(final int billingCycle) {
		this.billingCycle = billingCycle;
	}

	public int getBillingCycleCount() {
		return billingCycleCount;
	}

	@JsonProperty("BillingCycleCount")
	public void setBillingCycleCount(final int billingCycleCount) {
		this.billingCycleCount = billingCycleCount;
	}

	public String getPedalDNS() {
		return pedalDNS;
	}

	@JsonProperty("PedalDNS")
	public void setPedalDNS(final String pedalDNS) {
		this.pedalDNS = pedalDNS;
	}

	public String getPedalIPv6() {
		return pedalIPv6;
	}

	@JsonProperty("PedalIPv6")
	public void setPedalIPv6(final String pedalIPv6) {
		this.pedalIPv6 = pedalIPv6;
	}

	public String getPedalIPv4() {
		return pedalIPv4;
	}

	@JsonProperty("PedalIPv4")
	public void setPedalIPv4(final String pedalIPv4) {
		this.pedalIPv4 = pedalIPv4;
	}

	public String getMessageID() {
		return messageID;
	}

	@JsonProperty("MessageID")
	public void setMessageID(final String messageID) {
		this.messageID = messageID;
	}

	public double getSurcharge() {
		return surcharge;
	}

	@JsonProperty("Surcharge")
	public void setSurcharge(final double surcharge) {
		this.surcharge = surcharge;
	}

	public String getOfflineAuthCode() {
		return offlineAuthCode;
	}

	@JsonProperty("OfflineAuthCode")
	public void setOfflineAuthCode(final String offlineAuthCode) {
		this.offlineAuthCode = offlineAuthCode;
	}

	public boolean isStatusLoggingOn() {
		return statusLoggingOn;
	}

	@JsonProperty("StatusLoggingOn")
	public void setStatusLoggingOn(final boolean statusLoggingOn) {
		this.statusLoggingOn = statusLoggingOn;
	}

	public boolean isSaveCard() {
		return saveCard;
	}

	@JsonProperty("SaveCard")
	public void setSaveCard(boolean saveCard) {
		this.saveCard = saveCard;
	}

	public boolean isAllowPartialAuth() {
		return allowPartialAuth;
	}

	@JsonProperty("AllowPartialAuth")
	public void setAllowPartialAuth(final boolean allowPartialAuth) {
		this.allowPartialAuth = allowPartialAuth;
	}

	public String[] getCustomName() {
		return customName;
	}

	@JsonProperty("CustomName")
	public void setCustomName(final String[] customName) {
		this.customName = customName;
	}

	public String[] getCustomValues() {
		return customValues;
	}

	@JsonProperty("CustomValues")
	public void setCustomValues(final String[] customValues) {
		this.customValues = customValues;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	@JsonProperty("UpdatedBy")
	public void setUpdatedBy(final String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	@JsonProperty("CreatedBy")
	public void setCreatedBy(final String createdBy) {
		this.createdBy = createdBy;
	}

	public int getErrorCode() {
		return errorCode;
	}

	@JsonProperty("ErrorCode")
	public void setErrorCode(final int errorCode) {
		this.errorCode = errorCode;
	}

	public List<String> getErrorMessages() {
		return errorMessages;
	}

	@JsonProperty("ErrorMessages")
	public void setErrorMessages(final List<String> errorMessages) {
		this.errorMessages = errorMessages;
	}

	public int getErrorTypeID() {
		return errorTypeID;
	}

	@JsonProperty("ErrorTypeID")
	public void setErrorTypeID(final int errorTypeID) {
		this.errorTypeID = errorTypeID;
	}

	public int getOriginalTenderTypeID() {
		return originalTenderTypeID;
	}

	@JsonProperty("OriginalTenderTypeID")
	public void setOriginalTenderTypeID(final int originalTenderTypeID) {
		this.originalTenderTypeID = originalTenderTypeID;
	}

	public String getOriginalTCTransID() {
		return originalTCTransID;
	}

	@JsonProperty("OriginalTCTransID")
	public void setOriginalTCTransID(final String originalTCTransID) {
		this.originalTCTransID = originalTCTransID;
	}

	public boolean isTCIsTestAccount() {
		return tcIsTestAccount;
	}

	@JsonProperty("TCIsTestAccount")
	public void setTCIsTestAccount(final boolean tcIsTestAccount) {
		this.tcIsTestAccount = tcIsTestAccount;
	}

	public int getOrdinal() {
		return ordinal;
	}

	@JsonProperty("Ordinal")
	public void setOrdinal(final int ordinal) {
		this.ordinal = ordinal;
	}

	public int getOriginalOrdinal() {
		return originalOrdinal;
	}

	@JsonProperty("OriginalOrdinal")
	public void setOriginalOrdinal(final int originalOrdinal) {
		this.originalOrdinal = originalOrdinal;
	}

	public double getOriginalAmountAuthorized() {
		return originalAmountAuthorized;
	}

	@JsonProperty("OriginalAmountAuthorized")
	public void setOriginalAmountAuthorized(final double originalAmountAuthorized) {
		this.originalAmountAuthorized = originalAmountAuthorized;
	}

	public boolean isAVS() {
		return AVS;
	}

	@JsonProperty("AVS")
	public void setAVS(boolean aVS) {
		AVS = aVS;
	}

	public String getAVSAddress1() {
		return AVSAddress1;
	}

	@JsonProperty("AVSAddress1")
	public void setAVSAddress1(final String aVSAddress1) {
		AVSAddress1 = aVSAddress1;
	}

	public String getAVSZip() {
		return AVSZip;
	}

	@JsonProperty("AVSZip")
	public void setAVSZip(String aVSZip) {
		AVSZip = aVSZip;
	}
	
	
	public boolean isSignatureRequired() {
		return signatureRequired;
	}
	@JsonProperty("SignatureRequired")
	public void setSignatureRequired(final boolean signatureRequired) {
		this.signatureRequired = signatureRequired;
	}
	
	/*
	 * public Date getShipDate() { return shipDate; }
	 * 
	 * @JsonProperty("ShipDate") public void setShipDate(Date shipDate) {
	 * this.shipDate = shipDate; }
	 */

	public void setWorkingAmount() {
		Double tempAmount = null;

		// checkup added because in previous cases the amount came with a
		// "comma" from DAL bridge
		if (this.amount != null) {
			// amount is null when a transaction started but timeout.
			if (amount.contains(",")) {
				this.amount = amount.replace(",", "");
			}
		}

		if (amount == null || amount.isEmpty()) {
			amount = "00.00";
		} else {
			tempAmount = Double.parseDouble(amount);
		}
	}

	/**
	 * The formated amount is for the device display.
	 * 
	 * @return
	 */
	public final String getFormattedAmount() {

		final DecimalFormat formatter = new DecimalFormat("#,###.00");
		final double temp = (double) (getAmountAsLong()) / 100;
		final String tempamount = formatter.format(temp);
		return tempamount;
	}

	/**
	 * Ingenico requires a Long value when processing debit cards. This method
	 * converts the amount to a Long value. TODO: This is how it was implemented
	 * in the original code. //TODO why?
	 * 
	 * @return
	 */
	public final Long getAmountAsLong() {
		Long temp;
		temp = Long.valueOf(amount.replace(".", ""));

		return temp;
	}

	private final Double getCashBackAsDouble() {
		Double d = null;
		if (cashBack == null || cashBack.isEmpty()) {
			d = 0.00;
		} else {
			try {
				d = Double.parseDouble(cashBack);
			} catch (NumberFormatException e) {
				LOGGER.warn("Unable to parse CashBack");
				d = 0.00;
			}
		}
		return d;
	}

	/**
	 * Amount in cents.
	 * 
	 * @return
	 */
	public final String getAmountInCents() {

		String temp2 = amount.replace(".", "");

		return temp2;
	}

	public TransactionType getTransactionType() {
		transactionType = TransactionType.values()[paymentTypeID];
		return transactionType;
	}

	public int getPaymentTypeID() {
		return paymentTypeID;
	}

	/**
	 * 1 = sale, 2 = refund, 3=preAuth, 4=postauth, 5=void. See TransactionType. 
	 * 
	 * @param paymentTypeID
	 */

	@JsonProperty("PaymentTypeID")
	public void setPaymentTypeID(final int paymentTypeID) {
		this.paymentTypeID = paymentTypeID;
	}

	/**
	 * Not used at this moment.
	 * @return
	 */
	public String getCashBack() {
		return cashBack;
	}

	@JsonProperty("CashBack")
	public void setCashBack(final String val) {
		if (cashBack == null) {
			this.cashBack = "0.00";
		} else {
			this.cashBack = val;
		}
	}

	public Boolean isCashback() {
		Double d = getCashBackAsDouble();
		if (d == null || d == 0.0) {
			return false;
		} else {
			return true;
		}
	}

	public void setMasterCard(final boolean masterCard) {
		this.masterCard = masterCard;
	}

	public boolean isMasterCard() {
		return masterCard;
	}

	// ====================================================
	public boolean isDebitRefund() {
		return debitRefund;
	}

	public void setDebitRefund(final boolean debitRefund) {
		this.debitRefund = debitRefund;
	}

	public String getIin() {
		return iin;
	}

	public void setIin(final String iin) {
		this.iin = iin;
	}

	public String getLastFour() {
		return lastFour;
	}

	public void setLastFour(final String lastFour) {
		this.lastFour = lastFour;
	}

	public int getCashbackAmt() {
		return cashbackAmt;
	}

	public void setCashbackAmt(final int cashbackAmt) {
		this.cashbackAmt = cashbackAmt;
	}

	public EntryModeTypeId getEntryModeType() {
		return EntryModeTypeId.values()[entryModeTypeID];
	}
	
	
	/**
	 * Resets ARQC and ARPC.
	 */
	public final void resetValues() {
		this.arpc = null;
		this.arqc = null;
		this.arqcProp = null;
	}

	/**
	 * 
	 * @return type boolean
	 */
	public final boolean isEmvFallback() {
		return emvFallback;
	}
	
	/**
	 * 
	 * @param val type boolean
	 */
	@JsonProperty("EMVFallback")
	public final void setEmvFallback(final boolean val) {
		this.emvFallback = val;
	}
	
	public String getCustomFields() {
		return customFields;
	}
	
	@JsonProperty("CustomFields")
	public void setCustomFields(String customFields) {
		this.customFields = customFields;
	}
	
	@Override
	public final String toString() {
		return "{" + cardInfoToString() + "\"Amount\" : \"" + amount + "\"," 
				+ "\"CompanyID\" : \"" + companyID + "\","
		        + "\"EntryModeStatusID\" : \"" + entryModeStatusID + "\"," 
				+ "\"CustomerID\" : \"" + customerID + "\","
		        + "\"OriginalTCTransID\" : \"" + originalTCTransID + "\"," 
		        + "\"AVS\" : \"" + AVS + "\"," 
				+ "\"AVSAddress1\" : \"" + AVSAddress1 + "\"," 
		        + "\"AVSZip\" : \"" + AVSZip + "\"," + "\"TenderTypeID\" : \"" + tenderTypeID + "\"," 
				+ "\"OriginalTenderTypeID\" : \"" + originalTenderTypeID + "\"," 
		        + "\"TCCustID\" : \"" + tcCustID + "\"," + "\"TCLinkUserName\" : \""
		        + tcLinkUserName + "\"," + "\"TCLinkPassword\" : \"" + tcLinkPassword + "\","
		        + "\"TCIsTestAccount\" : \"" + tcIsTestAccount + "\"," 
		        + "\"Ordinal\" : \"" + ordinal + "\","
		        + "\"OriginalOrdinal\" : \"" + originalOrdinal + "\"," + "\"PaymentID\" : \"" + paymentID + "\","
		        + "\"PaymentSystemTypeID\" : \"" + paymentSystemTypeID + "\"," 
		        + "\"PaymentTypeID\" : \"" + paymentTypeID + "\","
		        + "\"SignatureRequired\" : \"" + signatureRequired + "\","
		        + "\"EMVFallback\" : \"" + emvFallback + "\","
		        + "\"OriginalAmountAuthorized\" : \"" + originalAmountAuthorized
		        + "\"," + "\"DebitCard\" : \"" + debitCard 
		        + "\"," + "\"CashBack\" : \""  + cashBack + "\"," 
		        + "\"ErrorCode\" : \"" + errorCode + "\"," 
		        + "\"ErrorMessages \" : \"" + null + "\"," 
		        + "\"ErrorTypeID\" : \"" + errorTypeID + "\"," 
		        + "\"SaveCard\" : \"" + saveCard + "\"," 
		        + "\"AllowPartialAuth \" : \"" + allowPartialAuth + "\"," 
		        + "\"CreatedBy\" : \"" + createdBy + "\"," 
		        + "\"UpdatedBy \" : \"" + updatedBy + "\"," 
		        + "\"Surcharge\" : \"" + surcharge + "\","
		        + "\"OfflineAuthCode \" : \"" + offlineAuthCode + "\"," 
		        + "\"StatusLoggingOn\" : \"" + statusLoggingOn + "\"," 
		        + "\"PedalDNS\" : \"" + pedalDNS + "\"," 
		        + "\"PedalIPv6\" : \"" + pedalIPv6 + "\","
		        + "\"PedalIPv4\" : \"" + pedalIPv4 + "\"," 
		        + "\"MessageID\" : \"" + messageID + "\","
		        + "\"BillingCycle\" : \"" + billingCycle + "\"," 
		        + "\"BillingCycleCount\" : \"" + billingCycleCount
		        + "\"," + "\"FirstAmount\" : \"" + firstAmount + "\"," 
		        + "\"LastAmount\" : \"" + lastAmount + "\","
		        + "\"Store\" : \"" + store + "\"," 
		        + "\"LastPaymentUnstore\" : \"" + lastPaymentUnstore + "\","
		        + "\"AuthorizeNow\" : \"" + authorizeNow + "\"," 
		        + "\"NumberOfPayments\" : \"" + numberOfPayments
		        + "\"," + "\"Start\" : \"" + start + "\"," 
		        + "\"VerifyTransaction\" : \"" + verifyTransaction + "\","
		        + "\"Token\" : \"" + token + "\"," 
		        + "\"DemoMode\" : \"" + demoMode + "\","
		        + "\"PreviousTransactionSucces\" : \"" + previousTransactionSucces + "\"," 
		        + "\"RequestToken\" : \"" + requestToken + "\"," 
		        + "\"IsEMV\" : \"" + isEMV + "\"," 
		        + "\"ConnectionIDForIPALink\" : \"" + connectionIDForIPALink + "\"," 
		        + "\"EMVFallBackTypeID\" : \"" + emvFallBackTypeId + "\"," 
		        + "\"StatusCodeID\" : \"" + statusCodeID + "\"," 
		        + "\"CustomFields\" : \"" + customFields + "\","
		        + "\"WorkflowType\" : \"" + workflowType + "\","
		        + "\"LoadTestMode\" : \"" + loadTestMode + "\"," 
		        + "\"ConnectionIDForDAL\" : \"" + connectionIDForDAL
		        + "\"" + "}";
	}
	
	
	/**
	 * 
	 * @return
	 */
	public final String toLogFile() {
		return "{"
	            + "\"Amount\" : \"" + amount + "\"," 
				+ "\"CompanyID\" : \"" + companyID + "\","
		        + "\"EntryModeStatusID\" : \"" + entryModeStatusID + "\"," 
				+ "\"CustomerID\" : \"" + customerID + "\","
		        + "\"OriginalTCTransID\" : \"" + originalTCTransID + "\"," 
		        + "\"AVS\" : \"" + AVS + "\"," 
				+ "\"AVSAddress1\" : \"" + AVSAddress1 + "\"," 
		        + "\"AVSZip\" : \"" + AVSZip + "\"," 
				+ "\"TenderTypeID\" : \"" + tenderTypeID + "\"," 
				+ "\"OriginalTenderTypeID\" : \"" + originalTenderTypeID + "\"," 
		        + "\"TCCustID\" : \"" + tcCustID + "\"," 
		        + "\"TCIsTestAccount\" : \"" + tcIsTestAccount + "\"," 
		        + "\"Ordinal\" : \"" + ordinal + "\","
		        + "\"OriginalOrdinal\" : \"" + originalOrdinal + "\"," 
		        + "\"PaymentID\" : \"" + paymentID + "\","
		        + "\"PaymentSystemTypeID\" : \"" + paymentSystemTypeID + "\"," 
		        + "\"PaymentTypeID\" : \"" + paymentTypeID + "\","
		        + "\"SignatureRequired\" : \"" + signatureRequired + "\","
		        + "\"EMVFallback\" : \"" + emvFallback + "\","
		        + "\"OriginalAmountAuthorized\" : \"" + originalAmountAuthorized + "\"," 
		        + "\"DebitCard\" : \"" + debitCard + "\"," 
		        + "\"CashBack\" : \""  + cashBack + "\"," 
		        + "\"ErrorCode\" : \"" + errorCode + "\"," 
		        + "\"ErrorMessages \" : \"" + null + "\"," 
		        + "\"ErrorTypeID\" : \"" + errorTypeID + "\"," 
		        + "\"SaveCard\" : \"" + saveCard + "\"," 
		        + "\"AllowPartialAuth \" : \"" + allowPartialAuth + "\"," 
		        + "\"CreatedBy\" : \"" + createdBy + "\"," 
		        + "\"UpdatedBy \" : \"" + updatedBy + "\"," 
		        + "\"Surcharge\" : \"" + surcharge + "\","
		        + "\"OfflineAuthCode \" : \"" + offlineAuthCode + "\"," 
		        + "\"StatusLoggingOn\" : \"" + statusLoggingOn + "\"," 
		        + "\"MessageID\" : \"" + messageID + "\","
		        + "\"BillingCycle\" : \"" + billingCycle + "\"," 
		        + "\"BillingCycleCount\" : \"" + billingCycleCount + "\"," 
		        + "\"FirstAmount\" : \"" + firstAmount + "\"," 
		        + "\"LastAmount\" : \"" + lastAmount + "\","
		        + "\"Store\" : \"" + store + "\"," 
		        + "\"LastPaymentUnstore\" : \"" + lastPaymentUnstore + "\","
		        + "\"AuthorizeNow\" : \"" + authorizeNow + "\"," 
		        + "\"NumberOfPayments\" : \"" + numberOfPayments + "\","
		        + "\"Start\" : \"" + start + "\"," 
		        + "\"VerifyTransaction\" : \"" + verifyTransaction + "\","
		        + "\"DemoMode\" : \"" + demoMode + "\","
		        + "\"PreviousTransactionSucces\" : \"" + previousTransactionSucces + "\"," 
		        + "\"IsEMV\" : \"" + isEMV + "\"," 
		        + "\"EMVFallBackTypeID\" : \"" + emvFallBackTypeId + "\"," 
		        + "\"StatusCodeID\" : \"" + statusCodeID + "\"," 
		        + "\"CustomFields\" : \"" + "" + "\","
		        + "\"LoadTestMode\" : \"" + loadTestMode + "\"," 
		        + "\"ConnectionIDForDAL\" : \"" + "" + "\"" 
		        + "}";
	}
	
	
	public String basicInputToString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Amount: ");
		sb.append(amount);
		sb.append(",CreatedBy: ");
		sb.append(createdBy);
		sb.append(",IsEMV: ");
		sb.append(isEMV);
		return sb.toString();
	}

	public void updateDataFromArpc(final ArpcData data) {

		transactionID = data.getTransactionId();
		offenders = data.getOffenders();
		errorType = data.getError();
		authorizationCode = data.getAuthorizationCode();
		// data.getPartialAmount();
		// data.getErrorType();
		// data.getStatus();
	}


}
