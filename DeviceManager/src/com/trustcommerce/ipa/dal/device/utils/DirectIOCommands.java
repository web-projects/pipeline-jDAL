package com.trustcommerce.ipa.dal.device.utils;

public class DirectIOCommands {

	/** Can only be set during a Manual Entry session. */
	
	public static final String DIRECT_SETTING_CVV2 = "CVV2=000";
	/** Beep on key press 0=off. */
	public static final String KEYBEEP_ZERO = "KEYBEEP=0";
	
	public static final String KEYBEEP_ON = "KEYBEEP=1";
	
	public static final String MANUAL_ENTRY_CVV2 = "MANUALENTRY=CVV2";

	public static final String MANUAL_ENTRY_PAN = "MANUALENTRY=PAN";
	
	public static final String MANUAL_ENTRY_CREATE_EXP = "MANUALENTRY=EXP";
	
	public static final String MANUAL_ENTRY_STATUS = "MANUALENTRYSTATUS";
	/** Can only be set during a Manual Entry session. Should call only after PAN, EXP and CVV2 are collected. */
	public static final String MANUAL_ENTRY_CREATE_TRACKS = "MANUALENTRY=CREATETRACKS";
	/** Begins the manual entry session. */
	public static final String MANUAL_ENTRY_ON = "MANUALENTRY=ON";
	/** this action disables the MSR device. */
	public static final String MANUAL_ENTRY_OFF = "MANUALENTRY=OFF";

	
}
