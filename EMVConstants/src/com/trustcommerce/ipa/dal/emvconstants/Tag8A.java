package com.trustcommerce.ipa.dal.emvconstants;


/**
 * 
 * @author luisa.lamore
 *
 */
public enum Tag8A {
	/** 51, 54. */
	Unknown(""),
	/** Online approved. */
	OnlineApproved("00"),
	/** Online Declined . */
	OnlineDeclined("05"),
	/** offline approved. */
	OfflineApproved("Y1"),
	/**Unable to go online - offline approved. */
	UnableOnlineOfflineApproved("Y3"),
	/** Unable to go online - offline declined. */
	UnableOnlineOfflineDeclined("Z3"),
	/** Offline declined. */
	OfflineDeclined("Z1");
	

	private String code;
	
	Tag8A(String code) {
		this.code = code;
	}
	
	public String getEmvCode() {
		return code;
	}
	
	public static Tag8A getTag8A(final String value) {
		if (value.equals(OnlineApproved.getEmvCode())) {
			return OnlineApproved;
		} else if (value.equals(OnlineDeclined.getEmvCode())) {
			return OnlineDeclined;
		} else if (value.equals(UnableOnlineOfflineApproved.getEmvCode())) {
			return UnableOnlineOfflineApproved;
		} else if (value.equals(OfflineApproved.getEmvCode())) {
			return OfflineApproved;
		} else if (value.equals(UnableOnlineOfflineDeclined.getEmvCode())) {
			return UnableOnlineOfflineDeclined;
		} else if (value.equals(OfflineDeclined.getEmvCode())) {
			return OfflineDeclined;
		} else {
			// If we get here, there is a bug somewhere ...
			return Unknown;
		}
	}
}
