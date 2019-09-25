package com.trustcommerce.ipa.dal.commport.exceptions;


/**
 * Luisa Lamore
 * @author t0127127
 *
 */
public class PortConnectionException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1310475953877241178L;


	/**
	 * Instantiates a new state initialization exception.
	 */
	public PortConnectionException() {
	}


	/**
	 * Instantiates a new state initialization exception.
	 * 
	 * @param message
	 *            the message
	 */
	public PortConnectionException(final String message) {
		super(message);
	}


	/**
	 * Instantiates a new state initialization exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public PortConnectionException(final Throwable cause) {
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
	public PortConnectionException(final String message, final Throwable cause) {
		super(message, cause);
	}


}
