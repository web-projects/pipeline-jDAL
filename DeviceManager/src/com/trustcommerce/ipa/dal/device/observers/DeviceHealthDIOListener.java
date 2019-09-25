package com.trustcommerce.ipa.dal.device.observers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.jpos.IngenicoConst;
import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.device.firmware.Firmware;
import com.trustcommerce.ipa.dal.device.interfaces.FirmwareProcessor;
import com.trustcommerce.ipa.dal.model.health.DeviceHealth;
import com.trustcommerce.ipa.dal.terminal.IngenicoTerminal;
import com.trustcommerce.ipa.dal.terminal.IngenicoTerminalSerial;

import jpos.events.DirectIOEvent;
import jpos.events.DirectIOListener;

public class DeviceHealthDIOListener implements DirectIOListener {

	/** slf4j. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceHealthDIOListener.class);
	// private static final DeviceEvent ingenicoDeviceEvent =
	// DeviceEvent.DEVICE;
	private Object ingenicoDevice;

/**
 * 
 * @param ingenicoDevice Object
 */
	public DeviceHealthDIOListener(final Object ingenicoDevice) {

		// ingenicoDevice.setEventListener(this);
		this.ingenicoDevice = ingenicoDevice;
	}

	@Override
	public final void directIOOccurred(final DirectIOEvent arg0) {
		LOGGER.debug(" -> directIOOccurred() {}", arg0.getData());

		DeviceHealth deviceHealth = null;
		if (arg0.getEventNumber() == IngenicoConst.JPOS_FC_HEALTH_STATS) {
			LOGGER.debug("DeviceHealthDIOListener: Got health status info.");

			DeviceState rsltState = DeviceState.READY;

			switch (arg0.getData()) {
			case IngenicoConst.ING_DIO_RESPONSE_SUCCESS:
				// 0
				try {
					final String newDeviceHealth = new String((byte[]) arg0.getObject());
					deviceHealth = new DeviceHealth(newDeviceHealth);
					LOGGER.debug("Found health, verify content: {}", newDeviceHealth);
				} catch (ClassCastException e) {
					LOGGER.error("Failed to cast object to bytes");
				}

				break;
			case IngenicoConst.ING_DIO_RESPONSE_ERROR:
				// 10000
				LOGGER.debug("Health request failed");
				break;


			default:

				final String msg2 = new String((byte[]) arg0.getObject());
				LOGGER.error("Error encountered retreiving health info. Code: {}, Message {}", arg0.getData(), msg2);
				rsltState = DeviceState.ERROR;
				break;
			}

			if (deviceHealth != null) {

				if (ingenicoDevice instanceof FirmwareProcessor) {
					((Firmware) ingenicoDevice).setLoadedFormVersion(deviceHealth.getApplicationVersion());
					((Firmware) ingenicoDevice).setModelReference(deviceHealth.getModelReference());
				} else if (ingenicoDevice instanceof IngenicoTerminal) {
					if (deviceHealth.getApplicationVersion() == null || deviceHealth.getApplicationVersion().isEmpty()) {
						((IngenicoTerminal) ingenicoDevice).setSerialNumber(deviceHealth.getSerialNo());
					} else {
						((IngenicoTerminal) ingenicoDevice).setDeviceHealth(deviceHealth, rsltState);
					}
				} else if (ingenicoDevice instanceof IngenicoTerminalSerial) {
					if (deviceHealth.getApplicationVersion() == null || deviceHealth.getApplicationVersion().isEmpty()) {
						((IngenicoTerminalSerial) ingenicoDevice).setSerialNumber(deviceHealth.getSerialNo());
					} else {
						((IngenicoTerminalSerial) ingenicoDevice).setSerialNumber(null);
					}
				}
			}

		}
		LOGGER.trace("<- directIOOccurred");
	}
}
