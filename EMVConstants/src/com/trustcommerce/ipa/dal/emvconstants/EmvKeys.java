package com.trustcommerce.ipa.dal.emvconstants;

/**
 * Keys use to build the Properties object in the ARQC data
 * 
 * @author luisa.lamore
 *
 */
public enum EmvKeys {
	/** For ARQC this value is always the same. */
	KEY_ACTION("action"), 
	/** Test. */
	KEY_CUSTID("custid"),
	/** Test. */
	KEY_PASSWORD("password"),
	/** debit or credit. If the emv_processing_code is omitted, the transaction is by default �credit�. */
	KEY_PROCESSING_CODE("emv_processingcode"),
	/** Amount in cents. */
	KEY_AMOUNT("amount"),
	/** Always 'y'. */
	KEY_DEVICE_CAPABLE("emv_device_capable"),
	/** Condition that causes a transaction to be rejected when it was approved online. */
	KEY_TAC_DEFAULT("emv_tac_default"),
	/** Condition that causes a transaction denial without attempt to go online. */
	KEY_TAC_DENIAL("emv_tac_denial"),
	/** Condition that causes a transaction to go online. */
	KEY_TAC_ONLINE("emv_tac_online"),
	/** TSYS EMV. */
	KEY_DEVICE_SERIAL("device_serial"),
	/** TSYS EMV. */
	KEY_EMV_KERNEL_VERSION("emv_kernel_version"),
	/** TSYS EMV. */
	KEY_FALLBACK_TYPE("emv_fallback_type"),
	/** pin block. */
	KEY_PIN_BLOCK("pin"),
	
	KEY_FALLBACK("emv_fallback");

	private String keyName;

	EmvKeys(final String value) {
		keyName = value;
	}

	public String key() {
		return keyName;
	}

}
