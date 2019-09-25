package com.trustcommerce.ipa.dal.bridge.types;

public final class Consts {


    /** Make it configurable. */
    
    public static final String VAULT_URL = "https://vault.trustcommerce.com/iin/lookuptrack.php";
    

    /** Applet uses this input parameter to process EMV debit cards. the value cannot be NULL, 
     * but the parameter can be ignored.*/
    public static final String IIN_SOURCE = "extended_fd";
    

    /** Timeout use when calling Vault web service. */
    public static final int DEVICETIMEOUT = 45000; // Timeout in milliseconds


	/** Title for the frame.*/
    public static final String GUI_PROCESS_PAYMENT_TITLE = "TC IPA     v. ";

}
