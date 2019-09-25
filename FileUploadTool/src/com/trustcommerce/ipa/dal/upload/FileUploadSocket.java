package com.trustcommerce.ipa.dal.upload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.configuration.types.ConfigurationException;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.logger.LoggerConfig;
import com.trustcommerce.ipa.dal.model.fileUpload.Upload;
/** */
public class FileUploadSocket {
	/** log4j logger.*/

	private static final Logger LOGGER = Logger.getLogger(FileUploadSocket.class);
	/** */
	private static final String SERVER_ADDRESS = "localhost";
	/** */
	private static int processId;
	/** */
	private BufferedReader in;
	/** */
	private PrintWriter out;
	/**
	 * 
	 * @return Socket
	 * @throws IOException Exception
	 */
	public final Socket connect() throws IOException {
		// String serverAddress = JOptionPane.showInputDialog(frame, "Enter IP
		// Address of the Server:",
		// "Welcome ",
		// JOptionPane.QUESTION_MESSAGE);

		// Make connection and initialize streams
		LOGGER.debug("connect: ");
		Socket socket = null;
		try {
			socket = new Socket(SERVER_ADDRESS, getPort());
		} catch (ConnectException e) {
			try {
				Thread.sleep(GlobalConstants.MESSAGE_WARN_SLEEP_TIMER);
			} catch (InterruptedException e1) {
			}
		}
		return socket;
	}
	/**
	 * 
	 * @param message String
	 */

	private void sendMessage(final String message) {

		out.println("FileUpload~" + message + "\n");
	}

	/**
	 * Implements the connection logic by prompting the end user for the
	 * server's IP address, connecting, setting up streams, and consuming the
	 * welcome messages from the server. The Capitalizer protocol says that the
	 * server sends three lines of text to the client immediately after
	 * establishing a connection.
	 * @throws IOException Exception while connecting to client
	 */
	public final void connectToServer() throws IOException {
		LOGGER.debug("connectToServer ");

		final Socket socket = connect();

		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		int counter = 0;
		// Consume the initial welcoming messages from the server
		for (int i = 0; i < 100; i++) {
			final String temp = in.readLine() + "\n";

			final Object o = getObject(temp);
			if (o instanceof String) {
				LOGGER.debug("** Received    *** " + "\n");
				if (((String) o).contains("Ready")) {
					final Upload upload = FileUploadUtil.getFormsUploadObject(processId);
					// Send message to jDAL
					sendMessage(upload.toString());

				} else {
					counter++;
					final String msg = (String) o;
					LOGGER.debug("Progress " + counter + " bytes uploaded" + msg);
				}
			}
			LOGGER.debug("done  " + i);
		}
	}

	/**
	 * Runs the client application.
	 * @param args String[]
	 * @throws Exception running Client application.
	 */
	public static void main(final String[] args) throws Exception {
		LoggerConfig.startLogger();

		final String temp = (String) args[0];
		processId = Integer.parseInt(temp);

		final FileUploadSocket client = new FileUploadSocket();
		client.connectToServer();
	}
	/**
	 * 
	 * @return int
	 */

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
		return paymentPort;
	}
/**
 * 
 * @param input String
 * @return Object
 */
	private static Object getObject(final String input) {
		LOGGER.debug(" -> getObject() " + input);

		Object object = null;
		final String[] temp = input.split("~");
		if (temp.length > 2) {
			// This is an object
			try {
				if (temp[0].equalsIgnoreCase("StatusCode")) {
					LOGGER.debug(" -> Received status code () " + input);
					if (temp[1].contains("911")) {
						System.exit(0);
					}
				} else if (temp[1].equalsIgnoreCase("Ready")) {
					LOGGER.debug(" -> Received Ready ");
					object = temp[1];
				}
			} catch (Exception e) {

			}

		}
		return object;
	}
}
