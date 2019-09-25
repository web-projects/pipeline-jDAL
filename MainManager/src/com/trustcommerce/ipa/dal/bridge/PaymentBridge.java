package com.trustcommerce.ipa.dal.bridge;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.trustcommerce.ipa.dal.bridge.model.CardInfo;
import com.trustcommerce.ipa.dal.bridge.model.CardInfo.CardType;
import com.trustcommerce.ipa.dal.bridge.observers.emv.EMVFinalRemoveListener;
import com.trustcommerce.ipa.dal.bridge.observers.emv.EMVFirstACListener;
import com.trustcommerce.ipa.dal.bridge.observers.emv.EMVPinListener;
import com.trustcommerce.ipa.dal.bridge.observers.emv.EMVRemoveAfterCancelListener;
import com.trustcommerce.ipa.dal.bridge.observers.emv.EMVRemoveForRetry;
import com.trustcommerce.ipa.dal.bridge.observers.emv.EMVSecondACListener;
import com.trustcommerce.ipa.dal.bridge.observers.emv.EMVTransactionPreparationListener;
import com.trustcommerce.ipa.dal.bridge.socket.SocketUtil;
import com.trustcommerce.ipa.dal.bridge.types.Consts;
import com.trustcommerce.ipa.dal.bridge.utils.SignatureUtil;
import com.trustcommerce.ipa.dal.configuration.app.AppConfiguration;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfiguration;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.configuration.types.ConfigurationException;
import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.constants.device.StartMode;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;
import com.trustcommerce.ipa.dal.constants.device.TerminalModel;
import com.trustcommerce.ipa.dal.constants.forms.SwipeMode;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.constants.messages.ShortMessages;
import com.trustcommerce.ipa.dal.constants.messages.SocketMessageType;
import com.trustcommerce.ipa.dal.constants.messages.Tag8AMessages;
import com.trustcommerce.ipa.dal.constants.paths.TcipaFiles;
import com.trustcommerce.ipa.dal.device.IngenicoMsr;
import com.trustcommerce.ipa.dal.device.IngenicoSignature;
import com.trustcommerce.ipa.dal.device.emv.EMVDataHandler;
import com.trustcommerce.ipa.dal.device.exceptions.CardReadException;
import com.trustcommerce.ipa.dal.device.interfaces.SignatureProcessor;
import com.trustcommerce.ipa.dal.device.interfaces.TransactionProcessor;
import com.trustcommerce.ipa.dal.emvconstants.ArpcStatus;
import com.trustcommerce.ipa.dal.emvconstants.DebitApplicationIdentifiers;
import com.trustcommerce.ipa.dal.emvconstants.EmvTags;
import com.trustcommerce.ipa.dal.emvconstants.EmvTagsConst;
import com.trustcommerce.ipa.dal.emvconstants.Tag8A;
import com.trustcommerce.ipa.dal.emvconstants.TransactionAction;
import com.trustcommerce.ipa.dal.emvconstants.TransactionType;
import com.trustcommerce.ipa.dal.exceptions.DeviceNotConnectedException;
import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;
import com.trustcommerce.ipa.dal.exceptions.MissingEncryptionKeyException;
import com.trustcommerce.ipa.dal.gui.controller.SwingController;
import com.trustcommerce.ipa.dal.gui.interfaces.BridgeEventListener;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.logger.FileIOUtils;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;
import com.trustcommerce.ipa.dal.model.Terminal;
import com.trustcommerce.ipa.dal.model.TransactionStatus;
import com.trustcommerce.ipa.dal.model.emv.ArpcData;
import com.trustcommerce.ipa.dal.model.emv.ArqcData;
import com.trustcommerce.ipa.dal.model.emv.EmvFinalData;
import com.trustcommerce.ipa.dal.model.emv.EmvRetry;
import com.trustcommerce.ipa.dal.model.emv.Retries;
import com.trustcommerce.ipa.dal.model.exceptions.BadDataException;
import com.trustcommerce.ipa.dal.model.payment.TerminalData;
import com.trustcommerce.ipa.dal.model.payment.Transaction;
import com.trustcommerce.ipa.dal.model.types.EntryModeTypeId;
import com.trustcommerce.ipa.dal.signature.SignatureImage;


/**
 * 
 * 
 * 1. Initialize device 2. Verify if device forms needs to be updated. 2.a If
 * device needs updating, download the new form and push to device 3. Start card
 * swipe sequence 4. Check if card IIN reports debit 4.a If so, give customer
 * debit/credit option 4.b If customer selects "debit", ask for the debit pin 5.
 * Ask customer to verify amount 5.a If customer approves, proceed with
 * transaction 6. Terminate device connection.
 * 
 * There are two main components for each Ingenico UPOS solution: - a PIN pad -
 * POS-based UPOS drivers
 */
public class PaymentBridge {

	/** log4j. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentBridge.class);
	
	private static final String DECLINE = "declined";
	/** */
	private static final String CARD_FILL = "XXXXXX";
	/** EMV iUP250 suffix when errors occurred.*/
	private static final String MSG_REMOVE_CARD = " Please Remove Card";
	/** configuration for the application. */
	private static ClientConfiguration clientConfiguration;
	/**Fallback types. */
	private enum FallbackType {
		UNKNOWN, TECHNICAL, MSR
	};
	
    /**
     * 
     */
	private FallbackType emvFallbackType;

	/**
	 * Swipe/Manual: Force immediate swipe without asking for manual/swipe
	 * SignatureOnly: This will cause the signature binary to be returned.
	 */
	private StartMode startMode;
	/** non-emv attempts counter. */
	private int nonEmvAttemptsCounter;
	/** emv attempts counter.*/
	private int emvRetryCounter;
	
	/*
	 * Data
	 */
	/** Device inuse.*/
	private TerminalModel terminalModel;
	/** callback class.*/
	private TransactionProcessor msrProcessor;
	/** callback class.*/
	private SignatureProcessor signatureProcessor;

	/** timer for the transaction.*/
	private Timer timer = new Timer("Transaction timer");

	/** Use for Manual entry only. */
	private boolean timeout;

	/** callback class.*/
	private Transaction transactionData;
	/**  set when transaction is debit.*/
	private boolean processDebit;
	
	/** set when the transaction is completed.*/
	private boolean transactionComplete;

	/** This flag in turned on when the 4 conditions to process EMV are true. */
	private boolean processTransactionAsEMV;
	
	/** set when the transaction fall on any of the fallback type.*/
	private boolean emvFallback;
	
	/** Set to true when receive the event EMV_CARD_DETECTED. */
	private boolean emvCardDetected;

	// private EmvData emvData;

	// Keep ARQC and ARPC around so we can handle invalid chip data later

	/** Authorization Request Cryptogram. */
	private Map<String, String> arqcMap;
	/** Authorization Response Cryptogram. */
	private ArpcData arpcData;
	
	/** emv card details.*/
	private CardInfo emvCardInfo;
	/** */
	private Map<String, String> transactionFields;
	/** set if the device overrides the 8a from the processor. */
	private String tag8aDefaultValue;
	
	/**Set if online pin is performed. */
	private boolean ranEmvOnlinePin;

	// TODO move to the transaction input data
	/** The cardType can be debit or credit. This is determine by the AID*/
	private CardInfo.CardType cardType;
	/** callback class.*/
	private SwingController gui;
	/** set for debit card.*/
	private boolean isDebitCard;
	/** set if received online pin signal.*/
	private boolean isEMVOnlinePin;
	/** Terminal details.*/
	private Terminal myTerminal;
	
	/** used for sending the ARPC for reversals. */
	private Retries emvRetries;
	
	/** true if failed to get online authorization. */
	private boolean failToProcess;
	

 
	/** service for global timeout.*/
	private ExecutorService globalTimeoutService;
	
	
	/** To check whether the transaction is cancelled due to card removed or anyother pin error. */
	private boolean transactionCancelled;
	/** In any version after 4.1 this key is true and it is set in the jpos.xml.*/
	private boolean jposXmlemvKey = true;
	
	
	final EmvFinalData emvFinalData = new EmvFinalData();

	/** 
	 * callable method for global timeout.
	 */
	private	Callable<Object> transactionGlobalTimeout = new Callable<Object>() {
		@Override
		public Object call() throws Exception {
			// Calculate your stuff
			LOGGER.debug("Callable<Object>");
			LOGGER.info("Time limit to process transaction: {} milliseconds ", clientConfiguration.getJdalTimeout());
			Thread.sleep(clientConfiguration.getJdalTimeout());
			LOGGER.debug("=====================================================");
			if (terminalModel == TerminalModel.iUP250) {
				handleIselfTerminatingStatus(EntryModeStatusID.Timeout,
	                    String.format(ShortMessages.TIME_OUT, clientConfiguration.getTimeoutPedal()));
			} else {
				timeout = true;
				if (processTransactionAsEMV && !emvFallback) {
					handleTerminatingStatus(EntryModeStatusID.Timeout,
							AppConfiguration.getLanguage().getString("EMV_GLOBAL_TIME_LIMIT"), true);
				} else {
					handleTerminatingStatus(EntryModeStatusID.Timeout,
							AppConfiguration.getLanguage().getString("GLOBAL_TIME_LIMIT"));
				}
			}
			return "42";
		}
	};

	/**
	 * Constructor for to process a transaction.
	 * 
	 * @param payment Transaction
	 * @param terminalInfo Terminal
	 * @param dalGui SwingController
	 * @throws ConfigurationException if there is any issue with the configuration file.
	 */
	public PaymentBridge(final Transaction payment, final Terminal terminalInfo, final SwingController dalGui)
			throws ConfigurationException {
		LOGGER.info("==================================================================");
		LOGGER.info("      LAUNCHING TRUSTCOMMERCE PAYMENT TRANSACTION BRIDGE          ");
		LOGGER.info("==================================================================");
		this.transactionData = payment;
		this.gui = dalGui;
		terminalModel = terminalInfo.getModelName();
		if (terminalModel == TerminalModel.iUP250) {
			gui.supressGui();
		}
		myTerminal = terminalInfo;
		initializeGui();
		ClientConfigurationUtil.resetConfiguration();
		clientConfiguration = ClientConfigurationUtil.getConfiguration();
		setGlobalTimeout();
		startMode = clientConfiguration.getStartMode();
		// The applet gets this parameter from the Vault
		cardType = CardInfo.CardType.UNKNOWN;
		emvFallbackType = FallbackType.TECHNICAL;
		emvRetries = new Retries();
	}
	
	/**
	 * Initializing the JDAL gui.
	 */
	private void initializeGui() {
		gui.showIntroMessage();
		gui.setEventListener(new GuiListener());
		gui.resetIgnoreCloseWindow();
	}

	/**
	 * This method perform the initialization of the objects required to process
	 * a transaction: Initialization of the MSR and Pin PAD. Request Health
	 * status of the terminal. Initializes the transaction mode: EMV or NON-EMV.
	 */
	public void initializeTransaction() {

		try {
			msrProcessor = new IngenicoMsr(transactionData, myTerminal);
			msrProcessor.setEventListener(new InitDeviceListener());
			msrProcessor.initializeDevice();

		} catch (IngenicoDeviceException e) {
			LOGGER.error("Error initializing devices: {}", e.getMessage());
			//SocketUtil.sendMessageToCaller(SocketMessageType.StatusCode, EntryModeStatusID.RequestJdalReboot);
		
			if (e.getErrorCode() != EntryModeStatusID.MSRIsCloseOrNull.getCode()) {
				handleTransactionTermination(EntryModeStatusID.MSRIsCloseOrNull,
				        AppConfiguration.getLanguage().getString("INITIALIZATION_ERROR"), ShortMessages.INITIALIZATION_ERROR);
			} else {
				handleTransactionTermination(EntryModeStatusID.Error,
				        AppConfiguration.getLanguage().getString("INITIALIZATION_ERROR"), ShortMessages.INITIALIZATION_ERROR);
			}
			return;
		} catch (DeviceNotConnectedException e) {
			// when the device is disconnected in between transactions, un-predictable port issues has been observed 
			LOGGER.error("Error initializing devices: {}", e.getMessage());
			handleDeviceNotConnected();
			return;

		}
 		startTimer();
 		
 		msrProcessor.setEventListener(new InitDeviceListener());
		msrProcessor.requestTerminalSerialNumber();
		if (transactionComplete) {
			// serial mismatch will cause the transaction to be cancelled
			return;
		}
		validateEmvTransaction();
		startMode = clientConfiguration.getStartMode();
		toggleDeviceMode();
	}
	
