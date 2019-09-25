@echo off
cls

echo ==================================================
echo         JDAL INSTALLATION AND BUILD TOOL
echo ==================================================

set INSTALL_ROOT=C:\TC
set INSTALL_HOME=%INSTALL_ROOT%\IPAApp\jDAL
set TC_HOME=C:\TrustCommerce
set JDAL_HOME=%TC_HOME%\jDAL
set OLDPATH=%PATH%
set path=%path%;C:\apache\apache-maven-3.3.9\bin

:: ***** BUILD TARGET BRANCH *****

:: Remove all files from jDAL subdirectory
IF EXIST %TC_HOME%\jDAL (
  pushd %TC_HOME%\jDAL
  rd /q /s . 2> NUL
  popd
)

:: CHECK FOR TARGET DIRECTORY
IF NOT EXIST %INSTALL_HOME%\NUL (
  MKDIR %INSTALL_HOME%
)

:: COPY FILES TO TARGET FOLDER
echo.
echo COPYING jDAL FILES TO BUILD DIRECTORY...

xcopy %cd% %INSTALL_HOME% /q /r /s /y

:: COPY jDAL SUPPORT FILES
:COPY_FILES
echo.
echo COPYING jDAL FILES TO TRUSTCOMMERCE DIRECTORY...
if NOT exist %JDAL_HOME%\config mkdir %JDAL_HOME\config
if NOT exist %JDAL_HOME%\Internal mkdir %JDAL_HOME\Internal
if NOT exist %JDAL_HOME%\jpos\res mkdir %JDAL_HOME%\jpos\res
if NOT exist %JDAL_HOME%\resources mkdir %JDAL_HOME%\resources
xcopy %INSTALL_HOME%\jDAL %JDAL_HOME% /y /e 

:BUILD_JARS
C:
:: tcIngenico.jar
echo.
echo Building jDAL main artifact ...
cd %INSTALL_HOME%\ParentPom

call mvn clean install

echo .
if exist %INSTALL_HOME%\MainManager\target\tcIngenico-1.0.1-SNAPSHOT-jar-with-dependencies.jar (
  echo Copying jDAL artifact to %JDAL_HOME% folder !!
  echo.
  copy /Y %INSTALL_HOME%\MainManager\target\tcIngenico-1.0.1-SNAPSHOT-jar-with-dependencies.jar %JDAL_HOME%\tcIngenico.jar
)

:: fileUploader.jar
echo.
echo.
echo Building the File Upload Manager artifact ...
cd %INSTALL_HOME%\ParentPomUploader
call mvn clean install

echo.
if exist %INSTALL_HOME%\FileUploadManager\target\fileUploader-1.0.1-SNAPSHOT-jar-with-dependencies.jar (
  echo Copying FileUploader artifact to %JDAL_HOME% folder !!
  copy /Y %INSTALL_HOME%\FileUploadManager\target\fileUploader-1.0.1-SNAPSHOT-jar-with-dependencies.jar %JDAL_HOME%\fileUploader.jar
)

cd %INSTALL_HOME%

echo Build Process Complete !!

:DONE
if NOT "%OLDPATH%" == "" set path=%OLDPATH%
set OLDPATH=
