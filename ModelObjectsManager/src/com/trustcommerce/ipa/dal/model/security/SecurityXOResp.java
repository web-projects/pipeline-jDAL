package com.trustcommerce.ipa.dal.model.security;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;



/**
 * {"PrimeID":106,"CompanyID":199,"AppID":44888,"KeyType":"app","AppType":"Pedal","KeyVal":"kNaPCQygikYGS9x5+XeKJjwIJBvZoixg0MqPsxi24w0=","KeyValDecrypted":"","ErrorMessages":null,"Salt":null,"SaltAsString":"","UID":47,"UserName":""}

 * @author luisa.lamore
 *
 */
public class SecurityXOResp {

	/** The key PK identifier . */
	private int primeID;
	/** The company id. */
	private int companyID;
	/** The app id. */
	private int appID;
	/** Client or App. */
	private String keyType;
	/** Current bridge user. */
	private String userName;
	/** The type of app requesting this key . */
	private String appType;
	/** The key . */
	private String keyVal;
	/** The app key decyrypted . */
	private String keyValDecrypted;
	/** The error message returned from TCLINK . */
	private List<String> errorMessages;
	/** A salt if used. . */
	private byte[] salt;
	/** The salt as base64 for passing the salt as a parameter . */
	private String saltAsString;
	/**
	 * The UID representing access for this key. Each app must authenticate
	 * under a user name and password. .
	 */
	private int uid;

	public int getPrimeID() {
		return primeID;
	}

	@JsonProperty("PrimeID")
	public void setPrimeID(final int primeID) {
		this.primeID = primeID;
	}


	public int getCompanyID() {
		return companyID;
	}

	@JsonProperty("CompanyID")
	public void setCompanyID(final int companyID) {
		this.companyID = companyID;
	}

	public int getAppID() {
		return appID;
	}

	@JsonProperty("AppID")
	public void setAppID(final int appID) {
		this.appID = appID;
	}

	public String getKeyType() {
		return keyType;
	}
	
	@JsonProperty("KeyType")
	public void setKeyType(final String keyType) {
		this.keyType = keyType;
	}

	public String getUserName() {
		if (userName == null) {
			return "";
		}
		return userName;
	}

	@JsonProperty("UserName")
	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public String getAppType() {
		return appType;
	}
	
	@JsonProperty("AppType")
	public void setAppType(final String appType) {
		this.appType = appType;
	}

	public String getKeyVal() {
		return keyVal;
	}

	@JsonProperty("KeyVal")
	public void setKeyVal(final String keyVal) {
		this.keyVal = keyVal;
	}

	public String getKeyValDecrypted() {
		return keyValDecrypted;
	}

	public void setKeyValDecrypted(final String keyValDecrypted) {
		this.keyValDecrypted = keyValDecrypted;
	}

	
	public List<String> getErrorMessages() {
		return errorMessages;
	}

	@JsonProperty("ErrorMessages")
	public void setErrorMessages(final List<String> errorMessages) {
		this.errorMessages = errorMessages;
	}

	public byte[] getSalt() {
		return salt;
	}

	@JsonProperty("Salt")
	public void setSalt(final byte[] salt) {
		this.salt = salt;
	}

	public String getSaltAsString() {
		return saltAsString;
	}

	@JsonProperty("SaltAsString")
	public void setSaltAsString(final String saltAsString) {
		this.saltAsString = saltAsString;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	@Override
	public final String toString() {
		return "{" 
				+ "\"PrimeID\" : \"" + primeID + "\"," 
				+ "\"CompanyID\" : \"" + companyID + "\","
		        + "\"AppID\" : \"" + appID + "\"," 
				+ "\"KeyType\" : \"" + keyType + "\","
		        + "\"AppType\" : \"" + appType + "\"," 
		        + "\"KeyVal\" : \"" + keyVal + "\","
		        + "\"KeyValDecrypted\" : \"" + keyValDecrypted + "\"," 
		        + "\"ErrorMessages\" : \""
		        + errorMessages + "\"," 
		        + "\"Salt\" : \"" + salt + "\"," 
		        + "\"SaltAsString\" : \""
		        + saltAsString + "\"," 
		        + "\"UID\" : \"" + uid + "\","
		        + "\"UserName\" : \"" + userName + "\"" 
		        + "}";
	}

}
