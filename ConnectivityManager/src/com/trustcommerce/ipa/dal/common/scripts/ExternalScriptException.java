package com.trustcommerce.ipa.dal.common.scripts;

public class ExternalScriptException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4384580946846623114L;

	/**
	 * Instantiates a new state initialization exception.
	 * 
	 * @param message
	 *            the message
	 */
	public ExternalScriptException(final String message) {
		super(message);
	}

	/**
	 * Instantiates a new state initialization exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public ExternalScriptException(final Throwable cause) {
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
	public ExternalScriptException(final String message,
			final Throwable cause) {
		super(message, cause);
	}
}
