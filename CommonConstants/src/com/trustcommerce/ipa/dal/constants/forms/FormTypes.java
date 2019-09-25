package com.trustcommerce.ipa.dal.constants.forms;

/**
 * Ingenico PIN pad format specifiers and their corresponding ID numbers are
 * defined in the SECURPROMPT.XML files.
 * 
 * @author luisa.lamore
 *
 */
public enum FormTypes {


	AMOUNT_VERIFICATION("AMTVER.K3Z"),
	
	EXPIRATION_DATE("EXPDATE.K3Z"),
	
	CARD_NUMBER("CARDNUM.K3Z"),
	
	CVV("CVV2.K3Z"),
	
	PIN_NUMBER("PIN.K3Z"),

	SIGNATURE("SIG.K3Z"),

	SELECT_CARD_TYPE("SELECT.K3Z"),
	/** Swipe or insert card for EMV, Swipe for regular. */
	SWIPE("SWIPE.K3Z"),

	TRANSACTION_STATUS("TRANSTATUS.K3Z"),

	POST_BOOT("UIAPOSTBOOT.K3Z"),

	EMVLANG128("EMVLANG128.K3Z"),

	BLANK("BLANK12864.K3Z"),

	ZIPCODE("ZIPCODE.K3Z"),

	// EMV Forms
	SWIPE_INSERT("SWIPEINSERT.K3Z"),

	INSERT("INSERT.K3Z"),
	/** EMV form only for iUP/iUN devices. */
	REINSERT("REINSERT.K3Z"),
	
	MESSAGE("MESSAGE.K3Z"),
	
	/** Form displayed while updating the firmware version. */
	FIRMWARE_UPLOAD("UPDATE.K3Z");

	private String name;

	FormTypes(String val) {
		name = val;
	}

	public String getName() {
		return name;
	}

}
