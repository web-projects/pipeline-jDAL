@echo off
cls

echo  ============================================
echo  TrustCommerce File Upload Manager Tool
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

echo TrustCommerce Form Package will take approximately 20 seconds ..

START /W %JRE_PATH%\TCIPAjDAL.exe -jar %FILE_UPLOAD% 1 false  

echo ****  The Form Package upload upload process has completed  ****
echo ****  The terminal reboot in progress ... ****

echo Please re-start TCIPA after the reboot completes

