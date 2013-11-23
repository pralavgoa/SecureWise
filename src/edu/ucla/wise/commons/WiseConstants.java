package edu.ucla.wise.commons;

/**
 * This class contains the constants used in the application.
 * @author ssakdeo
 * @version 1.0
 */
public class WiseConstants {
    public static final String ADMIN_APP = "admin";
    public static final String SURVEY_APP = "survey";

    /* This is used by remote servlet loader to initiate the monitoring process. */
    public static final String SURVEY_HEALTH_LOADER = "survey_health";

    public enum STATES {
    	started, completed, incompleter, non_responder, interrupted, 
    	start_reminder_1, start_reminder_2, start_reminder_3, completion_reminder_1, 
    	completion_reminder_2, completion_reminder_3,
    }

    public enum SURVEY_STATUS {
    	OK, FAIL, NOT_AVAIL
    }

    public static final long surveyCheckInterval = 10 * 60 * 1000; // 10 mins
    public static final long surveyUpdateInterval = 5 * 60 * 1000; // 5 mins
    public static final long dbSmtpCheckInterval = 3 * 60 * 1000; // 3 mins

    public static final String NEW_INVITEE_JSP_PAGE = "new_invitee.jsp";
}