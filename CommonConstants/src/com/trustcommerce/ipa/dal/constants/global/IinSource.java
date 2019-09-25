package com.trustcommerce.ipa.dal.constants.global;


/** The way each customer will connect. */
public enum IinSource {

	RapidConnect("extended_fd"),

	FirstData("fd"),

	BankOfAmerica("bofa");

	private String val;

	IinSource(final String value) {
		val = value;
	}

	public String getSource() {
		return val;
	}
}
