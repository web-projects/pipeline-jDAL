package com.trustcommerce.ipa.dal.gui.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.configuration.app.AppConfiguration;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.constants.gui.CommonGui;
import com.trustcommerce.ipa.dal.gui.constants.Captions;
import com.trustcommerce.ipa.dal.gui.interfaces.BridgeEventListener;
import com.trustcommerce.ipa.dal.gui.interfaces.SwingControlEventListener;
import com.trustcommerce.ipa.dal.gui.model.GuiInfo;


/**
 * 
 * @author luisa.lamore
 *
 */
public class SwingController extends JFrame implements ActionListener, SwingControlEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2993378633381583477L;

	private static final Logger LOGGER = LoggerFactory.getLogger(SwingController.class);
	
	private static final int WIDTH = 350;
	/** label and textboxes width. */
	private static final int HEADER_HEIGHT = 140;
	
	private static final int FOOTER_HEIGHT = 16;
	
	private static final int ERROR_FONT = 18;
	
	private static final String FONT_NAME = "Sans";

	private static final String TRUSTCOMMERCE = "TrustCommerce";

	// NOTE: This will have to be modified if the actual signature
	// box is changed in the device's form.

	public static String cardHolderName;
	/**MainFrame. */
	private JFrame mainFrame;
	/** Manual Button. */
	private JButton manualBtn;
	/** Swipe Button.*/
	private JButton swipeBtn;
	/** Reconnect Button.*/
	private JButton reconnectBtn;

	/**Buttons for the EMV. */
	private JButton voidPartialBtn;
	/** Ok. */
	private JButton okPartialBtn;


	/**buttons for reuse.*/
	private JButton skipBtn = new JButton(AppConfiguration.getLanguage().getString("SKIP_BUTTON"));
	/** */
	private JButton updateBtn = new JButton(AppConfiguration.getLanguage().getString("UPDATE_BUTTON"));

	/** Label use to display errors in RED. */
	private JLabel errorLabel;
	/** GUI LOGO. */
	private JLabel headerLabel;
	private JLabel statusLabel;

	/** panel with buttons. */
	private JPanel dataPanel;
	
	/** panel for Status Label. */
	private JPanel statusPanel;

	
	/** panel in the center, contains the statusLabel and dataPanel. */
	private JPanel centerPanel;
	
	/** panel in the bottom with user information/disclaimer. */
	private JLabel footerLabel;
	
	/** Panel for the manual payer name and last name. */
	private UserNamesPanel userNamesPanel;
	
	/** Panel where the payer signature is displayed. */
	private SignaturePanel signaturePanel;

	
	private GuiInfo guiInfo;

	// /** Signature. */

	private BridgeEventListener guiEventListener;
	// private ReconnectEventListener reconnectListener;
	
	/**
	 * To prevent the jdal from closing once the signature is required.
	 */
	private boolean ignoreCloseWindow;

	/**Constructor for swing controller.
	 * 
	 * @param guiInformation GuiInfo
	 */
	public SwingController(final GuiInfo guiInformation) {
		guiInfo = guiInformation;
		instantiateLabels();
		prepareJframe();
	}
	
	
	public void supressGui() {
		guiInfo.setShowFrame(false);
	}
	
 
	public void preparePaymentWidgets() {
		instantiatePaymentButtons();
		addPaymentWidgetsToGUI();
	}
	
	
	private void instantiateLabels() {
		
		// Label to display errors in red
		errorLabel = new JLabel();
		errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
		errorLabel.setForeground(Color.red);
		errorLabel.setFont(new Font(FONT_NAME, Font.BOLD, ERROR_FONT));
		errorLabel.setVisible(false);

		// Logo
		headerLabel = new JLabel("", JLabel.CENTER);
		headerLabel.setSize(WIDTH, HEADER_HEIGHT);
		headerLabel.setPreferredSize(new Dimension(WIDTH, HEADER_HEIGHT));
		addGuiHeader();
		
		// Process Status
		statusLabel = new JLabel("", JLabel.CENTER);
		statusLabel.setSize(WIDTH, 100);
		statusLabel.setPreferredSize(new Dimension(WIDTH, HEADER_HEIGHT));
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusLabel.setFont(new Font(FONT_NAME, Font.BOLD, ERROR_FONT));
		
		footerLabel = new JLabel();
		footerLabel.setHorizontalAlignment(SwingConstants.LEFT);
		footerLabel.setForeground(Color.black);
		footerLabel.setText(guiInfo.getFooter());
		
	}
	
	private void addGuiHeader() {
		// Add logo
		final String logoLocation = CommonGui.getTcIPALogo();
		final File icon = new File(logoLocation);
		ImageIcon iconLogo = null;
		if (icon.exists()) {
			iconLogo = new ImageIcon(logoLocation);
			headerLabel.setIcon(iconLogo);
		} else {
			headerLabel.setText("TrustCommerce");
		}
		final Font font = new Font("Rockwell Bold", Font.BOLD, 30);
		// set font for JLabel
		headerLabel.setFont(font);
		headerLabel.setForeground(Color.GRAY);
	}
	
	
	private void instantiatePaymentButtons() {
		
		
		manualBtn = new JButton(AppConfiguration.getLanguage().getString("MANUAL_BUTTON"));
		manualBtn.setVisible(false);
		manualBtn.setActionCommand("MANUAL");
		manualBtn.addActionListener(new ButtonClickListener());

		// the default mode is swipe
		swipeBtn = new JButton(AppConfiguration.getLanguage().getString("SWIPE_BUTTON"));
		swipeBtn.setVisible(false);
		swipeBtn.setActionCommand("SWIPE");
		swipeBtn.addActionListener(new ButtonClickListener());

		voidPartialBtn = new JButton(AppConfiguration.getLanguage().getString("VOID_BUTTON"));
		okPartialBtn = new JButton(AppConfiguration.getLanguage().getString("CONTINUE_BUTTON"));
		
		// JButton cancelButton = new JButton("Cancel");
		// cancelButton.setActionCommand("Cancel");
		

	}
	
	private void addPaymentWidgetsToGUI() {
		// add buttons panel (default) to the buttonsPanel.
		swipeBtn.setVisible(false);
		manualBtn.setVisible(false);
		final GridBagLayout gridBagLayout = populateControlPanel();
		dataPanel.setLayout(gridBagLayout);
		dataPanel.add(manualBtn);
		dataPanel.add(swipeBtn);
	}

	
	/**
	 * 
	 * @param newListener
	 *            BridgeEventListener
	 */
	public final void setEventListener(final BridgeEventListener newListener) {
		guiEventListener = newListener;
	}

	/*public void setEventListener(ReconnectEventListener newListener) {
		reconnectListener = newListener;
	}
*/
 	
	/**
	 * Call during construction of this object.
	 */
	private void prepareJframe() {

		// Ask for window decorations provided by the look and feel.
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// create frame with title
		mainFrame = new JFrame(guiInfo.getTitle() + guiInfo.getVersion());
		mainFrame.setSize(CommonGui.FRAME_WIDTH, CommonGui.FRAME_HEIGHT);

		// Hides the Max button on the top right corner
		mainFrame.setResizable(false);
		// removeMinMaxClose(mainFrame);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		mainFrame.getContentPane().setBackground(Captions.BG_COLOR);
		
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent windowEvent) {
				if (ignoreCloseWindow) {
					return;
				}
				LOGGER.info("jDAL has been shutdown from the task bar or GUI");
				if (guiEventListener != null) {
					guiEventListener.windowClosed();
				}
				// System.exit(0);
			}
		});

		final String iconLocation = CommonGui.getJdalIcon();
		final File icon = new File(iconLocation);
		ImageIcon image = null;
		if (icon.exists()) {
			image = new ImageIcon(iconLocation);
			mainFrame.setIconImage(image.getImage());
		}

		// rows, columns
		mainFrame.setLayout(new BorderLayout());
		final Container pane = mainFrame.getContentPane();

		// add the icon panel 
		pane.add(headerLabel, BorderLayout.PAGE_START);

		// Center panel
		//=================================================
		dataPanel = new JPanel();
		dataPanel.setVisible(true);
		dataPanel.setBackground(Captions.BG_COLOR);
		
		statusPanel = new JPanel();
		statusPanel.setVisible(true);
		statusPanel.setBackground(Captions.BG_COLOR);
		statusPanel.add(statusLabel);
		
		centerPanel = new JPanel();
		// 2 rows: for the status messages and for the buttons, 1 column
		centerPanel.setLayout(new GridLayout(2, 1));
		centerPanel.setBackground(Captions.BG_COLOR);
		centerPanel.add(statusPanel);
		centerPanel.add(dataPanel);
		
		// addMenuBar(); ????
		pane.add(centerPanel, BorderLayout.CENTER);
		// controlPanel.setLayout(new FlowLayout());
		//footerLabel = new JLabel(Captions.GUI_PROCESS_PAYMENT_FOOTER);

		pane.add(footerLabel, BorderLayout.PAGE_END);
		
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(false);
		footerLabel.setSize(new Dimension(mainFrame.getWidth(), FOOTER_HEIGHT));
	}
	
	/**
	 * If the device is iUP250 , mainframe should not be visible.
	 */
	public final void showMainFrame() {
		if (!guiInfo.isShowFrame()) {
			mainFrame.setVisible(false);
		} else {
			mainFrame.setVisible(true);
			mainFrame.toFront();
			mainFrame.setAlwaysOnTop(true);
		}
	}
	
	public final void hideMainFrame() {
		mainFrame.setVisible(false);
	}

	/**
	 * Adds the user label and swipe and manual buttons to the panel.
	 * 
	 * @return GridBagLayout
	 */
	private GridBagLayout populateControlPanel() {
		LOGGER.debug("-> populateControlPanel()");
		GridBagLayout gridbag = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		// Number of columns
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridx = 0;
		c.gridy = 0;
		setLayout(gridbag);

		//gridbag.setConstraints(statusLabel, c);
		//add(statusLabel);


		// Manual mode button
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.PAGE_END - 10;
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;

		gridbag.setConstraints(errorLabel, c);
		add(errorLabel);

		gridbag.setConstraints(manualBtn, c);
		manualBtn.setVisible(false);
		add(manualBtn);

		gridbag.setConstraints(voidPartialBtn, c);
		voidPartialBtn.setVisible(false);
		add(voidPartialBtn);

		gridbag.setConstraints(skipBtn, c);
		skipBtn.setVisible(false);
		add(skipBtn);
		skipBtn.addActionListener(this);

		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		gridbag.setConstraints(swipeBtn, c);
		swipeBtn.setVisible(false);
		add(swipeBtn);

		gridbag.setConstraints(okPartialBtn, c);
		okPartialBtn.setVisible(false);
		add(okPartialBtn);

		manualBtn.addActionListener(this);
		swipeBtn.addActionListener(this);

		gridbag.setConstraints(updateBtn, c);
		updateBtn.setVisible(false);
		add(updateBtn);
		updateBtn.addActionListener(this);


		LOGGER.trace("<- populateControlPanel");
		return gridbag;
	}

	/**
	 * 
	 * 
	 */
	public final void showIntroMessage() {

		statusLabel.setText(AppConfiguration.getLanguage().getString("INTRO_MESSAGE"));
		// controlPanel.add(cancelButton);
		showMainFrame();
	}
 
	
	/**
	 * 
	 * @param message String
	 */

	public final void showSimpleEventGui(final String message) {
		statusLabel.setText(message);
		mainFrame.setVisible(true);
	}
	
	


	/**
	 * Creating the Reconnect button in the GUi.
	 *  Not being Called.
	 * @param message resourceBundle
	 */

	public final void addReconnect(final String message) {
		LOGGER.debug("-> addReconnect() ");
		final String logoLocation = CommonGui.getTcIPALogo();
		final File icon = new File(logoLocation);
		ImageIcon iconLogo = null;
		if (icon.exists()) {
			iconLogo = new ImageIcon(logoLocation);
			headerLabel.setIcon(iconLogo);
		} else {
			headerLabel.setText("TrustCommerce");
		}
		statusLabel.setText(message);
		final Font font = new Font("Rockwell Bold", Font.BOLD, 30);
		// set font for JLabel
		headerLabel.setFont(font);
		headerLabel.setForeground(Color.GRAY);
		if (reconnectBtn == null) {
			reconnectBtn = new JButton("RECONNECT");
		}
		reconnectBtn.addActionListener(this);

		reconnectBtn.setVisible(false);
		dataPanel.add(reconnectBtn);
		centerPanel.add(dataPanel);

		mainFrame.setVisible(true);

	}
	
	/**
	 * Enabling the Reconnect Button and adding Listener.
	 * 
	 * @param message resourceBundle
	 */
	public final void enableReconnectButton(final String message) {

		LOGGER.debug("-> enableReconnectButton() ");
		reconnectBtn.setVisible(true);
		reconnectBtn.setActionCommand("RECONNECT");
		reconnectBtn.setEnabled(false);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {

					Thread.sleep(GlobalConstants.RECONNECT_TIMER);
					reconnectBtn.setEnabled(true);
					reconnectBtn.addActionListener(new ButtonClickListener());
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});

	}

	@Override
	public void actionPerformed(final ActionEvent arg0) {
		// TODO Auto-generated method stub

	}
	/**
	 * 
	 * @param comp Component
	 */
	@SuppressWarnings("unused")
	private static void removeMinMaxClose(final Component comp) {
		if (comp instanceof AbstractButton) {
			comp.getParent().remove(comp);
		}
		if (comp instanceof Container) {
			final Component[] comps = ((Container) comp).getComponents();
			for (int x = 0, y = comps.length; x < y; x++) {
				removeMinMaxClose(comps[x]);
			}
		}
	}

	/**
	 * Helper function to update the label.
	 * 
	 * @param msg
	 *            Text to display on the label
	 */
	public final void updateLabel(final String msg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				statusLabel.setText(msg);
			}
		});
	}

	/**
	 * 
	 */
	public final void displayManualMode() {
		manualBtn.setVisible(false);
		swipeBtn.setVisible(true);
		updateLabel(AppConfiguration.getLanguage().getString("ASK_CARD_NUMBER"));
	}
	

	/**
	 * After errors, we need to display the problem and show the swipe button.
	 * 
	 * @param message
	 *            String message to display in jdal
	 */
	public final void displaySwipeInsertMode(final String message) {
		LOGGER.debug("-> displaySwipeInsertMode()");
		// When showing the signature in the window , the button panel is getting removed. 
		//so need to add that again when displaying the swipe form
		manualBtn.setVisible(true);
		swipeBtn.setVisible(false);
		updateLabel(message);
	}
	
	
    /**
     * Payment mode buttons should be hide after Expiration data is presented.
     */
	public final void hideButtons() {
		if (swipeBtn != null) {
			swipeBtn.setVisible(false);
			manualBtn.setVisible(false);
		}
	}

	

	/**
	 * 
	 * @param message String
	 */
	public final void displayMessage(final String message) {
		hideButtons();
		updateLabel(message);
		try {
			Thread.sleep(GlobalConstants.MESSAGE_INFO_SLEEP_TIMER);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Displays a warning in the jDAL gui for a configurable amount of time.
	 * 
	 * @param message String
	 */
	public final void displayWarning(final String message) {
		hideButtons();
		updateLabel(message);
		try {
			Thread.sleep(GlobalConstants.MESSAGE_WARN_SLEEP_TIMER);
		} catch (InterruptedException e) {
		}
	}
	
	/**
	 * Displays a warning in the jDAL gui for a configurable amount of time.
	 * 
	 * @param message String
	 */
	public final void displayError(final String message) {
		hideButtons();
		updateLabel(message);
 	}

	/**
	 * Called when a transaction is completed.
	 */
	public final void resetWidgets() {
		cardHolderName = null;
		hideButtons();
		removeUserNamesPanel();
		centerPanel.add(dataPanel);
		// the following line is commented out because there are not popups to dispose anymore ...
		// JOptionPane.getRootFrame().dispose();
		mainFrame.setVisible(false);
	}
	
	
	/**
	 * initialize gui.
	 * panels are hidden.
	 */
	public final void initialize() {
		cardHolderName = null;
		removeUserNamesPanel();

	}

	/**
	 * Why is this here? this is not used. Is this for TCIPA?
	 */
	@SuppressWarnings("unused")
	private void addMenuBar() {
		final JFrame myFrame = new JFrame("Process Payment");
		// create frame with title
		final JMenuBar menuBar = new JMenuBar();

		// File menu
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		JMenuItem menuItem = new JMenuItem("Exit");
		menu.add(menuItem);
		// Help menu
		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);
		menuItem = new JMenuItem("About TrustCommerce Ingenico Application");
		menu.add(menuItem);
		myFrame.setJMenuBar(menuBar);
	}
    


	/**
	 * Callback from the User Panel, Replaces the User information panel by the default panel.
	 */
	@Override
	public final void userInfomationCompleted() {
		removeUserNamesPanel();
		centerPanel.add(dataPanel);
		guiEventListener.cardHolderCompleted();
	}

	/**
	 * Replaces the User information panel by the default panel on timeout only.
	 */
	@Override
	public final void userInfomationTimeout() {
		removeUserNamesPanel();
		centerPanel.add(dataPanel);
	}

	// ===========================================================
	// EMV
	/**
	 * 
	 */
	public final void activateVoidPartialButton() {
		voidPartialBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				LOGGER.info("Partial amount rejected");
				okPartialBtn.setVisible(false);
				voidPartialBtn.setVisible(false);
				guiEventListener.voidPartialClicked();
			}
		});
	}
	
	
	/**
	 * 
	 */
	public final void activateOKPartialButton() {
		okPartialBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				LOGGER.info("Partial amount accepted");
				okPartialBtn.setVisible(false);
				voidPartialBtn.setVisible(false);
				guiEventListener.okPartialClicked();
			}
		});
	}
	
	
	/**
	 * Partial approval screen.
	 * @param message String
	 */

	public final void displayPartialApprovalScreen(final String message) {
		updateLabel(message);
		okPartialBtn.setVisible(true);
		voidPartialBtn.setVisible(true);
	}
	
	
	/**
	 * 
	 * @param bufferedImage BufferedImage
	 */
	public final void showSignaturePanel(final BufferedImage bufferedImage) {
		// Without removing the buttonsPanel Panel, the use panel gets disalign
		LOGGER.debug("-> showSignaturePanel()");
		centerPanel.remove(dataPanel);
		signaturePanel = new SignaturePanel();
		signaturePanel.setVisible(true);
		signaturePanel.setEnabled(true);
		signaturePanel.createImageIcon(bufferedImage);
		centerPanel.add(signaturePanel);
		updateLabel(AppConfiguration.getLanguage().getString("SIGNATURE"));
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
		}
	}
	
	
	public final void removeSignaturePanel() {
		if (signaturePanel != null) {
			signaturePanel.setVisible(false);
			centerPanel.remove(signaturePanel);
			signaturePanel = null;
		}
	}
	

    /**
     * 
     */
	public final void showUserNamesPanel() {
		LOGGER.debug("-> showUserNamesPanel()");
		// Without removing the controlPanel Panel, the use panel gets disalign
		centerPanel.remove(dataPanel);
		userNamesPanel = new UserNamesPanel(this);
		userNamesPanel.setVisible(true);
		userNamesPanel.setEnabled(true);
		centerPanel.add(userNamesPanel);
		updateLabel(AppConfiguration.getLanguage().getString("COLLECT_CARD_HOLDER_NAME"));
		LOGGER.debug("<- showInputDialog()");
	}
	
	
	private final void removeUserNamesPanel() {
		if (userNamesPanel != null) {
			userNamesPanel.setVisible(false);
			centerPanel.remove(userNamesPanel);
			userNamesPanel = null;
		}
	}
	
	/**
	 * when the signature required is true, Ignore closing of jDAL.
	 */
	public void setIgnoreCloseWindow() {
		 ignoreCloseWindow = true;
	}
	
	/**
	 * when new transaction starts.
	 */
	public void resetIgnoreCloseWindow() {
		 ignoreCloseWindow = false;
	}
	
	
	/**Checks the command of the button clicked and calls corresponding listener.
	 * 
	 * @author ayswarya.prashant
	 *
	 */

	private class ButtonClickListener implements ActionListener {
		/** 
		 *@param e 
		 */
		public void actionPerformed(final ActionEvent e) {
			final String command = e.getActionCommand();
			if (command.equals("SWIPE")) {
				displaySwipeInsertMode(AppConfiguration.getLanguage().getString("ASK_SWIPE_CARD"));
				guiEventListener.goToSwipe();
			} else if (command.equals("MANUAL")) {
				displayManualMode();
				guiEventListener.goToManual();
			/*} else if (command.equals("RECONNECT")) {
				reconnectListener.reconnect();*/
			} else {
				statusLabel.setText(AppConfiguration.getLanguage().getString("CANCEL_BUTTON_CLICK"));
			}
		}
	}


}
