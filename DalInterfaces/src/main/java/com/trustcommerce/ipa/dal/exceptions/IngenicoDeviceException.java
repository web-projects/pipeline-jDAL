package com.trustcommerce.ipa.dal.exceptions;


public class IngenicoDeviceException extends Exception {

	/**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private int errorNumber;
	
	/**
	 * Instantiates a new state initialization exception.
	 * 
	 * @param message
	 *            the message
	 */
	public IngenicoDeviceException(final String message) {
		super(message);
	}

	/**
	 * Instantiates a new state initialization exception.
	 * 
	 * @param message
	 *            the message
	 */
	public IngenicoDeviceException(final String message, final int error) {
		super(message);
		errorNumber = error;
	}

	
	/**
	 * Instantiates a new state initialization exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public IngenicoDeviceException(final Throwable cause) {
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
	public IngenicoDeviceException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	
	public int getErrorCode() {
		return errorNumber;
	}

}
