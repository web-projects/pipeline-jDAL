package com.trustcommerce.ipa.dal.emvconstants;

public enum Tag4F {

	
	A00000003("Visa"),
	
	A00000004("MasterCard"),
	
	A00000152("Discover"),
	
	A00000065("JCB"),
	
	A00000025("AmericanExpress"),
	
	A00000620("DNA");
	
	
	private String aid;
	
	Tag4F(final String value) {
		aid = value;
	}
	
	public String getApplicationName() {
		return aid;
	}
	
}
