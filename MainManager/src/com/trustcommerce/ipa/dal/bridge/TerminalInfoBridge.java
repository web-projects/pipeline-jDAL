package com.trustcommerce.ipa.dal.bridge;

import org.apache.log4j.Logger;

import com.trustcommerce.ipa.dal.bridge.socket.SocketUtil;
import com.trustcommerce.ipa.dal.configuration.app.AppConfiguration;
import com.trustcommerce.ipa.dal.configuration.types.ConfigurationException;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;
import com.trustcommerce.ipa.dal.constants.device.TerminalModel;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.constants.messages.SocketMessageType;
import com.trustcommerce.ipa.dal.constants.paths.TcipaFiles;
import com.trustcommerce.ipa.dal.device.interfaces.TerminalInfoProcessor;
import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;
import com.trustcommerce.ipa.dal.exceptions.TerminalInfoException;
import com.trustcommerce.ipa.dal.gui.controller.SwingController;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.logger.FileIOUtils;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;
import com.trustcommerce.ipa.dal.model.Terminal;
import com.trustcommerce.ipa.dal.terminal.IngenicoTerminal;
 

public class TerminalInfoBridge {

	private static final transient Logger lOGGER = Logger.getLogger(TerminalInfoBridge.class);


	private Terminal terminalInfo;

	private TerminalInfoProcessor terminalProcessor;

	private TerminalModel terminalModel;
	
	private String serialNumber;
 
	private SwingController gui;
 
	/**
	 * @param terminalName TerminalModel
	 * @param gui SwingController
	 * @throws ConfigurationException Exception
	 */
	public TerminalInfoBridge(final TerminalModel terminalName, final SwingController gui) 
			throws ConfigurationException {
		lOGGER.debug("======================================================");
		lOGGER.debug("              LAUNCHING STATE MACHINE                ");
		lOGGER.debug("=====================================================");
		terminalModel = terminalName;
		this.gui = gui;
		showInitGui();
		initialize();
	}
	
	
	/**
	 * Returns the object of the type the terminal info.
	 * @return Terminal
	 */
	public Terminal getTerminalInfo() {
		return terminalInfo;
	}

	
 
	/**
	 * Initializing the Device Health.
	 * 
	 * @param connectedDevice
	 * @throws TerminalInfoException
	 */
	private void initialize() {
		//lOGGER.debug("-> initializetHealth() connected device: " + terminalModel);
		try {
			terminalProcessor = new IngenicoTerminal(terminalModel);
			terminalProcessor.setEventListener(new InitDeviceListener());
			terminalProcessor.requestTerminalInformation();
			if(terminalModel.name().equalsIgnoreCase("iPP350")) {
				terminalModel = ((IngenicoTerminal) terminalProcessor).getLocalMsrModel();
				if(terminalModel != terminalInfo.getModelName()) {
					terminalInfo.setModelName(terminalModel);
				}
			}
			lOGGER.debug("-> initializetHealth() connected device: " + terminalModel);
			
		} catch (TerminalInfoException e) {
			SocketUtil.sendMessageToCaller(SocketMessageType.StatusCode, EntryModeStatusID.RequestJdalReboot);
		} catch (IngenicoDeviceException e) {
			SocketUtil.sendMessageToCaller(SocketMessageType.StatusCode, EntryModeStatusID.RequestJdalReboot);
			if (gui != null) {
				gui.displayError(e.getMessage());
			}
		} finally {
			releaseMSR();
		}
	}
	
	private void showInitGui() {
		// when no device is connected code is  throwing null pointer exception. so added null check to prevent that.
		if (terminalModel == null) {
			return;
		}
		gui.preparePaymentWidgets();
		gui.showMainFrame();
		final String temp = String.format(AppConfiguration.getLanguage().getString("INITIALIZING"), 
		    terminalModel.name().equalsIgnoreCase("iPP320") || terminalModel.name().equalsIgnoreCase("iPP350") ? "iPP3XX" : terminalModel.name());
		gui.displayMessage(temp);
	}
    
    /**
     * 
     */
	private class InitDeviceListener implements DeviceEventListener {

		@Override
		public void handleEvent(final LocalActionEvent deviceEvent) {
			lOGGER.trace("-> handleEvent() " + deviceEvent.state);
			if (deviceEvent.event == TerminalEvent.TERMINAL_INFO) {
				switch (deviceEvent.state) {
				case READY:
					lOGGER.debug("Device ready: Start transaction processing");
					break;

				case DONE:
					lOGGER.info("Received event: DONE");
					terminalInfo = terminalProcessor.getTerminalInfo();
					lOGGER.debug(terminalInfo.toString());
					if (GlobalConstants.LOCAL_TEST) {
						FileIOUtils.saveDataToTempFile(terminalInfo.toString(), TcipaFiles.TERMINAL_INFO);
					}
					// A reconnection will send this messages
					SocketUtil.sendMessageToCaller(SocketMessageType.SocketStatus, "Ready"); 
					SocketUtil.sendMessageToCaller(SocketMessageType.TerminalInfo, terminalInfo.toString());
					gui.displayWarning("Initialization Completed ...");
					if (gui != null) {
						gui.hideMainFrame();
					}
					break;
					
				case GOT_SERIAL:
					lOGGER.debug("Received event: GOT_SERIAL");
					serialNumber = terminalProcessor.getSerialNumber();

					if (serialNumber == null) {
						// This case would be highly unlikely
						badConnection();
					}
					break;

				case ERROR:
					lOGGER.error("Error encountered initializing device " + terminalModel + "  Event code: "
					        + deviceEvent.state.toString());
					SocketUtil.sendMessageToCaller(SocketMessageType.StatusCode, EntryModeStatusID.Timeout);
					break;

				case HEALTH_STATUS_ERROR:
					lOGGER.fatal("Error encountered requesting Health Status: " + terminalModel);
					break;
					
				case DEVICE_NOT_CONNECTED:
					badConnection();
					break;
					
				default:
					// This is to ignore SIGCAP_NOT_ENABLED
					break;
				}
			}
		}
	}



/**
 * 
 */
	private void releaseMSR() {
		if (terminalProcessor != null) {
			terminalProcessor.setEventListener(null);
			terminalProcessor.releaseDevice();
			terminalProcessor = null;
		}
		
	}

	
	/**
	 * Bad connection always requires termination.
	 */
	private void badConnection() {
		SocketUtil.sendMessageToCaller(SocketMessageType.SocketStatus, EntryModeStatusID.BadConnection);
		SocketUtil.sendMessageToCaller(SocketMessageType.SocketStatus, EntryModeStatusID.RequestJdalShutDown);
		try {
			// to avoid doing anything else until jDAL terminates
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
	}

}
