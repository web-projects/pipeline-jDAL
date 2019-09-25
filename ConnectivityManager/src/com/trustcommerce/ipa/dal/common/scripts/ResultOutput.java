package com.trustcommerce.ipa.dal.common.scripts;

public class ResultOutput {

	private String strResult;
	private int exitResult;


	public String getStrResult() {
		return strResult;
	}


	public void setStrResult(String val) {
		strResult = val.replace("\n", "");
	}


	public int getExitResult() {
		return exitResult;
	}


	public void setExitResult(int exitResult) {
		this.exitResult = exitResult;
	}
}
