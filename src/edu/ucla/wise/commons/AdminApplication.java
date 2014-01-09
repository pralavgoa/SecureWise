package edu.ucla.wise.commons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.wise.initializer.StudySpaceParametersProvider;
import edu.ucla.wise.initializer.WiseProperties;
import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;

/*
 Admin information set -- 
 The class represents that Admin application
 Instances represent administrator user sessions
 TODO (med): untangle Frank's survey uploading spaghetti in load_data.jsp
 */
/**
 * This class represents the Admin information when running the Admin
 * application. Instance represent administrator user session.
 * 
 * @author mrao
 * @author dbell
 * @author ssakdeo
 */
public class AdminApplication extends WISEApplication {

    private final Logger log = Logger.getLogger(AdminApplication.class);

    public static String dbBackupPath, styleRootPath, imageRootPath;
    private static Hashtable<String, String> loggedIn = new Hashtable<String, String>();

    /* instance variables -- represent an individual administrator session */
    public StudySpace myStudySpace;
    // public String db_user; now only in databank
    public String dbPwd;
    public String studyId, studyName, studyTitle;
    public String studyXmlPath, studyCssPath, studyImagePath;
    public String emailFormat, coda, htmlSignature;
    public boolean pwValid;
    public AdminDataBank adb;

    // 08-Nov-2009
    // Pushed down into the children classes like Surveyor_Application
    // and AdminInfo as static in the children
    // Make Surveyor_Application a singleton class per JBOSS
    public static String ApplicationName = null;

    public static String sharedFileUrl;
    public static String sharedImageUrl;
    public static String servletUrl;

    private static final ConcurrentHashMap<String, Integer> loginAttemptNumbers = new ConcurrentHashMap<String, Integer>();
    private static final ConcurrentHashMap<String, Long> lastLoginTime = new ConcurrentHashMap<String, Long>();

    /**
     * Initializes the static instance fields of the class.
     * 
     * @param appContext	Name of the application context.
     */
    public static void initStaticFields(String appContext) {
    	if (ApplicationName == null) {
    		ApplicationName = appContext;
    	}
    	sharedFileUrl = WISEApplication.rootURL + "/" + ApplicationName
    			+ "/" + WISEApplication.sharedFilesLink + "/";
    	sharedImageUrl = sharedFileUrl + "images/";
    	servletUrl = WISEApplication.rootURL + ApplicationName + "/";
    }

    // can't abstract this to WISE_Application because parent's static method
    // doesn't call sub's initialize()
    // NB: there may be a better way to handle this in Java but I can't find it
    // at the moment
    public static String checkInit(String appContext, WiseProperties properties) throws IOException {
    	String initErr = null;
    	if (ApplicationName == null) {
    		initErr = initialize(appContext,properties);
    	}
    	if (ApplicationName == null) {
    		
    		/* *still* null means uninitialized */
    		initErr = "Wise Admin Application -- uncaught initialization error";
    	}
    	return initErr;
    }

    public static String forceInit(String appContext, WiseProperties properties) throws IOException {
	String initErr = null;
	initialize(appContext, properties);
	if (ApplicationName == null) // *still* null means uninitialized
	    initErr = "Wise Admin Application -- uncaught initialization error";
	return initErr;
    }

    public static String initialize(String appContext, WiseProperties properties) throws IOException {
    	AdminApplication.initStaticFields(appContext);
    	String initErr = WISEApplication.initialize(properties);
    	imageRootPath = WISEApplication.wiseProperties.getStringProperty("shared_image.path");
    	styleRootPath = WISEApplication.wiseProperties.getStringProperty("shared_style.path");
    	dbBackupPath = WISEApplication.wiseProperties.getStringProperty("db_backup.path")
    			+ System.getProperty("file.separator");
    	
    	/* don't need further error checking; prob ok if these are also null */
    	return initErr;
    }

    /**
     * Admin Application functions Note: need to improve encapsulation; store Admin
     * Application.
     * */

    /**
     * Constructor to create an Admin user session.
     * 
     * @param username			username to login into admin application.
     * @param passwordGiven		password to login.
     * @throws IllegalArgumentException
     */
    public AdminApplication(String username, String passwordGiven)
    		throws IllegalArgumentException {
    	StudySpaceParameters params = StudySpaceParametersProvider
    			.getInstance().getStudySpaceParameters(username);
    	if (params == null) {
    		logInfo("params object is null");
    		throw new IllegalArgumentException();
    	}

    	dbPwd = params.getDatabasePassword();
    	// 20dec db_pwd = sharedProps.getString(username + ".dbpass");

    	// logInfo("Given password is " + passwordGiven
    	//		+ " and actual password is " + dbPwd);

    	pwValid = passwordGiven.equalsIgnoreCase(dbPwd);
    	
    	/* get other properties */
    	if (pwValid) {
    		studyName = username;
    		studyId = params.getId();
    		// 20dec study_id = sharedProps.getString(username +
    		// ".studyid");
    		
    		/* get or instantiate the StudySpace, which contains the
    		 * DataBank for db access
    		 */
    		myStudySpace = StudySpace.getSpace(studyId);
    		studyTitle = myStudySpace.title;

    		/* assign other attributes */
    		studyXmlPath = WISEApplication.wiseProperties.getXmlRootPath() + System.getProperty("file.separator")
    				+ studyName + System.getProperty("file.separator");
    		studyCssPath = styleRootPath
    				+ System.getProperty("file.separator") + studyName
    				+ System.getProperty("file.separator");
    		studyImagePath = imageRootPath
    				+ System.getProperty("file.separator") + studyName
    				+ System.getProperty("file.separator");
    		
    		/* assign the AdminDataBank class */
    		adb = new AdminDataBank(myStudySpace.db);

    		/* record Admin user login */
    		loggedIn.put(studyName, studyId);
    		logInfo("Study name and study id inserted in loggedIn");
    	}	
    }

    /**
     * finalize() called by garbage collector to clean up all objects
     */
    @Override
    protected void finalize() throws Throwable {
    	try {
    		loggedIn.remove(studyName);
    	} catch (NullPointerException e) {
    		WISEApplication.logError("Exception deleting Admin user "
    				+ studyName + ": " + e.toString(), e);
    	} finally {
    		super.finalize();
    	}
    }

    /**
     * Returns all the admins that are logged into the system.
     * 
     * @return	String HTML format of all the admins logged in.
     */
    public static String listAdminsOnNow() {
    	String adminlist = "";
    	Enumeration<String> en = loggedIn.keys();
    	while (en.hasMoreElements())
    		adminlist += "<P>" + en.nextElement() + "</P>";
    	return adminlist;
    }

    /**
     * Returns a database Connection object
     * 
     * @return	Returns data base connection object.
     * @throws SQLException
     */
    public Connection getDBConnection() throws SQLException {
    	return myStudySpace.getDBConnection();
    }

    /**
     * Logs the message and the exception stack trace.
     * 
     * @param message	The message to be printed.
     * @param e			Exception to be logged.
     */
    public static void logError(String message, Exception e) {
    	WISEApplication.logError(message, e);
    }

    /**
     * Ask local copy of the StudySpace to parse out the preface file.
     * 
     * @return boolean True if the preface load happens correctly else false
     */
    public boolean parseMessageFile() {
    	return myStudySpace.loadPreface();
    }

    /**
     * print the email message body retrieve using sequence, message type -- guaranteed
     * 
     * @param 	seqId		Message sequence Id from which the text has to be printed.
     * @param 	msgType		Type of the message that is to be printed from the message sequence obtained
     * @return	String		HTML format of the message body.
     */
    public String renderMessageBody(String seqId, String msgType) {
    	String outputString = "";
    	outputString += "<table width=510 class=tth border=1 cellpadding=2 cellspacing=0 bgcolor=#FFFFF5>";
    	outputString += "<tr><td width=50 class=sfon>From: </td>";

    	/* get the message sequence from the hash */
    	MessageSequence msgSeq = myStudySpace.preface
    			.getMessageSequence(seqId);
    	if (msgSeq == null) {
    		logInfo("ADMIN INFO - PRINT MESSAGE BODY: Can't get the message sequence for requested Sequence, Message Type");
    		return null;
    	}

    	String emailFrom = msgSeq.fromString;
    	emailFrom = emailFrom.replaceAll("<", "&lt;");
    	emailFrom = emailFrom.replaceAll(">", "&gt;");
    	outputString += "<td>\"" + emailFrom + "\"";
    	outputString += "</td></tr>";

    	/* get the message from the message sequence hash */
    	Message m = msgSeq.getTypeMessage(msgType);
    	if (m == null) {
    		logInfo("ADMIN INFO - PRINT MESSAGE BODY: Can't get the message from sequence hash");
    		return null;
    	}

    	outputString += m.renderSampleAsHtmlRows() + "</table>";
    	return outputString;
    }

