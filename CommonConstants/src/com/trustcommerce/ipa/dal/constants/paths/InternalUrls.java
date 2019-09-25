package com.trustcommerce.ipa.dal.constants.paths;


/**
 * When adding a new url to this class, the corresponding mapping should be added to
 * PresentationController.java.
 * 
 * @author luisa.lamore
 *
 */
public class InternalUrls {

	public static final String SELECT = "http://localhost:8080/select";
	
	public static final String VERIFY_AMOUNT = "http://localhost:8080/verifyAmount?amount=";
	
	
	/**
	 * This page needs to be active for a longer time because it submits data to server.
	 */
	public static final String SUBMIT = "http://localhost:8080/submit?message=";
	
	public static final String CVV = "http://localhost:8080/cvv";
	
	public static final String EXP_DATE = "http://localhost:8080/expiration";
	
	public static final String SIGNATURE = "http://localhost:8080/signature";
	
	public static final String CARD_NUMBER = "http://localhost:8080/cardNumber";
	
	public static final String NAMES = "http://localhost:8080/names";
	
	public static final String DISPLAY_INFO = "http://localhost:8080/infoPage?message=";
	
	// Manual transaction pages
	public static final String MANUAL_PROMPS = "http://localhost:8080/manualInfo?message=";
	
	public static final String MANUAL_START = "http://localhost:8080/manual";
	
	public static final String MANUAL_USER_NAMES = "http://localhost:8080/nameEntry";
}