    /**
     * Setting the startmode of the transaction( swipe/manual).
     * @param value StartMode
     */
	public final void setStartMode(final StartMode value) {
		this.startMode = value;
	}

	/**
	 * We get here after the user press either the manual or swipe buttons.
	 * After that only one of the buttons will be displayed.
	 *
	 */
	public final void toggleDeviceMode() {
		LOGGER.info("-> toggleDeviceMode() ");

		if (startMode == StartMode.MANUAL) {
			LOGGER.info("Going to MANUAL mode");
			msrProcessor.setEventListener(new ManualEntryListener());
			msrProcessor.startManualEntryProcess();

		} else if (startMode == StartMode.SWIPE) {
			LOGGER.info("Going to SWIPE mode"); 
			if (msrProcessor != null && !transactionComplete) {
				msrProcessor.setEventListener(new MSRListener());
				try {
					msrProcessor.swipeCard();
				} catch (IngenicoDeviceException e) {
					LOGGER.error(e.getMessage());
				}
			}
		}
	}


 


	/**
	* Starts the "idle" timer Transaction timeout.
	*/
	private void startTimer() {
		try {
			timer.schedule(new TimeoutTrigger(), clientConfiguration.getMsrTimeout());
		} catch (IllegalStateException e) {
			LOGGER.trace("Timer in illegal state. Creating new timer");
			timer = new Timer("Transaction timer");
			startTimer();
		}
		LOGGER.trace("Timer started");
	}

	/**
	 * Timer use for special purposes, such as firmware update.
	 * 
	 * @param millisecs int
	 */
	private void startTimer(final int millisecs) {
		try {
			timer.schedule(new TimeoutTrigger(), millisecs);
		} catch (IllegalStateException e) {
			LOGGER.debug("Timer in illegal state. Creating new timer");
			timer = new Timer("Transaction timer");
			startTimer(millisecs);
		}
		LOGGER.trace("Timer started");
	}

	/**
	 * Stop the "idle" timer.
	 */
	private void stopTimer() {
		try {
			timer.cancel();
		} catch (IllegalStateException e) {
			LOGGER.trace("Timer already cancelled");
		}
	}

	/**
	 * Restart the "idle" timer.
	 */
	private void restartTimer() {
		LOGGER.trace("Restarting timer");
		stopTimer();
		startTimer();
	}

	/**
	 * 
	 * @param newTimeout int timeout in milli seconds
	 */
	private void restartTimer(final int newTimeout) {
		LOGGER.trace("Restarting timer");
		stopTimer();
		startTimer(newTimeout);
	}

	
	/**
	 * When the user unplugs the device in between transactions.
	 * In this case no reconnect button will be displayed and the transaction will be cancelled.
	 */
	private void handleDeviceNotConnected() {
		// Clear input data
		myTerminal = null;
		transactionData = null;
		final String temp = String.format(AppConfiguration.getLanguage()
				.getString("ERROR_CONNECTION_AND_CANCEL_TRANSACTION"), terminalModel);
		handleTransactionTermination(EntryModeStatusID.BadConnection, temp, ShortMessages.CANT_CONNECT);
	}


    /**
     * 
     * @param message String
     */
	private void reStartSwipeCardStep(final String message) {

		gui.displaySwipeInsertMode(message);
		msrProcessor.setEventListener(new MSRListener());
		try {
			msrProcessor.swipeCard();
		} catch (IngenicoDeviceException e) {
			LOGGER.error("reStartSwipeCardStep : {}", e.getMessage());
			handleTransactionTermination(EntryModeStatusID.CardNotRead,
			        AppConfiguration.getLanguage().getString("ERROR_DURING_SWIPE"), ShortMessages.CARD_NOT_READ);
		}
		restartTimer();
	}

	/**
	 * Method called after a bad swipe to restart the Swipe process. Also called
	 * after removal of the EMV card.
	 * 
	 *
	 */
	public final void swipeOrInsertCardStep() {
		LOGGER.debug("-> swipeOrInsertCardStep()");
		if (msrProcessor == null) {
			// to prevent issues due to timeouts
			return;
		}
		msrProcessor.setEventListener(new MSRListener());
		try {
			if (processTransactionAsEMV) {
				displaySwipeCardGui(AppConfiguration.getLanguage().getString("ASK_SWIPE_OR_INSERT"));
			} else if (emvFallback && nonEmvAttemptsCounter > 0) {
				displaySwipeCardGui(AppConfiguration.getLanguage().getString("ASK_SWIPE_CARD"));
			} else {
				displaySwipeCardGui(AppConfiguration.getLanguage().getString("ASK_SWIPE_CARD"));
			}
			msrProcessor.swipeCard();
		} catch (CardReadException e) {
			LOGGER.error(" SwipeCardStep :{}", e.getMessage());
			handleTransactionTermination(EntryModeStatusID.CardNotRead,
			        AppConfiguration.getLanguage().getString("MAX_SWIPES"), ShortMessages.CARD_NOT_READ);
		} catch (IngenicoDeviceException e) {
			LOGGER.error(" SwipeCardStep : {}", e.getMessage());
			handleTransactionTermination(EntryModeStatusID.CardNotRead,
			        AppConfiguration.getLanguage().getString("ERROR_DURING_SWIPE"), ShortMessages.CARD_NOT_READ);
		}
		restartTimer();
	}

	/**
	 * Calle when a new transaction starts.
	 * Also checks the max number of attempts from configuration file and if it exceeds throws an exception.
	 * @param message string
	 * 
	 * @throws CardReadException
	 */
	private void displaySwipeCardGui(final String message) throws CardReadException {
		LOGGER.debug("-> displaySwipeCardGui()");
		// EMV bad insert
		if (processTransactionAsEMV) {
			// case where StartMode = EMV
			if (StartMode.SWIPE != startMode) {
				return;
			} else {
				if (nonEmvAttemptsCounter >= clientConfiguration.getEmvRetryAttempts()) {
					throw new CardReadException("Card not read");
				} else {
					gui.displaySwipeInsertMode(AppConfiguration.getLanguage().getString("REPEAT_TRANSACTION_ERROR_OCURRED"));
				}
			}
		} else {
			// if the button is toggled from manual to swipe, need to show swipe form again. 
			//counter should not be incremented and  set the manual transaction to false.
			if (msrProcessor.isDoneManualTransaction()) {
				nonEmvAttemptsCounter = 0;
				msrProcessor.setDoneManualTransaction(false);
			}
			// EMV magstripe fallback
			if (emvFallback) {
				gui.displaySwipeInsertMode(message);
				return;
			} else {
				// non-emv mag stripe
				if (nonEmvAttemptsCounter == 0) {
					gui.displaySwipeInsertMode(message);
				} else {
					if (nonEmvAttemptsCounter >= clientConfiguration.getEmvRetryAttempts()) {
						throw new CardReadException("Card not read");
					} else {
						gui.displaySwipeInsertMode(AppConfiguration.getLanguage()
								.getString("REPEAT_TRANSACTION_ERROR_OCURRED"));
					}
				}
			}
		}
	}
	

	/**
	 * 
	 * @param track2
	 * @param iin
	 *            First 6 digits of the card
	 * @param lastFour
	 *            Last 4 digits of the card
	 * @return boolean
	 */
	private boolean debitRefundCardMatch(final String track2, final String iin, final String lastFour) {
		LOGGER.debug("-> debitRefundCardMatch() Validating iin and last four matches");
		String track = null;
		String sentinel = null;
		String item = null;
		final int startPos;

		if (track2 != null && !track2.isEmpty()) {
			track = track2;
			startPos = 0;
			sentinel = "=";
		} else {
			LOGGER.error("Failed to retrieve cleartext track2 data");
			return false;
		}

		try {
			// Ingenico -- compare the full length of IIN
			item = track.substring(startPos, startPos + iin.length());
			if (!iin.equals(item)) {
				LOGGER.warn("Expected IIN mismatched with swipped IIN. Expecting {} got {}", iin, item);
				return false;
			}
		} catch (IndexOutOfBoundsException e) {
			LOGGER.error("Failed to extract IIN of swiped card");
			return false;
		}

		try {
			final int pos = track.indexOf(sentinel);
			LOGGER.debug("Index of sentinel: " + Integer.toString(pos));

			item = track.substring(pos - 4, pos);
			if (!lastFour.equals(item)) {
				LOGGER.warn(
				        "Expected last four didn't match swipped last four. Expecting {} got {}", lastFour, item);
				return false;
			}
		} catch (IndexOutOfBoundsException e) {
			LOGGER.error("Failed to extract last 4 of swiped card");
			return false;
		}

		return true;
	}



	/**
	 * After we receive this error, we will redisplay the Swipe Form in the
	 * device.
	 * 
	 * @param e
	 */
	private void processMissingEncryptionKey() {
		LOGGER.debug("-> processMissingEncryptionKey()");
		displayWarning(AppConfiguration.getLanguage().getString("MISSING_DEBIT_KEY"), null);
 
		// swingControl.showManualButton(true);
		msrProcessor.setEventListener(new MSRListener());
		try {
			msrProcessor.swipeCard();
		} catch (IngenicoDeviceException ee) {
			LOGGER.error("processMissingEncryptionKey : {}", ee.getMessage());
			handleTransactionTermination(EntryModeStatusID.CardNotRead,
			        AppConfiguration.getLanguage().getString("ERROR_DURING_SWIPE"), ShortMessages.CARD_NOT_READ);
		}
		restartTimer();
	}


	
	/**
	 * Called from the EMVFirstACListener for Online Credit or Debit PIN.
	 */
	public final void processEMVOnlinePin() {
		LOGGER.debug("-> processEMVOnlinePin() Received Online PIN signal");
		msrProcessor.setEventListener(new EMVPinListener(this));
		requestDebitPin(transactionData.getAmountAsLong());
		isEMVOnlinePin = true;
	}


	/**
	 * EMV
	 * @param amount Long
	 */
	private void requestDebitPin(final Long amount) {
		try {
			// displays the PIN form 
			msrProcessor.getDebitPin(transactionData.getAmountAsLong());
		} catch (MissingEncryptionKeyException e) {
			LOGGER.warn(e.getMessage());
			processMissingEncryptionKey();
		} catch (IngenicoDeviceException e) {
			LOGGER.error("processDebitSelected : " + e.getMessage());
			if (processTransactionAsEMV) {
				// request card removal
				cancelEMVTransaction();
				deviceCleanup();
			} else {
				handleTransactionTermination(EntryModeStatusID.Unsupported,
				        AppConfiguration.getLanguage().getString("ERROR_DEBIT_CARD_NOT_SUPPORTED"), ShortMessages.UNSUPPORTED);
			}
		} catch (NumberFormatException e) {
			LOGGER.error("processDebitSelected : {}", e.getMessage());
			handleTransactionTermination(EntryModeStatusID.BadData,
			        AppConfiguration.getLanguage().getString("ERROR_ON_PIN_ENTRY"), ShortMessages.PIN_ERROR);
		}
	}

	/**
	 * 
	 */
	private void verifyAmount() {
		LOGGER.info("-> verifyAmount() " + transactionData.getFormattedAmount());
		final String msg1 = AppConfiguration.getLanguage().getString("ASK_VERIFY_AMOUNT");
		gui.displayMessage(String.format(msg1, transactionData.getFormattedAmount()));
		msrProcessor.setEventListener(new VerifyAmtListener());
		try {
			msrProcessor.verifyAmount(transactionData.getFormattedAmount());
		} catch (IngenicoDeviceException e) {
			LOGGER.error("verifyAmount : {} ", e.getMessage());
			handleTransactionTermination(EntryModeStatusID.Error,
			        AppConfiguration.getLanguage().getString("ERROR_VERIFYING_AMOUNT"), ShortMessages.AMOUNT_INCORRECT);
		}
		restartTimer();
	}

	/**
	 * This method will be needed for the iUP250 terminal.
	 */
	@SuppressWarnings("unused")
	private void displayZipCode() {
		LOGGER.info("-> displayZipCode() ");
		// In this cases jdal is not displaying anything. This is for testing
		// purposes
		gui.displayMessage(String.format(AppConfiguration.getLanguage()
				.getString("ASK_ZIPCODE"), transactionData.getFormattedAmount()));

		msrProcessor.setEventListener(new PINListener());
		msrProcessor.displayZipcode();
		restartTimer();
	}


