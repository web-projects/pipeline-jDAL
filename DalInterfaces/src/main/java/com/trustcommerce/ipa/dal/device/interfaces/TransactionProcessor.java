package com.trustcommerce.ipa.dal.device.interfaces;

import java.util.Map;

import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.constants.forms.SwipeMode;
import com.trustcommerce.ipa.dal.exceptions.DeviceNotConnectedException;
import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;
import com.trustcommerce.ipa.dal.exceptions.MissingEncryptionKeyException;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.model.emv.ArpcData;
import com.trustcommerce.ipa.dal.model.payment.Transaction;



/**
 * 
 * @author kmendoza
 *
 *         Interface class for devices to handle both the applet display and the
 *         device
 * 
 */
public interface TransactionProcessor {
 
	// Initialize connection to device
    void initializeDevice() throws IngenicoDeviceException, DeviceNotConnectedException;
    
 
    // Close connection to device
    void releaseDevice();

    // //////////////////////////////////////////////////////////////////////////
    // Processing steps
    // //////////////////////////////////////////////////////////////////////////

    void startManualEntryProcess();

    // Select credit or debit
    void selectCardType();

    /**
     * 
     * @param amount
     *            String Purchase amount
     * @throws JposException
     *             if the Encryption Key is missing.
     */
    void getDebitPin(final Long amount) throws IngenicoDeviceException, MissingEncryptionKeyException;

    /**
     * Verify amount.
     * 
     * @param amount
     *            String
     * @throws JposException
     */
    void verifyAmount(final String amount) throws IngenicoDeviceException;

    Transaction getTransactionData();

    /**
     * Set the DeviceEventListener instance to send state.
     */
    void setEventListener(DeviceEventListener newListener);
    
    /**
     * Sets the local Event listener to null.
     */
    void resetEventListener();

    void rebootIngenicoDevice();

    void displayTransactionStatus(final boolean transApproved);

    boolean isDeviceIsEmvEnabled();

    void showSwipeForm(final SwipeMode swipeMode);

    // NEW EMV

    /** Use to transfer the ARPC data to the terminal. */
    void sendARPCToTerminal(ArpcData arpcData);
    
    /**
     * Notifies the terminal the beginning of an EMV transaction.
     */
    void startEMVProcess();
    
    void cancelEMVTransaction();

    Map<String, String> getConfirmRespData();

    //void logEmvDeviceStatus(String status);

    void parseEMVStatus(String data);

    void notifyCallerOfStateChange(final DeviceState state);

    void enableMsrDataEvent();

    String getARQCData();

    String getTransactionPreparationResponse();

    void onlinePinComplete();

    void onlinePinBypass();

    /** Starts mag swipe processing. */
    void swipeCard() throws IngenicoDeviceException;
    
    void setTransPrepData(String val);

    void setARQCData(String val);

    void setConfirmRespData(String val);

    // Wait for MSR to fully disable before re-enabling swiper.
    void reenableSwiper();
    
    /**
     * Enables the MSR processor to work as EMV
     * @param val
     */
    void processTransactionAsEmv(boolean val);
    
    void displayZipcode();
    
    String getTracksData(Map<String, String> emvMap);
    
    /**
     * Sets or resets the configurable emv parameter in the terminal.
     * @param value int 1 to enable, 0 to disable
     * @throws IngenicoDeviceException
     */
    void updateEmvKey(final int value) throws IngenicoDeviceException;


	void displayWarningInTerminal(EntryModeStatusID errorCode, String msg);
	
	boolean isDoneManualTransaction();

	void setDoneManualTransaction(boolean val);
	
	/**
	 * Set to true after the MSR has been opened successfully.
	 * @return
	 */
	boolean initializationIsComplete();
	
	void requestTerminalSerialNumber();
	
	String getTerminalSerialNumber();
	
	void suspendEmvFlow();
}
