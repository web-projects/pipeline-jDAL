package com.trustcommerce.ipa.dal.exceptions;



public class DeviceNotConnectedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new CardReadException
	 * 
	 * @param message
	 *            the message
	 */
	public DeviceNotConnectedException(final String message) {
		super(message);
	}

	/**
	 * Instantiates a new CardReadException
	 * 
	 * @param cause
	 *            the cause
	 */
	public DeviceNotConnectedException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new CardReadException 
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public DeviceNotConnectedException(final String message, final Throwable cause) {
		super(message, cause);
	}

}