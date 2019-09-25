package com.trustcommerce.ipa.dal.model.emv;

import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.constants.paths.TcipaFiles;
import com.trustcommerce.ipa.dal.logger.FileIOUtils;

public class Retries {

	/** log4j logger.*/
	private static final Logger LOGGER = LoggerFactory.getLogger(Retries.class);
	
	
	Queue<EmvRetry> list;
	
	/**
	 * 
	 */
	public Retries() {
		list = new LinkedList<EmvRetry>();
	}
	
	/**
	 * Adding a new ARQC object to the queue.
	 * @param val EmvRetry
	 */
	public final void add(final EmvRetry val) {
		list.add(val);
	}
	
	
	/**
	 * A new ARPC has been received from client bridge.
	 * @param transId String
	 * @param retryCounter int
	 * @throws Exception
	 */
	public final void receiveArpc(final String transId, final int retryCounter) throws Exception {
		
		final EmvRetry r = list.peek();
		if (r.isReversedRequested()) {
			remove(r);
			throw new Exception("This ARPC is not valid, it was marked for reversal");
		} else {
			r.setArpcTransId(transId);
		}

	}
	
	
	/**
	 * The card was removed unexpectedly. Request reversal.
	 * @param retryCounter integer
	 * @return String TransactionId
	 */
	public final String requestReversal(final int retryCounter) {
		String transId = null;
		final EmvRetry r = list.peek();
		if (r != null) {
			transId = r.getArpcTransId();
			if (transId == null) {
				// have not received arpc yet, mark for reversal
				LOGGER.debug("mark Reversed Requested {}", retryCounter);
				r.setReversedRequested(true);
			} else {
				r.setArpcTransId(transId);
				remove(r);
			}
		}
		return transId;
	}
	
	/**
	 * Removes the first object from the queue.
	 * @param r EmvRetry pair
	 */
	private void remove(final EmvRetry r) {
		LOGGER.debug("{} : Removing {} from Queue ", list.size(), r);
		if (GlobalConstants.LOCAL_TEST) {
			FileIOUtils.appendToFile(r.toString(), TcipaFiles.EMV_FINAL_DATA);
		}
		list.remove();
		LOGGER.debug("{} : Removed ", list.size());
	}
	


	@Override
	public final String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Retries [list=");
		builder.append(list);
		builder.append("]");
		return builder.toString();
	}
}
