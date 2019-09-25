package com.trustcommerce.ipa.dal.model;

import org.codehaus.jackson.annotate.JsonProperty;

import com.trustcommerce.ipa.dal.model.types.TransactionStatusID;

public class TransactionStatus {

    private int companyID;

    private int paymentID;

    private int paymentTenderID;

    private int appID;

    private int transactionStatusID;

    private boolean signatureRequired;

    private String signatureAsString;

    private String signatureImageFormat;

    // public List<String> errorMessages;

    /**
     * JSon constructor.
     */
    public TransactionStatus() {
        signatureImageFormat = "png";
    }

    /**
     * 
     * @param deviceName
     * @param amount
     * @param transactionApproved int
     * @param requestSignature boolean
     */
    public TransactionStatus(final int transactionApproved, final boolean requestSignature) {

        this.transactionStatusID = transactionApproved;
        this.signatureRequired = requestSignature;
        this.setSignatureImageFormat("png");
    }
    /**
     * 
     * @return boolean
     */
    public final boolean isApproved() {
        if (transactionStatusID == TransactionStatusID.Approved.getCode() 
                || transactionStatusID == TransactionStatusID.Accepted.getCode()) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * 
     * @return String
     */
    public final String getSignatureAsString() {
        return signatureAsString;
    }
    /**
     * 
     * @param value String
     */
    @JsonProperty("SignatureAsString")
	public final void setSignatureAsString(final String value) {
        this.signatureAsString = value;
    }

//    public String getImage(byte[] signature) {
//        return Base64.encodeBase64String(signature);
//
//    }
    /**
     * 
     * @return String
     */
    public final String getSignatureImageFormat() {
        return signatureImageFormat;
    }
    /**
     * 
     * @param value String
     */
    @JsonProperty("SignatureImageFormat")
	public final void setSignatureImageFormat(final String value) {
        this.signatureImageFormat = value;
    }
    /**
     * 
     * @return boolean
     */
    public final boolean isSignatureRequired() {
        return signatureRequired;
    }
    /**
     * 
     * @param requestSignature boolean
     */
    @JsonProperty("SignatureRequired")
	public final void setSignatureRequired(final boolean requestSignature) {
        this.signatureRequired = requestSignature;
    }
    /**
     * 
     * @return int
     */
    public final int getCompanyID() {
        return companyID;
    }
   /**
    *@param companyID int
    */
    @JsonProperty("CompanyID")
	public final void setCompanyID(final int companyID) {
        this.companyID = companyID;
    }
    /**
     * 
     * @return int
     */
    public final int getPaymentID() {
        return paymentID;
    }
    /**
     * 
     * @param paymentID int
     */
    @JsonProperty("PaymentID")
	public final void setPaymentID(final int paymentID) {
        this.paymentID = paymentID;
    }
    /**
     * 
     * @return int
     */
    public final int getPaymentTenderID() {
        return paymentTenderID;
    }
    /**
     * 
     * @param paymentTenderID int
     */
    @JsonProperty("PaymentTenderID")
	public final void setPaymentTenderID(final int paymentTenderID) {
        this.paymentTenderID = paymentTenderID;
    }
    /**
     * 
     * @return int
     */
    public final int getAppID() {
        return appID;
    }
    /**
     * 
     * @param appID int
     */
    @JsonProperty("AppID")
	public final void setAppID(final int appID) {
        this.appID = appID;
    }
    /**
     * 
     * @return int
     */
    public final int getTransactionStatusID() {
        return transactionStatusID;
    }
    /**
     * 
     * @param transactionStatusID int
     */
    @JsonProperty("TransactionStatusID")
	public final void setTransactionStatusID(final int transactionStatusID) {
        this.transactionStatusID = transactionStatusID;
    }

    @Override
	public final String toString() {
        return "{" + "\"CompanyID\" : \"" + companyID + "\"," + "\"PaymentID\" : \"" + paymentID + "\","
                + "\"PaymentTenderID\" : \"" + paymentTenderID + "\"," + "\"AppID\" : \"" + appID + "\","
                + "\"TransactionStatusID\" : \"" + transactionStatusID + "\"," + "\"SignatureRequired\" : \""
                + signatureRequired + "\"," 
                + "\"SignatureAsString\" : \"" + signatureAsString + "\"," 
                + "\"SignatureImageFormat\" : \""
                + signatureImageFormat + "\"" + "}";
    }

}