 	// submitEMVCancelled() in Applet
	/**
	 * Force termination due to a unsuccessful transaction.
	 * 
	 * @param errorCode
	 *            EntryModeStatusID error code send with the payment object.
	 * @param msg
	 *            String message to display in the GUI
	 */
	public final void handleTerminatingStatus(final EntryModeStatusID errorCode, final String msg) {
		LOGGER.error("-> handleTerminatingStatus() : {} ", msg);
		// Marker to stop any other process
		transactionData = null;
		if (errorCode == EntryModeStatusID.BadConnection) {
			gui.displayError(msg);
			SocketUtil.requestJdalTermination(EntryModeStatusID.BadConnection);
		} 
		if (errorCode == EntryModeStatusID.Timeout) {
			// if the transaction is timed out set timeout as true to prevent
			// the device from proceeding further with the transaction.
			timeout = true;
		}
		if (errorCode == EntryModeStatusID.MSRIsCloseOrNull) {
			SocketUtil.sendMessageToCaller(SocketMessageType.StatusCode, EntryModeStatusID.Cancelled);
			return;
		} else {
			SocketUtil.sendMessageToCaller(SocketMessageType.StatusCode, errorCode);
			gui.displayWarning(msg);
		}
		deviceCleanup();
	}
	
	
	/**
	 * Force termination due to a unsuccessful transaction.
	 * 
	 * @param errorCode
	 *            EntryModeStatusID error code send with the payment object.
	 * @param msg
	 *            String message to display in the GUI
	 * @param displayError true if error should be displayed for a longer period of time    
	 */
	private final void handleTerminatingStatus(final EntryModeStatusID errorCode, final String msg,
	        final boolean displayError) {
		LOGGER.error("-> handleTerminatingStatus() : ***** {}", msg);
		// Marker to stop any other process
		transactionData = null;
		if (errorCode == EntryModeStatusID.BadConnection) {
			gui.displayError(msg);
			SocketUtil.requestJdalTermination(EntryModeStatusID.BadConnection);
		} else {
			gui.displayError(msg);
			SocketUtil.sendMessageToCaller(SocketMessageType.StatusCode, errorCode);
		}
		deviceCleanup();
	}
	
	/**
	 * 
	 * @param errorCode EntryModeStatusID
	 * @param terminalShortMessage String
	 * 
	 */
	private final void handleIselfTerminatingStatus(final EntryModeStatusID errorCode, 
	        final String terminalShortMessage) {
		LOGGER.error("-> handleIselfTerminatingStatus() : *****  {}", terminalShortMessage);
		stopTimer();
		// Marker to stop any other process
		transactionData = null;
		if (processTransactionAsEMV && !emvFallback) {
			msrProcessor.displayWarningInTerminal(errorCode, terminalShortMessage + MSG_REMOVE_CARD);
		} else {
			msrProcessor.displayWarningInTerminal(errorCode, terminalShortMessage);
		}
		SocketUtil.sendMessageToCaller(SocketMessageType.StatusCode, errorCode);
		try {
			Thread.sleep(GlobalConstants.MESSAGE_WARN_SLEEP_TIMER);
		} catch (InterruptedException e) {
		}
		deviceCleanup();
	}
	
	
	/**
	 * Terminates a transaction due to errors.
	 * @param errorStatus
	 * @param longMessage
	 * @param shortMessage
	 */
	public final void handleTransactionTermination(EntryModeStatusID errorStatus, String longMessage, String shortMessage) {
		if (terminalModel == TerminalModel.iUP250) {
			handleIselfTerminatingStatus(errorStatus, shortMessage);
		} else {
			handleTerminatingStatus(errorStatus, longMessage);
		}
	}
	
	
	/**
	 * Displays information in either the terminal or device. 
	 * @param message
	 * @param shortMessage
	 */
	public void displayWarning(final String message, final String shortMessage) {
		if (terminalModel == TerminalModel.iUP250) {
			if (shortMessage != null) {
				msrProcessor.displayWarningInTerminal(EntryModeStatusID.Success, shortMessage);
			}
		} else {
			gui.displayWarning(message);
		}
	}
	
	
	/**
	 * Displays information in either the terminal or device. 
	 * @param message String
	 * @param shortMessage String
	 */
	public final void displayMessage(final String message, final String shortMessage) {
		if (terminalModel == TerminalModel.iUP250) {
			if (shortMessage != null) {
				msrProcessor.displayWarningInTerminal(EntryModeStatusID.Success, shortMessage);
			}
		} else {
			gui.displayMessage(message);
		}
	}
	
 /**
  * 
  * @param errorCode EntryModeStatusID
  */
	private void sendPaymentResponseToDal(final EntryModeStatusID errorCode) {
		Transaction transaction = null;
		if (msrProcessor != null) {
			transaction = msrProcessor.getTransactionData();
			transaction.setEntryModeStatusID(errorCode.getCode());
			SocketUtil.sendMessageToCaller(SocketMessageType.Payment, transaction.toString());
		} else {
			SocketUtil.sendMessageToCaller(SocketMessageType.Payment, errorCode);
		}
	}

	
	/**
	 * Must be called when a transaction is complete.
	 */
	public final void deviceCleanup() {
		stopTimer();
		if (!transactionComplete) {
			LOGGER.debug("-> deviceCleanup()");
			transactionComplete = true;
			if (globalTimeoutService != null) {
				globalTimeoutService.shutdownNow();
			}
			gui.resetWidgets();
			releaseSignature();
			releaseMSR();
			SocketUtil.transactionComplete();
		}
 	}
	
	
	/**
	 * 
	 */

