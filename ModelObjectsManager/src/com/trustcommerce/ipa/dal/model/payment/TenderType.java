package com.trustcommerce.ipa.dal.model.payment;

public enum TenderType {

	TT1("Visa"),

	TT2("MasterCard"),

	TT3("American Express"),

	TT4("Diners Club"),

	TT5("EnRoute"),

	TT6("JCB"),

	TT7("Discover"),

	TT8("ACH"),

	TT9("Debit"),

	TT10("Debit Pinless"),

	TT17("Cash"),

	TT18("Check"),
	/** FSA/HSA.*/
	TT20("FSAHSA");

	private String value;

	TenderType(String value) {
		this.value = value;
	}

	public String getTenderTypeName() {
		return value;
	}
}
