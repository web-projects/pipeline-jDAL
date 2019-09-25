package com.trustcommerce.ipa.dal.model.types;

public enum TransactionStatusID {

	Approved(100),
	
	Accepted(101),
	
	Decline(102),
	
	Baddata(103),
	
	Error(104);
	
	// Disregard this one
	// Declined (105);
	
	private int code;
	
	TransactionStatusID(final int value) {
		code = value;
	}
	
	public int getCode() {
		return code;
	}
	
}
