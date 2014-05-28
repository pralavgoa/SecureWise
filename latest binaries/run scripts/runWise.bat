@echo off
rem -------------------------------------------------------------------------
rem Script to run working version of WISE
rem -------------------------------------------------------------------------

rem <-- START OF CONFIGURATION -->

rem WISE_HOME should be pointed at the directory containing the properties file for WISE
set WISE_HOME=C:\_Pralav\workspace\SecureWise\configuration\wise\dev\
set WISE_DIST=C:\_Pralav\workspace\SecureWise\latest binaries
set TOMCAT_HOME=C:\_Pralav\tools\apache-tomcat-7.0.47
set POST_DEPLOY_URL=dev.ctsi.ucla.edu:8080

rem <-- END OF CONFIGURATION -->
set WISE_SSW_HOME=%WISE_HOME%

rmdir /S /Q %TOMCAT_HOME%\webapps\WISE	
del %TOMCAT_HOME%\webapps\WISE.war

rmdir /S /Q %TOMCAT_HOME%\webapps\WiseStudySpaceWizard
del %TOMCAT_HOME%\webapps\WiseStudySpaceWizard.war

cd %TOMCAT_HOME%/bin
call startup.bat

timeout /t 10

call copy "%WISE_DIST%\WiseStudySpaceWizard.war" "%TOMCAT_HOME%\webapps"

timeout /t 20

call copy /y "%WISE_DIST%\WISE.war" "%TOMCAT_HOME%\webapps"

timeout /t 10
call start chrome "%POST_DEPLOY_URL%/WISE/admin" "%POST_DEPLOY_URL%/WiseStudySpaceWizard"