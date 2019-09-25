package com.trustcommerce.ipa.dal.model.exceptions;


public class BadDataException extends Exception {

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
	public BadDataException(final String message) {
		super(message);
	}


	/**
	 * Instantiates a new state initialization exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public BadDataException(final Throwable cause) {
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
	public BadDataException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
