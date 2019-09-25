package com.trustcommerce.ipa.dal.emvconstants;

public enum DebitApplicationIdentifiers {

	/** can also be used as credit, then pinblock is null. */
	// A000000003101002("Visa International", "VISA Debit"),
	/** can also be used as credit, then pinblock is null. */
	//A0000000032010("Visa International", "VISA Electron"),
	/** Mastercard International. */
	A0000000043060("Mastercard International", "Maestro (Debit)"),
	
	/** Mastercard International. */
	A000000004306001("Mastercard International", "Maestro (Debit)"),
	
	/** Mastercard International. */
	A0000000042203("Mastercard International", "MasterCard Specific - MasterCard U.S. Maestro"),
	/** Mastercard International. */
	A0000000046000("Mastercard International", "Cirrus"),
	/** Visa USA. Visa card. */
	A000000098("Visa USA", "Debit Card"),
	/** Visa USA. Visa Common Debit.*/
	A0000000980840("Visa USA", "Visa Common Debit"),
	/** Visa USA. Debit Card*/
	A0000000980848("Visa USA", "Debit Card"),
	/** Mastercard International. Maestro TEST. */
	B012345678("MasterCard International", "Maestro TEST"),
	/** Diners Club International Ltd. */
	A0000001524010("Diners Club International Ltd.", "Discover");
	
	private String vendor;

	private String cardname;

	private DebitApplicationIdentifiers(final String vendor, final String name) {
		this.vendor = vendor;
		this.cardname = name;
	}
	
	public static boolean isDebit(final String aid) {
		
		boolean isDebit = false;
		for (DebitApplicationIdentifiers aids: DebitApplicationIdentifiers.values()) {
			if (aid.contains(aids.name())) {
				isDebit =  true;
				break;
			} 
		}
		return isDebit;
	}
	
	public String getVendorName() {
		return vendor + cardname;
	}
}