    // print the message body
    // retrieve using survey, irb (not guaranteed)
    // I believe this is deprecated --DB
    // public String print_message_body(String survey_id, String irb_id, String
    // msg_type)
    // {
    // String outputString="";
    // outputString +=
    // "<table width=510 class=tth border=1 cellpadding=2 cellspacing=0 bgcolor=#FFFFF5>";
    //
    // try
    // {
    // //get the message sequence from the hash
    // Message_Sequence msg_seq = (Message_Sequence)
    // myStudySpace.preface.get_message_sequence(survey_id, irb_id);
    // if(msg_seq == null)
    // {
    // log_error("ADMIN INFO - PRINT MESSAGE BODY: Can't get the message sequence for requested Survey, IRB");
    // return null;
    // }
    //
    // outputString += "<tr><td width=50 class=sfon>From: </td>";
    // String email_from = msg_seq.from_string;
    // email_from = email_from.replaceAll("<", "&lt;");
    // email_from = email_from.replaceAll(">", "&gt;");
    // outputString +="<td>\""+ email_from +"\"";
    // outputString += "</td></tr>";
    //
    // //get the message from the message sequence hash
    // Message m = (Message) msg_seq.get_type_message(msg_type);
    // if(m == null)
    // {
    // log_error("ADMIN INFO - PRINT MESSAGE BODY: Can't get the requested message "
    // + msg_type +
    // " from Message sequence " + msg_seq.id);
    // return null;
    // }
    // outputString += m.renderSample_asHtmlRows() + "</table>";
    // }
    // catch (Exception e)
    // {
    // log_error("ADMIN INFO - PRINT MESSAGE BODY: "+e.toString());
    // }
    // return outputString;
    //
    // }

    /**
     * Prints invitees with state - excluding the initial invitees.
     * 
     * @param 	surveyId	Survey for which the invitees and their states are to be lsited.
     * @return	String		HTML format of the invitees and their states.
     */
    public String printInviteeWithState(String surveyId) {
    	String outputString = "";
    	String sql = "SELECT i.id, firstname, lastname, salutation, irb_id, state, " +
				"email FROM invitee as i, survey_user_state as s where i.id=s.invitee and survey= ?"
				+ " ORDER BY i.id";
    	String sqlm = "select invitee from survey_user_state where state='declined' and invitee= ?"; 
		Connection conn = null;
		PreparedStatement stmt = null;
		PreparedStatement stmtm = null;
    	
    	try {
    		conn = this.getDBConnection();
    		stmt = conn.prepareStatement(sql);
    		stmt.setString(1, surveyId);    		
    		ResultSet rs = stmt.executeQuery();
    		outputString += "<table class=tth border=1 cellpadding=2 cellspacing=0 bgcolor=#FFFFF5>";
    		outputString += "<tr>";
    		outputString += "<th class=sfon></th>";
    		outputString += "<th class=sfon>User ID</th>";
    		outputString += "<th class=sfon>User Name</th>";
    		outputString += "<th class=sfon>IRB ID</th>";
    		outputString += "<th class=sfon>User state</th>";
    		outputString += "<th class=sfon>User's Email Address</th></tr>";
    		
    		stmtm = conn.prepareStatement(sqlm);
    		while (rs.next()) {
    			stmtm.clearParameters();
    			stmtm.setInt(1, rs.getInt(1));
    			ResultSet rsm = stmtm.executeQuery();
    			if (rsm.next()) {
    				outputString += "<tr bgcolor='#E4E4E4'>";
    			} else {
    				outputString += "<tr>";
    			}
    			outputString += "<td><input type='checkbox' name='user' value='"
    					+ rs.getString(1) + "'></td>";
    			outputString += "<td>" + rs.getString(1) + "</td>";
    			outputString += "<td>" + rs.getString(4) + " "
    					+ rs.getString(2) + " " + rs.getString(3) + "</td>";
    			outputString += "<td>" + rs.getString(5) + "</td>";
    			outputString += "<td>" + rs.getString(6) + "</td>";
    			outputString += "<td>" + rs.getString(7) + "</td>";
    			outputString += "</tr>";
    		}
    		rs.close();
    		outputString += "</table>";
    	} catch (SQLException e) {
    		log.error("ADMIN Data Bank - PRINT INVITEE WITH STATE: " + e.toString(), e);
    	}  finally {
    		try {
    			if (conn != null) {
    				conn.close();
    			}
    			if(stmt != null) {
    				stmt.close();
    			}
    			if(stmtm != null) {
    				stmt.close();
    			}
    		} catch (SQLException e) {
        		log.error(
        				"ADMIN Data Bank - PRINT INVITEE WITH STATE: " + e.toString(), e);
        	}
    	}
    	return outputString;
    }

     /**
     * Returns invitees belonging to different IRB Ids..
     * 	
     * @return	Hashtable	hash table of keys as irb id and value as 
     * 						all the invitees belonging to this irb id.
     */
    public Hashtable<String, String> getIrbGroups() {
    	Hashtable<String, String> irbGroups = new Hashtable<String, String>();
    	
    	/* select the different IRBs from the invitees tables. */
		String sqle = "select distinct(irb_id) from invitee order by irb_id";
		
		/* select all the invitees belonging to a particular IRB */
		String sql1 = "select id from invitee where irb_id IS NULL";
		String sql2 = "select id from invitee where irb_id = ?";
		
    	Connection conn = null;
		PreparedStatement statement = null;
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;
    	try {
    		conn = this.getDBConnection();
    		statement = conn.prepareStatement(sqle);
    		stmt1 = conn.prepareStatement(sql1);
    		stmt2 = conn.prepareStatement(sql2);    		
    		ResultSet rse = statement.executeQuery();
    		while (rse.next()) {
    			String irbId = rse.getString("irb_id");
    			ResultSet rs;
    			if (irbId == null) {
    				rs = stmt1.executeQuery();
    			} else {
    				stmt2.clearParameters();
    				stmt2.setString(1, irbId);
    				rs = stmt2.executeQuery();
    			}    			
    			String irbGroupsId = " ";
    			while (rs.next()) {
    				irbGroupsId += rs.getString(1) + " ";
    			}
    			irbGroups.put(irbId, irbGroupsId);
    		}
    	} catch (NullPointerException e) {
    		log.error("ADMIN INFO - GET IRB GROUPS: " + e.toString(), e);
    	} catch (SQLException e) {
    		log.error("ADMIN INFO - GET IRB GROUPS: " + e.toString(), e);
    	} finally {
    		try {
    			if (conn != null) {
    				conn.close();
    			}
    			if(stmt1 != null) {
    				stmt1.close();
    			}
    			if(stmt2 != null) {
    				stmt2.close();
    			}
    			if(statement != null) {
    				statement.close();
    			}
    		} catch (SQLException e) {
        		log.error("ADMIN INFO - GET IRB GROUPS: " + e.toString(), e);
        	}
    	}
    	return irbGroups;
    }

