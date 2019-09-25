package com.trustcommerce.ipa.dal.device.interfaces;

import com.trustcommerce.ipa.dal.exceptions.FirmwareUploadException;
import com.trustcommerce.ipa.dal.exceptions.FormsPackageUploadException;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;

/**
 * 
 * @author luisa.lamore
 *
 */
public interface FormsPackageProcessor {

    /**
     * Performs the form package upload.
     * When the upload is complete, the device will reboot.
     * @param reboot true if the device should reboot
     * @throws FormsPackageUploadException 
     */
    void updateForms(boolean reboot) throws FormsPackageUploadException;
    
    
    /**
     * For internal use only.
     * @param filename
     * @throws FormsPackageUploadException
     */
    void updateSingleFile(final String filename) throws FormsPackageUploadException;
    
    /**
     * Closes and disables the connection to the MSR.
     */
    void releaseDevice();
    
   
    /**
     * Returns the version of the form packaged captured in the FormVersionDIOListener.
     * @return String with the version, null or empty when the FORMSVER.TXT is missing in the Device.
     */
    String getFormsPackageVersion();
    
    /**
     * Sends an jPOS event requesting the version of the forms package currently installed in the device
     * connected to the workstation.
     */
    void requestFormVersion();
    
    void setEventListener(DeviceEventListener newListener);
    
    void displayUploadForms() throws FirmwareUploadException;
}
