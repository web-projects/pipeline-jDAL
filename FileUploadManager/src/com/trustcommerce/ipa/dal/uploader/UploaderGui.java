package com.trustcommerce.ipa.dal.uploader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.configuration.app.AppConfiguration;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.uploader.constants.Captions;
import com.trustcommerce.ipa.dal.uploader.constants.Version;

/**
 * This class should have never been here.
 * Temporary location to separate this code from the FileUploadBridge.
 * @author luisa.lamore
 *
 */
public class UploaderGui {

	/** log4j logger. */

	private static final transient Logger LOGGER = LoggerFactory.getLogger(UploaderGui.class);
	/** label and textboxes width. */
	private static final int HEADER_HEIGHT = 140;
	private static final int FOOTER_HEIGHT = 16;
	private static final String FONT_NAME = "Sans";
	private static final int ERROR_FONT = 18;

	
	private JLabel headerLabel;
	private JLabel statusLabel;
	private JFrame mainFrame;
	private JLabel footerLabel;
	private JPanel imagePanel;

	/** panel in the center, contains the control panel. */
	private JPanel centerPanel;

	private static final int WIDTH = 350;

	
	public UploaderGui() {
		showFirmwareUploadGui();
	}
	
	//String message, boolean error
	public final void showFirmwareUploadGui() {

		mainFrame = new JFrame(Captions.GUI_PROCESS_PAYMENT_TITLE + Version.UPLOADER_VERSION);
		mainFrame.setSize(Captions.FRAME_WIDTH, Captions.FRAME_HEIGHT);

		// Hides the Max button on the top right corner
		mainFrame.setResizable(false);
		// removeMinMaxClose(mainFrame);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		mainFrame.getContentPane().setBackground(Captions.BG_COLOR);

		final String iconLocation = Captions.getJdalIcon();
		final File icon = new File(iconLocation);
		ImageIcon image = null;
		if (icon.exists()) {
			image = new ImageIcon(iconLocation);
			mainFrame.setIconImage(image.getImage());
		}
		Container pane = mainFrame.getContentPane();

		headerLabel = new JLabel("", JLabel.CENTER);
		headerLabel.setSize(WIDTH, HEADER_HEIGHT);
		headerLabel.setPreferredSize(new Dimension(WIDTH, HEADER_HEIGHT));
		pane.add(headerLabel, BorderLayout.PAGE_START);

		statusLabel = new JLabel("", JLabel.CENTER);
		statusLabel.setSize(WIDTH, 100);
		statusLabel.setPreferredSize(new Dimension(WIDTH, HEADER_HEIGHT));

		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusLabel.setFont(new Font(FONT_NAME, Font.BOLD, ERROR_FONT));

		pane.add(statusLabel, BorderLayout.CENTER);

		centerPanel = new JPanel();
		// 2 rows: for the status messages and for the buttons
		centerPanel.setLayout(new GridLayout(2, 1));
		centerPanel.setBackground(Captions.BG_COLOR);

		// Add logo
		final String logoLocation = Captions.getTcIPALogo();
		ImageIcon iconLogo = null;
		if (icon.exists()) {
			iconLogo = new ImageIcon(logoLocation);
			headerLabel.setIcon(iconLogo);
		} else {
			headerLabel.setText("TrustCommerce");
		}
		//statusLabel.setText(message);
		final Font font = new Font("Rockwell Bold", Font.BOLD, 30);
		// set font for JLabel
		headerLabel.setFont(font);
		headerLabel.setForeground(Color.GRAY);

		imagePanel = new JPanel();
		JLabel progressLabel = new JLabel();

		final String progressBarLocation = Captions.getProgressIcon();
		ImageIcon progressLogo = null;
		if (icon.exists()) {
			progressLogo = new ImageIcon(progressBarLocation);
			progressLabel.setIcon(progressLogo);
		}

		final GridBagLayout gridBagLayout = populateControlPanel();
		imagePanel.setLayout(gridBagLayout);
		imagePanel.setBackground(Captions.BG_COLOR);
		imagePanel.add(progressLabel);
		centerPanel.add(statusLabel);
		centerPanel.add(imagePanel);

		pane.add(centerPanel, BorderLayout.CENTER);

		footerLabel = new JLabel(AppConfiguration.getLanguage().getString("GUI_PROCESS_FW_UPLOAD"));
		footerLabel.setHorizontalAlignment(SwingConstants.LEFT);
		footerLabel.setForeground(Color.black);
		footerLabel.setSize(new Dimension(mainFrame.getWidth(), FOOTER_HEIGHT));

		pane.add(footerLabel, BorderLayout.PAGE_END);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(false);
	}

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
		// setLayout(gridbag);

		gridbag.setConstraints(statusLabel, c);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusLabel.setFont(new Font(FONT_NAME, Font.BOLD, ERROR_FONT));
		// add(statusLabel);

		// Manual mode button
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.PAGE_END - 10;
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;

		LOGGER.trace("<- populateControlPanel");
		return gridbag;
	}
	
	
	/**
	 * Helper function to update the label.
	 * 
	 * @param msg
	 *            Text to display on the label
	 */
	public final void updateLabel(final String msg) {
		if (mainFrame == null) {
			return;
		}
		mainFrame.setVisible(true);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				statusLabel.setText(msg);
			}
		});
	}

	/**
	 * Displays a warning in the jDAL gui for a configurable amount of time.
	 * 
	 * @param message String
	 */
	public final void displayWarning(final String message) {
		 
		imagePanel.setVisible(false);
		updateLabel(message);
		
		try {
			Thread.sleep(GlobalConstants.MESSAGE_ERROR_SLEEP_TIMER);
		} catch (InterruptedException e) {
		}
	}	


	public final void showFirmwareUploadCompleteGui(final String message) {
		if (mainFrame == null) {
			return;
		}
		mainFrame.setVisible(true);
 		statusLabel.setText(message);
		imagePanel.setVisible(false);
	}
}
