package com.trustcommerce.ipa.dal.bridge;

import org.apache.log4j.Logger;

import com.trustcommerce.ipa.dal.bridge.socket.SocketUtil;
import com.trustcommerce.ipa.dal.configuration.app.AppConfiguration;
import com.trustcommerce.ipa.dal.configuration.types.ConfigurationException;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.constants.messages.SocketMessageType;
import com.trustcommerce.ipa.dal.gui.controller.SwingController;
import com.trustcommerce.ipa.dal.model.Terminal;
import com.trustcommerce.ipa.dal.model.TransactionStatus;
import com.trustcommerce.ipa.dal.model.payment.Transaction;


/**
 * Main entry point to start the application.
 * 
 * @author TC
 *
 */
public class TransactionHelper {
	/** log4j logger.*/

	private static final Logger LOGGER = Logger.getLogger(TransactionHelper.class);
	/** */
	private PaymentBridge deviceBridge;
	/** */
	private boolean transactionComplete;

	/**
	 * Constructor invoke from Main.
	 * 
	 * @param payment Transaction
	 * @param terminalInfo terminalInfo
	 * @param gui SwingController
	 */
	public TransactionHelper(Transaction payment, Terminal terminalInfo, SwingController gui) {
		try {
			if (terminalInfo == null) {
				LOGGER.error("INTERNAL ERROR: Missing terminal Information. ");
				return;
			}
			deviceBridge = new PaymentBridge(payment, terminalInfo, gui);
		} catch (ConfigurationException e) {
			SocketUtil.sendMessageToCaller(SocketMessageType.StatusCode, EntryModeStatusID.BadData);
			return;
		}
		deviceBridge.initializeTransaction();
	}


	/**
	 * This only process the signature for Non-EMV transactions.
	 * @param status TransactionStatus
	 */
	public final void processSignature(final TransactionStatus status) {
		LOGGER.debug(" -> processSignature() " + status.toString());
		deviceBridge.processNonEmvTransactionStatus(status);
	}
	
	/**
	 * Calls the payment bridge to start the ARPC processing.
	 * @param arpc String
	 * @param errorCode integer
	 */
	public final void processArpc(final String arpc, final int errorCode) {
		LOGGER.debug(" -> processArpc() " + arpc);
		deviceBridge.handleAuthResponse(arpc, errorCode);
	}
	

	/**
	 * Set a transaction complete and performs a terminal clean up.
	 * @param message String
	 */
	public final void processTransactionComplete(final String message) {
		LOGGER.debug(" -> processTransactionComplete() ");
		if (!transactionComplete) {
			if (message == AppConfiguration.getLanguage().getString("JDAL_TERMINATION")) {
				deviceBridge.deviceCleanup();
			}
			deviceBridge = null;
			transactionComplete = true;
		}
	}


}