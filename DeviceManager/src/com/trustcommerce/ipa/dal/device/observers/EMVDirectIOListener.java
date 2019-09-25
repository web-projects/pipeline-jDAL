package com.trustcommerce.ipa.dal.device.observers;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.jpos.IngenicoConst;
import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.device.IngenicoUtil;
import com.trustcommerce.ipa.dal.device.emv.LogEMV;
import com.trustcommerce.ipa.dal.device.interfaces.TransactionProcessor;

import jpos.events.DirectIOEvent;
import jpos.events.DirectIOListener;

public class EMVDirectIOListener implements DirectIOListener {

	/** slf4j. */
	private static final Logger LOGGER = LoggerFactory.getLogger(EMVDirectIOListener.class);
	// private static final DeviceEvent ingenicoDeviceEvent =
	// DeviceEvent.DEVICE;
	private TransactionProcessor ingenicoDevice;

	public EMVDirectIOListener(TransactionProcessor ingenicoDevice) {

		// ingenicoDevice.setEventListener(this);
		this.ingenicoDevice = ingenicoDevice;
	}

	@Override
	public void directIOOccurred(final DirectIOEvent arg0) {
		LOGGER.debug(" -> directIOOccurred()  Received EMV DirectIOEvent ");

		final int event = arg0.getEventNumber();
		final int data = arg0.getData();
		final Object obj = arg0.getObject();
		LOGGER.debug("Event: {}  ,Data: {}, obj: {}", event, data, obj);

		if (obj instanceof byte[]) {
			final String message = IngenicoUtil.hexToAscii(Hex.encodeHexString((byte[]) arg0.getObject()));
			if (event == IngenicoConst.JPOS_FC_POLL_SMART) {
				//LOGGER.debug("EMV Message Event {}", message);

				switch (data) {
				case IngenicoConst.JPOS_RC_GOOD_READ:
					LOGGER.debug("EMV card read successful");
					break;
				case IngenicoConst.ING_DIO_EMV_TRANSPREP_SUBCOMMAND:
					ingenicoDevice.setTransPrepData(message);
					LOGGER.debug("Received Transaction prep DIO.");
					break;
				case IngenicoConst.ING_DIO_EMV_AUTHREQ_SUBCOMMAND:
					ingenicoDevice.setARQCData(message);
					LOGGER.debug("Received ARQC DIO.");
					break;
				case IngenicoConst.ING_DIO_EMV_STATUS_SUBCOMMAND:
					LOGGER.debug("Received EMV status DIO: 1 : message: {}", message);
					LogEMV.logEmvDeviceStatus(message);
					ingenicoDevice.parseEMVStatus(message);
					break;
				case IngenicoConst.ING_DIO_EMV_CONFRESP_SUBCOMMAND:
					ingenicoDevice.setConfirmRespData(message);
					LOGGER.debug("Received confirmation response.");
					break;
				default:
					LOGGER.warn("Unexpected data value: {}", Integer.toString(data));
					break;
				}
				LOGGER.debug("Re-enabling msr event");
				ingenicoDevice.enableMsrDataEvent();
			}
			
		} else if (event == IngenicoConst.JPOS_FC_POLL_SMART && data == IngenicoConst.ING_DIO_RESPONSE_ERROR) {
			// 103 and 10000
			// JPOS sometimes fails to start an EMV transaction especially when a card is bad and requires a
			// technical fallback.
			
			// This event is received when the card is pulled from the reader while the transaction is
			// waiting for authorization.
			String message2 = null;
			
			if (obj instanceof String) {
				// Error found on Bad chip or when the chip can not be read
				message2 = (String) obj;
				LOGGER.warn("IO Ingenico response error " + message2);
				// the following message causes a "retry" when a newbie payer accidentally inserts and immediately removes the card
				ingenicoDevice.notifyCallerOfStateChange(DeviceState.EMV_CARD_REMOVED);
			}
			
			// For reference: The vault includes the following line but it causes an infinite loop ....
			// ingenicoDevice.startEMVProcess();

		} else if (obj instanceof String) {
			// Error found on Bad cards
			final String message2 = (String) obj;
			LOGGER.warn(message2);
			if (message2.contains("Error")) {
				LOGGER.error("Error initializing for EMV transaction");
				//ingenicoDevice.startEMVProcess();
			}
		} else {
			LOGGER.warn("Event argument is not a byte or String");
		}
	}
		
}
