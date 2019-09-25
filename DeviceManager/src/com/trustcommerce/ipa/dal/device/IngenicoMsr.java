package com.trustcommerce.ipa.dal.device;

import java.util.Map;
import java.util.TimerTask;

import javax.naming.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingenico.api.jpos.IngenicoMSR;
import com.ingenico.api.jpos.IngenicoPINPad;
import com.ingenico.api.jpos.IngenicoPINPadConst;
import com.ingenico.jpos.IngenicoConst;
import com.ingenico.jpos.JposDeviceServiceConst;
import com.ingenico.jpos.UIAConst;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfiguration;
import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;
import com.trustcommerce.ipa.dal.constants.device.DeviceState;
import com.trustcommerce.ipa.dal.constants.device.EntryModeStatusID;
import com.trustcommerce.ipa.dal.constants.device.TerminalEvent;
import com.trustcommerce.ipa.dal.constants.device.TerminalModel;
import com.trustcommerce.ipa.dal.constants.forms.FormTypes;
import com.trustcommerce.ipa.dal.constants.forms.SwipeMode;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.device.emv.EMVDataHandler;
import com.trustcommerce.ipa.dal.device.emv.SimpleTimer;
import com.trustcommerce.ipa.dal.device.interfaces.TransactionProcessor;
import com.trustcommerce.ipa.dal.device.observers.EMVDirectIOListener;
import com.trustcommerce.ipa.dal.device.utils.DioConfig;
import com.trustcommerce.ipa.dal.device.utils.DioErrorMessages;
import com.trustcommerce.ipa.dal.device.utils.DioRunFile;
import com.trustcommerce.ipa.dal.device.utils.DirectIOCommands;
import com.trustcommerce.ipa.dal.device.utils.DioSetGetVar;
import com.trustcommerce.ipa.dal.emvconstants.EmvTagsConst;
import com.trustcommerce.ipa.dal.emvconstants.TransactionType;
import com.trustcommerce.ipa.dal.exceptions.DeviceNotConnectedException;
import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;
import com.trustcommerce.ipa.dal.exceptions.MissingEncryptionKeyException;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;
import com.trustcommerce.ipa.dal.model.LocalActionEvent;
import com.trustcommerce.ipa.dal.model.Terminal;
import com.trustcommerce.ipa.dal.model.emv.ArpcData;
import com.trustcommerce.ipa.dal.model.exceptions.BadDataException;
import com.trustcommerce.ipa.dal.model.exceptions.CardExpiredException;
import com.trustcommerce.ipa.dal.model.payment.Transaction;
import com.trustcommerce.ipa.dal.model.types.EntryModeTypeId;
import com.trustcommerce.ipa.dal.model.types.ManualEntryState;
import com.trustcommerce.ipa.dal.terminal.IngenicoTerminalSerial;

import jpos.JposConst;
import jpos.JposException;
import jpos.MSR;
import jpos.MSRConst;
import jpos.PINPad;
import jpos.events.DataEvent;
import jpos.events.DataListener;
import jpos.events.DirectIOEvent;
import jpos.events.DirectIOListener;
import jpos.events.ErrorEvent;
import jpos.events.ErrorListener;

/**
 * setDeviceEnabled
 * @author kmendoza
 *
 *         Ingenico Generic implementation of TransactionProcessor
 * 
 */
