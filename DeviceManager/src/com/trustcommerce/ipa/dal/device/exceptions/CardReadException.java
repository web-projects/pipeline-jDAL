package com.trustcommerce.ipa.dal.device.exceptions;

public class CardReadException extends Exception {

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
	public CardReadException(final String message) {
		super(message);
	}

	/**
	 * Instantiates a new CardReadException
	 * 
	 * @param cause
	 *            the cause
	 */
	public CardReadException(final Throwable cause) {
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
	public CardReadException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
