package com.trustcommerce.ipa.dal.model.security;

public class SecurityXO {

	/** The key PK identifier . */
	private int primeID;
	/** The company id. */
	private int companyID;
	/** The app id. */
	private int appID;
	/**
	 * A placeholder for using a guid in the future to identify an app or a
	 * request.
	 */
	private String appGuid;
	/** A placeholder for authenticating a license key upon the request. */
	private String licenseKey;
	/** Client or App. */
	private String keyType;
	/** Current bridge user. */
	private String userName;
	
	public SecurityXO() {
		userName = "";
		licenseKey = "";
		appGuid = "";
	}

	public int getPrimeID() {
		return primeID;
	}

	public void setPrimeID(int primeID) {
		this.primeID = primeID;
	}

	public int getCompanyID() {
		return companyID;
	}

	public void setCompanyID(int companyID) {
		this.companyID = companyID;
	}

	public int getAppID() {
		return appID;
	}

	public void setAppID(int appID) {
		this.appID = appID;
	}

	public String getAppGuid() {
		if (appGuid == null) {
			return "";
		}
		return appGuid;
	}

	public void setAppGuid(String appGuid) {
		this.appGuid = appGuid;
	}

	public String getLicenseKey() {
		if (licenseKey == null) {
			return "";
		}
		return licenseKey;
	}

	public void setLicenseKey(String licenseKey) {
		this.licenseKey = licenseKey;
	}

	public String getKeyType() {
		return keyType;
	}

	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}

	public String getUserName() {
		if (userName == null) {
			return "";
		}
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public final String toString() {
	    return "{" 
	    		+ "\"PrimeID\" : \"" + primeID + "\","
	    		+ "\"CompanyID\" : \"" + companyID + "\","
	    		+ "\"AppID\" : \"" + appID + "\","
	    		+ "\"LicenseKey\" : \"" + licenseKey + "\","
	    		+ "\"KeyType\" : \"" + keyType + "\","
	    		+ "\"UserName\" : \"" + userName + "\""
	    		+ "}";
    }

}
