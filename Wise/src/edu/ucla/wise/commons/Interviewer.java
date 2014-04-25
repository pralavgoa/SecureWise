package edu.ucla.wise.commons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * This class represents an interviewer object
 * 
 * @author Douglas Bell
 * @version 1.0
 */
public class Interviewer {
    public static final Logger LOGGER = Logger.getLogger(Interviewer.class);
    /** Instance Variables */
    public StudySpace studySpace;

    public String id;
    public String userName;
    public String email;
    public String firstName;
    public String lastName;
    public String salutation;
    public String loginTime;

    public String interviewSessionId;
    public String interviewAssignId;

    /**
     * Getter method
     * 
     * @return StudySpace
     */
    public StudySpace getStudySpace() {
	return this.studySpace;
    }

    /**
     * Setter method
     * 
     * @param StudySpace
     */
    public void setStudySpace(StudySpace studySpace) {
	this.studySpace = studySpace;
    }

    /**
     * Getter method
     * 
     * @return String id
     */
    public String getId() {
	return this.id;
    }

    /**
     * Setter method
     * 
     * @param id
     */
    public void setId(String id) {
	this.id = id;
    }

    /**
     * Getter method
     * 
     * @return String userName
     */
    public String getUserName() {
	return this.userName;
    }

    /**
     * Setter method
     * 
     * @param userName
     */
    public void setUserName(String userName) {
	this.userName = userName;
    }

    /**
     * Getter method
     * 
     * @return String email
     */
    public String getEmail() {
	return this.email;
    }

    /**
     * Setter method
     * 
     * @param email
     */
    public void setEmail(String email) {
	this.email = email;
    }

    /**
     * Getter method
     * 
     * @return String firstName
     */
    public String getFirstName() {
	return this.firstName;
    }

    /**
     * Setter method
     * 
     * @param firstName
     */
    public void setFirstName(String firstName) {
	this.firstName = firstName;
    }

    /**
     * Getter method
     * 
     * @return String lastName
     */
    public String getLastName() {
	return this.lastName;
    }

    /**
     * Setter method
     * 
     * @param lastName
     */
    public void setLastName(String lastName) {
	this.lastName = lastName;
    }

    /**
     * Getter method
     * 
     * @return String salutation
     */
    public String getSalutation() {
	return this.salutation;
    }

    /**
     * Setter method
     * 
     * @param salutation
     */
    public void setSalutation(String salutation) {
	this.salutation = salutation;
    }

    /**
     * Getter method
     * 
     * @return String loginTime
     */
    public String getLoginTime() {
	return this.loginTime;
    }

    /**
     * Setter method
     * 
     * @param loginTime
     */
    public void setLoginTime(String loginTime) {
	this.loginTime = loginTime;
    }

    /**
     * constructor: create an interviewer object.
     * 
     * @param studySpace
     *            StudySpace to which this interviewer is linked to
     */
    public Interviewer(StudySpace studySpace) {
	this.studySpace = studySpace;
    }

