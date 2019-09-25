package com.trustcommerce.ipa.dal.model.emv;

public class EmvRetry {

	
	private final String arqcCounter;
	
	private String arpcTransId;
	
	private boolean reversedRequested;
	

	private final String arqcDate;
	
	private final int retryCounter;
	
	/**
	 * 
	 * @param arqcCounter String
	 * @param arqcDate String
	 * @param retryCounter int
	 */
	public EmvRetry(final String arqcCounter, String arqcDate, int retryCounter) {
		this.arqcCounter = arqcCounter;
		this.arqcDate = arqcDate;
		this.retryCounter = retryCounter;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getArqcCounter() {
		return arqcCounter;
	}
	
	/**
	 * 
	 * @return String
	 */
	public final String getArpcTransId() {
		return arpcTransId;
	}
	
	/**
	 * 
	 * @param arpcTransId String
	 */
	public final void setArpcTransId(final String arpcTransId) {
		this.arpcTransId = arpcTransId;
	}
	
	/**
	 * 
	 * @return boolean
	 */
	public final boolean isReversedRequested() {
		return reversedRequested;
	}
	
	/**
	 * 
	 * @param reversedRequested boolean
	 */
	public final void setReversedRequested(final boolean reversedRequested) {
		this.reversedRequested = reversedRequested;
	}
	
	/**
	 * 
	 * @return String
	 */
	public final String getArqcDate() {
		return arqcDate;
	}


	@Override
	public final String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("EmvRetry [arqcCounter=");
		builder.append(arqcCounter);
		builder.append(", arpcTransId=");
		builder.append(arpcTransId);
		builder.append(", reversedRequested=");
		builder.append(reversedRequested);
		builder.append(", arqcDate=");
		builder.append(arqcDate);
		builder.append(", retryCounter=");
		builder.append(retryCounter);
		builder.append("]");
		return builder.toString();
	}
	
	
 
	
}
