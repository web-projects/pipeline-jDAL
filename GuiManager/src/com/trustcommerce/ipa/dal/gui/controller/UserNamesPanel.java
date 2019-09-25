package com.trustcommerce.ipa.dal.gui.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.configuration.app.AppConfiguration;
import com.trustcommerce.ipa.dal.gui.constants.Captions;
import com.trustcommerce.ipa.dal.gui.constants.GuiConstants;

/** Panel used for entering the coustmer firstname and lastname while diung manual transaction.*/
public class UserNamesPanel extends JPanel implements ActionListener, KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Log4j logger.
 	*/
	private static final Logger LOGGER = LoggerFactory.getLogger(UserNamesPanel.class);
	/** label and textboxes width. */
	private static final int WIDTH = 100;
	/** label and textboxes width. */
	private static final int HEIGHT = 30;
	
	private static final int FONT_SIZE = 14;

	/** Buttons. */
	private JButton okBtn = new JButton(AppConfiguration.getLanguage().getString("OK_BUTTON"));
	/** Cancel button for not entering username.*/
	private JButton cancelBtn = new JButton(AppConfiguration.getLanguage().getString("CANCEL_BUTTON"));
	/** Firstname label.*/
	private JLabel firstNameLabel;
	/** Lastname Label.*/
	private JLabel lastNameLabel;
	/** textfeild for entering firstname.*/
	private JTextField firstNameTxt;
	/**Textfield for entering Lastname.*/
	private JTextField lastNameTxt;

	private JPanel buttonPanel;
	private JPanel fnPanel;
	private JPanel lnPanel;
	/** For displaying anyerror occured while entering username.*/
	
	private JLabel errorLabel;
	
	private SwingController mainGui;

	
	/**
	 * 
	 * @param controller
	 */
	public UserNamesPanel(SwingController controller) {
		mainGui = controller;
		this.setBackground(Captions.BG_COLOR); 
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// create new Font
		Font lblfont = new Font("Sans", Font.PLAIN, FONT_SIZE);
		Font txtfont = new Font("Sans", Font.PLAIN, FONT_SIZE);

		errorLabel = new JLabel("");
		errorLabel.setHorizontalAlignment(SwingConstants.LEFT);
		errorLabel.setForeground(Color.red);
		errorLabel.setFont(new Font("Sans", Font.BOLD, 12));

		// First Name panel
		fnPanel = new JPanel();
		fnPanel.setSize(WIDTH, 50);
		fnPanel.setBackground(Captions.BG_COLOR); 

		firstNameLabel = new JLabel(AppConfiguration.getLanguage().getString("FIRST_NAME_TXT"));
		firstNameLabel.setSize(WIDTH, HEIGHT);
		firstNameLabel.setFont(lblfont);
		fnPanel.add(firstNameLabel);

		// 30 = 30 Columns
		firstNameTxt = new JTextField(GuiConstants.MAX_CHARS);
		firstNameTxt.setSize(WIDTH, HEIGHT);
		firstNameTxt.setFont(txtfont);
		firstNameTxt.setDocument(new JTextFieldLimit(GuiConstants.MAX_CHARS));
		
		// Listen for changes in the text
		firstNameTxt.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(final DocumentEvent e) {
				enableButtons();
			}
			@Override
			public void removeUpdate(final DocumentEvent e) {
			}
			@Override
			public void changedUpdate(final DocumentEvent e) {
			}
			public void enableButtons() {
				okBtn.setEnabled(true);
				okBtn.setFocusable(true);
			}
		});

		firstNameTxt.requestFocus();
		fnPanel.add(firstNameTxt);
		
		// Last Name panel
		lnPanel = new JPanel();
		lnPanel.setSize(WIDTH, 50);
		lnPanel.setBackground(Captions.BG_COLOR);
		
		
		lastNameLabel = new JLabel(AppConfiguration.getLanguage().getString("LAST_NAME_TXT"));
		lastNameLabel.setSize(WIDTH, HEIGHT);
		lastNameLabel.setFont(lblfont);
		lnPanel.add(lastNameLabel);

		lastNameTxt = new JTextField(GuiConstants.MAX_CHARS);
		lastNameTxt.setSize(WIDTH, HEIGHT);
		lastNameTxt.setFont(txtfont);
		lastNameTxt.setDocument(new JTextFieldLimit(GuiConstants.MAX_CHARS));
		lastNameTxt.setFocusTraversalKeysEnabled(false);
		lastNameTxt.addKeyListener(this);
		
		lnPanel.add(lastNameTxt);

		// In the buttonPanel
		buttonPanel = new JPanel();
		buttonPanel.setSize(400, 50);
		buttonPanel.setBackground(Captions.BG_COLOR);

		okBtn = new JButton(AppConfiguration.getLanguage().getString("OK_BUTTON"));
		okBtn.setSize(70, 40);
		okBtn.addActionListener(new ButtonClickListener());
		okBtn.setFocusable(true);
		okBtn.setEnabled(false);
		buttonPanel.add(okBtn);

		cancelBtn = new JButton(AppConfiguration.getLanguage().getString("CANCEL_BUTTON"));
		cancelBtn.setSize(70, 40);
		cancelBtn.addActionListener(new ButtonClickListener());
		cancelBtn.setEnabled(true);
		buttonPanel.add(cancelBtn);


		add(fnPanel);
		add(lnPanel);
		add(buttonPanel);
		add(errorLabel);
	}
	/**
	 * Determines which button is clicked in the panel and do the corresponding actions.
	 * @author ayswarya.prashant
	 *
	 */
	private class ButtonClickListener implements ActionListener {
		/**
		 * @param e ActionEvent
		 */
		public void actionPerformed(final ActionEvent e) {
			final String command = e.getActionCommand();
			if (command.equals(AppConfiguration.getLanguage().getString("OK_BUTTON"))) {
				LOGGER.debug("ok");
				final String name = firstNameTxt.getText();
				final String lastname = lastNameTxt.getText();
				if (validateCardHolderName(name, lastname)) {
					userInfomationCompleted();
				}

			} else if (command.equals(AppConfiguration.getLanguage().getString("CANCEL_BUTTON"))) {
				LOGGER.debug("cancel");
				SwingController.cardHolderName = "";
				userInfomationCompleted();
				
			} else {
				// statusLabel.setText("Cancel Button clicked.");
			}
		}
	}
	
	
	@Override
	public void actionPerformed(final ActionEvent e) {
		// TODO Auto-generated method stub

	}

	private boolean validateCardHolderName(final String name, final String lastName) {

		final Pattern pattern = Pattern.compile(GuiConstants.REGEX);

		final Matcher matcher1 = pattern.matcher(name);
		final Matcher matcher2 = pattern.matcher(lastName);

		if (!matcher1.matches()) {
			LOGGER.info(AppConfiguration.getLanguage().getString("CARD_HOLDER_NAME_INVALID_CHARACTERS"));
			firstNameTxt.setText("");
			updateLabel(AppConfiguration.getLanguage().getString("CARD_HOLDER_NAME_INVALID_CHARACTERS"));
			return false;
		}

		if (!matcher2.matches()) {
			LOGGER.info(AppConfiguration.getLanguage().getString("CARD_HOLDER_NAME_INVALID_CHARACTERS"));
			lastNameTxt.setText("");
			updateLabel(AppConfiguration.getLanguage().getString("CARD_HOLDER_NAME_INVALID_CHARACTERS"));
			return false;
		}
		SwingController.cardHolderName = name.trim() + " " + lastName.trim();
		return true;
	}

	/**
	 * Helper function to update the label.
	 * 
	 * @param msg
	 *            Text to display on the label
	 */
	private void updateLabel(final String msg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				errorLabel.setText(msg);
			}
		});
	}

	// ================  KeyListener methods ==============
	
	@Override
	public final void keyTyped(final KeyEvent e) {
		LOGGER.debug(e + " keyTyped");
	}

	@Override
	public final void keyPressed(final KeyEvent e) {
		LOGGER.debug(e + " keyPressed");
	}

	@Override
	public final void keyReleased(final KeyEvent e) {
		LOGGER.debug(e + " keyReleased");
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			LOGGER.debug("VK_ENTER");
			final String name = firstNameTxt.getText();
			final String lastname = lastNameTxt.getText();
			if (validateCardHolderName(name, lastname)) {
				userInfomationCompleted();
			}
		}
	}

	/**
	 * Closes the usernames panel after completing the userinformation.
	 */
	public final void userInfomationCompleted() {
		// TODO Auto-generated method stub
		mainGui.userInfomationCompleted();
	}

}
