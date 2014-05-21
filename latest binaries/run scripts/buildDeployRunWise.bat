@echo off
rem -------------------------------------------------------------------------
rem Script to run working version of WISE
rem -------------------------------------------------------------------------

rem WISE_HOME should be pointed at the directory containing the properties file for WISE
set WISE_HOME=C:\_Pralav\workspace\SecureWise\configuration\wise\dev\
set WISE_SSW_HOME=%WISE_HOME%
set TOMCAT_HOME=C:\_Pralav\tools\apache-tomcat-7.0.47
set WISE_SOURCE_HOME=C:\_Pralav\workspace\SecureWise
cd %WISE_SOURCE_HOME%

rem run ant for WiseShared
call ant -buildfile WiseShared\build.xml
rem run ant for WiseStudySpaceWizard
call ant -buildfile WiseStudySpaceWizard\build.xml
rem run ant for Wise
call ant -buildfile wise\build.xml

call copy /y "WiseStudySpaceWizard\dist\WiseStudySpaceWizard.war" "%WISE_SOURCE_HOME%\latest binaries"
call copy /y "wise\dist\WISE.war" "%WISE_SOURCE_HOME%\latest binaries"

rmdir /S /Q %TOMCAT_HOME%\webapps\WISE	
del %TOMCAT_HOME%\webapps\WISE.war

rmdir /S /Q %TOMCAT_HOME%\webapps\WiseStudySpaceWizard
del %TOMCAT_HOME%\webapps\WiseStudySpaceWizard.war

cd %TOMCAT_HOME%/bin
call startup.bat

timeout /t 10

call copy /y "%WISE_SOURCE_HOME%\WiseStudySpaceWizard\dist\WiseStudySpaceWizard.war" "%TOMCAT_HOME%\webapps"

timeout /t 20

call copy /y "%WISE_SOURCE_HOME%\wise\dist\WISE.war" "%TOMCAT_HOME%\webapps"

timeout /t 10
call start chrome "localhost:8080/WISE/admin" "localhost:8080/WiseStudySpaceWizard"