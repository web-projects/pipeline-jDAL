package com.trustcommerce.ipa.dal.model.emv;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.emvconstants.ArpcStatus;
import com.trustcommerce.ipa.dal.emvconstants.EmvTags;
import com.trustcommerce.ipa.dal.emvconstants.EmvTagsConst;

/**
 * 
 * @author luisa.lamore
 *
 */
public class ArpcData {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ArpcData.class);

	private static final String STATUS = "status";
	private static final String AUTHORIZATION_CODE = "emv_8a_authorizationresponsecode";
	private static final String TRANSACTION_ID = "transid";
	private static final String PARTIAL_AMOUNT = "partialamount";
	private static final String ERROR_TYPE = "errortype";
	private static final String ERROR = "error";
	private static final String OFFENDERS = "offenders";
	private static final String FAILED_TO_PROCESS = "failtoprocess";
	/** Possible value B098 received on errors. */
	private static final String MAGNEPRINTSTATUS = "magneprintstatus";

	private static final String DESCRIPTOR = "T";
	private static final String H = "h";
	private static final String A = "a";
	/** */
	private static final Map<String, String> ARPCSPECIFIERMAP = new HashMap<String, String>();

	static {
		ARPCSPECIFIERMAP.put(EmvTags.T8A.tagName(), A);
		ARPCSPECIFIERMAP.put(EmvTags.T91.tagName(), H);
		ARPCSPECIFIERMAP.put(EmvTags.T9F36.tagName(), H);
		ARPCSPECIFIERMAP.put(EmvTags.T71.tagName(), H);
		ARPCSPECIFIERMAP.put(EmvTags.T72.tagName(), H);
		// For partial amount
		ARPCSPECIFIERMAP.put(EmvTags.T9F02.tagName(), H);
	}
	/** */
	public enum Keys {
		status, partialamount, authcode, transid
	}


	private Map<String, String> arpcMap;

	/** Always must be part of the ARPC. String "status". */
	private ArpcStatus status;

	private String partialAmount;
	/** tag 8A. */
	private String authorizationCode;

	private String transactionId;
	
	private String arpcAsString;

	private String errorType;
	
	private String offenders;
	
	private String error;

	/**
	 * Example of the String received: {status=approved, authcode=123456, transid=027-0312663786}
	 * transid=027-0000030768,error=unknown,status=0,transid=027-0000030768"/ BEGIN\nerror=unknown\ntransid=027-0000030768\nEND\n
	 * 
	 * @param arcpReceived
	 */
	public ArpcData() {
		arpcMap = new HashMap<String, String>();
		status = ArpcStatus.unknown;
	}
	
	
	
	/**
	 * 
	 * @param arcpReceived String
	 */
	public final void parseArpcReceived(final String arcpReceived) {
		LOGGER.debug("-> parseArpcReceived()");
		// eliminate the open bracket
		arpcAsString = arcpReceived;
		String temp = arcpReceived.replace("{", "");
		temp = temp.replace("}", "");
		final String[] tempArray = temp.split(",");

		for (String s : tempArray) {
			final String[] temp2 = s.split("=");
			if (temp2.length == 2) {
				final String key = temp2[0].trim();
				final String value = temp2[1].trim();
				setValues(key, value);
				arpcMap.put(temp2[0].trim(), temp2[1].trim());
			}
		}
	}
	
	/**
	 * Parse ARPC values.
	 * @param key String
	 * @param val String
	 */
	private final void setValues(final String key, final String val) {
		
		if (STATUS.equalsIgnoreCase(key)) {
			try {
				status = ArpcStatus.valueOf(val);
			} catch (Exception e) {
				LOGGER.warn("Unkown value was received {} ", val);
				status = ArpcStatus.unknown;
			}
		} else if (AUTHORIZATION_CODE.equalsIgnoreCase(key)) {
			authorizationCode = val;
		} else if (TRANSACTION_ID.equalsIgnoreCase(key)) {
			transactionId = val;
		} else if (PARTIAL_AMOUNT.equalsIgnoreCase(key)) {
			partialAmount = val;
		} else if (ERROR.equalsIgnoreCase(key)) {
			error = val;
		} else if (ERROR_TYPE.equalsIgnoreCase(key)) {
			errorType = val;
		} else if (OFFENDERS.equalsIgnoreCase(key)) {
			offenders = val;
		}
	}

	/**
	 * Builds the ARPC String in the format required to inject it in the
	 * terminal.
	 * Example:
	 * 
	 * emv_91_issuerauthenticationdata=49E28D3E504FF2930012,
	 * emv_8a_authorizationresponsecode=00,status=approved 
	 * @param respData Map<String, String>
	 * @return String
	 */
	public static String buildARPCData(final Map<String, String> respData) {
		LOGGER.debug("-> buildARPCData() respData: {}", respData);
		String arpc = EmvTagsConst.EMV_DELIM;
		
		String specifier = null;
		String length = null;

		for (Map.Entry<String, String> data : respData.entrySet()) {
			specifier = ARPCSPECIFIERMAP.get(data.getKey());
			LOGGER.debug("data: {}", data.getKey());
			if (specifier != null) {
				LOGGER.debug("Found {} in arpcSpecifierMap ", data.getKey());
				final String value = data.getValue();

				if (specifier.equals(H)) {
					length = Integer.toHexString(value.length() / 2);
				} else if (specifier.equals(A)) {
					length = Integer.toHexString(value.length());
				}
				if ((length.length() % 2) != 0) {
					length = '0' + length;
				}

				final String tagName = data.getKey().split("_")[1];
				final String tagData = DESCRIPTOR + tagName + ":" + length + ":" + specifier 
						+ value + EmvTagsConst.EMV_DELIM;
				arpc += tagData;
			}
		}
		return arpc;
	}
	/**
	 * 
	 * @return Map<String, String>
	 */
	public final Map<String, String> getArpcMap() {
		return arpcMap;
	}
	/**
	 * 
	 * @param arpcMap Map<String, String>
	 */
	public final void setArpcMap(final Map<String, String> arpcMap) {
		this.arpcMap = arpcMap;
	}
	/**
	 * 
	 * @return ArpcStatus
	 */
	public final ArpcStatus getStatus() {
		return status;
	}
    /**
     * 
     * @param status ArpcStatuss
     */
	public final void setStatus(final ArpcStatus status) {
		this.status = status;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getPartialAmount() {
		return partialAmount;
	}
	/**
	 * 
	 * @param partialamount String
	 */
	public final void setPartialAmount(final String partialamount) {
		this.partialAmount = partialamount;
	}
	
	/**
	 * 
	 * @return String
	 */
	public final String getAuthorizationCode() {
		return authorizationCode;
	}
	
	
	/**
	 * 
	 * @return String
	 */
	public final String getTransactionId() {
		return transactionId;
	}
	
	/**
	 * 
	 * @param val String
	 */
	public final void setTransactionId(final String val) {
		this.transactionId = val;
	}

	/**
	 * 
	 * @return String
	 */
	public final String getErrorType() {
		return errorType;
	}
	/**
	 * 
	 * @param errorType String
	 */
	public final void setErrorType(final String errorType) {
		this.errorType = errorType;
	}

	@Override
	public final String toString() {
		return "ArpcData [arpcMap=" + arpcMap + ", status=" + status + ", partialAmount=" + partialAmount
		        + ", authorizationCode=" + authorizationCode + ", transactionId=" + transactionId + ", errorType="
		        + errorType + ", offenders=" + offenders + ", error=" + error + "]";
	}
	/**
	 * 
	 * @return String
	 */
	public final String getOffenders() {
		return offenders;
	}
	
	/**
	 * 
	 * @param offenders String
	 */
	public final void setOffenders(final String offenders) {
		this.offenders = offenders;
	}
	
	/**
	 * 
	 * @return String
	 */
	public final String getError() {
		return error;
	}
	
	/**
	 * 
	 * @param error String
	 */
	public final void setError(final String error) {
		this.error = error;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getArpcAsString() {
		return arpcAsString;
	}
	
	/**
	 * 
	 * @param value String
	 */
	public final void setArpcAsString(final String value) {
		this.arpcAsString = value;
	}
	
	/**
	 * Processor returns a failure.
	 * @return true if ARPC contains an error type 'failed to process' or error 'unkown'.
	 */
	public final boolean failedToProcess() {
		if (status == ArpcStatus.unknown) {
			// example: BEGIN\nerror=unknown\ntransid=027-0000030768\nEND
			return true;
		}
		if (status == ArpcStatus.error && errorType != null && errorType.equals(FAILED_TO_PROCESS)) {
			return true;
		} else {
			return false;
		}
	}
	
}