    /**
     * print table of initial invites, eligible invitees for a survey, by
     * message sequence (& therefore irb ID)
     * 	
     * @param 	surveyId	Survey Id whose invitees are to be listed.
     * @param 	isReminder	Is the message a reminder or not.
     * @return	String		HTML format of the invitees tables separated 
     * 						based on the message sequence type
     */
    public String renderInitialInviteTable(String surveyId,
    		boolean isReminder) {
    	String outputString = "";
    	MessageSequence[] msgSeqs = myStudySpace.preface
    			.getMessageSequences(surveyId);
    	if (msgSeqs.length == 0) {
    		return "No message sequences found in Preface file for selected Survey.";
    	}
    	Connection conn = null;
    	Statement stmt = null;
    	try {
    		conn = this.getDBConnection();
    		stmt = conn.createStatement();
    		for (int i = 0; i < msgSeqs.length; i++) {
    			MessageSequence msgSeq = msgSeqs[i];
    			String irbName = msgSeq.irbId;
    			if (CommonUtils.isEmpty(irbName)) {
    				irbName = "= ''";
    			} else {
    				irbName = "= '" + irbName + "'";
    			}
    			Message msg = msgSeq.inviteMsg;
    			outputString += "<form name=form1 method=post action='initial_invite_send.jsp'>\n"
    					+ "<input type='hidden' name='seq' value='"
    					+ msgSeq.id
    					+ "'>\n"
    					+ "<input type='hidden' name='reminder' value='"
    					+ String.valueOf(isReminder)
    					+ "'>\n"
    					+ "<input type='hidden' name='svy' value='"
    					+ surveyId
    					+ "'>\n"
    					+ "Start Message Sequence <B>"
    					+ msgSeq.id
    					+ "</b> (designated for IRB "
    					+ irbName
    					+ ")<BR>\n"
    					+ "...using Initial Message: "
    					+ "<a href='print_msg_body.jsp?seqID="
    					+ msgSeq.id
    					+ "&msgID=invite' target='_blank'>"
    					+ msg.subject
    					+ "</a><br>\n"
    					+ "<p align=center><input type='image' alt='Click to send email. This button is equivalent to the one at bottom.' "
    					+ "src='admin_images/send.gif'></p>"
    					+ "<table class=tth border=1 cellpadding=2 cellspacing=0 bgcolor=#FFFFF5>";
    			try {
    				
    				/* select the invitees without any states */
    				String sql = buildInitialInviteQuery(surveyId, msgSeq.id,
    						irbName, isReminder);
    				stmt.execute(sql);
    				ResultSet rs = stmt.getResultSet();
    				outputString += "<tr>";
    				outputString += "<th class=sfon></th>";
    				outputString += "<th class=sfon>User ID</th>";
    				outputString += "<th class=sfon>User Name</th>";
    				outputString += "<th class=sfon>IRB</th>";
    				outputString += "<th class=sfon>User's Email Address</th></tr>";

    				while (rs.next()) {
    					outputString += "<tr>";
    					outputString += "<td><input type='checkbox' name='user' value='"
    							+ rs.getString(1) + "'></td>";
    					outputString += "<td>" + rs.getString(1) + "</td>";
    					outputString += "<td>" + rs.getString(4) + " "
    							+ rs.getString(2) + " " + rs.getString(3)
    							+ "</td>";
    					outputString += "<td>" + rs.getString(5) + "</td>";
    					outputString += "<td>" + rs.getString(6) + "</td>";
    					outputString += "</tr>";
    				}
    				rs.close();
    			} catch (SQLException e) {
    				log.error(
    						"ADMIN Data Bank error - render_initial_invite_table: "
    								+ e.toString(), e);
    			}
    			outputString += "</table><p align='center'>"
    					+
    					// TODO: resolve file path references between admin and survey applications
    					"<input type='image' alt='Click to send email. This button is the same as one above.' src='admin_images/send.gif'>"
    					+ "</p></form>";
    		} // for
    	} catch (SQLException e) {
    		log.error(
    				"ADMIN Data Bank connection error - renderInitialInviteTable: "
    						+ e.toString(), e);
    	} finally {
    		try {
    			if (conn != null) {
    				conn.close();
    			}
    			if(stmt != null) {
    				stmt.close();
    			}
    		} catch (SQLException e) {
        		log.error(
        				"ADMIN Data Bank connection error - renderInitialInviteTable: " + e.toString(), e);
        	}
    	}
    	return outputString;
    }

    /**
     * Builds the query that is used to get information about invitees for sending emails.
     * 
     * @param 	surveyId	Survey Id for which all invitees have to listed.
     * @param 	msgSeq		To classify the invitee groups based on the message sequence.
     * @param 	irbName		Irb name whom the invitees are linked.
     * @param 	isReminder	Includes all the invitees who have not complete the survey incase 
     * 						if it true else it includes only the invitees who have not started 
     * 						received any mail presviously.
     * @return	String		The composed SQL query
     */
    private String buildInitialInviteQuery(String surveyId, String msgSeq,
    		String irbName, boolean isReminder) {
    	StringBuffer strBuff = new StringBuffer();
    	if (isReminder) {
    		strBuff.append("SELECT I.id, I.firstname, I.lastname, I.salutation, I.irb_id, AES_DECRYPT(I.email,'"
    				+ this.myStudySpace.db.emailEncryptionKey
    				+ "') FROM invitee as I, survey_user_state as S WHERE I.irb_id "
    				+ irbName
    				+ " AND I.id not in (select invitee from survey_user_state where survey='"
    				+ surveyId
    				+ "' AND state like 'completed') AND I.id=S.invitee AND S.message_sequence='"
    				+ msgSeq + "' ORDER BY id");
    	} else {
    		strBuff.append("SELECT id, firstname, lastname, salutation, irb_id, AES_DECRYPT(email,'"
    				+ this.myStudySpace.db.emailEncryptionKey
    				+ "') FROM invitee WHERE irb_id "
    				+ irbName
    				+ " AND id not in (select invitee from survey_user_state where survey='"
    				+ surveyId + "')" + "ORDER BY id");
    	}
    	return strBuff.toString();
    }

