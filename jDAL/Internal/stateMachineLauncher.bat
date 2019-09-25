@echo off
cls

echo  Ingenico State Machine

rem Setup here the location of the TC_HOME
rem =================================
SET TC_HOME=C:\TrustCommerce
rem =================================

:: %HOMEDRIVE% = C:
:: Program Files > ProgramFiles


SET JRE_PATH=%TC_HOME%\devices\ingenico\jre7\bin
SET APP=%TC_HOME%\jDAL\StateMachine.jar

echo JRE 32-bit PATH: %JRE_PATH%

%JRE_PATH%\TCIPAjDAL.exe -jar %APP%


