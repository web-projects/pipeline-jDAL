@echo off
cls

echo  Ingenico Device Interface

rem Setup here the location of the TC_HOME
rem =================================
SET TC_HOME=C:\TrustCommerce
rem =================================

:: %HOMEDRIVE% = C:
:: Program Files > ProgramFiles


SET JRE_PATH=%TC_HOME%\devices\ingenico\jre7\bin
SET jDAL=%TC_HOME%\jDAL\tcIngenico.jar

echo JRE 32-bit PATH: %JRE_PATH%

%JRE_PATH%\TCIPAjDAL.exe -jar %jDAL% %1 %2 %3 %4 %5 %6