    /**
     * print table of all sendable invitees, all invitees, by message sequence (&
     * therefore irb ID)
     * 
     * @param 	surveyId	Survey Id for which all invitees have to listed.
     * @return	String		HTML table format of all the invitees under the given survey id.
     */
    public String renderInviteTable(String surveyId) {
    	String outputString = "";
    	MessageSequence[] msgSeqs = myStudySpace.preface
    			.getMessageSequences(surveyId);
    	if (msgSeqs.length == 0)
    		return "No message sequences found in Preface file for selected Survey.";
    	String sql = "SELECT id, firstname, lastname, salutation, irb_id, AES_DECRYPT(email, '"
				+ this.myStudySpace.db.emailEncryptionKey
				+ "') FROM invitee WHERE irb_id = ?"
				+ " ORDER BY id";
    	Connection conn = null;
		PreparedStatement stmt = null;
    	try {
    		conn = this.getDBConnection();
    		stmt = conn.prepareStatement(sql);
    		for (int i = 0; i < msgSeqs.length; i++) {
    			MessageSequence msgSeq = msgSeqs[i];
    			String irbName = msgSeq.irbId;
    			    			
    			Message msg = msgSeq.inviteMsg;
    			outputString += "<form name=form1 method=post action='invite_send.jsp'>\n"
    					+ "<input type='hidden' name='seq' value='"
    					+ msgSeq.id
    					+ "'>\n"
    					+ // repeat form so we can use same hidden field names on each
    					"<input type='hidden' name='svy' value='"
    					+ surveyId
    					+ "'>\n"
    					+ "Using Message Sequence <B>"
    					+ msgSeq.id
    					+ "</b> (designated for IRB "
    					+ irbName
    					+ ")<BR>\n"
    					+ "...SEND Message: <BR>"
    					+ "<input type='radio' name='message' value='invite'>\n"
    					+ "<a href='print_msg_body.jsp?seqID="
    					+ msgSeq.id
    					+ "&msgID=invite' target='_blank'>"
    					+ msg.subject
    					+ "</a><br>\n";
    			for (int j = 0; j < msgSeq.totalOtherMessages(); j++) {
    				msg = msgSeq.getTypeMessage("" + j);
    				outputString += "<input type='radio' name='message' value='"
    						+ j
    						+ "'>\n"
    						+ "<a href='print_msg_body.jsp?seqID="
    						+ msgSeq.id
    						+ "&msgID="
    						+ j
    						+ "' target='_blank'>"
    						+ msg.subject + "</a><br>\n";
    			}
    			outputString += "<p align=center><input type='image' alt='Click to send email. This button is equivalent to the one at bottom.' "
    					+ "src='admin_images/send.gif'></p>"
    					+ "<table class=tth border=1 cellpadding=2 cellspacing=0 bgcolor=#FFFFF5>";
    			try {
    				
    				/* select the invitees without any states */
    				stmt.clearParameters();
    				stmt.setString(1, irbName);
    				ResultSet rs = stmt.executeQuery();
    				outputString += "<tr>";
    				outputString += "<th class=sfon></th>";
    				outputString += "<th class=sfon>User ID</th>";
    				outputString += "<th class=sfon>User Name</th>";
    				outputString += "<th class=sfon>IRB</th>";
    				outputString += "<th class=sfon>User's Email Address</th></tr>";

    				while (rs.next()) {
    					outputString += "<tr>";
    					outputString += "<td><input type='checkbox' name='user' value='"
    							+ rs.getString(1) + "'></td>";
    					outputString += "<td>" + rs.getString(1) + "</td>";
    					outputString += "<td>" + rs.getString(4) + " "
    							+ rs.getString(2) + " " + rs.getString(3)
    							+ "</td>";
    					outputString += "<td>" + rs.getString(5) + "</td>";
    					outputString += "<td>" + rs.getString(6) + "</td>";
    					outputString += "</tr>";
    				}
    				rs.close();
    			} catch (SQLException e) {
    				log.error(
    						"ADMIN Data Bank error - render_initial_invite_table: "
    								+ e.toString(), e);
    			}
    			outputString += "</table><p align='center'>"
    					+
    					// TODO: resolve file path references between admin and survey applications
    					"<input type='image' alt='Click to send email. This button is the same as one above.' src='admin_images/send.gif'>"
    					+ "</p></form>";
    		} // for
    		conn.close();
    	} catch (SQLException e) {
    		log.error(
    				"ADMIN Data Bank DB connection error - renderInitialInviteTable: "
    						+ e.toString(), e);
    	} finally {
    		try {
    			if (conn != null) {
    				conn.close();
    			}
    			if(stmt != null) {
    				stmt.close();
    			}
    		} catch (SQLException e) {
        		log.error(
        				"ADMIN Data Bank connection error - renderInitialInviteTable: " + e.toString(), e);
        	}
    	}
    	return outputString;
    }
    
    
   /**
    * Prints initial invitees in a table format for editing -- called by
    * load_invitee.jsp
    * 
    * @param 	surveyId	Survey Id for which all invitees have to listed.
    * @return	String		HTML editable table format of all the invitees under the given survey id.
    */
    public String printInitialInviteeEditable(String surveyId) {
     	String outputString = "";
     	Connection conn = null;
     	PreparedStatement stmt = null;
     	
     	/* select the invitees without any states */
 		String sql = "SELECT id, firstname, lastname, salutation, irb_id, AES_DECRYPT(email, '"
 				+ this.myStudySpace.db.emailEncryptionKey
 				+ "') FROM invitee WHERE id not in (select invitee from survey_user_state where survey= ?"
 				+ ") ORDER BY id";
     	try {
     		conn = this.getDBConnection();
     		stmt = conn.prepareStatement(sql);
     		stmt.setString(1, surveyId);
     		ResultSet rs = stmt.executeQuery();
     		outputString += "<table class=tth border=1 cellpadding=2 cellspacing=0 bgcolor=#FFFFF5>";
     		outputString += "<tr>";
     		// outputString += "<th class=sfon></th>";
     		outputString += "<th class=sfon>User ID</th>";
     		outputString += "<th class=sfon>First name</th>";
     		outputString += "<th class=sfon>Last name</th>";
     		outputString += "<th class=sfon>IRB</th>";
     		outputString += "<th class=sfon>User's Email Address</th>";
     		outputString += "<th class=sfon>Action</th></tr>";

     		while (rs.next()) {
     			outputString += "<tr>";
     			// outputString +=
     			// "<td><input type='checkbox' name='user' value='"+rs.getString(1)+"'></td>";
     			outputString += "<td>" + rs.getString(1) + "</td>";
     			outputString += "<td><input type='text' name='fname"
     					+ rs.getString(1) + "' value='" + rs.getString(2)
     					+ "'/></td>";
     			outputString += "<td><input type='text' name='lname"
     					+ rs.getString(1) + "' value='" + rs.getString(3)
     					+ "'/></td>";
     			outputString += "<td><input type='text' name='irb"
     					+ rs.getString(1) + "' value='" + rs.getString(5)
     					+ "'/></td>";
     			outputString += "<td><input type='text' name='email"
     					+ rs.getString(1) + "' value='" + rs.getString(6)
     					+ "'/></td>";
     			outputString += "<td><a href='javascript:update_inv("
     					+ rs.getString(1) + ");'> Update </a><br>"
     					+ "<a href='javascript:delete_inv(" + rs.getString(1)
     					+ ");'> Delete </a>" + "</td>";
     			outputString += "</tr>";
     		}
     		rs.close();
     		outputString += "</table>";
     		conn.close();
     	} catch (SQLException e) {
     		log.error(
     				"ADMIN Data Bank - PRINT INITIAL INVITEE EDITABLE: "
     						+ e.toString(), e);
     	} finally {
    		try {
    			if (conn != null) {
    				conn.close();
    			}
    			if(stmt != null) {
    				stmt.close();
    			}
    		} catch (SQLException e) {
        		log.error(
        				"ADMIN Data Bank - PRINT INITIAL INVITEE EDITABLE: " + e.toString(), e);
        	}
    	}
     	return outputString;
    }
    
 	/**
	 * This method is called from load_invitee.jsp and is used to 
	 * update/delete any of the invitees information
	 * 
	 * @param request	Http request that contains all the necessary 
	 * 					parameters to update an invitee in the data base
	 */
    public boolean updateInvitees(HttpServletRequest request) {
    	String delFlag = request.getParameter("delflag");
    	String updateID = request.getParameter("changeID");
    	PreparedStatement stmt = null;
    	Connection conn = null;
    	try {

    		if(SanityCheck.sanityCheck(updateID) || SanityCheck.sanityCheck(delFlag)){
    			return false;
    		}
    		conn = this.getDBConnection();
    		if (delFlag != null && delFlag.equals("true") && updateID != null) {
    			stmt = conn.prepareStatement("delete from invitee where id = "
    					+ updateID);
    		} else if (updateID != null) {
    			stmt = conn
    					.prepareStatement("update invitee set firstname=?, lastname=?, irb_id=?, email="
    							+ "AES_ENCRYPT(?,'"
    							+ this.myStudySpace.db.emailEncryptionKey
    							+ "')" + " where id=?");
    			String irbid = request.getParameter("irb" + updateID);
    			String firstName = request.getParameter("fname" + updateID);
    			String lastName = request.getParameter("lname" + updateID);
    			String emailId =  request.getParameter("email" + updateID);

    			if(SanityCheck.sanityCheck(emailId) || SanityCheck.sanityCheck(firstName) || SanityCheck.sanityCheck(lastName)
    					|| SanityCheck.sanityCheck(irbid)){
    				return false;
    			}
    			if (irbid.equals("") || irbid.equalsIgnoreCase("null"))
    				irbid = null;
    			stmt.setString(1,firstName);
    			stmt.setString(2,lastName);
    			stmt.setString(3,irbid);
    			stmt.setString(4,emailId );
    			stmt.setString(5, updateID);
    		}
    		if (stmt != null) {
    			stmt.execute();
    		}
    		
    	} catch (SQLException e) {
    		log.error("Deleting/Updating the invitee failed.", e);
    		logError("ADMIN INFO - UPDATE INVITEE: " + e.toString(), e);
    		return false;
    	} finally {
    		try {
	    		if (stmt != null) {
	    			stmt.close();
	    		}
	    		if (conn != null) {
	    			conn.close();
	    		} 
    		} catch (SQLException e) {
				log.error("check why prepared statement creation failed", e);
    		}
    		
    	}
    	return true;
    }
    
