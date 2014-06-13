@echo off
rem -------------------------------------------------------------------------
rem Script to run working version of WISE
rem -------------------------------------------------------------------------

rem <-- START OF CONFIGURATION -->

rem WISE_HOME should be pointed at the directory containing the properties file for WISE
set WISE_HOME=C:\_Pralav\workspace\SecureWise\configuration\wise\dev\local\
set TOMCAT_HOME=C:\_Pralav\tools\apache-tomcat-7.0.47
set WISE_SOURCE_HOME=C:\_Pralav\workspace\SecureWise
set POST_DEPLOY_URL=local.dev:8080

rem <-- END OF CONFIGURATION -->

set WISE_SSW_HOME=%WISE_HOME%

rem <-- BUILD CODE -->
cd %WISE_SOURCE_HOME%

call ant -buildfile WiseShared\build.xml
call ant -buildfile WiseStudySpaceWizard\build.xml
call ant -buildfile wise\build.xml
rem <-- END OF BUILD CODE -->

call copy /y "WiseStudySpaceWizard\dist\WiseStudySpaceWizard.war" "%WISE_SOURCE_HOME%\latest binaries"
call copy /y "wise\dist\WISE.war" "%WISE_SOURCE_HOME%\latest binaries"