package com.trustcommerce.ipa.dal.upload;

import com.trustcommerce.ipa.dal.model.fileUpload.Upload;
/** */
public class FileUploadUtil {
	
	
	/**
	 * 1 for form, 2 for firmware.
	 * @param packageTypeId int 
	 * @return Upload
	 */
	public static Upload getFormsUploadObject(final int packageTypeId) {
		
		
		final Upload p = new Upload();
		
		p.setManufacturerID(1);
		p.setFileName("");
		
		p.setPackageTypeID(packageTypeId);
		//p.setPackageTypeID(1);
		p.setVersion("tc_1.0.2_EMV");
		p.setModelNumber("iSC250");
		return p;
	}
	
	
}