    /**
     * Prints interviewer list for a studySpace
     * 
     * @return HTML table format of all the interviewers in the study space.
     */
    public String printInterviewer() {
    	String outputString = "";
    	String sql = "SELECT id, firstname, lastname, salutation, email FROM interviewer ORDER BY id";
    	PreparedStatement stmt = null;
    	Connection conn = null;    	
    	try {
    		conn = this.getDBConnection();
    		stmt = conn.prepareStatement(sql);    		
    		ResultSet rs = stmt.executeQuery();
    		outputString += "<table class=tth border=1 cellpadding=2 cellspacing=0 bgcolor=#FFFFF5>";
    		outputString += "<tr>";
    		outputString += "<th class=sfon></th>";
    		outputString += "<th class=sfon>Interviewer ID</th>";
    		outputString += "<th class=sfon>Interviewer Name</th>";
    		outputString += "<th class=sfon>Interviewer's Email Address</th>";
    		outputString += "<th class=sfon>Go to WATI</th></tr>";

    		while (rs.next()) {
    			outputString += "<tr>";
    			outputString += "<td><input type='radio' name='interviewer' value='"
    					+ rs.getString(1) + "'></td>";
    			outputString += "<td align=center>" + rs.getString(1) + "</td>";
    			outputString += "<td align=center>" + rs.getString(4) + " "
    					+ rs.getString(2) + " " + rs.getString(3) + "</td>";
    			outputString += "<td>" + rs.getString(5) + "</td>";
    			outputString += "<td align=center><a href='goto_wati.jsp?interview_id="
    					+ rs.getString(1)
    					+ "'><img src='admin_images/go_view.gif' border=0></a></td>";
    			outputString += "</tr>";
    		}
    		rs.close();
    		outputString += "</table>";
    		conn.close();
    	} catch (SQLException e) {
    		AdminApplication.logError(
    				"ADMIN INFO - PRINT INTERVIEWER LIST:" + e.toString(), e);
    	} finally {
    		try {
	    		if (stmt != null) {
	    			stmt.close();
	    		}
	    		if (conn != null) {
	    			conn.close();
	    		} 
    		} catch (SQLException e) {
				log.error("check why prepared statement creation failed", e);
    		}    		
    	}
    	return outputString;
    }

    /**
     * Prepares the message for email depending on the message sequence and then
     * sends it to the invitee.
     * 
     * @param 	msgType			Type of the message that has to be sent as email 
     * 							(The message type can be invite/interrupt/done/review/others)
     * @param 	messageSeqId	The ID of the message sequence which as to be emailed. 
     * 							This Id is the one given in the preface.xml
     * @param 	surveyId		The survey ID for which this message sequence is linked to 
     * 							and this should be same as the value in preface.xml
     * @param 	whereStr		The sql whereStr which is used to get the details of the 
     * 							person to whom the email has to be sent from the invitee table. 
     * @param 	isReminder		If the message is a reminder or not.
     * @return	String 			output message or message use ID for the invitee to whom email is sent.
     */
    public String sendMessages(String msgType, String messageSeqId,
    		String surveyId, String whereStr, boolean isReminder) {
    	return myStudySpace.sendInviteReturnDisplayMessage(msgType,
    			messageSeqId, surveyId, whereStr, isReminder);
    }

    //TODO: move the data base access to seperate class.
    /**
     * Gets the non-responders and incompleters of the survey from the tables.
     * 
     * @param spId	Array which contains the invitees who are non responders 
     * 				and incompleters and is sent as output
     * @param sId	Survey Id for which all non responders and incompleters 
     * 				invitees have to listed.	
     */
    public void getNonrespondersIncompleters(String[] spId, String sId) {
    	Connection conn = null;
    	PreparedStatement stmt1 = null;
    	PreparedStatement stmt2 = null;
    	
    	String sql1 = "select distinct(s.invitee) from survey_message_use as s, invitee as i where s.survey='"
				+ sId
				+ "' and s.invitee=i.id "
				+ "and s.invitee not in (select invitee from consent_response where answer='N') "
				+ "and not exists (select u.invitee from "
				+ sId
				+ "_data as u where u.invitee=s.invitee) "
				+ "group by s.invitee order by s.invitee";		
		
		String sql2 = "select distinct(invitee) from "
				+ sId
				+ "_data as s, invitee as i where s.invitee=i.id and status IS NOT NULL order by invitee";

    	try {
    		String nonresponderId = " ";
    		String incompleterId = " ";    		
    		
    		/* connect to the database */
    		conn = getDBConnection();
    		stmt1 = conn.prepareStatement(sql1);
    		stmt2 = conn.prepareStatement(sql2);
    		
    		/* get the non-responders user ID list */
    		ResultSet rs1 = stmt1.executeQuery();
    		while (rs1.next()) {
    			nonresponderId += rs1.getString(1) + " ";
    		}
    		
    		/* get the incompleters user ID list */
    		ResultSet rs2 = stmt2.executeQuery();
    		while (rs2.next()) {
    			incompleterId += rs2.getString(1) + " ";
    		}
    		spId[0] = nonresponderId;
    		spId[1] = incompleterId;

    	} catch (SQLException e) {
    		AdminApplication.logError("ADMIN APPLICATION - GET NONRESPONDERS INCOMPLETERS: "
    				+ e.toString(), e);
    	} finally {
    		try {
	    		if (stmt1 != null) {
	    			stmt1.close();
	    		}
	    		if (stmt2 != null) {
	    			stmt2.close();
	    		}
	    		if (conn != null) {
	    			conn.close();
	    		} 
    		} catch (SQLException e) {
				log.error("check why prepared statement creation failed", e);
    		}    		
    	}
    	return;
    }

    /**
     * Creates a CSV files.
     * 
     * @param filename	File name from which the table name is obtained.
     * @return true 	If the CSV file is created & written successfully, otherwise
     *         			it returns false.
     */
    public String buildCsvString(String filename) {

    	/* get the data table name */
    	String tname = filename.substring(0, filename.indexOf("."));
    	Connection conn = null;
    	Statement stmt = null;

    	try {
    		
    		/* get database connection */
    		conn = getDBConnection();
    		stmt = conn.createStatement();
    		String sql = "";
    		sql = "describe " + tname;
    		// log_error(sql);
    		stmt.execute(sql);
    		ResultSet rs = stmt.getResultSet();
    		String sqlm = "select ";
    		String outputStr = "";
    		String[] fieldName = new String[1000];
    		String[] delimitor = new String[1000];
    		int i = 0;
    		while (rs.next()) {

    			fieldName[i] = rs.getString("Field");
    			outputStr += "\"" + fieldName[i] + "\",";
    			sqlm += fieldName[i] + ",";
    			if (rs.getString("Type").indexOf("int") != -1
    					|| rs.getString("Type").indexOf("decimal") != -1) {
    				delimitor[i] = "";
    			} else {
    				delimitor[i] = "\"";
    			}
    			i++;
    		}

    		outputStr = outputStr.substring(0, outputStr.length() - 1)
    				+ "\n";
    		sqlm = sqlm.substring(0, sqlm.length() - 1) + " from " + tname;
    		// log_error(sqlm);
    		stmt.execute(sqlm);
    		rs = stmt.getResultSet();

    		while (rs.next()) {
    			for (int j = 0; j < i; j++) {
    				String field_value = rs.getString(fieldName[j]);
    				if (field_value == null
    						|| field_value.equalsIgnoreCase("null")) {
    					field_value = "";
    				}
    				if (field_value.indexOf("\"") != -1) {
    					field_value = field_value.replaceAll("\"", "\"\"");
    					logInfo(field_value);
    				}
    				// if(field_value.equalsIgnoreCase(""))
    				// delimitor[j] = "";
    				outputStr += delimitor[j] + field_value + delimitor[j]
    						+ ",";
    			}
    			outputStr = outputStr.substring(0, outputStr.length() - 1)
    					+ "\n";
    		}

    		return outputStr;
    	} catch (SQLException e) {
    		logError("ADMIN INFO - CREATE CSV FILE: " + e.toString(), e);
    		log.error("Database Error while download invitee list ", e);
    		return null;
    	} finally {
    		if (stmt != null) {
    			try {
    				stmt.close();
    			} catch (SQLException e) {
    				log.error("SQL connection closing failed", e);
    			}
    		}
    		if (conn != null) {
    			try {
    				conn.close();
    			} catch (SQLException e) {
    				log.error("SQL connection closing failed", e);
    			}
    		}
    	}
    }

    /**
     * Returns a string form of the file read from the file system.
     * 
     * @param 	filePath	Path where to find the files.
     * @param 	fileName	Name of the file to be read.
     * @return	String		String form of the given filename
     */
    public String buildXmlCssSql(String filePath, String fileName) {

    	log.info("The file name and filePath are :" + filePath + " " + fileName);
    	InputStream fileInputStream = CommonUtils.loadResource(filePath
    			+ fileName);
    	StringBuffer strBuff = new StringBuffer();
    	int ch;

    	if (fileInputStream != null) {
    		try {
    			while ((ch = fileInputStream.read()) != -1) {
    				strBuff.append(Character.valueOf((char) ch));
    			}
    		} catch (IOException e) {
    			log.error("I/O error occured", e);
    			return strBuff.toString();
    		} finally {
    			if (fileInputStream != null) {
    				try {
    					fileInputStream.close();
    				} catch (IOException e) {
    					log.error("I/O error occured", e);
       				}
    			}
    		}
    	}
    	return strBuff.toString();
    }

