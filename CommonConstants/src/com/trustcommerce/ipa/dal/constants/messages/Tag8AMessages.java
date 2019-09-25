package com.trustcommerce.ipa.dal.constants.messages;

public enum Tag8AMessages {

	M01("TAG8A_Messages_M01"),
	//M07("Pick up the card. There is a problem with the card. It has been marked for fraud. Call the 800 number on the back of the card to determine the issue."),
	M41("TAG8A_Messages_M41"),
	M13("TAG8A_Messages_M13"),
	M14("TAG8A_Messages_M14"),
	//M05("The transaction was declined without explanation by the card issuer."),
	M51("TAG8A_Messages_M51"),
	M61("TAG8A_Messages_M61"),
	M54("TAG8A_Messages_M54"),
	M12("TAG8A_Messages_M12"),
	M55("TAG8A_Messages_M55"),
	M22("TAG8A_Messages_M22"),
	M39("TAG8A_Messages_M39"),
	M65("TAG8A_Messages_M65"),
	M86("TAG8A_Messages_M86"),
	M87("TAG8A_Messages_M87"),
	MT3("TAG8A_Messages_MT3"),
	M75("TAG8A_Messages_M75"),
	M43("TAG8A_Messages_M43");
	
	private String code;
	
	Tag8AMessages(String code) {
		this.code = code;
	}
	
	public String get8AMessagesCode() {
		return code;
	}
}
