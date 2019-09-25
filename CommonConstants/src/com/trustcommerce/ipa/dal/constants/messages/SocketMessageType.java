package com.trustcommerce.ipa.dal.constants.messages;

public enum SocketMessageType {
	
	Unknown,
	
	Payment,
	
	Signature,
	
	FileUpload,
	
	TerminalInfo,
	
	/** Socket Status can be: Ready, Acknowledge, Error, Disconnected. */
	SocketStatus,
	/** Process status. */
	StatusCode,
	
	TransactionComplete,
	
	Reversal

}