    /**
     * Decodes a string that has been encoded by encode function.
     * 
     * @param 	charId	String to decode
     * @return	String	Decoded string.	
     */
    @Deprecated
    public static String decode(String charId) {
    	String result = new String();
    	int sum = 0;
    	for (int i = charId.length() - 1; i >= 0; i--) {
    		char c = charId.charAt(i);
    		int remainder = c - 65;
    		sum = sum * 26 + remainder;
    	}

    	sum = sum - 97654;
    	int remain = sum % 31;
    	if (remain == 0) {
    		sum = sum / 31;
    		result = Integer.toString(sum);
    	} else {
    		result = "invalid";
    	}
    	return result;
    }

    /**
     * Encodes a given string.
     * 
     * @param 	userId	String to encode.
     * @return	String	Encoded string.
     */
    @Deprecated
    public static String encode(String userId) {
    	int baseNumb = Integer.parseInt(userId) * 31 + 97654;
    	String s1 = Integer.toString(baseNumb);
    	String s2 = Integer.toString(26);
    	BigInteger b1 = new BigInteger(s1);
    	BigInteger b2 = new BigInteger(s2);

    	int counter = 0;
    	String charId = new String();
    	while (counter < 5) {
    		BigInteger[] bs = b1.divideAndRemainder(b2);
    		b1 = bs[0];
    		int encodeValue = bs[1].intValue() + 65;
    		charId = charId + (new Character((char) encodeValue).toString());
    		counter++;
    	}
    	return charId;
    }

    /**
     * Print invite list of users for a studySpace.
     * 
     * @return	String	HTML format of all the users in the system.
     */
    public String printInvite() {
    	String outputString = "";
    	Connection conn = null;
    	PreparedStatement stmt = null;
    	PreparedStatement stmtm = null;
    	String sql = "SELECT id, firstname, lastname, salutation, email FROM invitee ORDER BY id";
    	
    	String sqlm = "select distinct(invitee) from consent_response where invitee not in " +
    			"(select invitee from consent_response where answer='Y') and invitee= ?";
    	
    	try {
    		conn = this.getDBConnection();
    		stmt = conn.prepareStatement(sql);
    		stmtm = conn.prepareStatement(sqlm);
    		
    		ResultSet rs = stmt.executeQuery();
    		outputString += "<table class=tth border=1 cellpadding=2 cellspacing=0 bgcolor=#FFFFF5>";
    		outputString += "<tr>";
    		outputString += "<th class=sfon></th>";
    		outputString += "<th class=sfon>User ID</th>";
    		outputString += "<th class=sfon>User Name</th>";
    		outputString += "<th class=sfon>User's Email Address</th></tr>";

    		while (rs.next()) {
    			stmtm.clearParameters();
    			stmtm.setInt(1, Integer.parseInt(rs.getString(1)));
    			stmtm.execute(sqlm);
    			ResultSet rsm = stmtm.getResultSet();
    			if (rsm.next()) {
    				outputString += "<tr bgcolor='#E4E4E4'>";
    			} else {
    				outputString += "<tr>";
    			}
    			outputString += "<td><input type='checkbox' name='user' value='"
    					+ rs.getString(1) + "'></td>";
    			outputString += "<td>" + rs.getString(1) + "</td>";
    			outputString += "<td>" + rs.getString(4) + " "
    					+ rs.getString(2) + " " + rs.getString(3) + "</td>";
    			outputString += "<td>" + rs.getString(5) + "</td>";
    			outputString += "</tr>";
    		}
    		rs.close();
    		outputString += "</table>";
    		conn.close();
    	} catch (SQLException e) {
    		logError("ADMIN APPLICATION - PRINT INVITE: " + e.toString(), e);
    	} catch (NumberFormatException e) {
    		logError("ADMIN APPLICATION - PRINT INVITE: " + e.toString(), e);
    	} finally {
    		try {
	    		if (stmt != null) {
	    			stmt.close();
	    		}
	    		if (stmtm != null) {
	    			stmtm.close();
	    		}
	    		if (conn != null) {
	    			conn.close();
	    		} 
    		} catch (SQLException e) {
				log.error("check why prepared statement creation failed", e);
    		}    		
    	}
    	return outputString;
    }

    /**
     * Returns HTML showing counts of users in each state.
     * 
     * @param 	surveyId	Survey Id for which all non responders and incompleters 
     * 				    	invitees have to listed.
     * @return	String		Returns HTML format of the count of invitees in various states.
     */
    public String getUserCountsInStates(String surveyId) {
    	String outputString = "";
    	// Hashtable states_counts = new Hashtable();
    	int nNotInvited = 0, nInvited = 0, nDeclined = 0, nStarted = 0, nStartReminded = 0;
    	int nNotResponded = 0, nInterrupted = 0, nCompleteReminded = 0, nNotCompleted = 0, nCompleted = 0;
    	int nAll = 0;
    	
    	Connection conn = null;
    	PreparedStatement stmt1 = null;
    	PreparedStatement stmt2 = null;
    	String sql1 = "select count(distinct id) as uninvited from invitee where id not in " +
				"(select invitee from survey_user_state where survey=?)";
    	String sql2 = "select count(distinct invitee) as counts, state from survey_user_state where survey=?"
				+ " group by state order by state";
    	try {
    		
    		/* connect to the database */
    		conn = getDBConnection();
    		stmt1 = conn.prepareStatement(sql1);
    		stmt2 = conn.prepareStatement(sql2);
    		outputString += "<table border=0>";

    		stmt1.setString(1, surveyId);
    		ResultSet rs1 = stmt1.executeQuery();
    		while (rs1.next()) {
    			nNotInvited = rs1.getInt("uninvited");
    		}
    		nAll += nNotInvited;

    		stmt2.setString(1, surveyId);
    		ResultSet rs2 = stmt2.executeQuery();
    		while (rs2.next()) {
    			if (rs2.getString("state").equalsIgnoreCase("invited")) {
    				nInvited = rs2.getInt("counts");
    				nAll += nInvited;
    			}
    			if (rs2.getString("state").equalsIgnoreCase("declined")) {
    				nDeclined = rs2.getInt("counts");
    				nAll += nDeclined;
    			}
    			if (rs2.getString("state").equalsIgnoreCase("started")) {
    				nStarted = rs2.getInt("counts");
    				nAll += nStarted;
    			}
    			if (rs2.getString("state").equalsIgnoreCase("interrupted")) {
    				nInterrupted = rs2.getInt("counts");
    				nAll += nInterrupted;
    			}
    			if (rs2.getString("state").indexOf("start_reminder") != -1) {
    				nStartReminded += rs2.getInt("counts");
    				nAll += nStartReminded;
    			}
    			if (rs2.getString("state").equalsIgnoreCase("non_responder")) {
    				nNotResponded = rs2.getInt("counts");
    				nAll += nNotResponded;
    			}
    			if (rs2.getString("state").indexOf("completion_reminder") != -1) {
    				nCompleteReminded += rs2.getInt("counts");
    				nAll += nCompleteReminded;
    			}
    			if (rs2.getString("state").equalsIgnoreCase("incompleter")) {
    				nNotCompleted = rs2.getInt("counts");
    				nAll += nNotCompleted;
    			}
    			if (rs2.getString("state").equalsIgnoreCase("completed")) {
    				nCompleted = rs2.getInt("counts");
    				nAll += nCompleted;
    			}
    		}

    		outputString += "<tr><td><p class=\"status\">All</p></td><td align=center><a href='show_people.jsp?s="
    				+ surveyId + "&st=all'>" + nAll + "</a></td></tr>";
    		outputString += "<tr><td><p class=\"status\">Not Invited</p></td><td align=center><a href='show_people.jsp?s="
    				+ surveyId
    				+ "&st=not_invited'>"
    				+ nNotInvited
    				+ "</td></tr>";

    		outputString += "<tr><td><p class=\"status-category\"><u>Not Started</u></p></td><td></td></tr>";
    		outputString += "<tr><td><p class=\"status\">Invited</p></td><td align=center><a href='show_people.jsp?s="
    				+ surveyId
    				+ "&st=invited'>"
    				+ nInvited
    				+ "</a></td></tr>";
    		outputString += "<tr><td><p class=\"status\">Reminder Sent</p></td><td align=center><a href='show_people.jsp?s="
    				+ surveyId
    				+ "&st=start_reminder'>"
    				+ nStartReminded
    				+ "</a></td></tr>";

    		outputString += "<tr><td><p class=\"status-category\"><u>Incomplete</u></p></td><td/></tr>";
    		outputString += "<tr><td><p class=\"status\">Currently Taking</p></td><td align=center><a href='show_people.jsp?s="
    				+ surveyId
    				+ "&st=started'>"
    				+ nStarted
    				+ "</a></td></tr>";
    		outputString += "<tr><td><p class=\"status\">Interrupted</p></td><td align=center><a href='show_people.jsp?s="
    				+ surveyId
    				+ "&st=interrupted'>"
    				+ nInterrupted
    				+ "</a></td></tr>";
    		outputString += "<tr><td><p class=\"status\">Reminder Sent</p></td><td align=center><a href='show_people.jsp?s="
    				+ surveyId
    				+ "&st=completion_reminder'>"
    				+ nCompleteReminded + "</a></td></tr>";

    		outputString += "<tr><td><p class=\"status-category\"><u>End States</u></p></td><td/></tr>";
    		outputString += "<tr><td><p class=\"status\">Completed</p></td><td align=center><a href='show_people.jsp?s="
    				+ surveyId
    				+ "&st=completed'>"
    				+ nCompleted
    				+ "</a></td></tr>";
    		outputString += "<tr><td><p class=\"status\">Incompleter</p></td><td align=center><a href='show_people.jsp?s="
    				+ surveyId
    				+ "&st=incompleter'>"
    				+ nNotCompleted
    				+ "</a></td></tr>";
    		outputString += "<tr><td><p class=\"status\">Nonresponder</p></td><td align=center><a href='show_people.jsp?s="
    				+ surveyId
    				+ "&st=non_responder'>"
    				+ nNotResponded
    				+ "</a></td></tr>";
    		outputString += "<tr><td><p class=\"status\">Declined</p></td><td align=center><a href='show_people.jsp?s="
    				+ surveyId
    				+ "&st=declined'>"
    				+ nDeclined
    				+ "</a></td></tr>";

    		outputString += "</table>";
    		rs1.close();
    		rs2.close();
    	} catch (SQLException e) {
    		logError("ADMIN APPLICATION - GET USER COUNTS IN STATES: " + e.toString(),
    				e);
    	} finally {
    		try {
	    		if (stmt1 != null) {
	    			stmt1.close();
	    		}
	    		if (stmt2 != null) {
	    			stmt2.close();
	    		}
	    		if (conn != null) {
	    			conn.close();
	    		} 
    		} catch (SQLException e) {
				log.error("check why prepared statement creation failed", e);
    		}    		
    	}
    	return outputString;
    }

