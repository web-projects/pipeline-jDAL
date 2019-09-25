package com.trustcommerce.ipa.dal.device.types;


public class DeviceConsts {

	// TODO configurable 
	public static final boolean DISPLAY_ZIPCODE_FORM = false;
	
	public static final String VERSION_FILE = "FORMSVER.TXT";
	
    public static final String DFS_EMV_KEY = "DFS_0071_0001";
    
    public static final String DFS_IDENTITY_DATE = "DFS_0091_0010";
    /** A read-only variable containing the encryption type: 0=no-encryption, 11 generic TDES DUKPT encryption. */
    public static final String DFS_ENCRYPTION_CONFIG = "DFS_0091_0001";
    /** read-only variable indicating whether Track 1 or 2 should be returned encrypted for a card swipe. */
    public static final String DFS_TRACK12_ENCRYPTED = "DFS_0091_0017";
    
    public static final String EMV_KERNEL_VER = "EMV_KERNEL_VER";
    
    public static final String KEYSTATUS = "KEYSTATUS";

	
}
