package com.trustcommerce.ipa.dal.gui.controller;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.constants.gui.CommonGui;
import com.trustcommerce.ipa.dal.gui.constants.Captions;

public class SignaturePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**Log4j logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SignaturePanel.class);
 
	private static final int MARGIN = 25;
	
	public SignaturePanel() {
		setProperties();
	}
	
	private void setProperties() {
		this.setSize(CommonGui.FRAME_WIDTH - 10, 150);
		this.setBackground(Captions.BG_COLOR); 
		repaint();   
	}


	public void createImageIcon(final BufferedImage bImage) {
		LOGGER.debug("-> createImageIcon()");
		final JLabel picLabel = new JLabel(new ImageIcon(bImage), JLabel.CENTER);
		picLabel.setSize(CommonGui.FRAME_WIDTH - 10, 65);
		
		// testing to see location of the label
		// create a line border with the specified color and width
		//final Border border = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2);
		// set the border of this component
		//picLabel.setBorder(border);
		
		// set an empty border
		final Border border = picLabel.getBorder();
		final Border margin = new EmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN);
		picLabel.setBorder(new CompoundBorder(border, margin));
		
 		this.add(picLabel);
 		repaint();
 		LOGGER.debug("<- createImageIcon()");
	}

}
