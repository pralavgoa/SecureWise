package edu.ucla.wise.emailscheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.StudySpace;

public class StudySpaceFetcher {

    static Logger LOG = Logger.getLogger(StudySpaceFetcher.class);

    public static List<StudySpace> getStudySpaces(String appName) {

	LOG.info("Fetching study spaces for application " + appName);

	ArrayList<StudySpace> startConfigList = new ArrayList<StudySpace>();

	// start the email sending procedure
	java.util.Date today = new java.util.Date();

	LOG.info("Launching Email Manager on " + today.toString()
		+ " for studies assigned to " + appName + " on this server.");

	try {
	    AdminApplication.checkInit(appName);
	} catch (IOException e1) {
	    LOG.error("AdminInfo could not be initialized", e1);
	}

	StudySpace[] allSpaces;

	try {
	    allSpaces = StudySpace.getAll();

	    LOG.info("Found " + allSpaces.length + " study spaces");

	    for (StudySpace studySpace : allSpaces) {

		startConfigList.add(studySpace);
	    }

	} catch (Exception e) {
	    LOG.info(" --> Emailer err - Can't get study_spaces: "
		    + e.toString());
	    e.printStackTrace(System.out);
	}

	return startConfigList;
    }
}
