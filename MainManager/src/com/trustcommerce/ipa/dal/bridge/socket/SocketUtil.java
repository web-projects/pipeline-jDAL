package com.trustcommerce.ipa.dal.bridge.socket;

import org.apache.log4j.Logger;

import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.constants.messages.SocketMessageType;


/**
 * 
 * @author luisa.lamore
 *
 */
public class SocketUtil {
	
	/** log4j logger. */
	private static final Logger LOGGER = Logger.getLogger(SocketUtil.class);

	
	/**
	 * This method is used to send messages to DAL using the TCP Socket.
	 * The method will take care of building a String that includes delimiters.
	 * @param messageType SocketMessageType
	 * @param message String Message
	 */
	public static void sendMessageToCaller(final SocketMessageType messageType, final String message) {
		DalActivator.sendMessage(messageType, message);
	}

	/**
	 * This method is used to send messages to DAL using the TCP Socket.
	 * The method will take care of building a String that includes delimiters.
	 * If the message code is timout or cancelled, the transaction will be marked as completed
	 * @param messageType MessageType
	 * @param message int Message
	 */
	public static void sendMessageToCaller(final SocketMessageType messageType, final EntryModeStatusID messageCode) {
		LOGGER.debug("-> sendMessageToCaller() " + messageCode);
		DalActivator.sendMessage(messageType, Integer.toString(messageCode.getCode()));
 	}
	
	public static void transactionComplete() {
		DalActivator.transactionComplete();
	}
	
	
	/**
	 * This method is used to send messages to DAL using the TCP Socket.
	 * The method will take care of building a String that includes delimiters.
	 * @param messageCode EntryModeStatusID 
	 */
	public static void sendErrorMessageToCaller(final EntryModeStatusID messageCode) {
		LOGGER.debug("-> sendErrorMessageToCaller() " + messageCode);
		DalActivator.sendMessage(SocketMessageType.StatusCode, Integer.toString(messageCode.getCode()));
	}
	
	
	/**
	 * This method is used to request DAL to terminate jDAL due to fatal connection issues.
	 * The termination send the 911 and also indicates the reason for the termination.
	 */
	public static void requestJdalTermination(final EntryModeStatusID messageCode) {
		LOGGER.warn("=================== 911 : Waiting for jDAL to Terminate =========================");
		DalActivator.cannotConnectToTerminal(messageCode);
	}

}
