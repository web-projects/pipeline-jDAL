::----------------------------------------------------------------------------
:: NOTE: EXECUTE %MVN_EXE% -X TO DEBUG ISSUES
::----------------------------------------------------------------------------
@SET MVN_EXE=C:\apache\apache-maven-3.3.9\bin\mvn
@SET ARTID=jar-in-jar-loader
@SET JARFILE=%ARTID%-1.1.jar
@SET JARDIR=%USERPROFILE%\source\repos\IPA42\jDAL\LibsManager\libs\%JARFILE%

@cd libs
call %MVN_EXE% install:install-file -Dfile=%JARDIR% -DgroupId=org.eclipse.jdt.internal -DartifactId=%ARTID% -Dversion=1.1 -Dpackaging=jar
@ cd ..

@SET MVN_EXE=
@SET USER=
@SET JARFILE=
@SET JARDIR=
