package com.trustcommerce.ipa.dal.device.interfaces;

import java.awt.Point;

import com.trustcommerce.ipa.dal.exceptions.IngenicoDeviceException;
import com.trustcommerce.ipa.dal.listeners.DeviceEventListener;



public interface SignatureProcessor {

    // Signature capture
    void captureSignature() throws IngenicoDeviceException;


    void setEventListener(DeviceEventListener newListener);

    
    void releaseDevice();
    
    Point[] getSigPoints();
}
