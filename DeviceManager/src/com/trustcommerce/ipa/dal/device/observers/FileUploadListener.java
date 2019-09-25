package com.trustcommerce.ipa.dal.device.observers;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.api.jpos.IngenicoMSR;
import com.ingenico.jpos.IngenicoConst;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.constants.paths.TcipaFiles;
import com.trustcommerce.ipa.dal.device.firmware.Firmware;
import com.trustcommerce.ipa.dal.device.forms.FormsPackage;
import com.trustcommerce.ipa.dal.logger.FileIOUtils;

import jpos.JposException;
import jpos.events.DirectIOEvent;
import jpos.events.DirectIOListener;

/**
 * Listener used by updateForm().
 * 
 * @author TC
 *
 */
public class FileUploadListener implements DirectIOListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadListener.class);
	private IngenicoMSR msr;
	private Object ingenicoDevice;
	private java.util.Timer rebootTimer;
	private boolean activationComplete;
	
	public FileUploadListener(Object ingenicoDevice, IngenicoMSR msr) {

		this.msr = msr;
		this.ingenicoDevice = ingenicoDevice;
	}

	@Override
	public void directIOOccurred(final DirectIOEvent e) {

		if (!msr.equals(e.getSource()) || e.getEventNumber() == 146 || e.getEventNumber() == 161) {
			// Received Direct IO Event but not from msr instance. Ignoring ...
			// Also ignoring event 146: RUN_FILE, event 161: SETVAR
			return;
		}
		LOGGER.debug("-> directIOOccured() event:" + e.getEventNumber() + ",data: " + e.getData());

		// here we only care about the event ING_DIO_SAVE_FILE = 145.
		if (e.getEventNumber() == IngenicoConst.ING_DIO_SAVE_FILE) {
			// Limit responses related to event 145: ING_DIO_SAVE_FILE
			// meaning what happen after transfering a form to device
			DeviceState stateRslt = DeviceState.UNKNOWN;

			switch (e.getData()) {
			case IngenicoConst.ING_DIO_RESPONSE_SUCCESS:
				LOGGER.debug("File package saved to device successfully");
				// We get here to signal termination without reboot
				notifyCaller(DeviceState.FORMS_UPDATED);
				break;

			case IngenicoConst.ING_DIO_RESPONSE_ERROR:
				LOGGER.warn("100000: Error encountered saving file package to device. Info: " + e.getObject());
				stateRslt = DeviceState.ERROR;
				notifyCaller(DeviceState.ERROR);
				
				if (ClientConfigurationUtil.get().isDev()) {
					FileIOUtils.saveDataToTempFile(EntryModeStatusID.FormsPackageError.getCode(),
					        TcipaFiles.FORMS_UPLOAD_FILENAME);
				}
				// javax.swing.JOptionPane.showMessageDialog(null,
				// UserMessages.ERROR_FAILED_FORMS_UPDATE);
				LOGGER.warn("-> terminateJavaVirtualMachine() terminate with errors");
				notifyCaller(DeviceState.ERROR);
				break;

			case IngenicoConst.ING_DIO_RESPONSE_SUCCESS_PACKET_IN_PROGRESS:
				LOGGER.trace("Received 10002: ING_DIO_RESPONSE_SUCCESS_PACKET_IN_PROGRESS");
				processResponseSuccessPacketInProgress(e);
				break;

			case IngenicoConst.ING_DIO_ACTIVATION_REQUEST:
				LOGGER.info("100003: Package saved and an activation request was received. rebooting device ...");
				try {
					msr.setDeviceEnabled(true);
					// Set Reboot Timer to ensure device reboots
					activationComplete = false;
					rebootTimer = new java.util.Timer();
					rebootTimer.schedule(new RemindTask(), 120000);
					LOGGER.debug("sending DirectIO 106");
					msr.directIO(106, new int[] { 1 }, "");
					notifyCaller(DeviceState.INGENICO_REBOOT);
				} catch (JposException e1) {
					LOGGER.error("error rebooting device with message: " + e1.getMessage() + " Code: "
					        + e1.getErrorCode() + " Extended code: " + e1.getErrorCodeExtended());
				}
				break;
				
			case IngenicoConst.ING_DIO_RESPONSE_ACTIVATION_SUCCESS:
				LOGGER.debug("100004: Activation Completed");
				stateRslt = DeviceState.DONE;
				return;

			default:
				LOGGER.debug("Ignoring value");
				return;
			}


		} else if (e.getEventNumber() == 106) {
			// ingenicoDevice.notifyGuiOfStateChange(DeviceState.INGENICO_REBOOT);

		} else if (e.getEventNumber() == IngenicoConst.ING_DIO_GETSTATS) {
			// ING_DIO_GETSTATS = directIO number 13: -1 error, 0 = success
			LOGGER.warn(" got event " + 13 + " data:" + e.getData());
			processResponseSuccessPacketInProgress(e);
		} else {
			// 161
			LOGGER.warn(" got event " + e.getEventNumber());
		}

	}

	private void processResponseSuccessPacketInProgress(final DirectIOEvent event) {
		LOGGER.trace(event.getObject().toString());
		 
		final String temp = event.getObject().toString();
		final String[] tempArray = temp.split("\n");
		if (tempArray.length == 3) {
			LOGGER.trace(tempArray[0]); // 491 21168?
			LOGGER.trace(tempArray[1]); // 4000 8000
			LOGGER.debug(tempArray[2]); // File name

		}
		notifyCaller(DeviceState.PROGRESS_UPDATE);
	}

	@SuppressWarnings("unused")
	private static void deleteFile(final String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			// Should not happen
			return;
		}
		final File file = new File(filePath);
		file.deleteOnExit();
	}
	
	private void notifyCaller(final DeviceState state) {
		LOGGER.debug("-> notifyCaller() {} ", state); 
		if (ingenicoDevice instanceof FormsPackage) {
 			((FormsPackage) ingenicoDevice).notifyCallerOfStateChange(state);
			
		} else if (ingenicoDevice instanceof Firmware) {
			if (state == DeviceState.FORMS_UPDATED) {
				// temp update to send the correct notification
				((Firmware) ingenicoDevice).notifyCallerOfStateChange(DeviceState.FIRMWARE_UPDATED);
			} else {
				((Firmware) ingenicoDevice).notifyCallerOfStateChange(state);
			}
		}
		
		if(state == DeviceState.INGENICO_REBOOT) {
			activationComplete = true;
			// Remove Reboot Timer
			if(rebootTimer != null) {
				rebootTimer.cancel();
			}
		}
	}

	class RemindTask extends java.util.TimerTask 
	{
        public void run() {
            rebootTimer.cancel();
            if(!activationComplete) {
				try {
					if (!msr.getDeviceEnabled()) {
						msr.setDeviceEnabled(true);
					}
					int state = msr.getState();
					if(state !=  IngenicoConst.ING_COMM_ERROR_DETACH) { 
    					msr.clearInput();
    					LOGGER.warn("resending DirectIO 106 - state={}", state);
    					msr.directIO(106, new int[] { 1 }, "");
    					notifyCaller(DeviceState.INGENICO_REBOOT);
					}
				} catch (JposException e1) {
					LOGGER.error("error rebooting device with message: " + e1.getMessage() + " Code: "
					        + e1.getErrorCode() + " Extended code: " + e1.getErrorCodeExtended());
				}
            }
        }
    }
}
