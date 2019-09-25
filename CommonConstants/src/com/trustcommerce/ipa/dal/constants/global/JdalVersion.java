package com.trustcommerce.ipa.dal.constants.global;

public class JdalVersion {
	
	public final static String JDAL_VERSION = "4.2.9";
	public final static String JDAL_INTERNAL_VERSION = "4.2.9";
	
	// 4.2.9 Changes for IngenicoUSBDrivers version 3.15 to allow COM35 as the default port.
	// 4.2.8 Bug 3657: jDAL UI version is wrong when connecting to ingenico device.
	// 4.2.7 Task 17981: Rebuild jDAL with Ingenico 19.6 firmware support.
	// 4.2.8 Task 5288 : Rebuild jDAL from current IPA.POS branch, changing version number to 4.2.1 
    // 4.2.7 Bug 4760: iPP320 : Intermittently jDAL Does not Display The Postboot During Initializartion
    //        not a fix. Just added a log in the postboot method to get more info
	// 4.2.6 Bug 4797: JDAL 4.2 In Dev Mode Java Logs Capture DFS terminal status 
	// 4.2.6 Bug 4789: When Devices are swapped and send transaction request JDAL display "multiple devices connected..."
	// 4.2.6 Bug 4657: 2/1/17 Gui visible when transactions are processed in iUP250
	// 4.2.5 Task 4738: 1/30/2017 Determine during Terminal initialization if the PAN encryption key is missing.
	// 4.2.4 Bug 4657: 1/17/2017 Retry and Fallback bugs in iUP250
	// 
	
	// Released on 1/11/2017
	// 4.2.2 Bug 4635:iUP250 - On errors add text "Please Remove Card"
	// 4.2.2 01/11/2017 Bug-4563: Missing Credit card number in Offline Decline transactions
	// 4.2.2 Bug-4626: Added null check ups when releasing MSR
	// 4.2.2 Bug-4646: Change in text message
	
	// 4.2.0 b-4065 (11/16/2016) b-4248 ( 11/28/2016) b-4393(12/1/2016)
	// T-3890 (iPP320 L&F)
	// 4.2.1 b-4444 b-4452 b-4542 b-4532 b-4518 b-4481 b-4469 b-4543(12/19)
	// 4.2.0.25593 b- 4486
}
