package com.trustcommerce.ipa.dal.gui.interfaces;

import java.util.EventListener;

/**for reconnecting the device and proceed with the transaction.*/

public abstract interface ReconnectEventListener extends EventListener {
	
	/**for reconnecting the device and proceed with the transaction.*/
	void reconnect();

}
