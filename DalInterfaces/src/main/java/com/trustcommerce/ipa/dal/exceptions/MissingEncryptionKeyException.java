package com.trustcommerce.ipa.dal.exceptions;

public class MissingEncryptionKeyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new state initialization exception.
	 * 
	 * @param message
	 *            the message
	 */
	public MissingEncryptionKeyException(final String message) {
		super(message);
	}

	/**
	 * Instantiates a new state initialization exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public MissingEncryptionKeyException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new state initialization exception.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public MissingEncryptionKeyException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
