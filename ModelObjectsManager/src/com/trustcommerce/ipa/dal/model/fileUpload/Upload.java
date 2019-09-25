package com.trustcommerce.ipa.dal.model.fileUpload;

import org.codehaus.jackson.annotate.JsonProperty;

public class Upload {

 
	/** FK to the manufacturer. If > 0 apply this package to all devices or apps serving this manufacturer.*/
	private int manufacturerID;
	/** The type of package being deployed (1=forms, 2=firmware, others?).*/
	private int packageTypeID;
	/** The version of this software being deployed.*/
	private String version;
	/** The File name of the package to be deployed.*/
	private String fileName;
	/** The common name or model used to describe 1 device from another when both devices are 
	 * from the same manufacturer.*/
	public String modelNumber;
	
	public Upload() {
		super();
		// TODO Auto-generated constructor stub
	}

	
	public int getManufacturerID() {
		return manufacturerID;
	}
	
	@JsonProperty("ManufacturerID")
	public void setManufacturerID(final int manufacturerID) {
		this.manufacturerID = manufacturerID;
	}
	
	public int getPackageTypeID() {
		return packageTypeID;
	}
	
	@JsonProperty("PackageTypeID")
	public void setPackageTypeID(final int packageTypeID) {
		this.packageTypeID = packageTypeID;
	}
	
	public String getVersion() {
		return version;
	}
	
	@JsonProperty("Version")
	public void setVersion(final String version) {
		this.version = version;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	@JsonProperty("FileName")
	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}
	
	public String getModelNumber() {
		return modelNumber;
	}
	
	@JsonProperty("ModelNumber")
	public void setModelNumber(final String modelNumber) {
		this.modelNumber = modelNumber;
	}

	
	@Override
	public final String toString() {
		return "{"
				+ "\"ManufacturerID\" : \"" + manufacturerID + "\","
	            + "\"PackageTypeID\" : \"" + packageTypeID + "\","
				+ "\"Version\" : \"" + version + "\","
		        + "\"FileName\" : \"" + fileName + "\","
				+ "\"ModelNumber\" : \"" + modelNumber + "\""
		        + "}";
	}
}
