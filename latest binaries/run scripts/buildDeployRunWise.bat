@echo off
rem -------------------------------------------------------------------------
rem Script to run working version of WISE
rem -------------------------------------------------------------------------

rem WISE_HOME should be pointed at the directory containing the properties file for WISE
set WISE_HOME=C:\_Pralav\workspace\SecureWise\configuration\wise\dev\
set WISE_SSW_HOME=C:\_Pralav\workspace\SecureWise\configuration\wise_study_space_wizard\dev\
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

call copy /y "WiseStudySpaceWizard\dist\WiseStudySpaceWizard.war" "%TOMCAT_HOME%\webapps"
call copy /y "wise\dist\WISE.war" "%TOMCAT_HOME%\webapps"

cd %TOMCAT_HOME%/bin
startup.bat