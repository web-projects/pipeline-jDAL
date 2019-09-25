package com.trustcommerce.ipa.dal.uploader.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.device.firmware.Firmware;
import com.trustcommerce.ipa.dal.exceptions.FirmwareUploadException;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;
import com.trustcommerce.ipa.dal.model.fileUpload.UploadInputData;
import com.trustcommerce.ipa.dal.uploader.FileUploadActivator;

public class DeviceHealthStatus {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadActivator.class);
	private String modelReference = "";
	
	public String getFirmwareVersion(UploadInputData upload) throws FirmwareUploadException {
		
		Firmware firmware = new Firmware(upload);
		firmware.setEventListener(new DeviceHealthListener());
		String result = firmware.getFirmwareVersion();
		modelReference = firmware.getModelReference();
		return result;
	}

	public String getHardwareVersion() {
		if(modelReference.isEmpty()) {
			return "V3";
		}
		String [] value = modelReference.split("-");
		String worker = value[1].substring(0, 1);
		return worker.equals("3") ? "V4" : "V3";
	}
		
	public class DeviceHealthListener implements DeviceEventListener {

		@Override
		public void handleEvent(final LocalActionEvent deviceEvent) {

			LOGGER.debug("DeviceEvent: " + deviceEvent.state);
			switch (deviceEvent.state) {

			case ERROR:
			case REJECTED:
				// Nothing to handle here
				break;
				
			default:
				LOGGER.warn("Got the following state during file upload: " + deviceEvent.state.toString());
				break;
			}
		}
	}
}
