package com.trustcommerce.ipa.dal.device.forms;


import com.trustcommerce.ipa.dal.constants.device.TerminalModel;


/**
 * 
 * @author luisa.lamore
 *
 */
public class Form {

	private String formName;
	
	private TerminalModel deviceModel;
	
	private boolean isEmv;
	

	
	public Form(TerminalModel model) {
		this.deviceModel = model;

	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public TerminalModel getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(TerminalModel deviceModel) {
		this.deviceModel = deviceModel;
	}

	public boolean isEmv() {
		return isEmv;
	}

	public void setEmv(boolean isEmv) {
		this.isEmv = isEmv;
	}
	

}
