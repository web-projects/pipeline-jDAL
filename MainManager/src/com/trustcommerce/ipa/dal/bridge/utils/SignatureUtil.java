package com.trustcommerce.ipa.dal.bridge.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.constants.device.TerminalModel;
import com.trustcommerce.ipa.dal.model.emv.EmvFinalData;
import com.trustcommerce.ipa.dal.model.payment.Transaction;


/**
 * Requirement: Offline transactions always ask for signature
 * Debit transactions never require signature
 * 
 * @author luisa.lamore
 *
 */
public final class SignatureUtil {
	
	private SignatureUtil() {
		
	}
	
	/** slf4j logger.*/
	private static final Logger LOGGER = LoggerFactory.getLogger(SignatureUtil.class);
	
	/**
	 * @param transaction
	 *            Transaction
	 * @param emvFinalData
	 *            EmvFinalData
	 * @param terminalModel
	 *            TerminalModel
	 */
	public static void isSignatureRequired(final Transaction transaction, final EmvFinalData emvFinalData,
			final boolean isSignatureCapabl) {

		if (transaction.getPINBlock() == null || transaction.getPINBlock().isEmpty()) {
			signatureRequired(transaction, emvFinalData, isSignatureCapabl);
		} else {
			LOGGER.info("Pin number was entered by Payer, no signature required");
			transaction.setSignatureRequired(false);
		}
	}	

	
	/**
	 * Determines if the signature is required for EMV offline transactions.
	 * Later if the transaction is approved, the signature pin pad should open and the signature should be
	 * sent back to the server.
	 * @param transaction Transaction
	 * @param emvFinalData EmvFinalData
	 * @param terminalModel TerminalModel
	 */
	private static void signatureRequired(final Transaction transaction,
			final EmvFinalData emvFinalData, final boolean isSignatureCapable) {
		LOGGER.debug("-> isSignatureRequired()  transactionApproved: {} , isDebitCard {} ", 
				emvFinalData.getEmvFinalStatus(),  transaction.isDebitCard());

		if (emvFinalData.getEmvFinalStatus().equals("approved")) {
			
			// the message transaction approved is displayed after receiving the signature
			if (isSignatureCapable) {
				// Offline EMV transaction use the threashold amount from the config file
				final boolean overLimit = isOverThresholdLimit(emvFinalData);
				if ((!transaction.isDebitCard() && !overLimit) || emvFinalData.isEMVOfflineTransaction()) {
					// Requirement: Offline transactions always ask for signature
					LOGGER.info("Requesting a signature from Payer");
					transaction.setSignatureRequired(true);
				}
			}
		} else {
			LOGGER.info("Signature is not required");
		}
	}
	/**
	 * 
	 * @param emvFinalData EmvFinalData
	 * @return boolean
	 */
	
	private static boolean isOverThresholdLimit(final EmvFinalData emvFinalData) {
		String aid = emvFinalData.getApplicationAid();
		LOGGER.debug("aid from tag = {}", aid);
		if (aid != null && !aid.isEmpty()) {
			if (aid.length() >= 10) {
				aid = aid.substring(0, 10);
			} else if (aid.length() >= 9) {
				aid = aid.substring(0, 9);
			}
		}

		// amount in cents
		final int thresholdAmount = ClientConfigurationUtil.getThresholdForAID(aid);
		if (thresholdAmount > emvFinalData.getAmountauthorized()) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean overThreshold(int transactionAmount) {
		final int thresholdAmount = 2500;
		if (thresholdAmount < transactionAmount) {
			return true;
		} else {
			return false;
		}
	}
	
	
}
