package com.trustcommerce.ipa.dal.device.emv;

public class Emv {

    private String loadedFormVersion;
    
    private String emvFormsVersion;
    
    private String loadedFirmwareVersion;
    
    private String emvFirmwareVersion;

    public String getLoadedFormVersion() {
        return loadedFormVersion;
    }

    public void setLoadedFormVersion(String loadedFormVersion) {
        this.loadedFormVersion = loadedFormVersion;
    }

    public String getEmvFormsVersion() {
        return emvFormsVersion;
    }

    public void setEmvFormsVersion(String emvFormsVersion) {
        this.emvFormsVersion = emvFormsVersion;
    }

    public String getLoadedFirmwareVersion() {
        return loadedFirmwareVersion;
    }

    public void setLoadedFirmwareVersion(String loadedFirmwareVersion) {
        this.loadedFirmwareVersion = loadedFirmwareVersion;
    }

    public String getEmvFirmwareVersion() {
        return emvFirmwareVersion;
    }

    public void setEmvFirmwareVersion(String emvFirmwareVersion) {
        this.emvFirmwareVersion = emvFirmwareVersion;
    }
}
