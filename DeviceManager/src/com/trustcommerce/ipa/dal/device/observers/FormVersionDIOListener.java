package com.trustcommerce.ipa.dal.device.observers;

import org.apache.log4j.Logger;

import com.ingenico.jpos.IngenicoConst;
import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.device.forms.FormsPackage;

import jpos.events.DirectIOEvent;
import jpos.events.DirectIOListener;

// Listener used by needFormUpdate()


/**
 * Listener used by needFormUpdate()
 *  
 *
 */
public class FormVersionDIOListener implements DirectIOListener {

	private static final Logger logger = Logger.getLogger(FormVersionDIOListener.class);
	// IngenicoMsr
	private Object ingenicoDevice;
	private String loadedFormVersion;

	/**
	 * 
	 * @param ingenicoDevice
	 */
	public FormVersionDIOListener(final Object ingenicoDevice) {
		this.ingenicoDevice = ingenicoDevice;
	}

	@Override
	public final void directIOOccurred(final DirectIOEvent arg0) {
		logger.trace("--> directIOOccurred()");
		if (arg0.getEventNumber() == IngenicoConst.ING_DIO_RETRIEVE_FILE) {
			logger.debug("Got file status info. Value of data: " + arg0.getData());
			DeviceState rsltState = DeviceState.READY;

			switch (arg0.getData()) {
			case IngenicoConst.ING_DIO_RESPONSE_SUCCESS:
				logger.debug("Found version file, verify content: " + arg0.getObject());
				try {
					loadedFormVersion = new String((byte[]) arg0.getObject());
				} catch (ClassCastException e) {
					logger.debug("Failed to cast object to bytes");
					loadedFormVersion = "";
				}

				logger.trace("Value of loadedVer: " + this.loadedFormVersion);
				break;
				
			case IngenicoConst.ING_DIO_RESPONSE_ERROR:
				logger.info("Form version file not present in device");
				this.loadedFormVersion = "";
				break;
				
			default:
				final String msg = new String((byte[]) arg0.getObject());
				logger.fatal("Error encountered retreiving form version info file. Code: " + arg0.getData()
				        + " Message: " + msg);
				rsltState = DeviceState.ERROR;
				break;
			}

			if (ingenicoDevice instanceof FormsPackage) {
				((FormsPackage) ingenicoDevice).setLoadedFormVersion(loadedFormVersion, rsltState);
			}
		}
		logger.trace("<- FormVersionDIOListener");
	}
}
