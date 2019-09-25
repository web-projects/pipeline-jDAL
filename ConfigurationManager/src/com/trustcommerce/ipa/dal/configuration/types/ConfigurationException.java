package com.trustcommerce.ipa.dal.configuration.types;


/**
 * Luisa Lamore
 * @author t0127127
 *
 */
public class ConfigurationException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1310475953877241178L;


	/**
	 * Instantiates a new state initialization exception.
	 */
	public ConfigurationException() {
	}


	/**
	 * Instantiates a new state initialization exception.
	 * 
	 * @param message
	 *            the message
	 */
	public ConfigurationException(final String message) {
		super(message);
	}


	/**
	 * Instantiates a new state initialization exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public ConfigurationException(final Throwable cause) {
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
	public ConfigurationException(final String message, final Throwable cause) {
		super(message, cause);
	}


}
