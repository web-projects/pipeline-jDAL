package com.trustcommerce.ipa.dal.uploader.constants;

public class Version {

	
    /** Icon to be shown on the frame.*/
    public static final String UPLOADER_VERSION = "1.0.10";

    // 4.2.0 1.0.3 added new file to check for firmware upload results
    // 4.2.0 1.0.4 moved messages to the properties file.
    // 1.0.5 Separated report files for firmware and forms. Keep jDAL open until firmware reboot completes
    // 1.0.6 Fixes bug 4156. Creates a new InputUpload class to handle the upload input data
    // 4.2.7 1.0.7 - Task 19918 - interrogate device to determine firmware version. 
    // 4.2.7 1.0.8 - Bug  20463 - update_firmware.bat does not work due to firmware folder structure update.
    //             - Bug  21843 - Require update_firmware.bat for v3 and v4.
    //             - Bug  22265 - Device goes blank when firmware update failed due to missing files in firmware/form folder.
    // 4.2.8 1.0.9 - Added port agnostic uploading path.
    // 4.2.9 1.0.10 - Added IngenicoUSBDrivers 3.15 and COM35 support.
}
