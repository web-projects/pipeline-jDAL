package com.trustcommerce.ipa.dal.device.interfaces;


import com.trustcommerce.ipa.dal.exceptions.FileUploadException;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;


/**
 * 
 * @author luisa.lamore
 *
 */
public interface SingleUploadProcessor {

     
    void updateSingleFile() throws FileUploadException;
    
    void uploadFile() throws FileUploadException;
    
    void setEventListener(DeviceEventListener newListener);
}
