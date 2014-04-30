@echo off
rem -------------------------------------------------------------------------
rem Script to run working version of WISE
rem -------------------------------------------------------------------------

rem WISE_HOME should be pointed at the directory containing the properties file for WISE
set WISE_HOME=C:\_Pralav\workspace\SecureWise\configuration\wise\dev\
set WISE_SSW_HOME=C:\_Pralav\workspace\SecureWise\configuration\wise_study_space_wizard\dev\

set WISE_DIST=C:\_Pralav\workspace\SecureWise\latest binaries
set TOMCAT_HOME=C:\_Pralav\tools\apache-tomcat-7.0.47

copy "%WISE_DIST%\WISE.war" "%TOMCAT_HOME%\webapps"
copy "%WISE_DIST%\WiseStudySpaceWizard.war" "%TOMCAT_HOME%\webapps"

cd %TOMCAT_HOME%/bin
startup.bat