package com.trustcommerce.ipa.dal.bridge.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.bridge.TerminalInfoHelper;
import com.trustcommerce.ipa.dal.bridge.TransactionHelper;
import com.trustcommerce.ipa.dal.bridge.types.Consts;
import com.trustcommerce.ipa.dal.commport.exceptions.PortConnectionException;
import com.trustcommerce.ipa.dal.commport.ports.PortCommunicator;
import com.trustcommerce.ipa.dal.configuration.app.AppConfiguration;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.configuration.types.ConfigurationException;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.constants.device.TerminalModel;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.constants.global.JdalVersion;
import com.trustcommerce.ipa.dal.constants.messages.SocketMessageType;
import com.trustcommerce.ipa.dal.gui.controller.SwingController;
import com.trustcommerce.ipa.dal.gui.model.GuiInfo;
import com.trustcommerce.ipa.dal.logger.LoggerConfig;
import com.trustcommerce.ipa.dal.logger.Utils;
import com.trustcommerce.ipa.dal.model.TransactionStatus;
import com.trustcommerce.ipa.dal.model.fileUpload.Upload;
import com.trustcommerce.ipa.dal.model.payment.Transaction;


/**
 * A server program which accepts requests from clients to capitalize strings.
 * When clients connect, a new thread is started to handle an interactive dialog
 * in which the client sends in a string and the server thread sends back the
 * capitalized version of the string.
 *
 * The program runs in an infinite loop, so shutdown is platform dependent.
 * If you run it from a console window with the "java" interpreter, Ctrl+C
 * generally will shut it down.
 */

public class DalActivator {

	/**log4j logger.*/
	private static final Logger LOGGER = LoggerFactory.getLogger(DalActivator.class);
	/** */
	private static final String MESSAGE_DELIMITER = "~";
	/** */
	private static final String PAYMENT_SUFFIX = "EOF";
	
	private static final int SLEEPTIME_WAIT_BEFORE_SHUTDOWN = 5000;
	/** TCP/IP socket use to connect to DAL. */
	private static Socket socket;
	/** .*/
	private static PrintWriter out;
	/** .*/
	private static BufferedReader in;
    /** .*/
    private static boolean isDev;
    /** .*/
    private static TransactionHelper ingenicoApp;
 
    /** .*/
    private static TerminalModel terminalModel;
    /** .*/
    private static SwingController gui;
    /** .*/
    private static boolean connectedToDALBridge;


    /**
     * Sends a message using the TCP Socket to the caller application: in this case, the jDAL bridge.
     * @param messageType type SocketMessageType
     * @param message type String
     */
    static void sendMessage(final SocketMessageType messageType, final String message) {
    	LOGGER.debug("***************************************************************** ");
    	if (!connectedToDALBridge) {
    		return;
    	}
    	LOGGER.debug("-> sendMessage(): " + messageType.name());
		
		final StringBuilder sb = new StringBuilder(messageType.name());
		sb.append(MESSAGE_DELIMITER);
		sb.append(message);
		sb.append(MESSAGE_DELIMITER);
		sb.append(PAYMENT_SUFFIX);
		sb.append("\n");
		
		if (out != null) {
			out.println(sb.toString());
		}
 	}

	/**
	 * Single method use to send a Payment Transaction completion signal to DAL bridge.
	 */
	static void transactionComplete() {
		ingenicoApp = null;
 		sendMessage(SocketMessageType.SocketStatus, "TransactionComplete");
	}
	

