package com.trustcommerce.ipa.dal.emvconstants;

/**
 * If tag reads "XX" is because we are not using it. pin
 * 
 * @author luisa.lamore
 *
 */
public enum EmvTags {
	/** Application Identifier (ADF Name). */
	T4F("emv_4f_applicationidentifiericc"),
	/** Mnemonic associated with the AID. i.e. MasterCard*/
	T50("emv_50_applicationlabel"),
	/** track2. */
	T57("track2"),
	/** Application Primary Account Number (PAN). */
	T5A(""),
	/** Name of an individual. */
	T5B(""),
	/** ARPC data. Contains proprietary issuer data for transmission to the ICC before the second GENERATE AC command.*/
	T71("emv_71_issuerscripttemplate1"),
	/** ARPC data. Contains proprietary issuer data for transmission to the ICC after the second GENERATE AC command*/
	T72("emv_72_issuerscripttemplate2"),
	/**
	 * Application Interchange Profile (AIP). Indicates the capabilities of the
	 * card to support specific functions in the application
	 */
	T82("emv_82_applicationinterchangeprofile"),
	/** Dedicated File (DF) Name. */
	T84("emv_84_dedicatedfilename"),
	/**
	 * Authorization Response Code (ARC). Transaction disposition of the
	 * transaction received from the issuer for online authorizations. Source: Issuer/Terminal.
	 * ARPC.
	 */
	T8A("emv_8a_authorizationresponsecode"),
	/** Identifies a prioritised list of methods of verification of the cardholder supported by the card application. */
	T8E("emv_8e_cardholderverificationmethodlist"),
	/** ARPC.. (not in ARQC) . Issuer Authentication Data. */
	T91("emv_91_issuerauthenticationdata"),
	/** . */
	T95("emv_95_terminalverificationresults"),
	/** . */
	T9A("emv_9a_transactiondate"),
	/** . */
	T9B("emv_9b_transactionstatusinformation"),
	/**Indicates the type of financial transaction, represented by the first two digits of ISO 8583:1987
	 * Processing Code. value found 00. */
	T9C("emv_9c_transactiontype"),
	/** Cardholder Name. */
	T5F20("name"),
	/** Application Expiration Date .The date is expressed in the YYMMDD format. Master card adds 20YY */
	T5F24("emv_5f24_applicationexpirationdate"),
	/** . */
	T5F28("emv_5f28_issuercountrycode"),
	/** . */
	T5F2A("emv_5f2a_transactioncurrencycode"),
	/** . */
	T5F2D("emv_5f2d_languagepreference"),
	/** . */
	T5F30("emv_5f30_servicecode"),
	/** . */
	T5F34("emv_5f34_cardsequenceterminalnumber"),
	/** . */
	T5F39("emv_5f39_posentrymode"),
	/** In ARPC. Amount authorized can be a a Partial amount*/
	T9F02("emv_9f02_amountauthorized"),
	/** . */
	T9F03("emv_9f03_amountother"),
	/** . */
	T9F06("emv_9f06_applicationidentifierterminal"),
	/** optional. */
	T9F07("emv_9f07_applicationusagecontrol"),
	/** optional. */
	T9F08("emv_9f08_applicationversionnumbericc"),
	/** . */
	T9F09("emv_9f09_applicationversionnumberterminal"),
	/** optional. */
	T9F0D("emv_9f0d_issueractioncodedefault"),
	/** optional. */
	T9F0E("emv_9f0e_issueractioncodedenial"),
	/** optional. */
	T9F0F("emv_9f0f_issueractioncodeonline"),
	/** Contains proprietary application data for transmission to the issuer in an online transaction. 
	 * i.e. 06010A03A42002 . */
	T9F10("emv_9f10_issuerapplicationdata"),
	/** Indicates the code table according to ISO/IEC 8859 for displaying the Application Preferred Name. Missing. */
	T9F11("emv_9f11_issuercodetableindex"),
	/** Preferred mnemonic associated with the AID. Missing. */
	T9F12("emv_9f12_applicationpreferredname"),
	/** Lower Consecutive Offline Limit (LCOL). Missing. */
	T9F14("emv_9f14_lowerconsecutiveofflinelimit"),
	/** Number of PIN tries remaining. Missing. */
	T9F17("emv_9f17_personalidentificationnumbertrycounter"),
	/** . */
	T9F1A("emv_9f1a_terminalcountrycode"),
	/** . */
	T9F1E("emv_9f1e_interfacedeviceserialnumber"),
	/** . */
	T9F21("emv_9f21_transactiontime"),
	/** . */
	T9F26("emv_9f26_applicationcryptogram"),
	/** . */
	T9F27("emv_9f27_cryptograminformationdata"),
	/** . */
	T9F33("emv_9f33_terminalcapabilities"),
	/** . */
	T9F34("emv_9f34_cardholderverificationmethodresults"),
	/** . */
	T9F35("emv_9f35_terminaltype"),
	/** ARPC data. */
	T9F36("emv_9f36_applicationtransactioncounter"),
	/** . */
	T9F37("emv_9f37_unpredictablenumber"),
	/** Indicates the method by which the PAN was entered. Value = 05 when transaction is EMV online.
	 * Offline transactions do not returned this value. This value is generated in the ARQC */
	T9F39("emv_9f39_posentrymode"),
	/** . */
	T9F40("emv_9f40_additionalterminalcapabilities"),
	/** . */
	T9F41("emv_9f41_transactionsequencecounter"),
	/** . */
	T9F53("emv_9f53_transactioncategorycode"),
	/** . */
	TDF03("emv_tac_default"),
	/** . */
	TDF04("emv_tac_denial"),
	/** . */
	TDF05("emv_tac_online"),

	/** . */
	TFF21("encryptedtrack"),
	
	/** Ingenico firmware 19.6.2 CHANGED TFF21 to DFF21 **/
	DFF21("encryptedtrack");

	private String tag;

	EmvTags(final String value) {
		tag = value;
	}

	public String tagName() {
		return tag;
	}
}
