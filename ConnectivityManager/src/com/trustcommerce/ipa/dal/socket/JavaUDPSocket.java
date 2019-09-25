package com.trustcommerce.ipa.dal.socket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.apache.log4j.Logger;

import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;

/**
 * This class transfer the CC data to C# Code using UDP transport.
 * 
 * @author luisa.lamore
 *
 */
public class JavaUDPSocket {

	private static final Logger LOGGER = Logger.getLogger(JavaUDPSocket.class);
	private static final String LOCAL_IPADDRESS = "127.0.0.1";


	public void runJavaSocket(String message) {
		LOGGER.info("Java Sockets Program has started...");
		
		try {
			final String paymentPortTemp = ClientConfigurationUtil.getConfiguration().getPaymentPort();
			int paymentPort = 0;
			if (paymentPortTemp != null) {
				 paymentPort = Integer.parseInt(paymentPortTemp);
			}
			final DatagramSocket socket = new DatagramSocket();
			LOGGER.info("Sending data thru udp socket port " + paymentPort);

			// Send the Message
			final DatagramPacket datagramPacket = toDatagram(message, InetAddress.getByName(LOCAL_IPADDRESS), paymentPort);
			socket.send(datagramPacket);
			socket.close();
		} catch (Exception e) {
			LOGGER.error("Unable to open Payment port, please bad Client configuration ");
			e.printStackTrace();
		}
	}

	private DatagramPacket toDatagram(final String s, final InetAddress destIA, final int destPort) {
		// Deprecated in Java 1.1, but it works:
		byte[] buf = new byte[s.length() + 1];
		s.getBytes(0, s.length(), buf, 0);
		// The correct Java 1.1 approach, but it's
		// Broken (it truncates the String):
		// byte[] buf = s.getBytes();
		return new DatagramPacket(buf, buf.length, destIA, destPort);
	}
}
