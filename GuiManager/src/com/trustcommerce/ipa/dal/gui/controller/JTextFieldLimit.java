package com.trustcommerce.ipa.dal.gui.controller;

import javax.swing.text.AttributeSet;

import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * JTextFieldLimit.
 * 
 *
 */
public class JTextFieldLimit extends PlainDocument {
	/** */
	private static final long serialVersionUID = 1L;
	/** */
	private int limit;
	/** 
	 * @param limit int
	 */
	JTextFieldLimit(final int limit) {
		super();
		this.limit = limit;
	}
	
	/** 
	 * @param offset int
	 * @param str string
	 * @param attr AttributeSet
	 * @throws BadLocationException Exception
	 */
	public final void insertString(final int offset, final String str, final AttributeSet attr)
			throws BadLocationException {
		if (str == null) {
			return;
		}

		if ((getLength() + str.length()) <= limit) {
			super.insertString(offset, str, attr);
		}
	}
}
