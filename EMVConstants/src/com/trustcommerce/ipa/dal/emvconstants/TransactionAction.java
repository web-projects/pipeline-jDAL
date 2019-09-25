package com.trustcommerce.ipa.dal.emvconstants;

public enum TransactionAction {

	
	/** to be included in the final emv data. */
	AUTH_CONFIRMATION("emv_auth_confirmation"),
	/** to be included in the final emv data. */
	REFUND("emv_refund_confirmation"),
	
	AUTH("emv_auth"),
	
	SALE("sale"),
	
	CREDIT("credit2"),
	
	VOID("void");
	
	private String action;
	
	TransactionAction(final String val) {
		action = val;
	}
	
	public String getAction() {
		return action;
	}
}
