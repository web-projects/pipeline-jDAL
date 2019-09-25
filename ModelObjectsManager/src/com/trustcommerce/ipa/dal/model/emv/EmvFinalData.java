package com.trustcommerce.ipa.dal.model.emv;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.emvconstants.EmvKeys;
import com.trustcommerce.ipa.dal.emvconstants.EmvTags;
import com.trustcommerce.ipa.dal.emvconstants.Tag8A;


/**
 * 
 * @author luisa.lamore
 *
 */
public class EmvFinalData {

	/** log4j logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(EmvFinalData.class);
	/** . */
	private static final String EQUAL = "=";
	
	/** Map with the EMV Final Data tags. */
    private Map<String, String> transactionFields;
    /** Value of Tag 8A. */
    private Tag8A arpc8aTag;
    /** Tag 4F. */
    private String applicationAid;
    /** emv_9f02_amountauthorized. */
    private String amountauthorized;
    /** action could be emv_auth_confirmation or . */
    private String action;
    /** processingCode is debit/credit.*/
    private String processingCode;
    
    private String emvFinalStatus;
    
	/**
     * Constructor.
     */
    public EmvFinalData() {
    }


    /**
     * 
     * @param transactionFields Map<String, String> 
     */
    public final void setTransactionFields(final Map<String, String> transactionFields) {
    	LOGGER.debug("-> setTransactionFields()");
        this.transactionFields = transactionFields;
        if (transactionFields.containsKey(GlobalConstants.TRACK2)) {
        	// American Express is the unique case where the encrypted track is empty
        	transactionFields.remove(GlobalConstants.TRACK2);
        }
        final String tag8a = transactionFields.get(EmvTags.T8A.tagName());
		if (tag8a != null && !tag8a.isEmpty()) {
			this.arpc8aTag = Tag8A.getTag8A(tag8a);  
		}
		applicationAid = transactionFields.get(EmvTags.T4F.tagName());
		
		amountauthorized = transactionFields.get(EmvTags.T9F02.tagName());
		// Tag 91 should not exist in the EMV Final Data
		transactionFields.remove(EmvTags.T91.tagName());
    }

    /**
     * Returns the value of Tag 8A.
     * @return EMV tag 8A
     */
    public final Tag8A getArpc8aTag() {
        return arpc8aTag;
    }

    /**
     * Returns the value of Tag 4F, which contains the AID of the issuer.
     * @return String
     */
    public final String getApplicationAid() {
        return applicationAid;
    }
 
    
    /**
     * Returns the value of Tag 4F, which contains the AID of the issuer.
     * @return int
     */
    public final int getAmountauthorized() {
    	final int temp = Integer.parseInt(amountauthorized);
        return temp;
    }
 
    
    /**
     * Returns true when a transaction was approved or decline by the chip.
     * @return boolean
     */
    public final boolean isEMVOfflineTransaction() {
    	if (arpc8aTag == Tag8A.UnableOnlineOfflineDeclined || arpc8aTag == Tag8A.UnableOnlineOfflineApproved 
    			|| arpc8aTag == Tag8A.OfflineDeclined || arpc8aTag == Tag8A.OfflineApproved) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    
    /**
     * Returns true is the transaction was approved.
     * @return true is the transaction was approved.
     */
    public final boolean transactionApproved() {
    	if (arpc8aTag == Tag8A.OnlineApproved || arpc8aTag == Tag8A.UnableOnlineOfflineApproved) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    
	/**
	 * Returns all ARQC tags in a String format.
	 * 
	 * @return String
	 */
	public final String getPropertiesFromMap() {
		final StringBuilder sb = new StringBuilder();
		
		sb.append(EmvKeys.KEY_ACTION.key());
		sb.append(EQUAL);
		sb.append(action);

		final Iterator it = transactionFields.entrySet().iterator();
		while (it.hasNext()) {
			
			final Map.Entry pair = (Map.Entry) it.next();
			sb.append(",");
			sb.append((String) pair.getKey());
			sb.append(EQUAL);
			sb.append((String) pair.getValue());
		}
		return sb.toString();
	}
	

    @Override
	public final String toString() {
        return "{"
        		+ "\"Action\" : \"" + action 
        		+ "\"," + "\"ProcessingCode\" : \"" + processingCode 
        		+ "\"," + "\"TransactionFields\" : \"" + (new Gson()).toJson(transactionFields) + "\","
                + "\"Arpc8aTag\" : \"" + arpc8aTag + "\"" 
        		+ "}";
    }
    /**
     * 
     * @return String
     */
	public final String getAction() {
		return action;
	}

	/**
	 * 
	 * @param val String
	 */
	public final void setAction(final String val) {
		this.action = val;
	}
	
	/**
	 * Set before submitting the EMV Final Data.
	 * Credit or debit.
	 * @param val String
	 */
    public final void setProcessingCode(final String val) {
        this.processingCode = val;
    }

    
    
    public String getEmvFinalStatus() {
		return emvFinalStatus;
	}


	public void setEmvFinalStatus(String emvFinalStatus) {
		this.emvFinalStatus = emvFinalStatus;
	}
}
