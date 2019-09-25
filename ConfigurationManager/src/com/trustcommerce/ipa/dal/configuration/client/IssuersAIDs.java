package com.trustcommerce.ipa.dal.configuration.client;




/**
 * Registered Application Provider Identifier. RID.
 * 
 * The list does not inlcude the Registered Application Provider Identifier. (PIX).
 * 
 * @author luisa.lamore
 *
 */
public enum IssuersAIDs {

    /**Visa .*/
	A000000003("Visa"),
	/**MasterCard .*/
    A000000004("MasterCard"),
    /** .*/
    A000000005("MasterCard"),
    /** JCB.*/
    A000000065("JCB"),
    /** .*/
    A000000025("American Express"),
    /** Diners Club .*/
    A000000324("Diners Club"),
    /** Diners Club.*/
    A000000152("Diners Club");
 
    private String issuer;
    
	private IssuersAIDs(final String val) {
		issuer = val;
	}

	public String getIssuer() {
		return issuer;
	}
    
    
}