	private void releaseMSR() {
		LOGGER.debug("-> releaseMSR()");
		if (msrProcessor != null) {
			try {
				if (!jposXmlemvKey) {
					// this variable is only turned off on iUP250 EMV fallback cases
					msrProcessor.updateEmvKey(1);
				}
			} catch (IngenicoDeviceException e) {
				LOGGER.error("releaseMSR ERROR : {}", e.getMessage());
			}
			msrProcessor.setEventListener(null);
			msrProcessor.releaseDevice();
			msrProcessor = null;
		}
		timeout = true;
	}
	
	
	/**
	 * 
	 */
	private void releaseSignature() {
		if (signatureProcessor != null) {
			signatureProcessor.releaseDevice();
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// EMV
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Determine if this transaction can be perform as EMV.
	 * @return true if the transaction can be processed as EMV
	 */
	private boolean validateEmvTransaction() {
		LOGGER.info("-> validateEmvTransaction()");
		
		
		// First, determine is the customerId is configured to process EMV
		// Default is 1
		if (transactionData.getIsEMV()) {
			if (terminalModel != TerminalModel.iUP250) {
				// Exception when enabling this variable in iUP250. Already enabled by default.
				try {
					msrProcessor.updateEmvKey(1);
				} catch (IngenicoDeviceException e) {
					LOGGER.error("-> validateEmvTransaction IngenicoDeviceException {}", e.getMessage());
				}
			}

		} else {
			LOGGER.warn("Customer not configured for EMV");
			try {
				msrProcessor.updateEmvKey(0);
			} catch (IngenicoDeviceException e) {
				LOGGER.error("-> validateEmvTransaction IngenicoDeviceException {}", e.getMessage());
			}
			return false;
		}

		// Second, determine if the connected device has the emv key "on" and
		// the correct software.

		if (myTerminal.isEmvCapable()) {
			LOGGER.info("The connected terminal can process EMV transactions");
			processTransactionAsEMV = true;
			arpcData = new ArpcData();
			// update the MSR Processor
			if (msrProcessor != null) {
				msrProcessor.processTransactionAsEmv(true);
			}
			return true;
		} 
		
		LOGGER.info("Disabling EMV code path, since terminal is not enabled for EMV.");
		displayWarning(myTerminal.getIssues(), null);
		processTransactionAsEMV = false; // Redundant
		return false;
	}

	/**
	 * 
	 * @param currentTransaction Transaction
	 */
	private void verifyFallbackTransaction(final Transaction currentTransaction) {
		if (currentTransaction.getIsEMV() && myTerminal.isEmvCapable()) {
			// This information should not change at any moment ...
			
			if (emvCardDetected) {
				// cards with chip this is true
				LOGGER.info("submitting transaction as EMV Fallback");
				currentTransaction.setEmvFallBackTypeId(emvFallbackType.ordinal());
			}
			else {
				LOGGER.info("submitting transaction as MSR Fallback");
				currentTransaction.setEmvFallBackTypeId(emvFallbackType.MSR.ordinal());
			}
			currentTransaction.setEmvFallback(emvFallback);
		} 
	}
	/**
	 * 
	 */
	private void submitNonEmvTransactionData() {
		LOGGER.info("-> submitNonEmvTransactionData()");
		final Transaction transaction = msrProcessor.getTransactionData();
		verifyFallbackTransaction(transaction);
		// transaction.setDebitCard(isDebitCard);
 
		try {
			LOGGER.trace(transaction.toLogFile());
			// Get the name on card if this is a manual transaction
			if (transaction.getEntryModeType() == EntryModeTypeId.Keyed) {
				transaction.setCardHolderName(SwingController.cardHolderName);
			}
			// Get pinblock if needed
			if (processDebit) {
				transaction.setDebitCard(true);
				if (!transaction.validateDebitCardInfo()) {
					LOGGER.error("Either PIN block is empty or KSN is empty ");
					// TODO Now what??
				}
			}

			gui.displayMessage(AppConfiguration.getLanguage().getString("SUBMITTING_TRANSACTION"));
			// Transfer transaction to server ...
			LOGGER.info("submitting: {}", transaction.toLogFile());
			SocketUtil.sendMessageToCaller(SocketMessageType.Payment, transaction.toString());

			// avoid closing of jDAL.
			gui.setIgnoreCloseWindow();

			// Be careful here DEV should be always false if not in development
			// mode
			if (GlobalConstants.LOCAL_TEST) {
				FileIOUtils.saveDataToTempFile(transaction.toString(), TcipaFiles.CC_INFO_FILENAME);
			}

			restartTimer(clientConfiguration.getSignatureCaptureTimeout());
			LOGGER.info("<- submitTransactionData() in success state");
			return;
		} catch (Exception e) {
			LOGGER.error("JSException encountered submitting transaction: {}", e.getMessage());
		}

		// If it get's here there's an error submitting transaction
		gui.displayMessage(AppConfiguration.getLanguage().getString("ERROR_SUBMITTING_TRANSACTION"));
		LOGGER.info("<- submitTransactionDataToParent() in error state");

	}

	// ===============================================================================

	// EMV Methods
	// ===============================================================================
	/**
	 * 
	 */
	private void cancelEMVTransaction() {
		LOGGER.debug("-> cancelEMVTransaction() ");
		sendPaymentResponseToDal(EntryModeStatusID.Cancelled);
		gui.displayMessage(AppConfiguration.getLanguage().getString("CANCEL_AND_REMOVE_CARD"));
		if (msrProcessor != null) {
			msrProcessor.setEventListener(new EMVRemoveAfterCancelListener(this));
			msrProcessor.cancelEMVTransaction();
		}
	}

	/**
	 * Call after the user has clicked the Debit button from the SelectCard
	 * form.
	 */
	private void collectPin() {
		LOGGER.info("-> collectPin() ");
		processDebit = true;
		gui.displayMessage(AppConfiguration.getLanguage().getString("ASK_PAYER_TO_ENTER_PIN"));
		// updateLabel("Ask payer to enter pin");
		msrProcessor.setEventListener(new PINListener());
		requestDebitPin(transactionData.getAmountAsLong());
		// transactionTimer.restart();
		restartTimer();
	}

	/**
	 * Called by MSRListener, Event EMV_CARD_INSERTED.
	 * Calls the MSR to start or re-start the EMV process.
	 */
	private void startEMVProcess() {
		LOGGER.debug("-> startEMVProcess() Number of EMV attempts: {}", emvRetryCounter);
		stopTimer();
		gui.displayMessage(AppConfiguration.getLanguage().getString("ASK_FOLLOW_PROMPT"));
		msrProcessor.setEventListener(new EMVTransactionPreparationListener(this));
		msrProcessor.startEMVProcess();
	}

	/**
	 * Calls the lookupTrack URL in the Vault, to determine if the card is
	 * credit or debit. The card HAS to be in the bin in order to get the
	 * correct answer. Real cards get updated immediately, test cards do not, so
	 * not all the test cards will pass this testing.
	 * 
	 * @param encryptedtrack
	 *            Encrypted track to send to iin lookup
	 * @param useiinsource
	 *            True if the card is EMV
	 * @return true if track is possible debit. False otherwise
	 */
	private boolean isDebitCard(final String encryptedtrack, final boolean useiinsource) {
		LOGGER.debug("-> isDebitCard() useiinsource {}", useiinsource);
		if (emvCardInfo != null && emvCardInfo.getCardType() != CardInfo.CardType.UNKNOWN) {
			LOGGER.debug("card type is known; skip lookuptrack");
			return emvCardInfo.getCardType() == CardInfo.CardType.debit;
		}

		try {
			final URL url = new URL(Consts.VAULT_URL);
			final URLConnection conn = url.openConnection();
			conn.setConnectTimeout(Consts.DEVICETIMEOUT);
			conn.setReadTimeout(Consts.DEVICETIMEOUT);
			conn.setUseCaches(false);
			conn.setDoOutput(true);

			final OutputStreamWriter postData = new OutputStreamWriter(conn.getOutputStream());
			String postString = "track=" + URLEncoder.encode(encryptedtrack, "UTF-8");
			// In Non-emv cards useiinsource = false
			// TODO is iinSource ever null?
			if (useiinsource) {
				postString += "&iin_source="
				        + URLEncoder.encode(clientConfiguration.getIinSource().getSource(), "UTF-8");
			}
			postData.write(postString);
			postData.flush();

			final BufferedReader response = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String rslt = null;
			String tmp = null;
			do {
				tmp = response.readLine();
				LOGGER.debug(tmp);
				if (tmp != null) {
					rslt = tmp;
				}
			} while (tmp != null);

			LOGGER.info("lookuptrack returned [{} ]", rslt);

			isDebitCard = rslt.equals("D");
			response.close();
		} catch (MalformedURLException e) {
			LOGGER.warn("URL provided for checking debit status malformed: {}", e.getMessage());
		} catch (IOException e) {
			LOGGER.warn("Communication issue with iin lookup: {}", e.getMessage());
		}

		LOGGER.debug("<- isDebitCard() {}", isDebitCard);
		return isDebitCard;
	}

	// ============= EMVFinalRemoveListener  ===========================================

	/**
	 * Called from EMVFinalRemoveListener.
	 * @param transactionType TransactionType
	 */
	public final void processEmvCardRemoved(final TransactionAction transactionType) {
		LOGGER.info("-> processEmvCardRemoved() Card removed, submit final EMV data");
		if (transactionFields == null) {
			LOGGER.debug("get data from the chip");
			transactionFields = msrProcessor.getConfirmRespData();
		}

		if (tag8aDefaultValue != null && !tag8aDefaultValue.isEmpty()) {

			// Replace the 8a with value from RC PTS specs
			LOGGER.info("Add tag 8A with value of {}", tag8aDefaultValue);
			transactionFields.put(EmvTags.T8A.tagName(), tag8aDefaultValue);

		}
		submitEMVFinalData(transactionType);
	}

	/**
	 * Called from the EMVFinalRemoveListener. Applet name: sendEMVFinalData
	 * 
	 * @param finalTransactionType TransactionType
	 */
	private void submitEMVFinalData(final TransactionAction finalTransactionType) {
		LOGGER.info("-> submitEMVFinalData() finalTransactionType: {}", finalTransactionType);

		
		emvFinalData.setAction(finalTransactionType.getAction());
		final String processingCode = getProcessingCode();
		emvFinalData.setProcessingCode(processingCode);

		emvFinalData.setTransactionFields(transactionFields);
		// New for TCIPA
		final Transaction transaction = msrProcessor.getTransactionData();

		transaction.setSignatureRequired(transactionData.isSignatureRequired());
		transaction.setIsEMV(true);
		final String jSonString = (new Gson()).toJson(emvFinalData.getPropertiesFromMap());

		final String tags = emvFinalData.getPropertiesFromMap();
		transaction.setFinalEmvData(tags);
		transaction.setArpc(arpcData.getArpcAsString());

		boolean transactionApproved = false;
		
		if (emvFinalData.getEmvFinalStatus().equals("approved")) {
			transactionApproved = true;
		} else {
			transactionApproved = false;
		}
		
		// Processing Signature before sending the final payment object to update the signature required member
		SignatureUtil.isSignatureRequired(transaction, emvFinalData, myTerminal.isSignatureCapable());

		SocketUtil.sendMessageToCaller(SocketMessageType.Payment, transaction.toString());

		// avoid closing of jDAL.
		gui.setIgnoreCloseWindow();

		processTransactionStatus(transactionApproved, transaction.isSignatureRequired());

		if (GlobalConstants.LOCAL_TEST) {
			FileIOUtils.saveDataToTempFile(jSonString, TcipaFiles.EMV_FINAL_DATA);
			FileIOUtils.saveDataToTempFile(transaction.toString(), TcipaFiles.CC_INFO_FILENAME);
			FileIOUtils.appendToFile(emvRetries.toString(), TcipaFiles.EMV_FINAL_DATA);
		}
		// Vault code to submit EMV Final Data, here for reference
		// Object[] params = new Object[] { finalTransactionType, (new
		// Gson()).toJson(transactionFields),
		// processingCode, arpc8aTag };

		LOGGER.info("<- sendEMVFinalData() call submitEMVConfirm: transactionType= {}, processingCode={}",
				finalTransactionType, processingCode);
	}

	/**
	 * Determines if the signature is required only. Later if the transaction is
	 * approved, the signature pin pad should open and the signature should be
	 * sent back to the server.
	 * @param transactionStatus TransactionStatus
	 *            
	 */
	public final void processNonEmvTransactionStatus(final TransactionStatus transactionStatus) {
		if (timeout) {
			return;
		}
		LOGGER.info("-> processNonEmvTransactionStatus()  transactionApproved: {}", transactionStatus);
		if (terminalModel == TerminalModel.iSC250 || terminalModel == TerminalModel.iSC480) {
			// transaction.isSignatureRequired() is calculated in Store Procedure
			if (!transactionData.isDebitCard() & transactionStatus.isSignatureRequired()) {
				LOGGER.info("Requesting a signature from Payer");
				transactionData.setSignatureRequired(true);
			}
		}
		processTransactionStatus(transactionStatus.isApproved(), transactionData.isSignatureRequired());
	}

	/**
	 * 
	 * @param transactionApproved boolean generated during second generation
	 * @param signatureRequired boolean
	 */
	private void processTransactionStatus(final boolean transactionApproved, final boolean signatureRequired) {
		if (signatureRequired && transactionApproved) {
			// the message transaction approved is displayed after receiving the
			// signature
			requestSignature();
			restartTimer(clientConfiguration.getSignatureCaptureTimeout());
		} else {
			if (transactionApproved) {
				gui.displayMessage(AppConfiguration.getLanguage().getString("TRANSACTION_APPROVED"));
			} else {
				processDeclineTransactionStatus();
			}
			msrProcessor.displayTransactionStatus(transactionApproved);
			deviceCleanup();
		}

	}
	
	
	private void processDeclineTransactionStatus() {
		
		if (arpcData == null) {
			gui.displayMessage(AppConfiguration.getLanguage().getString("TRANSACTION_DECLINED"));
		} else {
			final String code = arpcData.getAuthorizationCode();
			// if code = 00 the transaction is approved by the processor but it
			// was declined by the chip
			if (code == null || code.equals(Tag8A.OnlineApproved.getEmvCode())
			        || code.equals(Tag8A.OnlineDeclined.getEmvCode())) {
				// EMV currently uses transaction cancelled ...
				gui.displayMessage(AppConfiguration.getLanguage().getString("TRANSACTION_DECLINED"));
			} else {
				// Test E2E 15 == Expired card.
				final Tag8AMessages arpc8AMessage = Tag8AMessages.valueOf("M" + code);
				LOGGER.debug("8a Message {}", arpc8AMessage);
				gui.displayMessage(AppConfiguration.getLanguage().getString("TRANSACTION_DECLINED"));
				// gui.displayMessage(AppConfiguration.getLanguage().getString(arpc8AMessage.get8AMessagesCode()));
			}
		}
	}

	/**
	 * 
	 * @return String with "credit" or "debit"
	 */
	private String getProcessingCode() {
		// TODO repeats in EmvPaymentDetail?
		final boolean result = isDebitCard(emvCardInfo.getTracksData(), true);
		if (result) {
			return "debit";
		} else {
			return "credit";
		}
	}

	// ====================================================================================================

	/**
	 * Method called from the EMVFirstACListener when a ARQC signal is received.
	 * 
	 * Init EMV step 4.a
	 * 
	 * The Application Cryptogram (AC) is generated by the card during Card
	 * Action Analysis. This cryptogram is sent to the card issuer in online
	 * authorization and clearing messages, and can be verified by the issuer to
	 * confirm the legitimacy of the transaction. There are 3 different types of
	 * application cryptogram that can be generated by the card, and the type is
	 * indicated in the Cryptogram Information Data.
	 */
	public final void processFirstApplicationCryptogram() {
		LOGGER.info(" -> processFirstApplicationCryptogram() : Received ARQC signal");

		final ArqcData arqcData = new ArqcData(clientConfiguration.getProcessingPlatform());
		arqcData.setDeviceSerial(myTerminal.getRawSerialNumber());
		arqcData.setEmvKernelVersion(myTerminal.getEmvKernelVersion());
		final String emvData = msrProcessor.getTransactionPreparationResponse() + EmvTagsConst.EMV_DELIM
		        + msrProcessor.getARQCData();

		arqcMap = EMVDataHandler.parseEMVDataMessage(emvData);

		String jsonString = null;

		getCreditCardNumber();

		if (!ranEmvOnlinePin) {
			// ranEmvOnlinePin is set to true when EMVPinListener receives the event ...
			// in offline test cards it is not set ..
			LOGGER.debug("Online pin not performed");
			arqcMap.put("encryptedtrack", msrProcessor.getTracksData(arqcMap));
			arqcMap.remove(GlobalConstants.TRACK2);
			arqcMap.remove(GlobalConstants.TRACK1);
			jsonString = (new Gson()).toJson(arqcMap);

		} else {
			LOGGER.debug("Online pin performed. Include pinblock in ARQC");
			jsonString =  processOnlinePIN(arqcData);
		}
		
		
		transactionData.cleanUpForEMV();
		if (jsonString != null) {

			arqcData.setAmount(transactionData.getAmountInCents());
			arqcData.setEmvDebitCard(isDebitCard(emvCardInfo.getTracksData(), true));
			arqcData.setArqc(arqcMap);

			try {
				updateArqc(arqcData, transactionData);

				LOGGER.info("Sending Transaction Data with ARQC to DAL!!! Waiting for response ...");
				transactionData.setCreditCardNumber(msrProcessor.getTransactionData().getCreditCardNumber());
				transactionData.setCustomerName(arqcData.getName());
				final String counter = transactionData.getArqcProp().getProperty(EmvTags.T9F41.name());
				emvRetries.add(new EmvRetry(counter,
				        arqcData.getTag(EmvTags.T9A) + "-" + arqcData.getTag(EmvTags.T9F21), emvRetryCounter));
				
				gui.displayMessage(AppConfiguration.getLanguage().getString("SUBMITTING_TRANSACTION"));
				SocketUtil.sendMessageToCaller(SocketMessageType.Payment, transactionData.toString());
				
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
			}
		} else {
			LOGGER.error("Not proceeding with ARQC");
		}
	}
	
	
	private static void updateArqc(final ArqcData req, final Transaction transaction) throws Exception {
		LOGGER.info("-> updateArqc() "); 
		final Properties props = req.getArqcPostData();
		transaction.setArqcProp(props);
	}
	
	
	/**
	 * @param arqcData ArqcData
	 * @return type String json String of the ARQC.
	 */
	private String processOnlinePIN(final ArqcData arqcData) {
		LOGGER.info(" -> processOnlinePIN()");
		String jsonString = null;
		// Clean up encrypted track 
		if (arqcMap.containsKey(GlobalConstants.TRACK2)) {
			String track2 = arqcMap.get(GlobalConstants.TRACK2);
			track2 = ';' + track2.replace('D', '=') + '?';
			arqcMap.put(GlobalConstants.TRACK2, track2);
		
			if (arqcMap.containsKey("encryptedtrack")) {
				LOGGER.debug("Generate encrypted track value");
				arqcMap.put("encryptedtrack", getTracksData(arqcMap));
				arqcMap.remove(GlobalConstants.TRACK2);
				arqcMap.remove(GlobalConstants.TRACK1);
			}

			// if the transaction is truly a debit transaction, specify emv_processing_code=debit
			final String pinAndKsn = msrProcessor.getTransactionData().getPINBlock();

			try {
				arqcData.setPinBlock(pinAndKsn);
				jsonString = (new Gson()).toJson(arqcMap);
			} catch (BadDataException e) {
				msrProcessor.cancelEMVTransaction();
			}

		} else {
			LOGGER.error("Missing track2 data from EMV processing");
		}
		return jsonString;
	}
/**
 * 
 * @param emvMap Map<String, String>
 * @return String
 */
	
	private String getTracksData(final Map<String, String> emvMap) {
		final String track1 = emvMap.get(GlobalConstants.TRACK1);
		final String track2 = emvMap.get(GlobalConstants.TRACK2);
		final String encryptedTrack = emvMap.get("encryptedtrack");

		return (track1 == null ? "" : track1) + "|" + (track2 == null ? "" : track2) + "|"
		        + (encryptedTrack == null ? "" : encryptedTrack);
	}
	/**
	 * Getting creditcard number from the track2.
	 */
	private void getCreditCardNumber() {
		final String track2 = arqcMap.get(GlobalConstants.TRACK2);
		if (track2 != null) {
			final String creditCardNumber = track2.split("D")[0];
			transactionData.setCreditCardNumber(creditCardNumber);
		}
	}

	/**
	 * Start second gen EMV process.
	 * example transid=027-0000025092,errortype=failtoprocess,status=error,magneprintstatus=B098.
	 * 
	 * @param arpcReceived
	 * @param errorTypeId integer
	 *  
	 */
	public final void handleAuthResponse(final String arpcReceived, final int errorTypeId) {
		LOGGER.debug("-> handleAuthResponse() arpc = " + arpcReceived + " retry " + emvRetryCounter);
		// if the transaction gets cancelled  , discard the arpc recieved.
		if (transactionCancelled) {
			return;
		}

		if (emvRetryCounter > 1) {
			// possible reversal in progress
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		arpcData.parseArpcReceived(arpcReceived);
		try {
			emvRetries.receiveArpc(arpcData.getTransactionId(), emvRetryCounter);
		} catch (Exception e) {
			LOGGER.debug(" ** Request Reversal of transid: {}", arpcData.getTransactionId());
			SocketUtil.sendMessageToCaller(SocketMessageType.Reversal, arpcData.getTransactionId());
			arpcData = new ArpcData();
			return;
		}
		transactionData.setArpc(arpcReceived);
		handleAuthResponse();
	}


	/**
	 * Init EMV step 4.b.
	 * 
	 * @param arqp
	 */
	private final void handleAuthResponse() {
		LOGGER.info("-> handleAuthResponse(): received arpc");
		gui.displayMessage(AppConfiguration.getLanguage().getString("SUBMITTING_TRANSACTION2"));

		LOGGER.debug(" arqp = {}", arpcData.getArpcMap());
		try {

			if (arpcData.getStatus() == ArpcStatus.approved || arpcData.getStatus() == ArpcStatus.declined
			        || arpcData.getStatus() == ArpcStatus.decline || arpcData.getStatus() == ArpcStatus.accepted) {
				////
				// Init EMV step 5
				////

				if (arpcData.getPartialAmount() != null) {
					LOGGER.info("Transaction partially approved");
					LOGGER.info("Value of partialamount: {}", arpcData.getPartialAmount());

					final String prompt = String.format(AppConfiguration.getLanguage().getString("APPROVED_AMOUNT"),
					        Float.parseFloat(arpcData.getPartialAmount()) / 100);

					final String partial = String.format("%012d", Integer.parseInt(arpcData.getPartialAmount()));
					arpcData.getArpcMap().put(EmvTags.T9F02.tagName(), partial);
					gui.activateOKPartialButton();
					gui.activateVoidPartialButton();

					gui.displayPartialApprovalScreen(prompt);

				} else {
					if (!arpcData.getArpcMap().containsKey(EmvTags.T8A.tagName())) {
						LOGGER.debug("Tag 8A not provided by platform");
						// TODO: fix tag 71, 72 and 91
						// if (arpcData.getAuthorizationCode() != null){
						// arpcData.getArpcMap().put(EmvTags.T91.tagName(),
						// arpcData.getAuthorizationCode());
						// }
						if (arpcData.getStatus() == ArpcStatus.approved
						        || arpcData.getStatus() == ArpcStatus.accepted) {
							arpcData.getArpcMap().put(EmvTags.T8A.tagName(), Tag8A.OnlineApproved.getEmvCode());
						} else {
							arpcData.getArpcMap().put(EmvTags.T8A.tagName(), Tag8A.OnlineDeclined.getEmvCode());
						}
					}
					
					msrProcessor.setEventListener(new EMVSecondACListener(this));
					//msrProcessor.suspendEmvFlow();
					msrProcessor.sendARPCToTerminal(arpcData);
				}

			} else if (arpcData.failedToProcess()) {
				LOGGER.error("Failed to get online authorization, going for stand-in approval");
				arpcData.getArpcMap().put(EmvTags.T8A.tagName(), Tag8A.UnableOnlineOfflineApproved.getEmvCode());

				final EMVSecondACListener listener = new EMVSecondACListener(this);
				failToProcess = true;
				msrProcessor.setEventListener(listener);
				msrProcessor.sendARPCToTerminal(arpcData);

			} else {
				// arpc: status = baddata, offenders=custid,password
				LOGGER.error("Authorization to platform failed. status={}", arpcData.getStatus().name());
				gui.displayMessage(AppConfiguration.getLanguage().getString("AUTHORIZATION_FAILED"));
				cancelEMVTransaction();
			}

		} catch (Exception e) {
			LOGGER.error("Exception encountered processing auth response. Message: {}", e.getMessage());
			LOGGER.error("Stack trace: {}", e);
			gui.displayMessage(AppConfiguration.getLanguage().getString("ERROR_TRANSACTION"));
			cancelEMVTransaction();
		}
		LOGGER.debug("<- handleAuthResponse() ARPC = {}", arpcData);
	}

	/**
	 * Updates the Device State after the following Listeners:
	 * EMVFirstACListener EMVSecondACListerner EMVTransactionPreparationListener.
	 * 
	 * @param state DeviceState
	 */
	public void baseEMVStateHandler(final DeviceState state) {
		LOGGER.debug("======================> baseEMVStateHandler() state: {}", state);

		if (timeout) {
			// when a timeout occurs everything else should be ignored. We are done.
			return;
		}
		
		switch (state) {
		case EMV_CARD_REMOVED:
			LOGGER.info("EMV_CARD_REMOVED: Card unexpectedly removed. EMV Attempts Counter : {},",  emvRetryCounter);
			//processReversal();
			// to check whether the maximum attempts to go to fallback has been
			// reached or not.
			validateFallBack();
			if (transactionData.getArqcProp() != null && !transactionData.getArqcProp().isEmpty()) {
				transactionCancelled = true; // true if the card is removed .
				gui.displayMessage(
				        AppConfiguration.getLanguage().getString("CARD_REMOVED_UNEXPECTEDLY_CANCELLED_TRANSACTION"));
				SocketUtil.sendMessageToCaller(SocketMessageType.StatusCode, EntryModeStatusID.Cancelled);
				deviceCleanup();
				return;
			}
			if (!emvFallback) {
				gui.displayMessage(AppConfiguration.getLanguage().getString("CARD_REMOVED_UNEXPECTEDLY"));
			}
			swipeOrInsertCardStep();
			break;

		case EMV_APP_BLOCKED:
		case EMV_CARD_BLOCKED:
			LOGGER.info("EMV_CARD_B LOCKED: Card blocked");
			handleTransactionTermination(EntryModeStatusID.EMVCardBlocked,
			        AppConfiguration.getLanguage().getString("CARD_BLOCKED"), ShortMessages.CARD_BLOCKED);
			break;

		case EMV_TRANS_CANCELED:
			LOGGER.info("EMV_TRANS_CANCELED");
			if (isEMVOnlinePin) {
				displayWarning(AppConfiguration.getLanguage().getString("ERROR_PIN_ENTRY"), null);
			} else {
				displayWarning(AppConfiguration.getLanguage().getString("TRANSACTION_CANCELLED_BY_CARD"), null);
			}
			SocketUtil.sendMessageToCaller(SocketMessageType.StatusCode, EntryModeStatusID.Cancelled);
			deviceCleanup();
			// msrProcessor.setEventListener(new EMVRemoveForRetry(this));
			break;

		case EMV_USER_INTERFACE_TIMEOUT:
			LOGGER.info("Entry timeout");
			displayWarning(AppConfiguration.getLanguage().getString("EMV_TIMEOUT_REMOVE_CARD"), null);
 			SocketUtil.sendMessageToCaller(SocketMessageType.StatusCode, EntryModeStatusID.Timeout);
			deviceCleanup();
			break;

		case EMV_CARD_NOT_SUPPORTED:
			LOGGER.info("Error during EMV processing: EMV Card not supported: No fallback");
			emvFallbackType = FallbackType.MSR;
			gui.displayMessage(AppConfiguration.getLanguage().getString("EMV_REMOVE_CARD_AND_SWIPE"));
			resetForFallback();
			break;

		case ERROR:
		case EMV_INVALID_CARD_DATA:
			// fall thru
			baseHandlerProcessEmvInvalidCardData();
			break;

		case EMV_AMOUNT_REJECTED:
			displayWarning(AppConfiguration.getLanguage().getString("EMV_AMOUNT_REJECTED_BY_PAYER"), null);
 			SocketUtil.sendMessageToCaller(SocketMessageType.StatusCode, EntryModeStatusID.Cancelled);
			deviceCleanup();
			
		default:
			LOGGER.debug("Not handling state " + state.toString());
		}
		LOGGER.debug("<- baseEMVStateHandler() state: " + state);
	}
	
	
	private void baseHandlerProcessEmvInvalidCardData() {
		// fall thru
		LOGGER.info("-> baseHandlerProcessEmvInvalidCardData() or error");
		// if non emv we should not even get here
		if (emvRetryCounter >= clientConfiguration.getEmvRetryAttempts()) {
			// Too many attempts!!
			emvFallbackType = FallbackType.TECHNICAL;
			LOGGER.info("Processing transaction as EMV fallback type : {}", emvFallbackType.name());
			gui.displayMessage(AppConfiguration.getLanguage().getString("EMV_REMOVE_CARD_AND_SWIPE"));
			resetForFallback();
		} else {
			LOGGER.info("Attempt to restart EMV processing");
			displayMessage(AppConfiguration.getLanguage().getString("RETRY_REINSERT"), "Card Problem. Remove Card");
		}
		msrProcessor.setEventListener(new EMVRemoveForRetry(this));
	}
	
	
    /**
     * Resetting the variables when its a fallback.
     */
	private void resetForFallback() {
		emvFallback = true;
		processTransactionAsEMV = false;
	}

	
	/**
	 * Part of the EMVFirstACListerner.
	 * 
	 * @param state DeviceState
	 */
	public final void processOfflineStatusResponse(final DeviceState state) {
		LOGGER.debug("-> processOfflineStatusResponse() state {}", state.toString());
		if (transactionData.getTransactionType() == TransactionType.sale) {
			emvFinalRemove(TransactionAction.SALE);
		} else if (transactionData.getTransactionType() == TransactionType.credit2) {
			emvFinalRemove(TransactionAction.CREDIT);
		}
		 
		if (state == DeviceState.EMV_TC) {
			LOGGER.warn("offline approved");
			emvFinalData.setEmvFinalStatus("approved");
			tag8aDefaultValue = Tag8A.UnableOnlineOfflineApproved.getEmvCode();
		} else {
			LOGGER.warn("offline declined (EMV_AAC) ");
			transactionData.setCreditCardNumberFromTrack2();
			emvFinalData.setEmvFinalStatus(DECLINE);
			tag8aDefaultValue = Tag8A.OfflineDeclined.getEmvCode();
		}
	}
	
	/**
	 * 
	 * @param finalTransactionType
	 *            TransactionAction
	 * @return EMVFinalRemoveListener
	 */
	private EMVFinalRemoveListener emvFinalRemove(final TransactionAction finalTransactionType) {
		return emvFinalRemove(finalTransactionType, null);
	}

	/**
	 * 
	 * @param finalTransactionType TransactionType
	 * @param finalTransactionFields Map<String, String>
	 * @return EMVFinalRemoveListener
	 */
	private EMVFinalRemoveListener emvFinalRemove(final TransactionAction finalTransactionType,
	        final Map<String, String> finalTransactionFields) {
		LOGGER.info("-> emvFinalRemove(), finalTransactionType={}", finalTransactionType);

		final EMVFinalRemoveListener listener = new EMVFinalRemoveListener(finalTransactionType, this);
		msrProcessor.setEventListener(listener);
		gui.displayMessage(AppConfiguration.getLanguage().getString("ASK_REMOVE_CARD"));

		return listener;
	}

	
	/**
	 * 
	 */
	public final void processEMVInvalidData() {
		if (getCardInfo() != null && getCardInfo().isMasterCard()) {
			LOGGER.debug("Construct confirmation response fields");

			final TransactionAction action = getTransactionAction();
			final Map<String, String> fields = EMVDataHandler.constructConfirmRespData(arqcMap, arpcData.getArpcMap());

			emvFinalRemove(action, fields);
		} else {
			LOGGER.debug("Cancelling transaction due to invalid card data");
			cancelEMVTransaction();
		}
	}	
	/**
	 * 
	 * @return TransactionAction
	 */
	private final TransactionAction getTransactionAction() {
		TransactionAction action = null;
		if (transactionData.getTransactionType() == TransactionType.credit2) {
			action = TransactionAction.REFUND;
		} else {
			action = TransactionAction.AUTH_CONFIRMATION;
		}
		return action;
	}

	/**
	 * EMV method to obtain card information.
	 * @return CardInfo
	 */
	public CardInfo getCardInfo() {
		LOGGER.debug("-> getCardInfo()");
		if (emvCardInfo == null) {
			final String transPrepResp = msrProcessor.getTransactionPreparationResponse();
			if (transPrepResp == null || transPrepResp.isEmpty()) {
				LOGGER.error("transPrepResp is null");
				return null;
			}
			// Get AID from EMV messages
			final Map<String, String> emvInfo = EMVDataHandler.parseEMVDataMessage(transPrepResp);

			final String aid = emvInfo.get("emv_4f_applicationidentifiericc");
			if (aid == null) {
				return null;
			}

			// Note: here the Applet call a JS function to get AID info
			final boolean isDebit = DebitApplicationIdentifiers.isDebit(aid);
			if (isDebit) {
				LOGGER.debug("aid={}", aid);
				cardType = CardType.debit;
			} else {
				cardType = CardType.credit;
			}
			final String tracksData = msrProcessor.getTracksData(emvInfo);
			// cardType = credit
			emvCardInfo = new CardInfo(aid, transactionData.isCashback(), cardType, transactionData.isMasterCard(),
			        tracksData);
		}

		return emvCardInfo;
	}

	// EMVSecondACListener method calls
	// =========================================

	/**
	 * EMV_INVALID_CARD_DATA.
	 */
	public final void handleEmvInvalidCardData() {
		LOGGER.debug("-> handleEmvInvalidCardData()");
		if (getCardInfo() != null && getCardInfo().isMasterCard()) {
			LOGGER.debug("Construct confirmation response fields");
			TransactionAction temp = null;
			if (transactionData.getTransactionType() == TransactionType.credit2) {
				temp = TransactionAction.REFUND;
			} else {
				temp = TransactionAction.AUTH_CONFIRMATION;
			}

			emvFinalRemove(temp, EMVDataHandler.constructConfirmRespData(arqcMap, arpcData.getArpcMap()));
		} else {
			LOGGER.debug("Canceling transaction due to invalid card data");
			cancelEMVTransaction();
		}
	}

	/**
	 * Second generation return TC or AAC.
	 * if failToProcess is received, this is a processor issue.
	 * 
	 * @param state DeviceState
	 * @param failToProcess
	 */
	public final void handleTcAndAcc(final DeviceState state) {
		LOGGER.debug("-> handleTcAndAcc() DeviceState: {}", state);
		TransactionAction temp = null;
		if (transactionData.getTransactionType() == TransactionType.credit2) {
			temp = TransactionAction.REFUND;
		} else {
			temp = TransactionAction.AUTH_CONFIRMATION;
		}
		
		emvFinalRemove(temp);

		String tag8a = null;
		/* need to set the transaction status based on the response by the device, 
		this status is used to determine whether the transaction is approved or declined.*/
		if (failToProcess) {
			if (state == DeviceState.EMV_TC) {
				emvFinalData.setEmvFinalStatus("approved");
				tag8a = Tag8A.UnableOnlineOfflineApproved.getEmvCode();
			} else {
				emvFinalData.setEmvFinalStatus(DECLINE);
				tag8a = Tag8A.UnableOnlineOfflineDeclined.getEmvCode();
			}
		} else if (state == DeviceState.EMV_TC) {
			emvFinalData.setEmvFinalStatus("approved");
			// Ensure tag 8a has a value for online and offline approve
			tag8a = Tag8A.OnlineApproved.getEmvCode();
			
		} else if (state == DeviceState.EMV_AAC) {
			// The chip decline the transaction
			emvFinalData.setEmvFinalStatus(DECLINE);
			// we do not use tag8A to know if the transaction was declined, we use 9f27
			// emv_9f27_cryptograminformationdata=00 -> decline
			// emv_9f27_cryptograminformationdata= 1 or 2 ?? -> approved
			tag8a = arpcData.getAuthorizationCode();

		}
		//Code added to match the vault 
		if (tag8a != null && !tag8a.isEmpty()) {
			tag8aDefaultValue = tag8a;
			// listener.setTag8aDefaultValue(Tag8aDefaultValue);
		}

	}

	// ============= EMVPinListener Methods  =============================================

	/**
	 * Process PIN operations.
	 * @param state type DeviceState
	 */
	public final void pinEMVStateHandler(final DeviceState state) {
		switch (state) {

		case DONE:
			LOGGER.debug("Done with pin entry");
			ranEmvOnlinePin = true;
			msrProcessor.setEventListener(new EMVFirstACListener(this));
			msrProcessor.onlinePinComplete();
			break;

		case ERROR:
			stopTimer();
			LOGGER.error("Error encountered during pin entry");
			transactionCancelled = true;
			displayWarning(AppConfiguration.getLanguage().getString("ERROR_PIN_ENTRY"), null);
			SocketUtil.sendErrorMessageToCaller(EntryModeStatusID.PinEntryError);
			msrProcessor.cancelEMVTransaction();
			break;

		case INPUT_ERROR:
			if (transactionCancelled) {
				LOGGER.debug("input error");
				gui.displayMessage(AppConfiguration.getLanguage().getString("PIN_REENTRY"));
				startTimer();
			}
			break;

		case REJECTED:
			LOGGER.debug("Pin entry cancelled; continue with PIN bypass");
			msrProcessor.setEventListener(new EMVFirstACListener(this));
			msrProcessor.onlinePinBypass();
			break;

		case PINPAD_TIMEOUT:
			LOGGER.info("PINPAD_TIMEOUT");
			transactionCancelled = true;
			displayWarning(AppConfiguration.getLanguage().getString("PIN_ENTRY_TIMEOUT"), null);
			SocketUtil.sendErrorMessageToCaller(EntryModeStatusID.Timeout);
			break;
		// When card is removed after "Error on Pin entry " issue
		case EMV_CARD_REMOVED:
			
			//if the card is removed, on pin entry without any error
			if (!transactionCancelled) {
				displayWarning(AppConfiguration.getLanguage().getString("CARD_REMOVED_UNEXPECTEDLY_CANCELLED_TRANSACTION"), null);
				SocketUtil.sendMessageToCaller(SocketMessageType.StatusCode, EntryModeStatusID.Cancelled);
			}
			transactionCancelled = true;
			deviceCleanup();
			break;
			// when pin entry is cancelled by the user
		case CANCELLED_BY_USER:
			LOGGER.debug("Pin entry cancelled; continue with PIN bypass");
			msrProcessor.setEventListener(new EMVFirstACListener(this));
			msrProcessor.onlinePinBypass();
			break;

		default:
			LOGGER.warn("Got the following state during pin entry: {}", state.toString());
			break;
		}

	}

	// =========== EMVTransactionPreparationListener Methods ==========================
    /**
     * 
     */
	public final void handleEmvTransactionPreparationReady() {
		LOGGER.debug("-> handleEmvTransactionPreparationReady() EMV CC Info ready");

		boolean cancel = false;

		if (getCardInfo() != null) {
			if (transactionData.getTransactionType() != TransactionType.credit2
			        && transactionData.getCashbackAmt() > 0) {
				// Determine if AID allows cashback
				if (!getCardInfo().isCashbackAllowed()) {
					LOGGER.info("AID is not valid for cashback. Cancelling transaction");
					final Map<String, String> emvCCInfo = EMVDataHandler
					        .parseEMVDataMessage(msrProcessor.getTransactionPreparationResponse());
					String cardBrand = emvCCInfo.get(EmvTags.T50.tagName());
					if (cardBrand == null) {
						cardBrand = "";
					}
					gui.displayMessage("Cashback not allowed on \"" + cardBrand + "\" cards");
					cancel = true;
				}
			}
		} else {
			LOGGER.error("Invalid Card Info");
			cancel = true;
		}

		if (!cancel) {
			////
			// Init EMV step 3
			////
			msrProcessor.setEventListener(new EMVFirstACListener(this));
		} else {
			cancelEMVTransaction();
		}
	}

	// =======================================================================================================

	/**
	 * Only use for Partial Payments. Not a feature in jDAL 4.21.
	 */
	private void sendArpcToMsr() {
		msrProcessor.setEventListener(new EMVSecondACListener(this));
		msrProcessor.sendARPCToTerminal(arpcData);

	}

    /**
     * jdal needs to timeout before epic timeout.
     * The timeout time is configurable.
     */

	private void setGlobalTimeout() {
		globalTimeoutService = Executors.newSingleThreadExecutor();

		try {
			final Future<Object> f = globalTimeoutService.submit(transactionGlobalTimeout);
			final Object o = f.get(1, TimeUnit.SECONDS);
			//LOGGER.debug(f.get(1, TimeUnit.SECONDS));
		} catch (final TimeoutException e) {
			LOGGER.debug("setGlobalTimeout() ");
		} catch (final Exception e) {
			//throw new RuntimeException(e);
			LOGGER.error("setGlobalTimeout() " + e.getMessage());
		} finally {
			LOGGER.debug("setGlobalTimeout(): ExecutorService shut down ");
			if (globalTimeoutService != null) {
				globalTimeoutService.shutdown();
			}
		}
	}

	// =========================================================================================
	// Signature ...
	// =========================================================================================

	/**
	 * Processing of Signature Capture by an EMV transaction.
	 */
	public final void requestSignature() {
		LOGGER.info(" Transaction requires signature ");
		
		gui.displayMessage(AppConfiguration.getLanguage().getString("ASK_FOR_SIGNATURE"));

		signatureProcessor = new IngenicoSignature(terminalModel, clientConfiguration.getSignatureCaptureTimeout());
		signatureProcessor.setEventListener(new SignatureListener());
		try {
			signatureProcessor.captureSignature();
		} catch (IngenicoDeviceException e) {
			// The transaction is approved regardless of the signature
			SocketUtil.sendMessageToCaller(SocketMessageType.Signature, EntryModeStatusID.Cancelled);

			// TcpSocket.sendMessage(EntryModeStatusID.Cancelled.getCode());
		}
	}


	
	/**
	 * Validate whether, maximum number of attempt has reached , and need to go to fallback.
	 * 
	 */
	private void validateFallBack() {
		if (emvRetryCounter >= clientConfiguration.getEmvRetryAttempts()) {
			resetForFallback();
			if (terminalModel == TerminalModel.iUP250) {
				// Reset the EMV bit so mag stripe will be read on any following insert
				try {
					msrProcessor.updateEmvKey(0);
					jposXmlemvKey = false;
				} catch (IngenicoDeviceException e) {
					if (e.getErrorCode() == EntryModeStatusID.CardStaysInSlot.getCode()) {
						displayWarning(null, "Remove Card");
					}
				}
			}
		}
	}
	
	
	/**
	 * Reversals are only called when the card is removed unexpectedly during second generation.
	 * 
	 */
	@SuppressWarnings("unused")
	private void processReversal() {
		LOGGER.debug("-> processReversal() emvRetryCounter {}", emvRetryCounter);
		if (transactionComplete) {
			return;
		}
		if (emvRetryCounter >= clientConfiguration.getEmvRetryAttempts()) {
			resetForFallback();
		}

		final String transId = emvRetries.requestReversal(emvRetryCounter);
		if (transId != null) {
			LOGGER.debug(" **  Reversal() transid: {}", transId);
			SocketUtil.sendMessageToCaller(SocketMessageType.Reversal, transId);
			transactionData.resetValues();
			arpcData = new ArpcData();
		}
	}
	
	// ================================================================================
	//                            INNER CLASSES  
	// ================================================================================
	
	/**
	 * 
	 *
	 */
	
	private class GuiListener implements BridgeEventListener {

		@Override
		public void goToManual() {
			setStartMode(StartMode.MANUAL);
			toggleDeviceMode();
		}

		@Override
		public void goToSwipe() {
			setStartMode(StartMode.SWIPE);
			toggleDeviceMode();
		}

		@Override
		public void windowClosed() {
			if (!msrProcessor.initializationIsComplete()) {
				return;
			}
			SocketUtil.sendMessageToCaller(SocketMessageType.StatusCode, EntryModeStatusID.Cancelled);
			deviceCleanup();
		}

		@Override
		public void okPartialClicked() {
			arpcData.getArpcMap().put(EmvTags.T8A.tagName(), Tag8A.OnlineApproved.getEmvCode());
			sendArpcToMsr();
		}

		@Override
		public void voidPartialClicked() {
			arpcData.getArpcMap().put(EmvTags.T8A.tagName(), Tag8A.OnlineDeclined.getEmvCode());
			sendArpcToMsr();
		}

		@Override
		public void cardHolderCompleted() {
			verifyAmount();
		}
	}
	
	/**
	 * 
	 *
	 */
	
	private class InitDeviceListener implements DeviceEventListener {

		@Override
		public void handleEvent(final LocalActionEvent deviceEvent) {
			LOGGER.trace("-> InitDeviceListener handleEvent() {}", deviceEvent.state);
			
			if (deviceEvent.event == TerminalEvent.DEVICE) {
				switch (deviceEvent.state) {
				case READY:
					LOGGER.debug("Device ready: Start transaction processing");
					break;
				case DEVICE_NOT_CONNECTED:
					LOGGER.error(terminalModel + " not connected");
					handleDeviceNotConnected();
					break;
				case SIGCAP_NOT_ENABLED:
					LOGGER.info("Received event: SIGCAP_NOT_ENABLED");
					break;

				case ERROR:
					LOGGER.error("Error encountered initializing device {}, code {}", terminalModel, 
							deviceEvent.state.toString());
					handleDeviceNotConnected();
					break;

				case HEALTH_STATUS_ERROR:
					LOGGER.error("Error encountered requesting Health Status: {}", terminalModel);
					// Possibly the terminal was swapped and undetected? this option may have to be removed
					break;
					
				case GOT_SERIAL:
					
					String terminalSerial = msrProcessor.getTerminalSerialNumber();
					if (terminalSerial == null) {
						// this case should never happen, but it is here for security sake
						LOGGER.error("Can not obtain Serial Number Information ");
						handleTransactionTermination(EntryModeStatusID.CantConnect,
						        AppConfiguration.getLanguage().getString("ERROR_CONNECTION_AND_CANCEL_TRANSACTION"), ShortMessages.CANT_CONNECT);
						
					} else if (!terminalSerial.equals(transactionData.getSerialNumber())) {
						// devices were swapped ... the terminal info object has to be refreshed
						TerminalInfoHelper.resetTerminalInfo();
						handleTransactionTermination(EntryModeStatusID.SerialNumberMismatch,
						        AppConfiguration.getLanguage().getString("SERIAL_MISMATCH"), ShortMessages.SERIAL_MISMATCH);
					}
					break;
				default:
					// This is to ignore SIGCAP_NOT_ENABLED
					break;
				}
			}
		}
	}
	
	
	/**
	 * Listens for events thrown by the MSR.
	 * 
	 * @author luisa.lamore
	 *
	 */
	private class MSRListener implements DeviceEventListener {

		@Override
		public void handleEvent(final LocalActionEvent deviceEvent) {
			LOGGER.debug("-> handleEvent: {}", deviceEvent.state);

			if (deviceEvent.event != TerminalEvent.MSR) {
				LOGGER.warn("Ignoring {}", deviceEvent.event.toString());
				return;
			}

			LOGGER.debug("DeviceEvent: {}", deviceEvent.state);

			switch (deviceEvent.state) {

			case READY :
				handleDeviceReadyEvent();
				break;

			case EMV_CARD_DETECTED:
				LOGGER.info("EMV Card Detected");
				// an EMV card was swiped ...
				emvCardDetected = true;

				if (processTransactionAsEMV && !emvFallback) {
					gui.displayMessage(AppConfiguration.getLanguage().getString("INFORM_CARD_DETECTED"));
					msrProcessor.showSwipeForm(SwipeMode.INSERT);
					
					restartTimer();
				} else {
					LOGGER.debug("EMV fallback mode, get the track data as it stands");
					verifyAmount();
				}
				break;

			case DONE:
				LOGGER.info("Done with card swipe");
				final TerminalData paymentInfo = msrProcessor.getTransactionData();

				if (transactionData.isDebitRefund() && !debitRefundCardMatch(paymentInfo.getTrack2Data(),
				        transactionData.getIin(), transactionData.getLastFour())) {
					LOGGER.debug("card mismatch; wait for card swipe again");
					gui.displayMessage(AppConfiguration.getLanguage().getString("CARD_MISMATCH"));
					msrProcessor.reenableSwiper();
				} else {
					stopTimer();
					LOGGER.debug("verify card type. Current Mode: {}", startMode);
					boolean promptForDebit = true;
					boolean oldDebitLookup = false;

					if (startMode == StartMode.SWIPE) {
						// oldDebitLookup = paymentInfo.isNonEMVCard();
						oldDebitLookup = true;
					}
					if (!transactionData.isDebitRefund()) {
						// This code is commented out until we figure out if
						// this affects EMV
						// Non EMV will always display the SELECT.K3Z prompt
						if (oldDebitLookup) {
							LOGGER.info("This is a Non EMV card");
							//processDebit = isDebitCard(paymentInfo.getEncryptedTracks(), false);
							promptForDebit = true;
						} else {
							LOGGER.info("This is an EMV Card"); // EMVLookup
							promptForDebit = isDebitCard(paymentInfo.getEncryptedTracks(), true);
						}

						if (promptForDebit) {
							LOGGER.debug("Displaying debit/credit form");
							gui.displayMessage(AppConfiguration.getLanguage().getString("ASK_DEBIT_OR_CREDIT"));
							msrProcessor.setEventListener(new CardSelectListener());
							msrProcessor.selectCardType();
							restartTimer();
						} else {
							// For Debit only or Credit only [C]
							LOGGER.debug("Suppressed the selection for debit or credit. Card type is known");
							processDebit = false;
							verifyAmount();
						}
					} else {
						collectPin();
					}
				}
				break;

			case INPUT_ERROR:
				restartTimer();
				if (terminalModel == TerminalModel.iUP250 && processTransactionAsEMV) {
					// error here when a bad chip does not start an EMV transaction only for this device
					// Here when the user inserts and leaves a card without chip in the slot
					processiUP250Fallback();
				} else {
					// Here when we force a bad swipe. In this case there is a retry.
					nonEmvAttemptsCounter++;
					swipeOrInsertCardStep();
				}
				break;

			case EXPIRED_CARD:
				// restartTimer();
				stopTimer();
				reStartSwipeCardStep(AppConfiguration.getLanguage().getString("MANUAL_ENTRY_EXPIRED_CARD"));
				break;

			case CANCELLED_BY_USER :
				handleTransactionTermination(EntryModeStatusID.Cancelled,
				        AppConfiguration.getLanguage().getString("CANCELLED"), ShortMessages.CANCEL_MESSAGE);
				break;

			case TRANSACTION_APPROVED:
				LOGGER.info("TRANSACTION_APPROVED");
				gui.displayMessage(AppConfiguration.getLanguage().getString("TRANSACTION_APPROVED"));
				break;

			case TRANSACTION_DECLINED:
				LOGGER.info("TRANSACTION_DECLINED");
				gui.displayMessage(AppConfiguration.getLanguage().getString("TRANSACTION_DECLINED"));
				break;
				
			case EMV_CARD_INSERTED:
				emvRetryCounter++;
				LOGGER.info("EMV Card Inserted  {}  time(s) ", emvRetryCounter);
				// After a fallback a swipe should get processed resetForFallback();
				if (processTransactionAsEMV) {
					if (emvFallback) {
						if (emvRetryCounter <= clientConfiguration.getEmvRetryAttempts()) {
							// If user accidently insert the card on fallback
							swipeOrInsertCardStep();
						} else {
							LOGGER.debug("Payer keeps dipping after fallback");
							handleTransactionTermination(EntryModeStatusID.CardNotRead,
							        AppConfiguration.getLanguage().getString("MAX_SWIPES_INSERTS"),
							        ShortMessages.CARD_NOT_READ);
						}
					} else {
						// card is inserted when the transaction is timed out.
						if (timeout) {
							return;
						}
						LOGGER.info("Smart card inserted to device. Initiate EMV transaction");
						startMode = StartMode.EMV;
						startEMVProcess();
					}
				} else {
					ignoreEMVInsert();
				}
				break;

			case REJECTED:
				gui.displayMessage(AppConfiguration.getLanguage().getString("CANCEL_MESSAGE"));
				deviceCleanup();
				break;

			case ERROR:
				stopTimer();
				handleTransactionTermination(EntryModeStatusID.CardNotRead,
				        AppConfiguration.getLanguage().getString("ERROR_DURING_SWIPE"), ShortMessages.CARD_NOT_READ);
				break;

			default:
				LOGGER.warn("Got the following state during card swipe: {}", deviceEvent.state.toString());
				break;
			}
			LOGGER.debug("<- handleEvent()");
		}
		
		/**
		 * get here if card is inserted when custId is not set for EMV or, if
		 * this is a fallback.
		 */
		private void ignoreEMVInsert() {
			LOGGER.debug("ignoreEMVInsert(): Ignoring EMV insert, non EMV capabilities");
			if (terminalModel == TerminalModel.iUP250) {
				processNonEmvSelectCardType();
				return;
			}
			if (emvFallback) {
				handleTransactionTermination(EntryModeStatusID.CardNotRead,
				        AppConfiguration.getLanguage().getString("MAX_SWIPES_INSERTS"), ShortMessages.CANCEL_MESSAGE);
			} else {

				handleTransactionTermination(EntryModeStatusID.CardNotRead,
				        AppConfiguration.getLanguage().getString("DEVICE_CANNOT_PROCESS_EMV"),
				        ShortMessages.CANNOT_DO_EMV);

			}
		}
		
		/**
		 * 
		 */
		private void processNonEmvSelectCardType() {
			// If the cust is EMV but the EMV capabilities are false ... we proceed to card selection
			msrProcessor.setEventListener(new CardSelectListener());
			msrProcessor.selectCardType();
			restartTimer();
		}
		
		/**
		 * 
		 */
		private void handleDeviceReadyEvent() {
			LOGGER.debug("-> handleDeviceReadyEvent()");

			if (transactionData.isDebitRefund()) {
				// debit refund can happen in EMV and non-EMV
				final String postfix = transactionData.getIin() + CARD_FILL + transactionData.getLastFour();
				gui.displayMessage(
				        String.format(AppConfiguration.getLanguage().getString("ASK_SWIPE_CARD_NUM"), postfix));
			} else {
				// regular transaction

				if (processTransactionAsEMV) {
					if (emvFallback) {
						processNonEmvOrFallback();
					} else {
						gui.displaySwipeInsertMode(AppConfiguration.getLanguage().getString("ASK_SWIPE_OR_INSERT"));
						if (terminalModel == TerminalModel.iUP250 && emvRetryCounter > 0) {
							msrProcessor.showSwipeForm(SwipeMode.INSERT);
						} else {
							msrProcessor.showSwipeForm(SwipeMode.SWIPE_INSERT);
						}
					}
				} else {
					processNonEmvOrFallback();
					// form with label "Insert and Remove Card on green" in iUP250, others "please swipe"
					msrProcessor.showSwipeForm(SwipeMode.SWIPE);
				}
			}
			restartTimer();
		}
		
		/**
		 * Called when the MSR listener received a INPUT_ERROR when processing a transaction in the iUP/iUN.
		 * In this cases, we proceed with a "swipe" by disabling the EMV key.
		 */
		private void processiUP250Fallback() {
			// We end up here ONLY with iUP/iUN device when a non-EMV card is inserted and removed
			// This should cause a retry + fallback.
			LOGGER.debug("-> processiUP250Fallback(), Card inserted did not have a chip !!!!!");
			resetForFallback();
			try {
				msrProcessor.updateEmvKey(0);
				jposXmlemvKey = false;
				swipeOrInsertCardStep();
			} catch (IngenicoDeviceException e) {
				if (e.getErrorCode() == EntryModeStatusID.CardStaysInSlot.getCode()) {
					LOGGER.warn("Card is inside the slot!!");
					displayWarning("Ask payer to Remove Card", "Card Error. Remove Card");
					try {
						Thread.sleep(4000);
						processiUP250Fallback();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		
		/**
		 * If the transaction is non-emv or emvfallback .
		 */
		private void processNonEmvOrFallback() {
			try {
				displaySwipeCardGui(AppConfiguration.getLanguage().getString("ASK_SWIPE_CARD")); //
				if (!emvFallback) {
					//nonEmvAttemptsCounter++;
				}
			} catch (CardReadException e) {
				LOGGER.error(" SwipeCardStep : {} ", e.getMessage());
				handleTransactionTermination(EntryModeStatusID.CardNotRead,
				        AppConfiguration.getLanguage().getString("MAX_SWIPES"), ShortMessages.CARD_NOT_READ);
			}
		}
	}
	
	//-----------------------------------------------------------------------------------------------------------------
	

	/**
	 * Listens for a device response after the user selects Credit or Debit.
	 * 
	 *
	 */
	private class PINListener implements DeviceEventListener {
 

		@Override
		public void handleEvent(final LocalActionEvent deviceEvent) {
			LOGGER.debug("-> handleEvent() {}", deviceEvent.state.toString());
			if (deviceEvent.event != TerminalEvent.PIN && deviceEvent.event != TerminalEvent.ZIPCODE) {
				LOGGER.warn("Ignoring {} ", deviceEvent.event.toString());
				return;
			}

			LOGGER.debug("Got pin entry event");

			switch (deviceEvent.state) {
			case DONE:
				LOGGER.debug("Customer provided pin number");
				verifyAmount();
				break;

			case ERROR:
				// stopTimer(); TODO
				LOGGER.error("FATAL ERROR {}", AppConfiguration.getLanguage().getString("ERROR_ON_PIN_ENTRY"));
				handleTransactionTermination(EntryModeStatusID.Error,
				        AppConfiguration.getLanguage().getString("ERROR_ON_PIN_ENTRY"), ShortMessages.PIN_ERROR);
				break;

			case INPUT_ERROR:
				gui.displayMessage(AppConfiguration.getLanguage().getString("RE_ENTER_PIN_NUMBER"));
				restartTimer();
				break;

			case PINPAD_TIMEOUT:
				handleTransactionTermination(EntryModeStatusID.Timeout,
				        AppConfiguration.getLanguage().getString("TRANSACTION_TIMEOUT"), ShortMessages.TIME_OUT);
 				break;

			case ZIPCODE_INPUT_ERROR:
				gui.displayMessage(AppConfiguration.getLanguage().getString("RE_ENTER_ZIPCODE"));
				restartTimer();
				break;

			case CANCELLED_BY_USER:
				stopTimer();
				handleTransactionTermination(EntryModeStatusID.Cancelled,
				        AppConfiguration.getLanguage().getString("CANCEL_MESSAGE"), ShortMessages.CANCEL_MESSAGE);
				break;

			case DEBIT_MISSING_ENCRYPTION_KEY:
				gui.displayMessage(AppConfiguration.getLanguage().getString("ERROR_DEBIT_CARD_NOT_SUPPORTED"));
				restartTimer();
				break;

			default:
				LOGGER.warn("Got the following state during pin entry: {}", deviceEvent.state.toString());
				break;
			}
		}
	}
	
	
	/**
	 * Listens for events thrown by the MSR.
	 * 
	 * @author luisa.lamore
	 *
	 */
	private class SignatureListener implements DeviceEventListener {

		@Override
		public void handleEvent(final LocalActionEvent deviceEvent) {

			if (deviceEvent.event != TerminalEvent.SIGNATURE) {
				LOGGER.warn("Ignoring {}", deviceEvent.event.toString());
				return;
			}

			LOGGER.debug("-> handleEvent() DeviceEvent: {}", deviceEvent.state);
			switch (deviceEvent.state) {

			case DONE:
				// will use for signature ready stopTimer();
				LOGGER.info("Payer clicked accept signature button ");

				final Point[] sigPoints = signatureProcessor.getSigPoints();
				final SignatureImage image = new SignatureImage(terminalModel, sigPoints);
				final String signatureAsHex = image.getImageAsHex();
				if (signatureAsHex == null) {
					gui.displayMessage("Signature Size Overflow Error");
				} else {
					if (GlobalConstants.SHOW_SIGNATURE) {
						gui.showSignaturePanel(image.getBufferedImage());
					}
					SocketUtil.sendMessageToCaller(SocketMessageType.Signature, signatureAsHex);
					gui.removeSignaturePanel();
					gui.displayMessage(AppConfiguration.getLanguage().getString("TRANSACTION_COMPLETE"));
				}
				break;

			case ERROR:
				SocketUtil.sendMessageToCaller(SocketMessageType.Signature, EntryModeStatusID.BadData);
				break;

			case CANCELLED_BY_USER:
				LOGGER.debug("Payer Clicked delete button ");
			//	String signature = "";
				gui.displayMessage(AppConfiguration.getLanguage().getString("SIGNATURE_DELETE_BUTTON_CLICKED"));
				SocketUtil.sendMessageToCaller(SocketMessageType.Signature, EntryModeStatusID.Cancelled);
				break;

			default:
				LOGGER.warn("Got the following state during card swipe: {}", deviceEvent.state.toString());
				break;
			}
			stopTimer();
			deviceCleanup();
		}

	}
	
	
	// //
	// End step 4: TODO
	// //
	/**
	 * 
	 *
	 */
	private class CardSelectListener implements DeviceEventListener {

		/** 
		 * @param deviceEvent LocalActionEvent
		 */
		public void handleEvent(final LocalActionEvent deviceEvent) {
			LOGGER.debug("handleEvent() " + deviceEvent.event.toString());
			if (deviceEvent.event != TerminalEvent.CARDTYPESELECT) {
				LOGGER.warn("Ignoring " + deviceEvent.event.toString());
				return;
			}

			LOGGER.debug("Got credit/debit selection event");
			switch (deviceEvent.state) {
			case CREDIT_SELECT:
				LOGGER.info("Credit card selected");
				processDebit = false;
				// if (DeviceModel.iUP250 == transactionData.getDeviceName()) {
				// displayZipCode();
				// } else {
				verifyAmount();
				// }
				break;

			case DEBIT_SELECT:
				collectPin();
				break;

			case CANCELLED_BY_USER:
				handleTransactionTermination(EntryModeStatusID.Cancelled,
				        AppConfiguration.getLanguage().getString("CANCEL_MESSAGE"), ShortMessages.CANCEL_MESSAGE);
				break;

			case ERROR:
				stopTimer();
				handleTransactionTermination(EntryModeStatusID.Error,
				        AppConfiguration.getLanguage().getString("ERROR_ON_CARD_TYPE"), ShortMessages.ERROR_ON_CARD_TYPE);
				break;

			default:
				LOGGER.warn("Unexpected state received: {} ", deviceEvent.state.toString());
				break;
			}
		}

	}
	
	
	/**
	 * Works with the ManualEntryDataListener class to receive events related to
	 * actions performed in the Ingenico Device with the purpose of updating the
	 * GUI.
	 * 
	 * @author tc
	 *
	 */
	private class ManualEntryListener implements DeviceEventListener {

		@Override
		public void handleEvent(final LocalActionEvent localEvent) {

			LOGGER.debug("-> handleEvent() DeviceEventListener state {}, event {}", localEvent.state, localEvent.event);
			if ((localEvent.event == TerminalEvent.MANUAL_ENTRY) || (localEvent.event == TerminalEvent.AMOUNT)) {

				manualentryevents:

				switch (localEvent.state) {
				case MANUAL_PAN_ENTRY:
					restartTimer();
					gui.displayManualMode();
					break;

				case MANUAL_CVV_ENTRY:
					restartTimer();
					final String msg1 = AppConfiguration.getLanguage().getString("ASK_CVV");
					gui.displayMessage(msg1);
					break;

				case DIRECT_CVV_ENTRY:
					// restartTimer();
					break;

				case MANUAL_ZIPCODE_ENTRY:
					restartTimer();
					gui.displayMessage(AppConfiguration.getLanguage().getString("ASK_ZIPCODE"));
					break;

				case MANUAL_EXP_ENTRY:
					restartTimer();
					gui.displayMessage(AppConfiguration.getLanguage().getString("ASK_EXP_DATE"));
					gui.hideButtons();
					break;

				case MANUAL_ENTRY_OFF:
					restartTimer();
					break;

				case MANUAL_ENTRY_CANCELED:
					handleTerminatingStatus(EntryModeStatusID.Cancelled,
					        AppConfiguration.getLanguage().getString("CANCEL_MESSAGE"));
					break;

				case EXPIRED_CARD:
					restartTimer();
					reStartSwipeCardStep(AppConfiguration.getLanguage().getString("MANUAL_ENTRY_EXPIRED_CARD"));
					break;

				case MANUAL_ENTRY_DONE:
					restartTimer(60000);
					gui.showUserNamesPanel();
					if (timeout) {
						break manualentryevents;
					}
					processDebit = false;
					break;

				case MANUAL_ENTRY_ERROR:
					gui.displayMessage(
					        AppConfiguration.getLanguage().getString("REPEAT_TRANSACTION_ERROR_OCURRED"));
					restartTimer();
					break;

				case INPUT_ERROR:
					if (nonEmvAttemptsCounter > clientConfiguration.getEmvRetryAttempts()) {
						handleTerminatingStatus(EntryModeStatusID.BadData,
						        AppConfiguration.getLanguage().getString("MAX_MANUAL_ATTEMPTS"));
						break;
					} else {
						gui.displayMessage(
						        AppConfiguration.getLanguage().getString("MANUAL_TRANSACTION_INPUT_ERROR"));
						nonEmvAttemptsCounter++;
						try {
							Thread.sleep(GlobalConstants.MESSAGE_WARN_SLEEP_TIMER);
						} catch (InterruptedException e) {
						}
						startMode = StartMode.MANUAL;
						toggleDeviceMode();
						restartTimer();
					}
					break;

				case CANCELLED_BY_USER:
					handleTerminatingStatus(EntryModeStatusID.Error,
					        AppConfiguration.getLanguage().getString("CANCEL_MESSAGE"));
					LOGGER.error("MANUAL_ENTRY_ERROR: REJECTED: Error encountered during manual entry");
					break;

				case ACCEPTED:
					processDebit = false;
					submitNonEmvTransactionData();
					deviceCleanup();
					break;

				default:
					break;

				}
			}

		}

	}
	
	
	/**
	 * 
	 *  
	 *
	 */
	private class VerifyAmtListener implements DeviceEventListener {
	 

		@Override
		public void handleEvent(final LocalActionEvent localEvent) {
			if (localEvent.event != TerminalEvent.AMOUNT) {
				LOGGER.warn("Ignoring event that's not an AMOUNT");
				return;
			}

			// This is the last step. So stop the timer
			stopTimer();

			LOGGER.info("Device event: {}", localEvent.state);
			switch (localEvent.state) {
			case ACCEPTED:
				restartTimer(clientConfiguration.getSignatureCaptureTimeout());
				submitNonEmvTransactionData();
				break;

			case CANCELLED_BY_USER:
				handleTransactionTermination(EntryModeStatusID.Cancelled,
				        AppConfiguration.getLanguage().getString("CANCEL_MESSAGE"), ShortMessages.CANCEL_MESSAGE);
				deviceCleanup();
				break;

			default:
				LOGGER.warn("Received {} state from AMOUNT Event", localEvent.state.toString());
				handleTransactionTermination(EntryModeStatusID.Error,
				        AppConfiguration.getLanguage().getString("ERROR_VERIFYING_AMOUNT"), ShortMessages.CANCEL_MESSAGE);
				deviceCleanup();
				break;
			}

			// Verify amount is the last step in the transaction processing
			// Proceed with device cleanup

		}
	}
	
	
	/**
	 * Class to handle idle timeout at the applet level.
	 * 
	 * @author kmendoza TODO: Relocate this and the *Timer() functions to its
	 *         own class
	 */
	private class TimeoutTrigger extends TimerTask {
		/** */
		public void run() {
			
			if (transactionComplete) {
				return;
			}
			LOGGER.error(" ****************  Transaction timeout  ***************************");
			gui.userInfomationTimeout();
			if (terminalModel == TerminalModel.iUP250) {
				handleIselfTerminatingStatus(EntryModeStatusID.Timeout, ShortMessages.TIME_OUT);
			} else {
				if (signatureProcessor == null) {
					handleTerminatingStatus(EntryModeStatusID.Timeout,
					        AppConfiguration.getLanguage().getString("TRANSACTION_TIMEOUT"));
				} else {
					handleTerminatingStatus(EntryModeStatusID.Timeout,
					        AppConfiguration.getLanguage().getString("SIGNATURE_TIMEOUT"));
				}
				
			}
		}
	}

}
