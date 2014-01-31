package edu.ucla.wise.admin.healthmon;

import java.util.Date;

import edu.ucla.wise.commons.DataBank;
import edu.ucla.wise.commons.WiseConstants;
import edu.ucla.wise.commons.WiseConstants.SURVEY_STATUS;

/**
 * HealthStatus is a class used to update the status of 
 * email database and survey on wise admin page.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class HealthStatus {

    private static HealthStatus healthStatus = null;

    private HealthStatus() {
    }

    public static synchronized HealthStatus getInstance() {
		if (healthStatus == null) {
		    healthStatus = new HealthStatus();
		}
		return healthStatus;
    }

    public boolean dbIsAlive;
    public Date dbLastUpdatedTime;
    public boolean smtpIsAlive;
    public Date smtpLastUpdatedTime;
    
    /**
     * Returns status of DB.
     * 
     * @returns dbIsAlive status of DB if it is alive or not.
     */
    public boolean isDbIsAlive() {
    	return dbIsAlive;
    }

    /**
     * Sets status of DB.
     * 
     * @param dbIsAlive DB status to set.
     */
    public void setDbIsAlive(boolean dbIsAlive) {
    	this.dbIsAlive = dbIsAlive;
    }

    /**
     * Returns last updated time of DB.
     * 
     * @returns dbLastUpdatedTime last updated time of DB.
     */
    public Date getDbLastUpdatedTime() {
    	return dbLastUpdatedTime;
    }

    /**
     * Sets last updated time of DB.
     * 
     * @param dbLastUpdatedTime last updated time of DB.
     */
    public void setDbLastUpdatedTime(Date dbLastUpdatedTime) {
    	this.dbLastUpdatedTime = dbLastUpdatedTime;
    }

    /**
     * Returns status of smtp.
     * 
     * @returns smtpIsAlive status of smtp.
     */
    public boolean isSmtpIsAlive() {
    	return smtpIsAlive;
    }
    
    /**
     * Sets status of smtp.
     * 
     * @param smtpIsAlive status of smtp to set.
     */
    public void setSmtpIsAlive(boolean smtpIsAlive) {
    	this.smtpIsAlive = smtpIsAlive;
    }
    
    /**
     * Returns last updated time of smtp.
     * 
     * @returns smtpLastUpdatedTime last updated time of smtp.
     */
    public Date getSmtpLastUpdatedTime() {
    	return smtpLastUpdatedTime;
    }

    /**
     * Sets last updated time of smtp.
     * 
     * @param smtpLastUpdatedTime last updated time of smtp.
     */
    public void setSmtpLastUpdatedTime(Date smtpLastUpdatedTime) {
    	this.smtpLastUpdatedTime = smtpLastUpdatedTime;
    }
    
    /**
     * Returns the status of the survey based on the last updated time of the survey.
     *  
     * @param String	studyName Study space for which status has to be found.
     * @param DataBank	db Databank to get the last updated time of the survey.
     * 
     * @return SURVEY_STATUS status of the survey.
     */
    public SURVEY_STATUS isSurveyAlive(String studyName, DataBank db) {
		long lastUpdateTime = db.lastSurveyHealthUpdateTime(studyName), currentTimeMillis = System
				.currentTimeMillis();
		
		/* if the difference is more than 10min */
		if (lastUpdateTime == 0) {
		    return SURVEY_STATUS.NOT_AVAIL;
		}
		return (currentTimeMillis - lastUpdateTime) < WiseConstants.surveyCheckInterval ? SURVEY_STATUS.OK
				: SURVEY_STATUS.FAIL;
    }

    /**
     * Sets the parameters with respect to smtp
     * 
     * @param health Boolean value if the smtp is live or dead
     * @param updateTime last updated time when the smtp status is checked.
     */
    public void updateSmtp(boolean health, Date updateTime) {
		this.smtpIsAlive = health;
		this.smtpLastUpdatedTime = updateTime;
    }

    /**
     * Sets the parameters with respect to DB
     * 
     * @param health Boolean value if the DB is live or dead
     * @param updateTime last updated time when the DB status is checked.
     */
    public void updateDb(boolean health, Date updateTime) {
		this.dbIsAlive = health;
		this.dbLastUpdatedTime = updateTime;
    }

}
