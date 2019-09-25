package com.trustcommerce.ipa.dal.bridge;

import com.trustcommerce.ipa.dal.configuration.types.ConfigurationException;
import com.trustcommerce.ipa.dal.constants.device.TerminalModel;
import com.trustcommerce.ipa.dal.gui.controller.SwingController;
import com.trustcommerce.ipa.dal.model.Terminal;

public class TerminalInfoHelper {

	
	private static Terminal terminalInfo;
	
	
	private static SwingController gui;
	
	
	private static TerminalModel model;
	
	
	private static TerminalInfoBridge bridge;
	
	
	/**
	 * Returns the object of the type the terminal info.
	 * @return Terminal
	 */
	public static Terminal getTerminalInfo() {
		
		if (bridge == null || terminalInfo == null) {
			initialize(model, gui);
		}
		terminalInfo = bridge.getTerminalInfo();
		
		return terminalInfo;
	}


	public static void setGui(final SwingController swingController) {
		gui = swingController;
	}
	
	
	public static void setDeviceModel(final TerminalModel terminalName) {
		model = terminalName;
	}
	
	
	public static void initialize(final TerminalModel terminalName, final SwingController gui) {
		try {
			bridge = new TerminalInfoBridge(terminalName, gui);
			 
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Returns the object of the type the terminal info.
	 * @return Terminal
	 */
	public static void resetTerminalInfo() {
		terminalInfo = null;
	}
	
}