	/**
	 * When we detect that we cannot connect to a terminal we need to request jDAL termination.
	 * @param messageCode type EntryModeStatusID
	 */
	static void cannotConnectToTerminal(final EntryModeStatusID messageCode) {
		sendMessage(SocketMessageType.StatusCode, messageCode.getCodeAsString());
		sendMessage(SocketMessageType.StatusCode, EntryModeStatusID.RequestJdalShutDown.getCodeAsString());
		try {
			// To avoid extra processing ... while waiting for DAL to terminate jDAL
			Thread.sleep(SLEEPTIME_WAIT_BEFORE_SHUTDOWN);
		} catch (InterruptedException e) {
 		}
	}

	
	/**
	 * Application method to run the server which runs in an infinite loop listening
	 * on port 9898. When a connection is requested, it spawns a new thread to
	 * do the servicing and immediately returns to listening. The server keeps a
	 * unique client number for each client that connects just to show
	 * interesting logging messages. It is certainly not necessary to do this.
	 * @param args string[]
	 * @throws Exception socket connection exception
	 */
	public static void main(final String[] args) throws Exception {
		LOGGER.info("jDAL is running.");
		connectedToDALBridge = false;
		Utils.createTcipaLogFolder();
		LoggerConfig.startLogger();
		Utils.logHeader("TrustCommerce jDAL Started");
		isDev = ClientConfigurationUtil.getConfiguration().isDev();
		gui = new SwingController(getGuiInformation());
		gui.preparePaymentWidgets();
		
		// ===================
		// Bad connections will shut down jDAL
		verifyConnections(true);
		// initialize the terminal info helper
		TerminalInfoHelper.setGui(gui);
		TerminalInfoHelper.setDeviceModel(terminalModel);
		TerminalInfoHelper.getTerminalInfo();
		

		// ===================
		
		int clientNumber = 0;
		final int port = getPort();
		LOGGER.info("Connecting to port " + port);
		
	
		ServerSocket listener = null;

		try {
			listener = new ServerSocket(port);
			// infinite loop
			//while (true) {
				LOGGER.debug("Listening ..... ");

				socket = listener.accept();
				clientNumber++;
				if (clientNumber > 1) {
					LOGGER.error("===============================================================");
					LOGGER.error("Security issue. Connection Refused. Only one client can be connected to jDAL");
					LOGGER.error("===============================================================");
					clientNumber--;
				} else {
					LOGGER.debug("Connection Accepted");
					connectedToDALBridge = true;
					new Capitalizer(socket, clientNumber).start();
				}
			//}
	 
		} catch (BindException e) {
			// Address already in use: JVM_Bind
			LOGGER.error(" *** INTERNAL ERROR, jDAL is already running !!!! ***");
			System.exit(1);
		} finally {
			if (listener != null) {
				listener.close();
			}
			LOGGER.warn("done");
		}
		

	}

	/**
	 * A private thread to handle capitalization requests on a particular
	 * socket. The client terminates the dialogue by sending a single line
	 * containing only a period.
	 */
	private static class Capitalizer extends Thread {
		/** Only one client connection is accepted. */
		private int clientNumber;
		/** */
		public Capitalizer(Socket socket, final int clientNo) {
			LOGGER.debug("-> Constructor ");
			

			// this.socket = socket;
			this.clientNumber = clientNo;
			LOGGER.info("New connection with client# " + clientNumber + " at " + socket);
		}