    /**
     * Prints the user groups identified by their states.
     * 
     * @param 	state		State of the invitees who are supposed to be returned.
     * @param 	surveyId	Survey Id for which invitees have to be listed.
     * @return	String		HTML format of the invitees who belong to a state given.
     */
    public String printUserState(String state, String surveyId) {
    	String outputString = "";
    	try {
    		/* connect to the database */
    		Connection conn = getDBConnection();
    		Statement stmt = conn.createStatement();
    		String sql = "";

    		if (state.equalsIgnoreCase("not_invited")) {

    			outputString += "<tr><td class=sfon align=center>ID</td>"
    					+ "<td class=sfon align=center>Name</td>"
    					+ "<td class=sfon align=center>Email Address</td></tr>";

    			sql = "select id, firstname, lastname, AES_DECRYPT(email,'"
    					+ this.myStudySpace.db.emailEncryptionKey
    					+ "') as email from invitee where id not in (select invitee from "
    					+ "survey_user_state where survey='"
    					+ surveyId + "')";
    			stmt.execute(sql);
    			ResultSet rs = stmt.getResultSet();
    			while (rs.next()) {
    				outputString += "<tr><td align=center>"
    						+ rs.getString("id") + "</td>";
    				outputString += "<td align=center>"
    						+ rs.getString("firstname") + " "
    						+ rs.getString("lastname") + "</td>";
    				outputString += "<td align=center>" + rs.getString("email")
    						+ "</td></tr>";
    			}
    		} else if (state.equalsIgnoreCase("all")) {
    			
    			/* all users who have been invited */
    			outputString += "<tr><td class=sfon align=center>ID</td>"
    					+ "<td class=sfon align=center>Name</td>"
    					+ "</td><td class=sfon align=center>State</td>"
    					+ "<td class=sfon align=center>Email</td>";
    			sql = "select i.id, i.firstname, i.lastname, AES_DECRYPT(i.email, '"
    					+ this.myStudySpace.db.emailEncryptionKey
    					+ "') as email, u.state as state "
    					+ "from invitee as i, survey_user_state as u "
    					+ "where i.id=u.invitee and u.survey='"
    					+ surveyId
    					+ "' order by i.id";
    			stmt.execute(sql);
    			ResultSet rs = stmt.getResultSet();
    			//String user_id = "";
    			while (rs.next()) {
    				outputString += "<tr><td align=center>"
    						+ rs.getString("id") + "</td>";
    				outputString += "<td align=center>"
    						+ rs.getString("firstname") + " "
    						+ rs.getString("lastname") + "</td>";
    				outputString += "<td align=center>" + rs.getString("state")
    						+ "</td>";
    				outputString += "<td align=center>" + rs.getString("email")
    						+ "</td></tr>";
    			}
    			
    			/* all users who have not been invited */
    			sql = "select id, firstname, lastname, AES_DECRYPT(email,'"
    					+ this.myStudySpace.db.emailEncryptionKey
    					+ "') as email from invitee where id not in (select invitee " 
    					+ "from survey_user_state where survey='"
    					+ surveyId + "')";
    			stmt.execute(sql);
    			rs = stmt.getResultSet();
    			while (rs.next()) {
    				outputString += "<tr><td align=center>"
    						+ rs.getString("id") + "</td>";
    				outputString += "<td align=center>"
    						+ rs.getString("firstname") + " "
    						+ rs.getString("lastname") + "</td>";
    				outputString += "<td align=center>" + "Not Invited"
    						+ "</td>";
    				outputString += "<td align=center>" + rs.getString("email")
    						+ "</td></tr>";
    			}
    		} else {
    			outputString += "<tr><td class=sfon align=center>ID</td>"
    					+ "<td class=sfon align=center>Name</td>"
    					+ "</td><td class=sfon align=center>State</td>"
    					+ "<td class=sfon align=center>Entry Time</td>"
    					+ "<td class=sfon align=center>Email</td>"
    					+ "<td class=sfon align=center>Messages (Sent Time)";
    			sql = "select i.id, firstname, lastname, AES_DECRYPT(email, '"
    					+ this.myStudySpace.db.emailEncryptionKey
    					+ "') as email, state, entry_time, message, sent_date "
    					+ "from invitee as i, survey_message_use as m, survey_user_state as u "
    					+ "where i.id = m.invitee and i.id=u.invitee and m.survey=u.survey and u.survey='"
    					+ surveyId + "' " + "and state like '" + state
    					+ "%' order by i.id";

    			stmt.execute(sql);
    			ResultSet rs = stmt.getResultSet();
    			String userId = "", lastUserId = "";
    			while (rs.next()) {
    				userId = rs.getString("id");
    				if (!userId.equalsIgnoreCase(lastUserId)) {
    					lastUserId = userId;
    					
    					/* print out the new row */
    					outputString += "</td></tr><tr><td align=center>"
    							+ userId + "</td>";
    					outputString += "<td align=center>"
    							+ rs.getString("firstname") + " "
    							+ rs.getString("lastname") + "</td>";
    					outputString += "<td align=center>"
    							+ rs.getString("state") + "</td>";
    					outputString += "<td align=center>"
    							+ rs.getString("entry_time") + "</td>";
    					outputString += "<td align=center>"
    							+ rs.getString("email") + "</td>";
    					outputString += "<td align=center>"
    							+ rs.getString("message") + " "
    							+ rs.getString("sent_date");
    				} else {
    					
    					/* append other messages under the same invitee ID */
    					outputString += "<br>" + rs.getString("message") + " "
    							+ rs.getString("sent_date");
    				}
    			}
    			outputString += "</td></tr>";
    		}

    		stmt.close();
    		conn.close();
    	} catch (SQLException e) {
    		logError("ADMIN INFO - PRINT USER STATE: " + e.toString(), e);
    	} 
    	return outputString;
    }

