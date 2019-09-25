package com.trustcommerce.ipa.dal.device.interfaces;

import com.trustcommerce.ipa.dal.exceptions.FirmwareUploadException;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;

/**
 *     
 * @author luisa.lamore
 *
 */
public interface FirmwareProcessor {

    /**
     * 
     * @param formFile
     */
    void updateFirmware() throws FirmwareUploadException;
    
    String getFirmwareVersion();
    
    void setLoadedFormVersion(final String val);

	void updateFirmware2(boolean reboot) throws FirmwareUploadException;
    
    void setEventListener(DeviceEventListener newListener);
    
    void displayUploadForms() throws FirmwareUploadException;
}
