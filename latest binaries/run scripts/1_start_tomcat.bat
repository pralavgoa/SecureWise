set WISE_HOME=C:\_Pralav\workspace\SecureWise\configuration\wise\dev\local
set TOMCAT_HOME=C:\_Pralav\tools\apache-tomcat-7.0.47
set POST_DEPLOY_URL=local.dev:8080

set WISE_SSW_HOME=%WISE_HOME%


rmdir /S /Q %TOMCAT_HOME%\webapps\WISE	
del %TOMCAT_HOME%\webapps\WISE.war

rmdir /S /Q %TOMCAT_HOME%\webapps\WiseStudySpaceWizard
del %TOMCAT_HOME%\webapps\WiseStudySpaceWizard.war

cd %TOMCAT_HOME%\bin
call startup.bat
