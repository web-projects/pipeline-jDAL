package com.trustcommerce.ipa.dal.model.emv;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.constants.global.ProcessorPlatforms;
import com.trustcommerce.ipa.dal.emvconstants.EmvKeys;
import com.trustcommerce.ipa.dal.emvconstants.EmvTags;
import com.trustcommerce.ipa.dal.emvconstants.EmvTagsConst;
import com.trustcommerce.ipa.dal.emvconstants.TransactionAction;
import com.trustcommerce.ipa.dal.model.exceptions.BadDataException;

/**
 * This class build the EMV ARQC properties that will be submited to tcLink.
 * 
 * Note: PROD and QA use the same custids but the passwords differ ... For
 * testing purposes TCIPA QA password is testipa1
 * 
 * @author luisa.lamore
 *
 */
public class ArqcData {

	/** log4j logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ArqcData.class);

	/**
	 * For TSYS EMV. Since all supported Ingenico devices have pinpad, this
	 * value is hardcoded to �y�; however, this may change in the future..
	 */
	private static final String YES = "y";

	/** Amount is the transaction amount in cents. */
	private String amount;
	/** credit or debit. */
	private boolean emvDebitCard;

	/** For TSYS EMV. Contact kernel version (e.g. emv_kernel_version=0487). */
	private String emvKernelVersion;
	/** For TSYS EMV. i.e. device_serial=80504859 */
	private String deviceSerial;
	/** Amount is the transaction amount in cents. */
	private String name;
	/** emv_9f41_. */
	private String transactionSequenceCounter;
	/** encripted pin and ksn. Even if the PIN is entered by Payer, it will not get generated if it is offlinePIN.*/
	private String pinBlock;
	/** ARQC tags. */
	private Map<String, String> arqc;
	/** Processing Platforms. */
	private ProcessorPlatforms processor;
	
	/**
	 * Constructor.
	 * @param processorPlatform enum type ProcessorPlatforms
	 */
	public ArqcData(final ProcessorPlatforms processorPlatform) {
		arqc = new HashMap<String, String>();
		processor = processorPlatform;
		// PIN block is always sent
		pinBlock = "";
	}

	/**
	 * 
	 * @return String
	 */
	public final String getAmount() {
		return amount;
	}

	/**
	 * 
	 * @param tag
	 *            EmvTags
	 * @return String
	 */
	public final String getTag(final EmvTags tag) {
		final String temp = arqc.get(tag.tagName());
		return temp;
	}

	/**
	 * 
	 * @param amount
	 *            String
	 */
	public final void setAmount(final String amount) {
		this.amount = amount;
	}

	/**
	 * Returns the Card Holder Name.
	 * 
	 * @return String
	 */
	public final String getName() {
		return (String) arqc.get("name");
	}
	
	/**
	 * 
	 * @return String
	 */
	public final String getDeviceSerial() {
		return deviceSerial;
	}
	
	/**
	 * 
	 * @param value String
	 */
	public final void setDeviceSerial(final String value) {
		this.deviceSerial = value;
	}
	
	/**
	 * Set during the First Cryptogram process.
	 * @param val boolean
	 */
	public final void setEmvDebitCard(final boolean val) {
		this.emvDebitCard = val;
	}
	
	/**
	 * 
	 * @return Map<String, String>
	 */
	public final Map<String, String> getArqc() {
		return arqc;
	}
	
	/**
	 * 
	 * @param arqc Map<String, String>
	 */
	public final void setArqc(final Map<String, String> arqc) {
		this.arqc = arqc;
	}

	/**
	 * Returns all ARQC tags required by TcLink.
	 * 
	 * @return type Properties
	 */
	public final Properties getArqcPostData() {
		LOGGER.info("-> getArqcPostData()");
		final Properties p = new Properties();

		p.put(EmvKeys.KEY_ACTION.key(), TransactionAction.AUTH.getAction());
		// Amount is the transaction amount in cents
		p.put(EmvKeys.KEY_AMOUNT.key(), amount);

		// OnlinePIN and OfflinePin vs. Signature (see tfs 4042)
		
		// pin and emv_processingcode tags must always be together in the ARQC for either credit or debit
		p.put(EmvKeys.KEY_PIN_BLOCK.key(), pinBlock);
		if (emvDebitCard) {
			p.put(EmvKeys.KEY_PROCESSING_CODE.key(), "debit");
		} else {
			p.put(EmvKeys.KEY_PROCESSING_CODE.key(), "credit");
		}	
		
 		
		final Iterator it = arqc.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry pair = (Map.Entry) it.next();
			final String key = (String) pair.getKey();
			if (!key.contains(EmvTagsConst.emv_5f30_servicecode)
			        && !key.contains(EmvTagsConst.emv_5f2d_languagepreference)) {
				// tags emv_5f30_servicecode and emv_5f2d_languagepreference are
				// not used
				p.put((String) pair.getKey(), (String) pair.getValue());
			}
		}
		it.remove(); // avoids a ConcurrentModificationException
		
		// TSYS needs additional tags
		if (processor == ProcessorPlatforms.Vital) {
			getTsysTags(p);
		}
		
		return p;
	}
	
	/**
	 * Parameters for TSYS EMV
	 * @param p Properties type
	 */
	private final void getTsysTags(final Properties p) {

		// Hardcode it for now as all Ingenico devices are pinpad capable.
		p.put("device_pinpad_capable", YES);
		if (deviceSerial != null) {
			p.put("device_serial", deviceSerial);
		}
		// final String emvKernelVersion =
		// device.getDeviceVariable("EMV_KERNEL_VER");
		if (emvKernelVersion != null) {
			p.put("emv_kernel_version", emvKernelVersion);
		}
	}

	@Override
	public final String toString() {
		return "{" 

		        + "\"amount\" : \"" + amount 
		        + "\"," + "\"action\" : \"" + TransactionAction.AUTH.getAction() 
		        + "\"," + "\"emvDebitCard\" : \"" + emvDebitCard + "\"," + "\"," + "\"" + "}";
	}
	
	/**
	 * 
	 * @param props Properties
	 * @return String
	 */
	public static String outputToFile(final Properties props) {
		final StringBuilder sb = new StringBuilder();
		for (Map.Entry<Object, Object> e : props.entrySet()) {
			final String key = (String) e.getKey();
			final String value = (String) e.getValue();
			sb.append(key);
			sb.append("=");
			sb.append(value);
			sb.append(System.lineSeparator());
		}
		sb.append("=======================================================");
		return sb.toString();
	}
	/**
	 * 
	 * @return String
	 */
	public final String getEmvKernelVersion() {
		return emvKernelVersion;
	}
	/**
	 * 
	 * @param emvKernelVersion String
	 */
	public final void setEmvKernelVersion(final String emvKernelVersion) {
		this.emvKernelVersion = emvKernelVersion;
	}

	/**
	 * 
	 * @param pinAndksn
	 *            String
 	 * @throws BadDataException
	 *             PIN or KSN are missing
	 */
	public final void setPinBlock(final String pinAndksn) throws BadDataException {
		if (pinAndksn == null || pinAndksn.isEmpty()) {
			LOGGER.error("setPinBlock: PIN block is empty");
			throw new BadDataException("PIN block is empty");
		} else {
			pinBlock = pinAndksn;
			arqc.put("pin", pinBlock);
		}
	}
}
