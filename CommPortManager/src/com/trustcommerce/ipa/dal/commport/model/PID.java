package com.trustcommerce.ipa.dal.commport.model;


/**
 * The following PID are used to find what device is connected to the work station.
 * @author tc
 *
 */
public enum PID {
	
	iSC250("0062"),
	iSC480("0061"),
	iPP350("0060"),
	iPP320("0059"),
	iUP250("0057");
	
	private String pid;
	
	PID(final String val) {
		pid = val;
	}
	
	public String getPid() {
		return pid;
	}
}
