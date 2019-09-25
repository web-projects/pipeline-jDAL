@echo off
cls

echo  ============================================
echo  TrustCommerce Firmware Upload Manager Tool
echo  * Compatible with FileUploadManager v. 1.0.8
echo  ============================================

rem Setup here the location of the TC_HOME
rem =================================
SET TC_HOME=C:\TrustCommerce
rem =================================

:: %HOMEDRIVE% = C:
:: Program Files > ProgramFiles

SET JRE_PATH=%TC_HOME%\devices\ingenico\jre7\bin
SET FILE_UPLOAD=%TC_HOME%\jDAL\fileUploader.jar

rem delete temporary files
if exist %TC_HOME%\logs\fileUploaderResults.txt (
   echo clean up old files ...
   del %TC_HOME%\logs\fileUploaderResults.txt
)
if exist %TC_HOME%\logs\upload_firmware_error.txt (
   del %TC_HOME%\logs\upload_firmware_error.txt
)

echo Preparing upload environment ...

rem Find if another TrustCommerce JVM is running ...
tasklist | find /i "TCIPAjDAL" >nul 2>&1
IF ERRORLEVEL 1 (
  rem not found
) ELSE (
  echo Another application is preventing the file upload from completing ....
  taskkill /IM TCIPAjDAL.exe /T /F
)

echo Ingenico Firmware %1 upload will start soon ... please wait ..

 
START /W %JRE_PATH%\TCIPAjDAL.exe -jar %FILE_UPLOAD% 7 true

if exist %TC_HOME%\logs\upload_firmware_error.txt (
   echo Problems encounter while uploading the firmware ....  
   echo Please resolve issues and re-launch upload_firmware.bat  ....
   goto STAY
) else (
   ECHO Initial firmware process has been completed, do not unplug your device ....  
   goto CONTINUE
)

:LOOP

tasklist | find /i "TCIPAjDAL" >nul 2>&1
IF ERRORLEVEL 1 (
  GOTO CONTINUE
) ELSE (
  ECHO Firmware Upload in progress ....
  Timeout /T 5 /Nobreak   
  GOTO LOOP
)

:CONTINUE
echo Device is rebooting ... After reboot please wait for the final update process to complete ...
Timeout /T 3 /Nobreak  
echo Final Upload in progress, your device will reboot again in 10 seconds ...
rem reinstall forms
START /W %JRE_PATH%\TCIPAjDAL.exe -jar %FILE_UPLOAD% 9
GOTO DONE 

:DONE
echo ****  The Firmware upload process is now completed  ****
echo Open C:\TrustCommerce\logs\fileUploaderResults.txt to verify upload results
echo Please re-start TCIPA when ready to process transactions

:OUT
::: exit

:STAY
