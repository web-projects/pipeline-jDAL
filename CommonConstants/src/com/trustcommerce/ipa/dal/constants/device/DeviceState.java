package com.trustcommerce.ipa.dal.constants.device;


/**
 * Device state.
 * @author luisa.lamore
 *
 */
public enum DeviceState {
	READY,
	ERROR, 
	HEALTH_STATUS_ERROR,
	DONE,
	GOT_SERIAL,
	ACCEPTED,
	CANCELLED_BY_USER,
	TIMEOUT,
	SIGCAP_NOT_ENABLED,
	INPUT_ERROR,
	/** When the pinpad times out, Payer is requested to "try again". */
	PINPAD_TIMEOUT,
	ZIPCODE_INPUT_ERROR,
	CREDIT_SELECT,
	DEBIT_SELECT,
	DEBIT_MISSING_ENCRYPTION_KEY,
	MANUAL_ENTRY_OFF,
	MANUAL_ENTRY_ERROR,
	MANUAL_PAN_ENTRY,
	MANUAL_EXP_ENTRY,
	/** CVV entry form. */
	MANUAL_CVV_ENTRY,
	/** Direct setting of Manual CVV2 entry (no form). */
	DIRECT_CVV_ENTRY,
	MANUAL_ZIPCODE_ENTRY, 
	MANUAL_CREATE_TRACKS,
	MANUAL_ENTRY_DONE,
	MANUAL_ENTRY_CANCELED,
	DEVICE_NOT_CONNECTED, 
	INGENICO_REBOOT,
	PROGRESS_UPDATE,
	FORMS_UPDATED,
	FIRMWARE_UPDATED,
	UNKNOWN,
	TRANSACTION_APPROVED,
	TRANSACTION_DECLINED,
	EXPIRED_CARD,
	
    EMV_CARD_DETECTED,
    EMV_CARD_INSERTED,
    EMV_CARD_REMOVED,
    EMV_TRANS_PREP_READY,
    /** Authorization Request Cryptogram. generated whenever the card requests online authorization. */
    EMV_ARQC,
    /** Application Authentication Cryptogram. Generated whenever a card declines a transaction. */
    EMV_AAC,
    /** Transaction Certificate. A TC is a type of Application Cryptogram that is generated 
     * whenever a card approves a transaction. */
    EMV_TC,
    EMV_ONLINE_PIN,
    EMV_INVALID_CARD_DATA,
    EMV_CARD_BLOCKED,
    EMV_TRANS_CANCELED,
    EMV_APP_BLOCKED,
    EMV_USER_INTERFACE_TIMEOUT,
    EMV_CARD_NOT_SUPPORTED,
    EMV_AMOUNT_REJECTED,
    EMV_FINAL_DATA_IN_PROGRESS,
    REJECTED,
    PROGRESS
}


