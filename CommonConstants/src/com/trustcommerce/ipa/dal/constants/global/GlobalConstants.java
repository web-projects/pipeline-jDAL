package com.trustcommerce.ipa.dal.constants.global;

/**
 * Global constants are here temporarily until they get moved to the app config.
 * 
 * @author luisa.lamore
 *
 */
public final class GlobalConstants {

	private GlobalConstants() {
		// to prevent instantiation
	}

	public static final String DEFAULT_TC_HOME = "C:\\TrustCommerce";

	public static final String TC_HOME = "TC_HOME";

	public static final boolean LOCAL_TEST = false;
	
	public static final long MESSAGE_WARN_SLEEP_TIMER = 4000;
	
	public static final long MESSAGE_INFO_SLEEP_TIMER = 1500;
	
	public static final long MESSAGE_ERROR_SLEEP_TIMER = 6000;
	
	public static final String DUKPT_KEY = "DFS_0091_0002";
	
	public static final String TRACK2 = "track2";
	
	public static final String TRACK1 = "track1";
	
	public static final long  RECONNECT_TIMER = 40000;
	
	public static final boolean SHOW_SIGNATURE = true;
	
    public static final String MIN_EMV_APP_VERSION = "13.1.12";
    
    public static final String FORM_PACKAGE_SUFFIX = "EMV";
	
}
