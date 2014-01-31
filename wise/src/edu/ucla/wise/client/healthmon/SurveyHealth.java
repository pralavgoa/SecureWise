/**
 * 
 */
package edu.ucla.wise.client.healthmon;

import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.WiseConstants;

/**
 * This class is a single thread per survey application which runs and reports
 * the health of the survey server in the database. The goal is to make the
 * admin application to read "survey_health" in database and display the health
 * of survey application.
 * 
 * @author ssakdeo
 * @version 1.0 
 * 
 */
public class SurveyHealth implements Runnable {

    public StudySpace studySpace;
    public static Set<String> monitorStudies = new HashSet<String>();
    static Logger log = Logger.getLogger(SurveyHealth.class);

    private SurveyHealth(StudySpace study) {
    	this.studySpace = study;
    }

    /**
     * This function will start monitoring for "this" survey if it has already
     * been not started. If the monitoring has already started then this
     * function will just return;
     * 
     * @param survey
     */
    public static synchronized void monitor(StudySpace study) {
		if (!monitorStudies.contains(study.studyName)) {
		    monitorStudies.add(study.studyName);
		    Thread t = new Thread(new SurveyHealth(study));
		    t.start();
		}
    }

    @Override
    public synchronized void run() {
		while (true) {
		    studySpace.db.updateSurveyHealthStatus(studySpace.studyName);
		    try {
		    	Thread.sleep(WiseConstants.surveyUpdateInterval);
		    } catch (InterruptedException e) {
		    	log.error("WISE ADMIN - SURVEY HEALTH:" + e.toString(), e);
		    }
		}
    }
}