		/**
		 * Services this thread's client by first sending the client a welcome
		 * message then repeatedly reading strings and sending back the
		 * capitalized version of the string.
		 */
		public void run() {
			try {

				// Decorate the streams so we can send characters
				// and not just bytes. Ensure output is flushed
				// after every newline.
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				// when no device is connected send bad connection to dal and request a shutdown
				if (terminalModel == null) {
					cannotConnectToTerminal(EntryModeStatusID.BadConnection);					
				} else {
					// Send a signal message to the client ....
					sendMessage(SocketMessageType.SocketStatus, "Ready");
					//if (terminalInfo != null) {
						//sendMessage(SocketMessageType.TerminalInfo, terminalInfo.toString());
					//}
				}
				
				if (GlobalConstants.LOCAL_TEST) {
					sendMessage(SocketMessageType.SocketStatus, "Enter a line with only a period to quit");
				}

				// Get messages from the client, line by line
				while (true) {
 					final String input = in.readLine();
 					LOGGER.debug("input received ");
 					
					if (input == null || input.equals(".")) {
						openGui(AppConfiguration.getLanguage().getString("JDAL_TERMINATION"));
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
 						}
						LOGGER.warn("Received termination signal: Closing socket ... Good Bye!!");
						break;
					}
					
					// =============================================
					
					final Object object = getObjectType(input);
					final TerminalModel model = null;

					// Upload
					if (object instanceof Transaction) {
						LOGGER.debug(" Received an object instance of  Transaction");
						if (!processTransactionReceived(object)) {
							continue;
						}

					} else if (object instanceof Upload) {
						LOGGER.warn(" Received an object instance of  Upload. Use the File Uploader tool");
						sendMessage(SocketMessageType.SocketStatus, "Acknowledge: FileUpload object Received ");


					} else if (object instanceof TransactionStatus) {
						LOGGER.debug(" Received an object instance of  TransactionStatus");
						if (ingenicoApp != null) {
							sendMessage(SocketMessageType.SocketStatus, "Acknowledgement: Signature object Received ");
							final TransactionStatus ts = (TransactionStatus) object;
							ingenicoApp.processSignature(ts);
						}

					} else if (object instanceof Integer) {
						final Integer value = (Integer) object;
						LOGGER.info(" *** Received status code {}", value);
						validateTransactionResponse(value);

					} else if (object instanceof String) {
						final String temp = (String) object;
						LOGGER.info(" Received a request for {}", temp);
						sendMessage(SocketMessageType.SocketStatus, "Acknowledgement:" + temp + " Received ");
						
						if (temp.equalsIgnoreCase(SocketMessageType.TerminalInfo.name())) {
							sendMessage(SocketMessageType.TerminalInfo, TerminalInfoHelper.getTerminalInfo().toString());
 
						} else {
							LOGGER.debug("Not implemented ");
						}
					} else {
						LOGGER.info("Received an acknowledgement ... ");
					}

				}
			} catch (IOException e) {
				LOGGER.error("IOException: Error handling client# " + clientNumber + ": " + e.getMessage());
				LOGGER.error("ERROR CONNECTION CLOSED");

			} finally {
				try {
					out.flush();
					socket.close();
				} catch (IOException e) {
					LOGGER.info("Couldn't close a socket, what's going on?");
				}
				LOGGER.warn("finally ... Connection with client# " + clientNumber + " closed. jDAL is terminating");
 				System.exit(0);
			}
		}

	}
	
	
	private static boolean processTransactionReceived(Object object) {
		LOGGER.debug(" Received an object instance of  Transaction");
		sendMessage(SocketMessageType.SocketStatus, "Acknowledge: Payment object Received ");
		final Transaction transactionData = (Transaction) object;
		
		if (ingenicoApp == null) {
			if (!verifyConnections(false)) {
				// device has been disconnected ...accidentally ..
				return false;
			}
			// Failure to validate will send a 911 ...
			if (!validateTransactionRequest(transactionData)) {
				return false;
			}
 			if (transactionData.getArpc() == null || transactionData.getArpc().isEmpty()) {
				// not left over messages after transaction cancelled on second gen
				ingenicoApp = new TransactionHelper(transactionData, TerminalInfoHelper.getTerminalInfo(), gui);
			}
		} else {
			if (!validateTransactionResponse(transactionData.getErrorTypeID())) {
				return false;
			}
			if (transactionData.getArpc() == null || transactionData.getArpc().isEmpty()) {
				verifyConnections(false);
				ingenicoApp = new TransactionHelper(transactionData, TerminalInfoHelper.getTerminalInfo(), gui);
			} else {
				LOGGER.debug(" Received ARPC");
				ingenicoApp.processArpc(transactionData.getArpc(), transactionData.getErrorTypeID());
			}
		}
		return true;
	}

	/** getting port number from the configuration file.*/
	private static int getPort() {

		int paymentPort = 0;
		try {
			final String paymentPortTemp = ClientConfigurationUtil.getConfiguration().getPaymentPort();
			if (paymentPortTemp != null) {
				paymentPort = Integer.parseInt(paymentPortTemp);
			}
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOGGER.debug(" <- getPort() " + paymentPort);
		return paymentPort;
	}
	
	/**
	 * 
	 * @param input String
	 * @return Object
	 */
	private static Object getObjectType(final String input) {
		LOGGER.debug(" -> getObjectType() ");
		if (input == null) {
			return null;
		}
		final ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS, false);

		Object object = null;
		final String[] temp = input.split(MESSAGE_DELIMITER, 2);
		if (temp.length == 2) {
			// This is an object
			try {
				if (temp[0].equalsIgnoreCase(SocketMessageType.Payment.name())) {
					object = mapper.readValue(temp[1], Transaction.class);

				} else if (temp[0].equalsIgnoreCase(SocketMessageType.FileUpload.name())) {
					object = mapper.readValue(temp[1], Upload.class);

				} else if (temp[0].equalsIgnoreCase(SocketMessageType.Signature.name())) {
					object = new ObjectMapper().readValue(temp[1], TransactionStatus.class);
					
				} else if (temp[0].equalsIgnoreCase(SocketMessageType.TerminalInfo.name())) {
					object = temp[0];
					
				} else if (temp[0].equalsIgnoreCase(SocketMessageType.StatusCode.name())) {
					// the socket status needs the status code
					object = Integer.parseInt(temp[1]);
				}
			} catch (Exception e) {

			}

		} else if (temp.length == 1) {
			// problems ... ignore
		}
		return object;
	}
 	

	/**
	 * This is for showing Error Gui if any error occurred when transaction
	 * starts.
	 * 
	 * @param message
	 * @param terminalModel
	 */
	public static void openGui(final String message) {
		
		if (gui == null) {
			return;
		}
		//gui.addReconnect(message);

		/*if (message == UserMessages.ERROR_CONNECTION) {
			// add the Reconnect button and gets the Reconnect button Listener.
			logger.debug("Opening Error_Connection GUI");
			gui.setEventListener(activator.new ReconnectButtonListener());
			gui.enableReconnectButton(message);
			gui.showSimpleEventGui(message);
			gui.showMainFrame();*/
		if (message == AppConfiguration.getLanguage().getString("UPLOAD_COMPLETE")) {
			gui.showSimpleEventGui(message);
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
			}
		} else {
			gui.showSimpleEventGui(message);
			try {
				Thread.sleep(GlobalConstants.MESSAGE_WARN_SLEEP_TIMER * 2);
			} catch (InterruptedException e) {
			}
			if (ingenicoApp != null) {
				ingenicoApp.processTransactionComplete(AppConfiguration.getLanguage().getString("JDAL_TERMINATION"));
			}
			gui.resetWidgets();
		}

	}
	
	
	/**
	 * 
	 * @param jdalStartUp boolean. True if TCP socket is not up yet ... 
	 */
	private static boolean verifyConnections(final boolean jdalStartUp) {
		boolean result = false; // assume the worse
		TerminalModel model = null;
		try {
			model = PortCommunicator.getConnectedDevice();
		} catch (PortConnectionException e) {
			openGui(e.getMessage());
			cannotConnectToTerminal(EntryModeStatusID.BadConnection);
			return result;
		}
		
		if (model == null) {
			// terminal is not connected in the USB port
			if (jdalStartUp) {
				openGui(AppConfiguration.getLanguage().getString("ERROR_CONNECTION"));
			} else {
				openGui(AppConfiguration.getLanguage().getString("ERROR_CONNECTION_AND_CANCEL_TRANSACTION"));
				cannotConnectToTerminal(EntryModeStatusID.BadConnection);
			}
			return false;
		}

		if (terminalModel == null) {
			// Initialization Process: first time connections are established
			terminalModel = model;
			
		} else if (model != terminalModel) {
			// the devices were swapped ...
			sendMessage(SocketMessageType.StatusCode, EntryModeStatusID.SerialNumberMismatch.getCodeAsString());
			// The current terminal info is not valid anymore, it contains info from the previous device
			TerminalInfoHelper.resetTerminalInfo();
			terminalModel = model;
			TerminalInfoHelper.setDeviceModel(model);
			openGui(AppConfiguration.getLanguage().getString("SERIAL_MISMATCH"));

		} else {
			terminalModel = model;
			result = true;
		}

		return result;
	}
	
	
	/**
	 * Validates that the transaction can be processed. 
	 * @param errorTypeId
	 * @return false if the transaction needs interruption
	 */
	private static boolean validateTransactionResponse(int errorTypeId) {
		// Detect problem with internal network connection
		LOGGER.debug(" -> validateTransactionResponse() error: {}", errorTypeId);
		if (errorTypeId == EntryModeStatusID.DNSValue.getCode() 
				|| errorTypeId == EntryModeStatusID.LinkFailure.getCode()
				|| errorTypeId == EntryModeStatusID.TCLinkCommunicationFailure.getCode()) {
			
			LOGGER.warn(" Received error type {} => transaction terminated", errorTypeId);
			sendMessage(SocketMessageType.StatusCode, Integer.toString(errorTypeId));
			final String temp = String.format(AppConfiguration.getLanguage().getString("FAIL_TO_PROCESS"), errorTypeId);
			openGui(temp);
			return false;
		 
		} else if (errorTypeId == EntryModeStatusID.TCLinkIsDown.getCode()) {
			final String temp = String.format(AppConfiguration.getLanguage().getString("TCLINK_DOWN"));
			openGui(temp);
			return false;
			
		} else {
			return true;
		}
	}
	
	
	/**
	 * Validates a new transaction request.
	 * If the serial number stored in the db does not match the SN of the terminal attached, cancel transaction.
	 * 
	 * @param trans Transaction
	 * @return false if the transaction needs interruption
	 */
	private static boolean validateTransactionRequest(Transaction trans) {
		LOGGER.debug(" -> validateTransactionRequest() ");
		if (trans.getSerialNumber() == null || trans.getSerialNumber().isEmpty()) {
			LOGGER.error("*** INTERNAL: Serial number missing in Payment object ");
			return false;
		}
		if (trans.getAmount() == null || trans.getAmount().isEmpty()) {
			LOGGER.error("*** INTERNAL: amount missing in Payment object ");
			return false;
		}
		return true;
	}
	
	
	private static GuiInfo getGuiInformation() {
		GuiInfo info = new GuiInfo();
		info.setTitle(Consts.GUI_PROCESS_PAYMENT_TITLE);
		info.setVersion(JdalVersion.JDAL_VERSION);
		info.setFooter(AppConfiguration.getLanguage().getString("GUI_PROCESS_PAYMENT_FOOTER"));
		info.setShowFrame(true);
		return info;
	}

}

//https://www.cs.uic.edu/~troy/spring05/cs450/sockets/socket.html