    /**
     * Prints the Audit logs of the users
     * 
     * @return String	HTML format of the audit logs
     */
    public String printAuditLogs(){
    	String outputString="";
    	Connection conn = null;
    	PreparedStatement stmt = null;
    	String sql = "select invitee, concat(firstname,' ',lastname) as name, AES_DECRYPT(patient_name,'"
				+ this.myStudySpace.db.emailEncryptionKey
				+ "')as ptname,ipAddress ,actions,updated_time from audit_logs";
    	try {

    		/* connect to the database */
    		conn = getDBConnection();
    		stmt = conn.prepareStatement(sql);
    		outputString += "<tr><td class=sfon align=center>ID</td>"
    				+ "<td class=sfon align=center>User Name</td>"
    				+ "<td class=sfon align=center>Patient Name</td>"
    				+ "<td class=sfon align=center>IP Address</td>"
    				+ "<td class=sfon align=center>Action</td>"
    				+ "<td class=sfon align=center>TimeStamp</td></tr>";

    		ResultSet rs = stmt.executeQuery();
    		while (rs.next()) {
    			outputString += "<tr><td align=center>"
    					+ rs.getString("invitee") + "</td>";
    			outputString += "<td align=center>"
    					+ rs.getString("name") + "</td>";
    			outputString += "<td align=center>" + rs.getString("ptname")
    					+ "</td>";
    			outputString += "<td align=center>" + rs.getString("ipAddress")
    					+ "</td>";
    			outputString += "<td align=center>" + rs.getString("actions")
    					+ "</td>";
    			outputString += "<td align=center>" + rs.getString("updated_time")
    					+ "</td></tr>";
    		}
    		stmt.close();
    		conn.close();
    	} catch (SQLException e) {
    		logError("ADMIN INFO - PRINT AUDIT LOGS: " + e.toString(), e);
    	} finally {
    		try {
	    		if (stmt != null) {
	    			stmt.close();
	    		}
	    		if (conn != null) {
	    			conn.close();
	    		} 
    		} catch (SQLException e) {
				log.error("check why prepared statement creation failed", e);
    		}    		
    	}
    	return outputString;
    }

    /**
     * Forms a remote URL
     * 
     * @param 	fileType	File type name.
     * @param 	studyName	Study space name.
     * @return	String		URL formed
     */
    public String makeRemoteURL(String fileType, String studyName) {
    	String urlStr = myStudySpace.servletUrlRoot
    			+ WiseConstants.SURVEY_APP + "/" + "admin_" + fileType
    			+ "_loader" + "?SID=" + studyId + "&SurveyName=" + studyName;
    	return urlStr;
    }

    // call the "loader" servlet (survey_loader, preface_loader, etc.) to notify
    // remote
    // Surveyor Application that a survey or some other file has changed and
    // needs to be flushed and reread
    // Not using on Ansari because Apache seems to be blocking the local call
    /*public String load_remote(String file_type, String study_name) {
	String url_str = make_remoteURL(file_type, study_name);
	log.error("The Url accessed while uploading the data is " + url_str);
	String upload_result = "";
	URL url = null;
	BufferedReader in = null;
	String current_line = null;

	try {
	    url = new URL(url_str);
	    in = new BufferedReader(new InputStreamReader(url.openStream()));
	    while ((current_line = in.readLine()) != null) {
		upload_result += current_line;
	    }
	} catch (IOException e) {
	    log.error("Reader failed to read ", e);
	    log_error("Wise error: Remote " + file_type + " load error after"
		    + upload_result + ": " + e.toString(), e);
	} finally {
	    if (in != null) {
		try {
		    in.close();
		} catch (IOException e) {
		    // That's okie!
		    log.error("Reader Stream close failure ", e);
		}
	    }

	}
	return upload_result;
    }*/
    
    /**
     * Loads the remote URL.
     * @param 	fileType	File name to form URL.
     * @param 	studyName	Study space name.
     * @return	String		
     */
    public String loadRemote(String fileType, String studyName) {
    	String urlStr = makeRemoteURL(fileType, studyName);
    	log.info("The Url accessed while uploading the data is " + urlStr);
    	String uploadResult = "";
    	//URL url = null;
    	BufferedReader in = null;
    	String currentLine = null;

    	try {
    		X509TrustManager tm = new X509TrustManager() {
    			@Override
    			public X509Certificate[] getAcceptedIssuers() {
    				return null;
    			}

    			@Override
    			public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
    			}

    			@Override
    			public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
    			}
    		};

    		SSLContext ctx = SSLContext.getInstance("TLS");
    		ctx.init(null, new TrustManager[] { tm }, null);
    		javax.net.ssl.HttpsURLConnection conn = (javax.net.ssl.HttpsURLConnection) new java.net.URL(urlStr).openConnection();
    		conn.setSSLSocketFactory(ctx.getSocketFactory());
    		conn.setHostnameVerifier(new HostnameVerifier() {
    			@Override
    			public boolean verify(String paramString, SSLSession paramSSLSession) {
    				return true;
    			}
    		});
    		in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    		while ((currentLine = in.readLine()) != null){
    			//System.out.println(input);
    			uploadResult += currentLine;
    		}
    		in.close();
    	} catch (NoSuchAlgorithmException e){
    		log.error("Reader failed to read due to ", e);
    		logError("Wise error: Remote " + fileType + " load error after"
    				+ uploadResult + ": " + e.toString(), e);			
    	} catch (KeyManagementException e){
    		log.error("Reader failed to read due to ", e);
    		logError("Wise error: Remote " + fileType + " load error after"
    				+ uploadResult + ": " + e.toString(), e);			
    	} catch ( MalformedURLException e){
    		log.error("Reader failed to read due to ", e);
    		logError("Wise error: Remote " + fileType + " load error after"
    				+ uploadResult + ": " + e.toString(), e);			
    	} catch (IOException e){
    		log.error("Reader failed to read due to ", e);
    		logError("Wise error: Remote " + fileType + " load error after"
    				+ uploadResult + ": " + e.toString(), e);			
    	} catch (Exception e){
    		log.error(e);
    	}finally {
    		if (in != null) {
    			try {
    				in.close();
    			} catch (IOException e) {
    				// That's okie!
    				log.error("Reader Stream close failure ", e);
    			}
    		}
    	}
    	return uploadResult;
    }
 
    /**
     * Return the complete URL to the servlet root directory for the application
     * administering the survey
     * 
     * @return String 	Root servlet URL.
     */
    public String getStudyServerPath() {
    	return myStudySpace.servletUrlRoot;
    }

    //TODO: READ the databank at call time rather than relying on JSP tool to know proper status
    /**
     * Clears, drops, or archives survey data depending on survey's status.
     * D - Clear submitted data from surveys in Development mode
     * R - Remove entire survey in Development mode
     * P - clean up and archive the data of surveys in production mode
     * 
     * @param 	surveyId		Survey name whose status has to be changed.
     * @param 	surveyStatus	Status of the survey to be changed to.
     * @return	String 			
     */
    public String clearSurvey(String surveyId, String surveyStatus) {
    	if (surveyId == null || studyId == null || surveyStatus == null) {
    		return "<p align=center>SURVEY clear ERROR: can't get the survey id/status or study id </p>";
    	}
    	DataBank db = myStudySpace.db;
    	Survey survey = myStudySpace.getSurvey(surveyId);
    	if (surveyStatus.equalsIgnoreCase("D")) {
    		return db.clearSurveyData(survey);
    	} else if (surveyStatus.equalsIgnoreCase("R")) {
    		return db.deleteSurvey(survey);
    	} else if (surveyStatus.equalsIgnoreCase("P")) {
    		return db.archiveProdSurvey(survey);
    	}
    	return "Unrecognized Survey Status/Type";
    }

    /**
     * Returns number of login attempt.
     * 
     * @return the loginAttemptNumbers
     */
    public static ConcurrentHashMap<String, Integer> getLoginAttemptNumbers() {
    	return loginAttemptNumbers;
    }

    /**
     * Returns the last logged in time.
     * 
     * @return the lastlogintime
     */
    public static ConcurrentHashMap<String, Long> getLastlogintime() {
    	return lastLoginTime;
    }
}
