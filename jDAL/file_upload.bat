@echo off
cls

echo  =================================
echo  Ingenico File Upload Manager Tool
echo  =================================

rem Setup here the location of the TC_HOME
rem =================================
SET TC_HOME=C:\TrustCommerce
rem =================================

:: %HOMEDRIVE% = C:
:: Program Files > ProgramFiles


SET JRE_PATH=%TC_HOME%\devices\ingenico\jre7\bin
SET FILE_UPLOAD=%TC_HOME%\jDAL\fileUploader.jar

echo JRE 32-bit PATH: %JRE_PATH%

echo Argument List:
echo arg1 :  1 is forms and 2 is firmware
echo arg2 : {true or false} where true is to show UI
echo arg3 : {filename} from Package.FileName
echo arg4 : {version} from Package.Version

rem example:
rem %JRE_PATH%\TCIPAjDAL.exe -jar %FILE_UPLOAD% 2 true  UGEN021312.OGZ 13.04 %

%JRE_PATH%\TCIPAjDAL.exe -jar %FILE_UPLOAD% %1 %2 %3 %4 %5 


