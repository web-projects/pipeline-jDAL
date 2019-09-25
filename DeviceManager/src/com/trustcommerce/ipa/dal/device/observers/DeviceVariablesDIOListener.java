package com.trustcommerce.ipa.dal.device.observers;

import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.jpos.IngenicoConst;
import com.trustcommerce.ipa.dal.device.interfaces.TerminalInfoProcessor;
import com.trustcommerce.ipa.dal.device.types.DeviceConsts;

import jpos.events.DirectIOEvent;
import jpos.events.DirectIOListener;

/**
 * 
 * 
 *
 */
public class DeviceVariablesDIOListener implements DirectIOListener {

	private static final Logger lOGGER = LoggerFactory.getLogger(DeviceVariablesDIOListener.class);

	private Object obj;

	public Semaphore permits = new Semaphore(1);

	public DeviceVariablesDIOListener(Object object) {
		obj = object;
	}

	public void directIOOccurred(final DirectIOEvent directIOEvent) {
		lOGGER.debug("-> directIOOccurred() Event: {}", directIOEvent.getEventNumber());
		final Integer eventNumber = directIOEvent.getEventNumber();
		final Object retObject = directIOEvent.getObject();

		switch (eventNumber) {
		case IngenicoConst.ING_DIO_GET_UIA_VARIABLE:
			try {
				final String convertedObject = new String((byte[]) retObject, "US-ASCII");
				// Do no log the convertedObject: Possible PCI complaint issue

				final String[] value = convertedObject.split("=", 2);

				if (value[0].equalsIgnoreCase(DeviceConsts.EMV_KERNEL_VER)) {
					((TerminalInfoProcessor) obj).setKernelVersion(value[1]);
				} else if (value[0].equalsIgnoreCase(DeviceConsts.DFS_EMV_KEY)) {
					((TerminalInfoProcessor) obj).setTerminalEmvKey(value[1]);
				} else if (value[0].equalsIgnoreCase(DeviceConsts.DFS_ENCRYPTION_CONFIG)) {
					final Integer temp = Integer.parseInt(value[1]);
					((TerminalInfoProcessor) obj).setEncryptionConfiguration(temp);
				} else if (value[0].equalsIgnoreCase(DeviceConsts.DFS_TRACK12_ENCRYPTED)) {
					final Integer temp = Integer.parseInt(value[1]);
					// ((TerminalInfoProcessor) obj).setEncryptionConfiguration(temp);
				} else if (value[0].equalsIgnoreCase(DeviceConsts.KEYSTATUS)) {
					((TerminalInfoProcessor) obj).setDukptKeyStatus(value[1]);
				} else {
					lOGGER.debug("Received {}", value[0]);
				}
			} catch (NumberFormatException e) {
				lOGGER.warn("Error trying to obtain UIA variable {} ", e.getMessage());
			} catch (Exception e) {
				lOGGER.warn(e.getMessage());
			}
			break;
			
		default:
			break;
		}

		permits.release();
		lOGGER.debug("<- directIOOccurred()  ");
	}
}