    /**
     * check the interviewer's verification when logging in and assign the
     * attributes.
     * 
     * @param interviewId
     *            Id of the interviewer who is trying to login to the system.
     * @param interviewUsername
     *            User name of the interviewer.
     * @return boolean If the user is logging in with valid credentials or not.
     */
    public boolean verifyInterviewer(String interviewId,
	    String interviewUsername) {
	boolean getResult = false;
	this.id = interviewId;
	this.userName = interviewUsername;

	try {

	    /* connect to the database */
	    Connection conn = this.studySpace.getDBConnection();
	    Statement statement = conn.createStatement();
	    Statement statement_1 = conn.createStatement();

	    /* check if the record exists in the table of interviewer */
	    String sql = "select firstname, lastname, salutation, email, submittime from interviewer where id='"
		    + this.id + "' and username='" + this.userName + "'";
	    statement.execute(sql);
	    ResultSet rs = statement.getResultSet();
	    this.loginTime = null;

	    /* if the interviewer exists in the current database */
	    if (rs.next()) {

		/* update the login time */
		String sql_1 = "update interviewer set submittime=now() where id='"
			+ this.id + "'";
		statement_1.execute(sql_1);

		/* assign the attributes */
		sql_1 = "select firstname, lastname, salutation, email, submittime from interviewer where id='"
			+ this.id + "'";
		statement_1.execute(sql_1);
		ResultSet rs_1 = statement_1.getResultSet();
		this.loginTime = null;
		if (rs_1.next()) {
		    this.firstName = rs.getString("firstname");
		    this.lastName = rs.getString("lastname");
		    this.salutation = rs.getString("salutation");
		    this.email = rs.getString("email");
		    this.loginTime = rs.getString("submittime");
		    getResult = true;
		}
		rs_1.close();
		statement_1.close();
	    }
	    rs.close();
	    statement.close();
	    conn.close();
	} catch (SQLException e) {
	    LOGGER.error("INTERVIEWER - VERIFY INTERVIEWER:" + e.toString(),
		    null);
	    getResult = false;
	}
	return getResult;
    }

    /**
     * Creates an interview survey message in the table of survey_message_use
     * before starting the interview.
     * 
     * @param inviteeId
     *            Invitee ID for whom the message has to be created.
     * @param surveyId
     *            Survey ID to whom the invitee is linked to.
     * @return String message ID is returned which is used for making the URL
     *         for the invitee to access the system
     */
    public String createSurveyMessage(String inviteeId, String surveyId) {
	String surveyMsgId = null;
	try {

	    /* connect to the database */
	    Connection conn = this.studySpace.getDBConnection();
	    Statement statement = conn.createStatement();
	    String messageId = org.apache.commons.lang3.RandomStringUtils
		    .randomAlphanumeric(22);

	    /* insert an interview record */
	    String sql = "INSERT INTO survey_message_use (invitee, survey, message, sent_date) "
		    + " values ('"
		    + messageId
		    + "','"
		    + inviteeId
		    + "','"
		    + surveyId + "','interview', now())";
	    statement.execute(sql);
	    surveyMsgId = messageId;

	    statement.close();
	    conn.close();
	} catch (SQLException e) {
	    LOGGER.error("INTERVIEW - CREATE SURVEY MESSAGE:" + e.toString(),
		    null);
	}
	return surveyMsgId;
    }

    /**
     * create an interview session in the table of interview_session when
     * starting the interview.
     * 
     * @param userSession
     *            Session ID whose value is put into the data base for this
     *            interviewer.
     */
    public void beginSession(String userSession) {

	/*
	 * the interview_session_id is a foreign key reference to the user's
	 * survey session id
	 */
	this.interviewSessionId = userSession;

	/*
	 * the interview_assign_id is a foreign key reference to the interviewer
	 * assignment id which value has been assigned in the
	 * Begin_Interview.jsp
	 */
	try {

	    /* connect to the database */
	    Connection conn = this.studySpace.getDBConnection();
	    Statement statement = conn.createStatement();

	    /* insert a session record */
	    String sql = "INSERT INTO interview_session (session_id, assign_id) VALUES ('"
		    + userSession + "','" + this.interviewAssignId + "')";
	    statement.execute(sql);
	    statement.close();
	    conn.close();
	} catch (SQLException e) {
	    LOGGER.error("INTERVIEW - BEGIN SESSION:" + e.toString(), null);
	}
    }

    /**
     * save the interview session info in the table of interview_assignment
     * before ending the session
     */
    public void setDone() {
	try {

	    /* connect to the database */
	    Connection conn = this.studySpace.getDBConnection();
	    Statement statement = conn.createStatement();
	    String sql = "UPDATE interview_assignment SET close_date = now(), pending=0 WHERE id = "
		    + this.interviewAssignId;
	    statement.execute(sql);
	    statement.close();
	    conn.close();
	} catch (SQLException e) {
	    LOGGER.error("INTERVIEW - SET DONE:" + e.toString(), null);
	}
    }

}
