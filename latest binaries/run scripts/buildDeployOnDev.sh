#!/bin/sh
# -------------------------------------------------------------------------
# Script to run working version of WISE
# -------------------------------------------------------------------------

# <-- START OF CONFIGURATION -->

# WISE_HOME should be pointed at the directory containing the properties file for WISE
export WISE_HOME=/c/_Pralav/workspace/SecureWise/configuration/wise/dev/local/
export TOMCAT_HOME=/c/_Pralav/tools/apache-tomcat-7.0.47
export WISE_SOURCE_HOME=/c/_Pralav/workspace/SecureWise

# <-- END OF CONFIGURATION -->

export WISE_SSW_HOME=$WISE_HOME

# <-- BUILD CODE -->
cd $WISE_SOURCE_HOME

ant -buildfile WiseShared/build.xml
ant -buildfile WiseStudySpaceWizard/build.xml
ant -buildfile wise/build.xml
# <-- END OF BUILD CODE -->

cp -f "WiseStudySpaceWizard/dist/WiseStudySpaceWizard.war" "$WISE_SOURCE_HOME/latest binaries"
cp -f "wise/dist/WISE.war" "$WISE_SOURCE_HOME/latest binaries"

cd "latest binaries"

scp WiseStudySpaceWizard.war pdesai@dev.ctsi.ucla.edu:/opt/apache-tomcat/apache-tomcat-7.0.53/webapps/ 

sleep 20

scp WISE.war pdesai@dev.ctsi.ucla.edu:/opt/apache-tomcat/apache-tomcat-7.0.53/webapps/