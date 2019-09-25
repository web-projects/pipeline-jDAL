package com.trustcommerce.ipa.dal.constants.device;

public enum EntryModeStatusID {
	/** User cancelled. */
	Success(0),
	/** Bad data. */
	BadData(103),
	/** Transaction called by user. */
	Cancelled(150),
	/** The MSR can not read a card. */
	CardNotRead(155),
	/** MSR, pin pad or signature reached a timeout. */
	Timeout(160),
	/** Generic error. */
	Error(165),
	/** Currently use for debit transactions. */
	Unsupported(170),
	/** EMV process was cancelled. Still pending*/
	CancelledByCard(171),
	/** Error encountered during pin entry.*/
	PinEntryError(178),
	/** Pin try limit exceeded.*/
	PinEntryExceed(179),
	/** Attached device serial does not match with db serial. */
	SerialNumberMismatch(185),
	/** File Upload was successful. */
	Reboot(801),
	/** The device is not connected/plugged in to the workstation. */
	BadConnection(175),
	// TODO
	EMVCancelled(501),
	/**
	 * Port errorty trying to deploy package. The device is running on the wrong
	 * port.
	 */
	ErrorOnPort(808),
	/** Fail To Process card or card blocked or can not connect to IPA service. */
	ProcessorCommunicationFailure(406),
	/** Server cannot connect to TCLink. */
	CantConnect(400),
	/** Unable to resolve DNS Host Name. */
	DNSValue(401),
	/** Communication lost with processor during the payment.. */
	LinkFailure(402),
	/** TCLink cannot be called  .. */
	TCLinkCommunicationFailure(403),
	/** TCLink is down  application cannot reach vault.trustcommerce.com .. */
	TCLinkIsDown(408),
	/** Card error. */
	Decline(205),
	/** Suspicious of fraud. */
	EMVCardBlocked(166),
	/** Request to reboot jdal. */
	RequestJdalReboot(811), 
	/** Request to shutdown jdal.*/ 
	RequestJdalShutDown(911),
	/** Internal jDAL error.*/ 
	MSRIsCloseOrNull(666),
	/** Use for any type of error while updating the forms packaging. */
	FormsPackageError(802),
	/** Internal jDAL error: User left card in slot.*/
	CardStaysInSlot(8),
	/** Use for any type of error while updating the firmware packaging. */
	FirmwarePackageError(803);

	private int code;

	EntryModeStatusID(final int val) {
		code = val;
	}

	public int getCode() {
		return code;
	}
	
	public String getCodeAsString() {
		return Integer.toString(code);
	}

}