public class IngenicoMsr implements TransactionProcessor, ErrorListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(IngenicoMsr.class);
	
	private TerminalModel deviceName;
	
	private LocalMsr myMsr;

	// Data state information
	private DeviceEventListener deviceEventListener;
	private TerminalEvent ingenicoDeviceEvent;

	
	private Transaction inputData;

	// Keep last listener set
	private DirectIOListener lastIOListener;
	private DataListener lastMSRDataListener;
	private DataListener lastPINDataListener;

	// Device connections

	/** Magnetic Stripe Reader. */
	private IngenicoMSR msr;
	private IngenicoPINPad pinPad;


	// JPOS_FC_HEALTH_STATS (13/0xD)
	private ManualEntryState currentState = ManualEntryState.MANUAL_ENTRY_OFF;
	private boolean isManualPanEntryStageSuccessful = true;

	private ClientConfiguration clientConfiguration;

	private String confirmRespData = "";

	private boolean emvTransPrepReady;

	/** Can not be null. */
	private String transPrepData = "";
 
	private String arqcData;

	private boolean processTransactionAsEmv;

	private SimpleTimer swiperReenablerTimer;
	/** True when the current process is the Second Generation. */
	private boolean inSecondGen;
	
	private Terminal terminalInformation;
	
	private boolean doneManualTransaction;
	
	private String terminalSerial;
	
	
	/**
	 * Constructor
	 * 
	 * @param terminalInfo
	 *            Terminal type
	 * @param transaction Transaction type
	 * @throws  ConfigurationException 
	 * @throws ConfigurationException
	 * @throws com.trustcommerce.ipa.dal.configuration.types.ConfigurationException 
	 */
	public IngenicoMsr(final Transaction transaction, final Terminal terminalInfo)   {
		this.terminalInformation = terminalInfo;
		try {
			this.clientConfiguration = ClientConfigurationUtil.getConfiguration();
		} catch (com.trustcommerce.ipa.dal.configuration.types.ConfigurationException e) {
			// TODO Auto-generated catch block
		}
		this.deviceName = terminalInfo.getModelName();
		inputData = transaction;
		myMsr = new LocalMsr(deviceName);
	}

	/**
	 * 
	 */
	public final void processTransactionAsEmv(final boolean val) {
		processTransactionAsEmv = val;
		inputData.setEntryModeTypeID(EntryModeTypeId.EMV_Chip_Read.ordinal());
	}


	/**
	 * Send state message to DeviceEventListener instance.
	 * Old name: generateState
	 * 
	 * @param state
	 *            State to send to listener
	 */
	public final void notifyCallerOfStateChange(final DeviceState state) {
		if (state == DeviceState.UNKNOWN) {
			// Nothing to notify
			return;
		}
		if (deviceEventListener != null) {
			deviceEventListener.handleEvent(new LocalActionEvent(this, state, ingenicoDeviceEvent));
		}
	}
	

	@Override
	public final void errorOccurred(final ErrorEvent ee) {
		LOGGER.error("-> errorOccurred() ");
		DeviceState state = DeviceState.UNKNOWN;
		final Object source = ee.getSource();

		try {
			if (source instanceof MSR) {
				final MSR oMSR = (MSR) ee.getSource();
				if (oMSR.getErrorReportingType() == MSRConst.MSR_ERT_CARD) {
					// Also here when the user cancels the "Enter Card Number"
					// form in the device ?
					LOGGER.warn("ERROR REPORTING BY CARD");
					LOGGER.info("ErrorCode: {}", ee.getErrorCode()); // 111=JPOS_E_FAILURE
					LOGGER.info("ErrorCodeExtended: {}", ee.getErrorCodeExtended()); // 0
					LOGGER.info("ErrorResponseValue: {} ", ee.getErrorResponse()); // 12=JPOS_ER_CLEAR
				}
			} else if (source instanceof PINPad) {
				LOGGER.warn("ERROR REPORTING BY PINPad, ErrorCode: {}", ee.getErrorCode());
				LOGGER.info("ErrorResponse: {}", ee.getErrorResponse()); // 12=JPOS_ER_CLEAR
				if (ee.getErrorCode() == 112) {
					notifyCallerOfStateChange(DeviceState.PINPAD_TIMEOUT);
					return;
				}
			}

			if (ee.getErrorCode() == JposConst.JPOS_SUCCESS) {
				LOGGER.debug("Received success code: 0");

			} else if (ee.getErrorLocus() == JposConst.JPOS_EL_INPUT
			        || ee.getErrorLocus() == JposConst.JPOS_EL_INPUT_DATA) {
				LOGGER.warn("Error encountered reading input data");
				state = DeviceState.INPUT_ERROR;

				// 1 second sleep to give the driver time to get its bearings
				Thread.sleep(1000);
				if (source.equals(msr)) {
					if (!msr.getClaimed()) {
						msr.claim(clientConfiguration.getMsrTimeout());
					}
					msr.clearInput();
					myMsr.enableMSR();
				} else if (source.equals(pinPad)) {
					LOGGER.warn("Input error caused by pin pad");
				} else {
					LOGGER.warn("Input error source unknown");
				}
			} else {
				state = DeviceState.ERROR;
			}
		} catch (JposException e) {
			LOGGER.error("JposException: Failed to clear input for swipe retry");
			state = DeviceState.ERROR;
		} catch (InterruptedException e) {
			LOGGER.error("JposException: Post-input error sleep interrupted");
			state = DeviceState.ERROR;
		}
		notifyCallerOfStateChange(state);
	}

	/**
	 * Check the exception caught and generate state as necessary.
	 * 
	 * @param e
	 *            JposException instance
	 * 
	 * @param msgPrefix
	 *            msgPrefix Log message prefix
	 */
	private void initFailHandler(final JposException e, final String msgPrefix) {
		if (e.getErrorCode() == JposConst.JPOS_E_NOSERVICE) {
			LOGGER.error("Device not connected");
			notifyCallerOfStateChange(DeviceState.DEVICE_NOT_CONNECTED);
		} else {
			LOGGER.error("{} : Error {}: {}", msgPrefix, e.getMessage(), e.getErrorCode());
			notifyCallerOfStateChange(DeviceState.ERROR);
		}
	}

	@Override
	public final void initializeDevice() throws IngenicoDeviceException, DeviceNotConnectedException {
		LOGGER.debug("-> initializeDevice");

		ingenicoDeviceEvent = TerminalEvent.DEVICE;
		myMsr.initMsr(this);
		msr = myMsr.getMsr();
		//msr = SingleMSR.getInstance(this, deviceName, clientConfiguration.getMsrTimeout());
 
		//initMSR();
		initPINPad();
		notifyCallerOfStateChange(DeviceState.READY);
		LOGGER.trace("<- initializeDevice()");
	}
	
	
	@Override
	public final void requestTerminalSerialNumber() {
		LOGGER.debug("-> requestTerminalSerialNumber");
		final IngenicoTerminalSerial serial = new IngenicoTerminalSerial(myMsr);
		serial.setEventListener(new SerialListener());
		serial.requesSerialNumberValue();
	}
	
	/**
	 * Open and claim the PIN pad device.
	 * 
	 * @throws IngenicoDeviceException
	 * @throws DeviceNotConnectedException if the terminal can not be connected
	 */
	private void initPINPad() throws IngenicoDeviceException, DeviceNotConnectedException {
		// Load the default listeners
		LOGGER.info("-> initPINPad() {}", deviceName);
		pinPad = new IngenicoPINPad();
		try {
			pinPad.addErrorListener(this);
			pinPad.open("PINPad_" + deviceName);
			pinPad.claim(clientConfiguration.getSignatureCaptureTimeout());
			pinPad.clearInput();
			pinPad.clearInputProperties();
			LOGGER.info("Pin pad initialization complete");
			
		} catch (JposException e) {
			if (e.getErrorCode() == JposConst.JPOS_E_FAILURE) {
				if (e.getMessage().contains("Could not communicate with devicePort")
						|| e.getMessage().contains("Device is detached")) {
					throw new DeviceNotConnectedException("Device is detached");
				}
			} else {
				inputData.setEntryModeStatusID(EntryModeStatusID.Error.getCode());
				throw new IngenicoDeviceException(DioErrorMessages.getErrorMessage("Error initializing PINPad: ", e));
			}
		}  
 	}

	
	


	/**
	 * Set the card information obtained from the MSR.
	 * Use for Non-EMV transactions only.
	 * @param id
	 */
	private void setCardInformation(EntryModeTypeId id) {
		LOGGER.debug("-> setCardInformation()");
		inputData.setEntryModeTypeID(id.ordinal());
		try {
			final String track1 = new String(msr.getTrack1Data());
			final String track2 = new String(msr.getTrack2Data());
			final String track3 = new String(msr.getTrack3Data());
			if (track3.isEmpty()) {
				LOGGER.warn("Track 3 is empty. This device may be missing the Encryption key!!");
			}
			inputData.setTracks(track1, track2, track3);
			inputData.setCreditCardNumber(msr.getAccountNumber());
			inputData.setCcExpirationDate(msr.getExpirationDate());
			inputData.setServiceCode(msr.getServiceCode());
			inputData.validateData();

			if (id == EntryModeTypeId.Swiped) {
				inputData.setLastname(msr.getSurname());
				inputData.setFirstName(msr.getFirstName());
				inputData.setTitle(msr.getTitle());
				inputData.setMiddlename(msr.getMiddleInitial());
			}

		} catch (BadDataException e) {
			LOGGER.error("BadDataException:{}", e.getMessage());
			notifyCallerOfStateChange(DeviceState.MANUAL_ENTRY_ERROR);
		} catch (JposException e) {
			LOGGER.error("JposException:{}, ", e.getMessage());
			notifyCallerOfStateChange(DeviceState.MANUAL_ENTRY_ERROR);
		} catch (CardExpiredException e) {
			LOGGER.error("CardExpiredException:{}", e.getMessage());
			notifyCallerOfStateChange(DeviceState.EXPIRED_CARD);
		}
	}

	/**
	 * Call on termination.
	 */
	@Override
	public final void releaseDevice() {
		LOGGER.debug("-> releaseDevice()");
		try {
			if (pinPad != null) {
				LOGGER.debug("Releasing pinpad");
				pinPad.close();
			}
			myMsr.releaseAndCloseMsr(lastIOListener);
 			deviceEventListener = null;
		} catch (JposException e) {
			final String temp = DioErrorMessages.getErrorMessage("releaseDevice", e);
			LOGGER.warn(temp);
			
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				LOGGER.warn("releaseDevice(): Received from the jPOS an InterruptedException !!!! {}", e.getMessage());
				// Commented out the line below inherited from the applet.
				// Thread.currentThread().interrupt();
				// At this point, we can not receive messages from the terminal ... TODO should close the msr?
				if (msr != null) {
					try {
						msr.close();
					} catch (JposException e1) {
					}
				}
			} else {
				LOGGER.warn("An unkown error happened while releasing MSR: {}", e.getMessage());
			}
		}
		pinPad = null;

	}

	@Override
	public void showSwipeForm(final SwipeMode swipeMode) {
		LOGGER.debug("-> showSwipeForm() {}", swipeMode.name());
		String formName = null;
		try {
			switch (swipeMode) {
			case SWIPE_INSERT:
				formName = FormTypes.SWIPE_INSERT.getName();
				break;
			case INSERT:
				formName = FormTypes.INSERT.getName();
				break;
			case REINSERT:
				formName = FormTypes.REINSERT.getName();
				break;
			case SWIPE:
				formName = FormTypes.SWIPE.getName();
				if (terminalInformation.getModelName() == TerminalModel.iUP250) {
					DioRunFile.showDeviceForm(msr, formName, 0);
				} else {
					DioRunFile.showDeviceForm(pinPad, formName, 1);
				}	
				return;
			default:
				LOGGER.error("INTERNAL ERROR: Unknown form name"); 
				return;
			}

			if (terminalInformation.getModelName() == TerminalModel.iUP250) {
				DioRunFile.showDeviceForm(msr, formName, 0);
			} else {
				DioRunFile.showDeviceForm(pinPad, formName, 0);
			}
			
		} catch (JposException | IngenicoDeviceException e) {
			LOGGER.error("Failed to show swipe form: name= {}, error= {}", formName, e.getMessage());
			notifyCallerOfStateChange(DeviceState.ERROR);
		}
	}

	@Override
	public final void swipeCard() throws IngenicoDeviceException {
		LOGGER.debug("-> swipeCard()");
		currentState = ManualEntryState.MANUAL_ENTRY_OFF;
		ingenicoDeviceEvent = TerminalEvent.MSR;
		// Manual transaction disables sound. So re-enable it when swiping.
		DioSetGetVar.enableSound(msr);
		
		//logMsr(); uncomment if need msr info
		try {
			// Disable manual entry just in case of a mode switch
			msr.directIO(IngenicoConst.JPOS_FC_SET_VARIABLE, new int[] { 1 }, new String("MANUALENTRY=OFF"));

			// Display the form
			if (!msr.getClaimed()) {
				msr.claim(clientConfiguration.getMsrTimeout());
			}
			myMsr.enableMSR();
			
			resetlastIOListener(new SwiperCardDIOListener());

			if (clientConfiguration.isContactlessEnabled()) {
				// Changing configuration must be done before enabling MSR
				LOGGER.info("Enabling Contactless Option");
				msr.directIO(IngenicoConst.JPOS_FC_CONFIG, new int[] { 0 }, new String("rfid:Integer:1"));
			}
 
			// Clear any input that got cached before enabling data event
			msr.clearInput();
			msr.clearInputProperties();
			msr.setDataEventEnabled(true);
 
			if (lastMSRDataListener != null) {
				LOGGER.debug("Removing the previous MSR data listener");
				msr.removeDataListener(lastMSRDataListener);
			}
			lastMSRDataListener = new MSRDataListener();
			msr.addDataListener(lastMSRDataListener);
 
			LOGGER.trace("Device ready for card swipe");

		} catch (JposException e) {
			throw new IngenicoDeviceException(DioErrorMessages.getErrorMessage("Error swiping cardR: ", e));
		}

		notifyCallerOfStateChange(DeviceState.READY);
		LOGGER.debug("<- swipeCard()");
	}


	@Override
	public final void selectCardType() {
		LOGGER.debug("-> selectCardType()");
		ingenicoDeviceEvent = TerminalEvent.CARDTYPESELECT;

		try {
			myMsr.enableMSR();
			resetlastIOListener(new CardTypeDIOListener());
			DioRunFile.showDeviceForm(msr, FormTypes.SELECT_CARD_TYPE.getName(), 1);
		} catch (JposException e) {
			LOGGER.error("Error encountered display credit/debit select form. Code: {}, extended code {}, Message {}",
					e.getErrorCode(), e.getErrorCodeExtended(), e.getMessage());
			notifyCallerOfStateChange(DeviceState.ERROR);
		} catch (IngenicoDeviceException e) {
			LOGGER.error("Error encountered display credit/debit select form. Code: {}, extended code {}, Message {}",
					e.getErrorCode(), e.getMessage());
			notifyCallerOfStateChange(DeviceState.ERROR);
		}
		LOGGER.trace("End selectCardType()");
	}



	/**
	 * 
	 * When the PIN entry is enabled, the PINPP[Model without i].K3Z form will
	 * be displayed The form displayed must be set in the jpos.xml file.
	 * Pin_Pad_iPP350, as in the following example:
	 * <prop name="pinform" type="String" value="PINPP350.K3Z"/>
	 * 
	 * @throws IngenicoDeviceException
	 * @throws MissingEncryptionKeyException
	 * 
	 */
	@Override
	public final void getDebitPin(final Long amount)
	        throws NumberFormatException, IngenicoDeviceException, MissingEncryptionKeyException {
		LOGGER.debug("-> getDebitPin() {}", amount);
		ingenicoDeviceEvent = TerminalEvent.PIN;

		if (lastPINDataListener != null) {
			pinPad.removeDataListener(lastPINDataListener);
		}
		lastPINDataListener = new PINDataListener();
		pinPad.addDataListener(lastPINDataListener);

		LOGGER.trace("Set account number");
		try {
			pinPad.setAccountNumber(inputData.getCreditCardNumber());
			pinPad.setAmount(amount);
	
			pinPad.setDataEventEnabled(true);

			if (!pinPad.getDeviceEnabled()) {
				pinPad.setDeviceEnabled(true);
			}
			LOGGER.debug("Begin EFT Transaction");
			pinPad.beginEFTTransaction(JposDeviceServiceConst.SVC_ENCRYPTTYPE_DUKPT, 0);
			// Displays form PIN*.K3Z
			pinPad.enablePINEntry();
			LOGGER.debug("Enabled PIN entry");
		} catch (JposException e) {
			// MissingEncryptionKeyException
			if (e.getErrorCode() == 114 && e.getErrorCodeExtended() == 201) {
				// This error combination means the device is missing the encryption key
				throw new MissingEncryptionKeyException("Missing Encryption Key");
			} else if (e.getErrorCode() == 106) {
				//Error getting Debit Card PIN: : Can not set property - beginEFTRanscation called Code: 106
				LOGGER.error("getDebitPin() {}", e);
				notifyCallerOfStateChange(DeviceState.ERROR);
			} else {
				throw new IngenicoDeviceException(DioErrorMessages.getErrorMessage("Error getting Debit Card PIN: ", e));
			}
		}
		LOGGER.trace("<- getDebitPin()");
	}

	

	public void displayZipcode() {
		LOGGER.debug("-> displayZipcode() ");
		ingenicoDeviceEvent = TerminalEvent.ZIPCODE;
		resetlastIOListener(new ZipCodeDIOListener());
		try {
			DioRunFile.displayZipCodeForm(msr);
		} catch (JposException | IngenicoDeviceException e) {
			LOGGER.error("Failed to disable MSR: {}", e);
			notifyCallerOfStateChange(DeviceState.ZIPCODE_INPUT_ERROR);
		}
	}

	/**
	 * Displays the form in the device that request the user to verify the
	 * amount.
	 * 
	 * @param amount
	 *            String
	 * @throws IngenicoDeviceException
	 */
	@Override
	public final void verifyAmount(final String amount) throws IngenicoDeviceException {
		LOGGER.debug("-> verifyAmount() " + amount);
		if (amount == null) {
			LOGGER.error("verifyAmount() AMOUNT IS NULL!!!!!!");
		}
		ingenicoDeviceEvent = TerminalEvent.AMOUNT;
		resetlastIOListener(new VerifyAmountDIOListener());
		try {
			DioRunFile.displayAmountVerificationForm(msr, "$" + amount);
		} catch (JposException e) {
			throw new IngenicoDeviceException(DioErrorMessages.getErrorMessage("Error Verifying Amount: " + amount, e));
		}
	}




	@Override
	public final void setEventListener(final DeviceEventListener newListener) {
		deviceEventListener = newListener;
	}
	
	@Override
	public final void resetEventListener() {
		deviceEventListener = null;
	}

	
	/**
	 * Replaces the current listener.
	 * @param listener type DirectIOListener
	 */
	private void resetlastIOListener(final DirectIOListener listener) {

		if (this.lastIOListener != null) {
			this.msr.removeDirectIOListener(lastIOListener);
		}
		this.lastIOListener = listener;
		this.msr.addDirectIOListener(listener);

	}



	@Override
	public final void startManualEntryProcess() {
		LOGGER.debug("-> startManualEntryProcess() ");
		ingenicoDeviceEvent = TerminalEvent.MANUAL_ENTRY;
		setDoneManualTransaction(true);
		try {
			resetlastIOListener(new ManualEntryDataListener());

			myMsr.enableMSR();
			if (lastMSRDataListener != null) {
				msr.removeDataListener(lastMSRDataListener);
			}
			lastMSRDataListener = new ManualEntryDataListener();
			msr.addDataListener(lastMSRDataListener);
			msr.setDataEventEnabled(true);
			// Now start the manual pan entry process
			startManualPanEntryProcess();
		} catch (JposException e) {
			LOGGER.error("Could not start the manaul entry process");
			notifyCallerOfStateChange(DeviceState.MANUAL_ENTRY_ERROR);
		} catch (IngenicoDeviceException e) {
			LOGGER.error("Could not start the manaul entry process 2");
			notifyCallerOfStateChange(DeviceState.MANUAL_ENTRY_ERROR);
		}
	}

	private void startManualPanEntryProcess() throws JposException, IngenicoDeviceException {
		LOGGER.info("-> startManualPanEntryProcess() " + currentState);
		msr.clearInput();
		DioRunFile.displayAccountNumberEntry(msr);
		currentState = ManualEntryState.MANUAL_PAN_ENTRY;
		notifyCallerOfStateChange(DeviceState.MANUAL_PAN_ENTRY);
	}

	public final void rebootIngenicoDevice() {
		LOGGER.warn("************** REBOOTING INGENICO DEVICE *****************");
		try {
			msr.directIO(IngenicoConst.ING_DIO_REBOOT, 0, null);
		} catch (JposException e) {
			LOGGER.error("Could not reboot device, a manual reboot may be necessary");
			notifyCallerOfStateChange(DeviceState.MANUAL_ENTRY_ERROR);
		}
	}


	public Transaction getTransactionData() {
		return inputData;
	}

	/**
	 * Use for devices that do not use the signature capture featured. Also use
	 * when the noSignature parameter is received.
	 * 
	 * @param transApproved
	 */
	public final void displayTransactionStatus(final boolean transApproved) {
		LOGGER.info("-> displayTransactionStatus() {} ", transApproved);
		ingenicoDeviceEvent = TerminalEvent.MSR;
		String buf = "VAR=";
		if (transApproved) {
			notifyCallerOfStateChange(DeviceState.TRANSACTION_APPROVED);
			buf = buf + "Approved";
		} else {
			notifyCallerOfStateChange(DeviceState.TRANSACTION_DECLINED);
			buf = buf + "Declined";
		}

		try {
			DioRunFile.showDeviceForm(msr, FormTypes.TRANSACTION_STATUS.getName(), buf, 1,
			        clientConfiguration.getSignatureCaptureTimeout());
			try {
				Thread.sleep(GlobalConstants.MESSAGE_WARN_SLEEP_TIMER);
			} catch (InterruptedException e) {
			}
		} catch (JposException | IngenicoDeviceException e) {
			LOGGER.error("Error found when trying to display the Transaction Status: {}", e.getMessage());
			e.printStackTrace();
		}
		LOGGER.debug("<- displayTransactionStatus() ");
	}

	// ===================================== EMV ============================================


	/**
	 * getVariable(String s).
	 * 
	 * @param s
	 */
	public final void getDirectIOVariable(final String s) {
		ingenicoDeviceEvent = TerminalEvent.MSR;
		try {
			final int[] nData = new int[] { 0 };
			msr.directIO(IngenicoConst.ING_DIO_GET_UIA_VARIABLE, nData, s);

		} catch (JposException e) {
			LOGGER.error("Failed to retrieve EMV information. Cause: " + e.getMessage() 
			+ " ErrorCode: " + e.getErrorCode() + " Extended code: " + e.getErrorCodeExtended());
			notifyCallerOfStateChange(DeviceState.ERROR);
		}

	}

	// =================================================================================
	// EMV NEW METHODS
	// =================================================================================
	
	/**
	 * starts the EMV transaction process.
	 * 
	 * Called by the device Bridge after receiving notification that card has
	 * been inserted. The finalAmount should be in "cents" i.e. 1.00 send as 100
	 */
	@Override
	public final void startEMVProcess() throws IllegalArgumentException {
		LOGGER.debug("-> startEMVProcess() inputData: " + inputData);
		processTransactionAsEmv(true);
		inSecondGen = false;

		int finalAmount = Integer.parseInt(inputData.getAmountInCents());
		int cashbackAmt = inputData.getCashbackAmt();
		final TransactionType transactionType = inputData.getTransactionType();
		
		emvTransPrepReady = false;
		ingenicoDeviceEvent = TerminalEvent.EMV;

		String dioMsg = EmvTagsConst.EMV_DELIM;
		if (transactionType == TransactionType.sale || transactionType == TransactionType.preAuth) {
			dioMsg += "00";
		} else if (transactionType.equals("credit2")) {
			dioMsg += "01";
			finalAmount *= -1;
			cashbackAmt *= -1;
		} else {
			throw new IllegalArgumentException("txnType invalid. Value provided: \"" + transactionType + "\"");
		}
		dioMsg += "A0" + Integer.toString(finalAmount) + EmvTagsConst.EMV_DELIM + Integer.toString(cashbackAmt)
		        + EmvTagsConst.EMV_DELIM;
		msr.removeDataListener(lastMSRDataListener);
		msr.removeDirectIOListener(lastIOListener);

		final EMVDirectIOListener newListener = new EMVDirectIOListener(this);
		msr.addDirectIOListener(newListener);
		lastIOListener = newListener;
		
		try {
			LOGGER.debug("startEMVProcess::dioMsg: " + dioMsg);
			msr.directIO(IngenicoConst.JPOS_FC_POLL_SMART, new int[] { IngenicoConst.ING_DIO_EMV_TRANSINIT_SUBCOMMAND },
			        dioMsg);
		} catch (JposException e) {
			LOGGER.error("Error initializing EMV transaction: \"" + e.getMessage() + "\" ErrorCode: " + e.getErrorCode()
			        + " Extended Errorcode: " + e.getErrorCodeExtended());
			notifyCallerOfStateChange(DeviceState.ERROR);
		}

		LOGGER.trace("<- startEMVProcess");
	}

	public void setTransPrepData(String message) {
		transPrepData += message;
	}

	@Override
	public final void cancelEMVTransaction() {
		LOGGER.debug("-> cancelEMVTransaction()");
		try {
			msr.directIO(IngenicoConst.JPOS_FC_POLL_SMART, new int[] { IngenicoConst.ING_DIO_EMV_SYNREQ_SUBCOMMAND },
			        EmvTagsConst.EMV_DELIM + "C");
		} catch (JposException e) {
			LOGGER.error("Failed to cancel the EMV transaction. Cause: " + e.getMessage()
			+ " ErrorCode: " + e.getErrorCode() + " Extended Errorcode: " + e.getErrorCodeExtended());
			notifyCallerOfStateChange(DeviceState.ERROR);
		}
	}

	/**
	 * Call after a EMV card has been removed.
	 */
	@Override
	public final Map<String, String> getConfirmRespData() {
		return EMVDataHandler.emvDataToJSONString(confirmRespData);
	}

	/**
	 * Parse out the EMV status data from the device and send the appropriate
	 * signal. The ordering of which flag to check is ordered based on priority
	 * and when it will occur relative to other flags.
	 * 
	 * 00IS-MAA------------------S------ first after selecting language
	 * 00IS----------------------------- (32 chars)
	 * 00IS-MAA------------------------- (after removed card)
	 * 
	 * @param data
	 */
	public void parseEMVStatus(String data) {
		LOGGER.debug("-> parseEMVStatus() data " + data);

		boolean statusHandled = false;

		// EMV status indicates all the changes since the start of a
		// transaction.
		// Each change doesn't get triggered individually, so we may receive
		// EMV_TRANS_PREP_READY and EMV_ONLINE_PIN in a single EMV status event.
		// In this case, the two states will be generated.
		// This could be handled cleanly with the State Pattern, but that
		// requires substantial change.

		if (!emvTransPrepReady && data.substring(27, 28).equals("S")) {
			// Transaction preparation data ready
			LOGGER.debug("Status indicates transaction preparation response ready");
			notifyCallerOfStateChange(DeviceState.EMV_TRANS_PREP_READY);
			emvTransPrepReady = true;
			statusHandled = true;
		}
		
		if (data.charAt(11) == 'R') {
			LOGGER.debug("Amount confirmation rejected");
			notifyCallerOfStateChange(DeviceState.EMV_AMOUNT_REJECTED);
			
		} else if (data.charAt(3) == 'R') {
			LOGGER.debug("Status indicates card is removed");
			notifyCallerOfStateChange(DeviceState.EMV_CARD_REMOVED);
			
		} else if (data.charAt(5) != '-') {
			// Process completion signal
			final char flag3Value = data.charAt(5); // Flag 3 - Indicates if the EMV process is completed.
			
			if (flag3Value == 'E') { // E = Error or incompletion reason.
				// Flag 16 - Indicates if the transaction is cancelled.
				final char flag16Value = data.charAt(18); 
				// Flag 17 - Indicates if card data unreadable or is of an invalid format.
				final char flag17Value = data.charAt(19); 
				// Flag 18 - Indicates if a card or application block is detected.
				final char flag18Value = data.charAt(20); 
				// Flag 19 - Indicates if an error or incompletion is detected.
				final char flag19Value = data.charAt(21);
				// Flag 21 - Indicates if card is not supported (e.g. Application ID not found)
				final char flag21Value = data.charAt(23);	
				                                    // 
				if (flag17Value == 'I') { // I = Card data is invalid but
					// fallback is allowed.
					LOGGER.warn("Card data is invalid, but fallback is allowed");
					notifyCallerOfStateChange(DeviceState.EMV_INVALID_CARD_DATA);
				} else if (flag18Value == 'B') {
					LOGGER.warn("Card or application is blocked");
					notifyCallerOfStateChange(DeviceState.EMV_CARD_BLOCKED);
				} else if (flag16Value == 'C') {
					LOGGER.warn("Transaction cancelled");
					notifyCallerOfStateChange(DeviceState.EMV_TRANS_CANCELED);
				} else if (flag18Value == 'A') {
					LOGGER.warn("Status indicates application is blocked");
					notifyCallerOfStateChange(DeviceState.EMV_APP_BLOCKED);
				} else if (flag19Value == 'O') {
					LOGGER.warn("Status indicates user interface timeout");
					notifyCallerOfStateChange(DeviceState.EMV_USER_INTERFACE_TIMEOUT);
				} else if (flag21Value == 'N') {
					LOGGER.warn("Status indicates card is not supported");
					notifyCallerOfStateChange(DeviceState.EMV_CARD_NOT_SUPPORTED);

				} else {
					LOGGER.error("Status indicates an error occurred during processing");
					notifyCallerOfStateChange(DeviceState.ERROR);
				}
			} else {
				LOGGER.debug(" ****** Status indicates transaction approval ******** ");

				// Verify completion message is available
				if (data.charAt(17) == 'S') {
					LOGGER.debug("Status indicates confirmation data is ready");
					notifyCallerOfStateChange(flag3Value == 'D' ? DeviceState.EMV_AAC : DeviceState.EMV_TC);
				} else if (data.charAt(17) == 'F') {
					LOGGER.error("Status indicates that confirmation response failed to be sent");
					notifyCallerOfStateChange(DeviceState.ERROR);
				} else {
					LOGGER.debug("Status indicates confirmation response message not ready yet");
				}
			}
		} 
		
		if (data.substring(28, 29).equals("1") && data.substring(29, 30).equals("R")) {
			LOGGER.info("Status indicates online pin requested");
			final Map<String, String> parsedTransPrep = EMVDataHandler.parseEMVDataMessage(transPrepData);
			final String track2 = parsedTransPrep.get(GlobalConstants.TRACK2);

			if (track2 != null) {
				LOGGER.debug(" **** Obtained track 2 ");
			}

			try {
				// Vault acctInfo = creditCardNumber
				final String creditCardNumber = track2.split("D")[0];
				inputData.setCreditCardNumber(creditCardNumber);

				LOGGER.debug("Value of acctInfo");
				notifyCallerOfStateChange(DeviceState.EMV_ONLINE_PIN);
			} catch (IndexOutOfBoundsException oob) {
				LOGGER.error("Received index out of range splitting track2 data for online pin processing");
				try {
					final String dioMsg = EmvTagsConst.EMV_DELIM + "C";
					msr.directIO(IngenicoConst.JPOS_FC_POLL_SMART,
					        new int[] { IngenicoConst.ING_DIO_EMV_TRANSINIT_SUBCOMMAND }, dioMsg);
				} catch (JposException e) {
					LOGGER.error("Error initializing for EMV transaction: \"" + e.getMessage() + "\" Code: "
					        + e.getErrorCode() + " Extended Errorcode: " + e.getErrorCodeExtended());
					notifyCallerOfStateChange(DeviceState.ERROR);
				}
			}
		}

		if (data.substring(15, 16).equals("S")) {
			if (inSecondGen) {
				LOGGER.debug("Status indicates EMV FInal Data in progress");
			} else {
				LOGGER.debug("Status indicates ARQC is ready");
				notifyCallerOfStateChange(DeviceState.EMV_ARQC);
			}
		}
		LOGGER.trace("<- parseEMVStatus");
	}

	@Override
	public final String getARQCData() {
		return arqcData;
	}

	@Override
	public final void setARQCData(final String val) {
		arqcData = arqcData + val;
	}

	@Override
	public final void setConfirmRespData(final String val) {
		confirmRespData = confirmRespData + val;
	}

	@Override
	public final String getTransactionPreparationResponse() {
		return transPrepData;
	}

	@Override
	public final void onlinePinComplete() {
		LOGGER.debug("-> onlinePinComplete() ");
		ingenicoDeviceEvent = TerminalEvent.EMV;
		try {
			msr.directIO(IngenicoConst.JPOS_FC_POLL_SMART, new int[] { IngenicoConst.ING_DIO_EMV_SYNREQ_SUBCOMMAND },
			        EmvTagsConst.EMV_DELIM + "R");
		} catch (JposException e) {
			LOGGER.error("Failed to cancel EMV transaction. Cause: {} ", e.getMessage() 
			+ " ErrorCode: " + e.getErrorCode() + " Extended Errorcode: " + e.getErrorCodeExtended());
			notifyCallerOfStateChange(DeviceState.ERROR);
		}
	}

	@Override
	public final void onlinePinBypass() {
		ingenicoDeviceEvent = TerminalEvent.EMV;
		try {
			msr.directIO(IngenicoConst.JPOS_FC_POLL_SMART, new int[] { IngenicoConst.ING_DIO_EMV_SYNREQ_SUBCOMMAND },
			        EmvTagsConst.EMV_DELIM + "B");
		} catch (JposException e) {
			LOGGER.error("Failed to continue with PIN bypass. Cause: {}, code {}", e.getMessage(), e.getErrorCode()
			        + " Extended Errorcode: " + e.getErrorCodeExtended());
			notifyCallerOfStateChange(DeviceState.ERROR);
		}
	}



	@Override
	public final void reenableSwiper() {
		// There is a problem with Ingenico devices. After a successful card swipe, the device goes into
		// 'auto disable' mode even though 'auto disable' mode is off.
		// This problem is only applicable to JPOS; OPOS seems to be working correctly. To get around this problem, 
		// a timer is created to bring the device back up.
		try {
			// Make sure the device is going down
			if (msr.getDeviceEnabled()) {
				msr.setDeviceEnabled(false);
			}
			startSwiperEnabler();
		} catch (JposException ex) {
			LOGGER.error("Failed to disable MSR: " + ex);
			notifyCallerOfStateChange(DeviceState.ERROR);
		}
	}


	private void stopSwiperEnabler() {
		if (swiperReenablerTimer == null) {
			return;
		}
		swiperReenablerTimer.stop();
		swiperReenablerTimer = null;
	}

	// = =====================================================================

	public void enableMsrDataEvent() {
		LOGGER.debug("-> enableMsrDataEvent()");
		try {
			if (msr != null && !msr.getDataEventEnabled()) {
				msr.setDataEventEnabled(true);
			}
		} catch (JposException e) {
			LOGGER.error("Error encountered re-enabling data event. Code: " + e.getErrorCode() + " Extended code: "
			        + e.getErrorCodeExtended() + " Error Message: " + e.getMessage());
			notifyCallerOfStateChange(DeviceState.ERROR);
		}
	}

	public void reEnableDataEvent() {
		try {
			if (!msr.getDeviceEnabled()) {
				LOGGER.debug("Re-enabling device");
				msr.setDeviceEnabled(true);
			}
			enableMsrDataEvent();

		} catch (JposException e) {
			LOGGER.error("JPOS exception occurred while re-enabling data event. Error Code: " + e.getErrorCode()
			        + " Extended Code: " + e.getErrorCodeExtended() + " Message: " + e.getMessage());
			notifyCallerOfStateChange(DeviceState.ERROR);
		}
	}

	/**
	 * Injects the ARPC data in the terminal. sendARPC()
	 */
	@Override
	public final void sendARPCToTerminal(final ArpcData arpcData) {
		LOGGER.debug("-> sendARPCToTerminal()" + arpcData);
		inputData.updateDataFromArpc(arpcData);

		try {
			final String arpc = ArpcData.buildARPCData(arpcData.getArpcMap());
			LOGGER.debug(" arpc:  " + arpc);
			inSecondGen = true;
			//logMsr();
			msr.directIO(IngenicoConst.JPOS_FC_POLL_SMART, new int[] { IngenicoConst.ING_DIO_EMV_AUTHRESP_SUBCOMMAND },
			        arpc);
		} catch (JposException e) {
			LOGGER.error("Error sending ARPC message to device: " + e.getMessage() + " code: " + e.getErrorCode()
			        + " extended code" + e.getErrorCodeExtended());
			notifyCallerOfStateChange(DeviceState.ERROR);
		}
	}

	/**
	 * EMV process. TEMP Location.
	 * 
	 * @param emvMap
	 * @return
	 */
	public final String getTracksData(Map<String, String> emvMap) {
		final String track1 = emvMap.get(GlobalConstants.TRACK1);
		final String track2 = emvMap.get(GlobalConstants.TRACK2);
		final String track3 = emvMap.get("encryptedtrack");

		inputData.setTracks(track1, track2, track3);

		return (track1 == null ? "" : track1) + "|" + (track2 == null ? "" : track2) + "|"
		        + (track3 == null ? "" : track3);
	}
	
	/**
	 * isEmvEnabled() - emvEnabled
	 */
	public final boolean isDeviceIsEmvEnabled() {
		return terminalInformation.isEMVBitOn();
	}

	@Override
	public final void updateEmvKey(final int value) throws IngenicoDeviceException {
		LOGGER.debug("-> updateEmvKey() new value {}", value);
		try {
			DioConfig.injectEmvKey(msr, value);
		} catch (JposException e) {
			LOGGER.error("updateEmvKey : IngenicoDeviceException: {}", e.getMessage());
			if (e.getErrorCode() == 114 && e.getErrorCodeExtended() == 299) {
				throw new IngenicoDeviceException("Please Remove Card", EntryModeStatusID.CardStaysInSlot.getCode());
			} else {
				throw new IngenicoDeviceException(e.getMessage());
			}
		} catch (IngenicoDeviceException e) {
			LOGGER.error("updateEmvKey IngenicoDeviceException: {}", e.getMessage());
		}
	}
	
	
	@Override
	public final void suspendEmvFlow() {
		LOGGER.debug("-> suspendEmvFlow() new value {}");
		try {
			DioSetGetVar.suspendEmvFlow(msr);
		} catch (IngenicoDeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Display messages on the terminal for iUP250 only
	 */
	public final void displayWarningInTerminal(final EntryModeStatusID errorCode, final String msg) {
		try {
			DioRunFile.displayMessageInTerminal(msr, errorCode, msg);
		} catch (JposException e) {
				//throw new IngenicoDeviceException(DeviceUtils.getErrorMessage("Error" + errorCode, e));
				// TODO 
		} catch (IngenicoDeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	
	/**
	 * 
	 */
	private void logMsr() {
		try {
			final StringBuilder sb = new StringBuilder("MSR:");
			sb.append("State:");
			sb.append(msr.getState());
			sb.append(" AutoDisable:");
			sb.append(msr.getAutoDisable());
			sb.append(" DeviceEnabled:");
			sb.append(msr.getDeviceEnabled());

			sb.append(" Claimed:");
			sb.append(msr.getClaimed());
			
			sb.append(" DataEventEnabled:");
			sb.append(msr.getDataEventEnabled());

			LOGGER.debug(sb.toString());
			
		} catch (JposException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Override
	public final boolean isDoneManualTransaction() {

		return doneManualTransaction;
	}

	@Override
	public final void setDoneManualTransaction(final boolean val) {
		this.doneManualTransaction = val;

	}

	@Override
	public boolean initializationIsComplete() {
 		return myMsr.IsMsrIsOpen();
	}

	@Override
	public String getTerminalSerialNumber() {
		return terminalSerial;
	}

	
	//==================================================================================================================
	// Inner Classes		
	//==================================================================================================================		
	
	
	private void startSwiperEnabler() {
		class SwiperEnablerTimer extends SimpleTimer {
			public SwiperEnablerTimer(long timeout) {
				super(timeout);
			}

			@Override
			public TimerTask getInstance() {
				class SwiperEnablerTimerTask extends TimerTask {
					@Override
					public void run() {
						try {
							if (msr.getDeviceEnabled()) {
								// The divice is still enabled; continue to
								// wait.
								startSwiperEnabler();
							} else {
								// The divice is now disabled; re-enable the
								// device.
								swipeCard();
							}
						} catch (JposException ex) {
							LOGGER.error("Failed to re-enable the swiper: " + ex);
							notifyCallerOfStateChange(DeviceState.ERROR);
						} catch (IngenicoDeviceException e) {
							LOGGER.error("Failed to re-enable swiper: " + e);
							notifyCallerOfStateChange(DeviceState.ERROR);
						}
					}
				}
				return new SwiperEnablerTimerTask();
			}
		}

		stopSwiperEnabler();

		swiperReenablerTimer = new SwiperEnablerTimer(100); // repeat every 100
		// milliseconds
		swiperReenablerTimer.start();
	}

	
	private class VerifyAmountDIOListener implements DirectIOListener {

		public void directIOOccurred(final DirectIOEvent arg0) {
			final int eventNumber = arg0.getEventNumber();
			final int data = arg0.getData();
			LOGGER.debug("-> directIOOccurred(), eventNumber: " + eventNumber);
			if (eventNumber == IngenicoConst.ING_DIO_RUN_FILE) {

				switch (data) {
				case IngenicoConst.ING_DIO_RESPONSE_SUCCESS:
					LOGGER.debug("Verify amount form displayed");
					break;
				case UIAConst.UIA_SBC_ENTER:
					LOGGER.debug("YES button has been pressed");
					notifyCallerOfStateChange(DeviceState.ACCEPTED);
					break;
				case UIAConst.UIA_SBC_CANCEL:
					LOGGER.info("CANCEL or NO button pressed");
					DioSetGetVar.enableSound(msr);
					notifyCallerOfStateChange(DeviceState.CANCELLED_BY_USER);
					break;
				default:
					LOGGER.error("Failed to display amount form. Error message: {}", arg0.getObject().toString());
					DioSetGetVar.enableSound(msr);
					notifyCallerOfStateChange(DeviceState.ERROR);
					break;
				}
			} else if (eventNumber == IngenicoConst.ING_DIO_SETVAR) {
				if (data != IngenicoConst.ING_DIO_RESPONSE_SUCCESS) {
					LOGGER.error("Failed to set variable into device");
					notifyCallerOfStateChange(DeviceState.ERROR);
				} else {
					LOGGER.debug("Variable set successfully into device");
				}
			} else {
				LOGGER.warn("Unhandled direct IO occurred: {}, data {} ", Integer.toString(eventNumber), 
						Integer.toString(data));
			}
		}
	}

	
	/**
	 * This class in exclusively used and tested for the iUP250 device.
	 * 
	 * @author luisa.lamore
	 *
	 */
	private class ZipCodeDIOListener implements DirectIOListener {

		public void directIOOccurred(final DirectIOEvent arg0) {
			final int eventNumber = arg0.getEventNumber();
			final int data = arg0.getData();
			LOGGER.debug("-> directIOOccurred() eventNumber: {} data: {}", eventNumber, data);
			if (eventNumber == IngenicoConst.ING_DIO_RUN_FILE) {

				switch (data) {
				case IngenicoConst.ING_DIO_RESPONSE_SUCCESS:
					LOGGER.debug("Zip form displayed");
					break;
				case UIAConst.UIA_SBC_ENTER:
					LOGGER.debug("YES button pressed");
					notifyCallerOfStateChange(DeviceState.DONE);
					break;
				case 57:
					LOGGER.debug("Unkown");
					notifyCallerOfStateChange(DeviceState.DONE);
					break;
				case UIAConst.UIA_SBC_CANCEL:
					LOGGER.info("Cancel button pressed");
					DioSetGetVar.enableSound(msr);
					notifyCallerOfStateChange(DeviceState.CANCELLED_BY_USER);
					break;
				default:
					LOGGER.error("Failed to display form. Error message: {}", arg0.getObject().toString());
					notifyCallerOfStateChange(DeviceState.ERROR);
					break;
				}
			} else if (eventNumber == IngenicoConst.ING_DIO_SETVAR) {
				if (data != IngenicoConst.ING_DIO_RESPONSE_SUCCESS) {
					LOGGER.error("Failed to set variable to device");
					notifyCallerOfStateChange(DeviceState.ERROR);
				} else {
					LOGGER.debug("Variable set successfully");
				}
			} else {
				LOGGER.warn("Unhandled direct IO event: " + Integer.toString(eventNumber) + " data: "
				        + Integer.toString(data));
			}
		}
	}
	
	
	/**
	 * 
	 * @author TC
	 *
	 */
	private class ManualEntryDataListener implements DataListener, DirectIOListener {

		/**
		 * 0x03 = MSC_POLLCMD_RECV_SOURCE_MANUALENTRY - manual entry The event
		 * status 15475769 happened when the payer enters card number with more
		 * than 16 digits The event status 14426165 happens on AMEX cards with
		 * only 15 digits.
		 */
		@Override
		public void dataOccurred(final DataEvent event) {

			LOGGER.debug("-> dataOccurred() Event status: {}", event.getStatus());
			if (msr == null) {
				return;
			}
			try {
				msr.directIO(IngenicoConst.JPOS_FC_SET_VARIABLE, new int[] { 1 },
				        new String(DirectIOCommands.MANUAL_ENTRY_OFF));
				currentState = ManualEntryState.MANUAL_ENTRY_DONE;
				setCardInformation(EntryModeTypeId.Keyed);
				notifyCallerOfStateChange(DeviceState.MANUAL_ENTRY_DONE);
				msr.setDeviceEnabled(false);
			} catch (JposException e) {
				LOGGER.error("JposException Message: {}", e.getMessage());
				notifyCallerOfStateChange(DeviceState.MANUAL_ENTRY_ERROR);
			}
		}

		/**
		 * Updates the display in the ingenico device by selecting a form based
		 * on the event received. The DirectIOEvent will only be fired if pData
		 * has a value of '1' or '2'. The DirectIOEvent will fire after the user
		 * has performed some type of input (e.g., key presses).
		 * 
		 * Events received here: Event 146 = ING_DIO_RUN_FILE Event 162 =
		 * ING_DIO_GET_UIA_VARIABLE or JPOS_FC_GET_VARIABLE (not used here)
		 * Event 161 = ING_DIO_SETVAR 59 = UIA_SBC_CANCEL 58 = UIA_SBC_ENTER
		 * 
		 * @author TC
		 */
		@Override
		public void directIOOccurred(final DirectIOEvent event) {
			LOGGER.debug("****************************** MSR DirectIO Event ******************************");
			LOGGER.debug("-> directIOOccurred() {}, currentState: {}", event.getEventNumber(), currentState);
			final Integer eventNumber = event.getEventNumber();
			final Integer retData = event.getData();
			final Object retObject = event.getObject();

			LOGGER.debug("MSR :: DirectIO Event :: Event Number  = {} ", eventNumber);
			LOGGER.debug("MSR :: DirectIO Event :: Data          = {} ", retData);
			LOGGER.debug("MSR :: DirectIO Event :: Object        = {} ", retObject);

			byte uiaResult = 0;
			byte[] result = null;
			if (event.getObject() != null) {
				result = (byte[]) event.getObject();
				uiaResult = result[0];
			}

			switch (eventNumber) {

			case 146:
				LOGGER.debug("|-------------------- Run File (0x92 / 146) --------------------|");
				// here when click Enter on the CC number
				if (uiaResult == UIAConst.UIA_SBC_ENTER) {
					sendDirectIOToDevice();
				}
				break;

			case 161:
				LOGGER.debug("|-------------------- Set UIA Variable (0xA1 / 161) --------------------|");
				switch (retData) {
				case 0:
					LOGGER.debug("MSR :: DirectIO Event :: Set UIA Variable :: Success " + currentState);
					// First time here, set the device to Manual Mode
					// Then when the CVV form is omitted
					if (clientConfiguration.isShowCvvForm()) {
						// if manual status is MANUAL_ENTRY_OFF tracks are
						// created ...
						break;
					}
					sendDirectIOToDevice();
					break;
				default:
					LOGGER.debug("MSR :: DirectIO Event :: Set UIA Variable :: ERROR");
					break;
				}
				break;

			case 162:
				LOGGER.debug("|-------------------- Get UIA Variable (0xA2 / 162) --------------------|");
				// Stop here after user enters the CVV in the form
				if (currentState == ManualEntryState.MANUAL_PAN_ENTRY) {
					final String manualEntryStatus = IngenicoUtil.asciiBytesToString(result);
					final String status = IngenicoUtil.extractGetResult(manualEntryStatus);

					if (Integer.parseInt(status) == 0) {
						// here when clicking the "green button" without
						// entering the card number ...
						isManualPanEntryStageSuccessful = false;
						try {
							startManualPanEntryProcess();
						} catch (JposException e) {
							LOGGER.debug("Error occured during starting of manual  entry process");
							notifyCallerOfStateChange(DeviceState.MANUAL_ENTRY_ERROR);
						} catch (IngenicoDeviceException e) {
							LOGGER.debug("Error occured during starting of manual  entry process 2");
							notifyCallerOfStateChange(DeviceState.MANUAL_ENTRY_ERROR);
						}
					} else if (Integer.parseInt(status) == 1) {
						LOGGER.debug("Manual Entry Status succesfully set on device");
						isManualPanEntryStageSuccessful = true;
					}
				}
				break;
			}

			if (uiaResult == UIAConst.UIA_SBC_CANCEL) {
				LOGGER.debug("Cancel button pressed: 59");
				inputData.setEntryModeStatusID(EntryModeStatusID.Cancelled.getCode());
				notifyCallerOfStateChange(DeviceState.MANUAL_ENTRY_CANCELED);

			} else if (uiaResult == 77) {
				// Here when entered only 4 digits in the card number form and
				// click enter
				LOGGER.debug(" ?????????????? ");
			}

		}

		private void sendDirectIOToDevice() {
			LOGGER.trace("-> sendDirectIOToDevice() {}", currentState.name());
			try {
				if (currentState == ManualEntryState.MANUAL_PAN_ENTRY) {
					DioSetGetVar.setManualEntryStatus(msr);
					if (isManualPanEntryStageSuccessful) {
						currentState = DioRunFile.displayManualExpDate(msr);
						notifyCallerOfStateChange(DeviceState.MANUAL_EXP_ENTRY);
					}
				} else if (currentState == ManualEntryState.MANUAL_EXP_ENTRY) {

					if (clientConfiguration.isShowCvvForm()) {
						currentState = DioRunFile.displayManualCVVForm(msr);
						notifyCallerOfStateChange(DeviceState.MANUAL_CVV_ENTRY);
					} else {
						currentState = ManualEntryState.MANUAL_CREATE_TRACKS;
						DioSetGetVar.setManualCVVEntryValue(msr);
						notifyCallerOfStateChange(DeviceState.DIRECT_CVV_ENTRY);
					}
				} else if (currentState == ManualEntryState.MANUAL_ZIPCODE_ENTRY) {
					currentState = DioRunFile.displayManualCVVForm(msr);
					notifyCallerOfStateChange(DeviceState.MANUAL_CVV_ENTRY);
				} else if (currentState == ManualEntryState.MANUAL_CREATE_TRACKS) {
					currentState = ManualEntryState.MANUAL_ENTRY_OFF;
					DioSetGetVar.requestManualEntryCreationOfTracks(msr);
				}
			} catch (JposException | IngenicoDeviceException e) {
				LOGGER.error("Error JposException {} ", e.getMessage());
				notifyCallerOfStateChange(DeviceState.MANUAL_ENTRY_ERROR);
			}

		}
	}
	
	
	/**
	 * 
	 * @author TC
	 *
	 */
	private class SwiperCardDIOListener implements DirectIOListener {

		public void directIOOccurred(DirectIOEvent event) {
			LOGGER.debug("-> directIOOccurred(), eventNumber: {}, data: {}", event.getEventNumber(), event.getData());
			final int eventNumber = event.getEventNumber();
			final int data = event.getData();

			if (eventNumber == IngenicoConst.ING_DIO_RUN_FILE) {

				switch (data) {
				case IngenicoConst.ING_DIO_RESPONSE_SUCCESS:
					LOGGER.debug("Swipe card form is displayed");
					break;

				case UIAConst.UIA_SBC_CANCEL:
					LOGGER.debug("Cancel button is being pressed"); // 59
					notifyCallerOfStateChange(DeviceState.CANCELLED_BY_USER);
					break;

				case UIAConst.UIA_SBC_ENTER:
					LOGGER.debug("Swipe should ignore the ENTER key"); // 58
					break;

				case UIAConst.UIA_SBC_CLEAR:
					LOGGER.debug("Swipe should ignore the CLEAR key"); // 61
					break;

				default:
					notifyCallerOfStateChange(DeviceState.ERROR);

					msr.removeDirectIOListener(lastIOListener);
					lastIOListener = null;
					break;
				}
			} else if (eventNumber == IngenicoConst.ING_DIO_SETVAR) {
				// event 161
				if (data != IngenicoConst.ING_DIO_RESPONSE_SUCCESS) {
					LOGGER.error("Failed to set variable in to device");
					notifyCallerOfStateChange(DeviceState.ERROR);

					msr.removeDirectIOListener(lastIOListener);
					lastIOListener = null;
				} else {
					LOGGER.debug("Variable set successfully to device");
				}
			} else if (eventNumber == IngenicoConst.JPOS_FC_CONFIG) {
				// Event 11
				LOGGER.debug("Terminal configuration updated");
			} else {
				LOGGER.warn("Unhandled direct IO event occurred: {} , event data {} ", Integer.toString(eventNumber), 
				        Integer.toString(data));
			}
		}
	}

	
	/**
	 * Listener used by swipeCard() or smart card.
	 * 
	 *
	 */
	private class MSRDataListener implements DataListener {
		@Override
		public void dataOccurred(final DataEvent arg0) {
			LOGGER.debug("-> dataOccurred(), status: {}", arg0.getStatus());
			if (arg0.getSource().equals(msr)) {
				final int status = arg0.getStatus();
				LOGGER.debug("MSR status {}", status);

				switch (status) {
				case IngenicoConst.JPOS_MSC_POLLCMD_RECV_SOURCE_SMARTCARD:
				case IngenicoConst.JPOS_RC_SMC_INSERTED:
					LOGGER.info("EMV Card inserted");
					notifyCallerOfStateChange(DeviceState.EMV_CARD_INSERTED);
					break;
				default:
					try {
						LOGGER.debug("Value of service code: {}", msr.getServiceCode());
						setCardInformation(EntryModeTypeId.Swiped);
						if (inputData.isEMVCard()) {
							LOGGER.debug("****   EMV_CARD_DETECTED");
							reEnableDataEvent();
							notifyCallerOfStateChange(DeviceState.EMV_CARD_DETECTED);
						} else {
							// TODO check this change for non EMV transactions
							notifyCallerOfStateChange(DeviceState.DONE);
						}
					} catch (JposException e) {
						LOGGER.warn("Failed to retrieve track data. Cause: {}", e.getMessage());
						notifyCallerOfStateChange(DeviceState.ERROR);
						return;
					}
				}
			}
		}
	}

	
	/**
	 * Listener to events thrown when and after the device shows the
	 * "Select Card type" form. A first event will indicate that the form is
	 * displayed to the user. A follow up event will let the code know the
	 * option selected by the Payer.
	 */
	private class CardTypeDIOListener implements DirectIOListener {
		@Override
		public void directIOOccurred(final DirectIOEvent arg0) {

			LOGGER.debug("-> directIOOccurred {}", arg0.getEventNumber());
			if (!arg0.getSource().equals(msr)) {
				LOGGER.warn("CardTypeDIOListener received event from unexpected source. Ignoring");
				return;
			}

			final int eventNumber = arg0.getEventNumber();
			if (eventNumber == IngenicoConst.ING_DIO_RUN_FILE) {
				if (arg0.getData() == IngenicoConst.ING_DIO_RESPONSE_SUCCESS) {
					LOGGER.debug("Select Card type form successfully displayed");
				} else {
					try {
						final byte[] data = (byte[]) arg0.getObject();

						LOGGER.debug("Got key press from card type choice");
						// This corresponds to the Button ID.
						switch (data[0]) {
						case 'C':
							LOGGER.debug("CREDIT button selected");
							notifyCallerOfStateChange(DeviceState.CREDIT_SELECT);
							break;
						case 'D':
							LOGGER.debug("DEBIT button selected");
							notifyCallerOfStateChange(DeviceState.DEBIT_SELECT);
							break;
						case 59:
							LOGGER.debug("Cancel button selected");
							notifyCallerOfStateChange(DeviceState.CANCELLED_BY_USER);
							break;

						default:
							LOGGER.warn("This is not a TCIPA form, please update package: {}", data[0]);
							notifyCallerOfStateChange(DeviceState.ERROR);
						}
					} catch (ClassCastException e) {
						LOGGER.trace("Unable to cast DirectIOEvent.object to byte array");
					}
				}

			}
		}
	}	
	
	
	/**
	 * Listener used by getDebitPin().
	 * 
	 *
	 */
	private class PINDataListener implements DataListener {
		@Override
		public void dataOccurred(final DataEvent arg0) {
			LOGGER.debug("-> dataOccurred(), status: " + arg0.getStatus());
			
			if (arg0.getSource().equals(pinPad)) {
				final IngenicoPINPad src = (IngenicoPINPad) arg0.getSource();
				final int status = arg0.getStatus();
				LOGGER.debug("Value of status: " + status);
				if (status == IngenicoPINPadConst.PPAD_SUCCESS) {
					LOGGER.debug("Pin entry successful");
					try {
						// pinBlock = src.getEncryptedPIN().substring(4);
						final String pin = IngenicoUtil.getPinBlock(src.getEncryptedPIN());
						inputData.setEncryptedPIN(pin);
						final String ksn = src.getAdditionalSecurityInformation();
						inputData.setKsn(ksn);
					} catch (JposException e) {
						LOGGER.error("Error encountered retrieving pin entry info: " + e.getMessage());
						notifyCallerOfStateChange(DeviceState.ERROR);
					}

					notifyCallerOfStateChange(DeviceState.DONE);
				} else if (status == IngenicoPINPadConst.PPAD_CANCEL) {
					LOGGER.debug("User cancelled transaction");
					notifyCallerOfStateChange(DeviceState.CANCELLED_BY_USER);
				} else {
					LOGGER.error("Error encountered during pin entry. Status {}", status);
					notifyCallerOfStateChange(DeviceState.ERROR);
				}

				// Remove the data listener
				src.removeDataListener(lastPINDataListener);
				lastPINDataListener = null;
			} else {
				LOGGER.warn("Received data event from unknown source. Ignoring");
			}
		}
	}
	
		
	private class SerialListener implements TerminalEventListener {

		@Override
		public void handleEvent(LocalActionEvent deviceEvent, Object object) {
			LOGGER.trace("-> SerialListener handleEvent() {}", deviceEvent.state);
			
			String tempString = (String) object;

			switch (deviceEvent.state) {
			case GOT_SERIAL:
				LOGGER.debug("Received event: GOT_SERIAL");
				terminalSerial = tempString;

				if (terminalSerial == null) {
					// This case would be highly unlikely
					LOGGER.error("Error encountered obtaining serial");
				} else {
					notifyCallerOfStateChange(DeviceState.GOT_SERIAL);
				}
				break;

			case ERROR:
				LOGGER.error("Error encountered ");
				break;

			default:
				// This is to ignore SIGCAP_NOT_ENABLED
				break;
			}
		}
	}
	

}

/**
 * Clear Screen = 0x31 = 49 Clear LD = 0x30 = 48 Get Health Stat = 0x0D = 13 Get
 * UIA Var = 0xA2 = 162 Run File = 0x92 = 146 Save File = 0x91 = 145 Set
 * Variable = 0xA1 = 161 Position Txt Cr = 0x36 = 54 Get ChckBox = 0x12 = 18 Get
 * Radio = 0x13 = 19 Send Raw Data = 0x00 = 0 Device Reset = 0x09 = 9 Retrieve
 * File = 0x94 = 148 File Status = 0x95 = 149 Disply Txt At = 0x34 = 52 Device
 * Lght Con = 0xA6 = 166 Display Text = 0x32 = 50 Delete File = 0x93 = 147 TMS
 * Trigger = 0xEF = 239 Delete Rec Cont = 0x35 = 53 Smart Card Msg = 0x67 = 103
 */
