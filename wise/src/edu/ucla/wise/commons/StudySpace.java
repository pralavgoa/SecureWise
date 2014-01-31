package edu.ucla.wise.commons;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;

import edu.ucla.wise.initializer.StudySpaceParametersProvider;
import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;

/**
 * Study space is the core of WISE system -- represents the core abstractions
 * for individual survey projects.
 *  
 * @author Douglas Bell
 * @version 1.0  
 */
public class StudySpace {

	private static Logger log = Logger.getLogger(StudySpace.class);

	/** CLASS STATIC VARIABLES */
	private static Hashtable<String, StudySpace> ALL_SPACES; // contains actual study spaces indexed by name

	/* 
	 * Contains index of all study names in the properties file by ID.
	 * NOTE: Properties are read once at startup, therefore must restart server
	 * if a Study Space is added
	 */
	private static Hashtable<String, String> SPACE_NAMES; 
	// public static String xml_loc;
	public static String font = "<font face='Verdana, Arial, Helvetica, sans-serif' size='-1'>";

	/** INSTANCE VARIABLES */
	public Hashtable<String, Survey> surveys;
	public Preface preface;

	public String id; // the study_space's number, which can be encoded
	public String studyName;
	public String title;

	// DIRECTORIES AND PATHS
	public String serverUrl;
	public String dirName;
	private String prefacePath;
	private String application;
	public String appUrlRoot, servletUrlRoot;
	public String sharedFileUrlRoot, styleUrl, imageUrl;
	public String emailSendingTime;

	public DataBank db; // one DB per SS

	/** CLASS FUNCTIONS */

	/** static initializer */
	static {
		ALL_SPACES = new Hashtable<String, StudySpace>();
		SPACE_NAMES = new Hashtable<String, String>();

		/* better not to parse all ss's in advance */
		// Load_Study_Spaces();
	}

	/**
	 * Sets all the study spaces in the WISE system.
	 */
	public static void setupStudies() {
		DataBank.SetupDB(WISEApplication.wiseProperties);

		/* 
		 * Just read the names of all unique Studies and save the name:ID pairs
		 * in a hash for quicker lookup later
		 * note when called by a reload, does not drop already-parsed studies
		 * but does reread props file to enable load of new studies
		 * TODO (low): consider a private "stub" class to hold all values from
		 * props file without parsing XML file
		 */		
		Map<String,StudySpaceParameters> allSpaceParams = StudySpaceParametersProvider.getInstance().getStudySpaceParametersMap();
		Iterator<String> allSpaceParamsItr = allSpaceParams.keySet().iterator();

		while (allSpaceParamsItr.hasNext()) {
			String spaceName = allSpaceParamsItr.next();
			SPACE_NAMES.put(allSpaceParams.get(spaceName).getId(), spaceName);
		}
		log.info("study space setup complete");

		/*
		Enumeration enu = WISE_Application.sharedProps.getKeys();
		while (enu.hasMoreElements()) {
		    String key = (String) enu.nextElement();
		    if (key.indexOf(".studyid") != -1) // pull out just the study ID
						       // properties
		    {
			String idNum = WISE_Application.sharedProps.getString(key);
			String study_name = key.substring(0, key.indexOf(".studyid"));
			SPACE_names.put(idNum, study_name);
		    }
		}

		 */
	}

	/**
	 * Search by the numeric study ID and return the StudySpace instance
	 * 
	 * @param studyID
	 * @return
	 */
	public static StudySpace getSpace(String studyID) {
		if (SPACE_NAMES == null || ALL_SPACES == null) {
			WISEApplication.logError(
					"GET Study Space failure - hash uninitialized. Try server restart on "
							+ WISEApplication.rootURL + ", "
							+ SurveyorApplication.ApplicationName, null);
		}
		StudySpace ss = ALL_SPACES.get(studyID);		
		if (ss == null) {
			String sName = SPACE_NAMES.get(studyID);
			if (sName != null) {
				ss = new StudySpace(sName);

				/* put Study_Space in ALL_SPACES */
				ALL_SPACES.put(ss.id, ss);
			}

		}
		return ss;
	}

	/**
	 * Load all the StudySpace objects applicable for the given instance of the application.
	 * 
	 * @return String	Message if the load is successful or not.
	 */
	public static String loadStudySpaces() {
		String spaceName = "";
		String resultstr = "";
		try {
			if (SPACE_NAMES == null || SPACE_NAMES.size() < 1) {
				return "Error: No Study Spaces found in props file";
			}

			Map<String, StudySpaceParameters> allSpaceParams = StudySpaceParametersProvider
					.getInstance().getStudySpaceParametersMap();	
			log.info("There are " + allSpaceParams.size()
					+ " StudySpaceParameters objects");
			Iterator<String> allSpaceNameItr = allSpaceParams.keySet()
					.iterator();

			while (allSpaceNameItr.hasNext()) {

				spaceName = allSpaceNameItr.next();
				String studySvr = allSpaceParams.get(spaceName).getServerUrl();
				String studyApp = allSpaceParams.get(spaceName)
						.getServerApplication();

				if (studySvr.equalsIgnoreCase(WISEApplication.rootURL)
						&& studyApp.equalsIgnoreCase(SurveyorApplication.ApplicationName)
						&& !Strings.isNullOrEmpty(spaceName)) {

					/* create new StudySpace */
					StudySpace ss = new StudySpace(spaceName);

					/* put StudySpace in ALL_SPACES */
					ALL_SPACES.put(ss.id, ss);
					resultstr += "Loaded Study Space: " + ss.id + " for user "
							+ ss.db.dbuser + " <BR>\n";
				}

			}

			/*
			 * 20dec // get study space info from shared properties Enumeration
			 * enu = SPACE_names.keys(); while (enu.hasMoreElements()) { studyID
			 * = (String) enu.nextElement(); study_name =
			 * SPACE_names.get(studyID); String studySvr =
			 * WISE_Application.sharedProps .getString(study_name + ".server");
			 * String studyApp = WISE_Application.sharedProps
			 * .getString(study_name + ".serverApp"); if
			 * (studySvr.equalsIgnoreCase(WISE_Application.rootURL) && studyApp
			 * .equalsIgnoreCase(Surveyor_Application.ApplicationName) &&
			 * study_name != null && !study_name.equals("")) { // create new
			 * Study_Space Study_Space ss = new Study_Space(study_name); // put
			 * Study_Space in ALL_SPACES ALL_SPACES.put(ss.id, ss); resultstr +=
			 * "Loaded Study Space: " + ss.id + " for user " + ss.db.dbuser +
			 * " <BR>\n"; } }
			 */
		} catch (ClassCastException  e) {
			log.error("Load Study Spaces Error for  name " + spaceName, e);
		} catch (NullPointerException  e) {
			log.error("Load Study Spaces Error for  name " + spaceName, e);
		}
		return resultstr;
	}

	/**
	 * Constructor to create study space and initialize the surveys & messages hashtables
	 * 
	 * @param	studyName 	Name of the study space that has to be initialized.
	 */
	public StudySpace(String studyName) {
		if (studyName == null || studyName.equals(""))  {// will still return an uninitialized instance
			return;
		}
		this.studyName = studyName;
		StudySpaceParameters spaceParams = StudySpaceParametersProvider
				.getInstance().getStudySpaceParameters(studyName);

		db = new DataBank(this, spaceParams); // one DB per SS

		/* Construct instance variables for this particular study space */
		id = spaceParams.getId();

		// 20dec id = WISE_Application.sharedProps.getString(studyName +
		// ".studyid");

		title = spaceParams.getProjectTitle();

		// 20dec title = WISE_Application.sharedProps.getString(studyName
		// + ".proj.title");

		/*
		 *  SET UP all of the paths that will apply for this Study Space,
		 *  regardless of the app instantiating it
		 */
		serverUrl = spaceParams.getServerUrl();
		// 20dec server_url =
		// WISE_Application.sharedProps.getString(studyName
		// + ".server");

		String dirInProps = spaceParams.getFolderName();

		// 20decString dir_in_props = WISE_Application.sharedProps
		// .getString(studyName + ".dirName");

		if (dirInProps == null) {
			dirName = studyName; // default
		} else {
			dirName = dirInProps;
		}
		application = spaceParams.getServerApplication();
		emailSendingTime = spaceParams.getEmailSendingTime();

		// 20dec application =
		// WISE_Application.sharedProps.getString(studyName
		// + ".serverApp");
		// Manoj changes
		// servlet_urlRoot = server_url + "/"+ application + "/servlet/";
		appUrlRoot = serverUrl + "/" + application + "/";
		servletUrlRoot = serverUrl + "/" + application + "/";
		sharedFileUrlRoot = appUrlRoot
				+ spaceParams.getSharedFiles_linkName() + "/";

		// 20dec sharedFile_urlRoot = app_urlRoot
		// + WISE_Application.sharedProps.getString(studyName
		// + ".sharedFiles_linkName") + "/";

		/* project-specific styles and images need to be in shared area so
		 * they can be uploaded by admin server
		 */
		styleUrl = sharedFileUrlRoot + "style/" + dirName + "/";
		imageUrl = sharedFileUrlRoot + "images/" + dirName + "/";

		/* create & initialize the Preface */
		prefacePath = SurveyorApplication.wiseProperties.getApplicationName() + "/" + dirName
				+ "/preface.xml";
		loadPreface();

		/* create the message sender */
		surveys = new Hashtable<String, Survey>();
		db.readSurveys();		
	}

	/**
	 * Returns all the study spaces in the system. This is used while sending emails.
	 * 
	 * 
	 * @return	StudySpace	Array of studySpaces in the system.
	 */
	public static StudySpace[] getAll() {
		int nSpaces = ALL_SPACES.size();
		log.info("There are " + nSpaces + " Study Spaces");
		if (nSpaces < 1) {
			loadStudySpaces();
			nSpaces = ALL_SPACES.size();
			log.info("Loaded " + nSpaces + " study spaces");
		}
		StudySpace[] result = new StudySpace[nSpaces];
		Enumeration<StudySpace> et = StudySpace.ALL_SPACES.elements();
		int i = 0;
		while (et.hasMoreElements() && i < nSpaces) {
			result[i++] = et.nextElement();
		}
		return result;
	}

	/**
	 * Deconstructor to destroy the surveys and messages hashtables
	 */
	public void destroy() {
		surveys = null;
	}

	/** 
	 * Establishes data base connection and returns a Connection object.
	 * 
	 * @return	Connection 	Connection object used to talk to data base.
	 */
	public Connection getDBConnection() throws SQLException {
		// return
		// DriverManager.getConnection(mysql_url+dbdata+"?user="+dbuser+"&password="+dbpwd);
		// return
		// DriverManager.getConnection("jdbc:mysql://"+mysql_server+"/"+dbdata+"?user="+dbuser+"&password="+dbpwd);
		return db.getDBConnection();
	}

	/**
	 * Returns the DataBank class that is linked to this studySpace.
	 * 
	 * @return DataBank		DataBank instance.
	 */
	public DataBank getDB() {
		return db;
	}

	/**
	 * Searches by the survey ID and returns a specific survey
	 * 
	 * @param  surveyId	Survey Id which has to be returned.
	 * @return Survey	Survey object associated with the surveyId.
	 */
	public Survey getSurvey(String surveyId) {
		Survey s = surveys.get(surveyId);
		return s;
	}

	/**
	 * Load or Reload a survey from file, return survey ID or null if unsuccessful
	 * 
	 * @param 	filename	File from which the survey has to be loaded.     * 
	 * @return	String		The survey Id  that has been loaded.	
	 */
	public String loadSurvey(String filename) {
		String sid = null;
		Survey s;
		try {
			// String file_loc = SurveyorApplication.xmlLoc
			//	 		+ System.getProperty("file.separator") + dirName
			//			+ System.getProperty("file.separator") + filename;
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setCoalescing(true);
			factory.setExpandEntityReferences(false);
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(true);

			/*
			 * Document xml_doc = factory.newDocumentBuilder().parse(
			 * CommonUtils.loadResource(file_loc));
			 */

			log.info("Fetching survey file " + filename + " from database for "
					+ studyName);
			InputStream surveyFileInputStream = db.getXmlFileFromDatabase(
					filename, studyName);

			if (surveyFileInputStream == null) {
				throw new FileNotFoundException();
			}

			Document xmlDoc = factory.newDocumentBuilder().parse(
					surveyFileInputStream);

			s = new Survey(xmlDoc, this);
			if (s != null) {
				sid = s.getId();
				surveys.put(sid, s);
			}

		} catch(DOMException e){
			log.error(
					"WISE - SURVEY parse error: " + e.toString() + "\n" + id
					+ "\n" + this.toString(), null);

		}
		catch (FileNotFoundException e) {
			log.error("Study Space " + dirName
					+ " failed to parse survey " + filename + ". Error: " + e, e);
		} catch (SAXException e) {
			log.error("Study Space " + dirName + " failed to parse survey "
					+ filename + ". Error: " + e, e);
		} catch (ParserConfigurationException e) {
			log.error("Study Space " + dirName + " failed to parse survey "
					+ filename + ". Error: " + e, e);
		} catch (IOException e) {
			log.error("Study Space " + dirName + " failed to parse survey "
					+ filename + ". Error: " + e, e);
		}
		return sid;	
	}

	/**
	 * Drops survey related to surveyId from the study space.
	 * 
	 * @param surveyId Id of the survey to be dropped.
	 */
	public void dropSurvey(String surveyId) {
		surveys.remove(surveyId);
	}

	/**
	 * Loads the preface file
	 * 
	 * @return boolean 	True if the preface load happens correctly else false.
	 */
	public boolean loadPreface() {

		// TODO: check admin; call when new preface uploaded
		preface = new Preface(this, "preface.xml");
		if (preface == null) {
			return false;
		}
		preface.setHrefs(servletUrlRoot, imageUrl);
		return true;
	}

	/**
	 * Gets a preface
	 * 
	 * @return	Preface	preface object related to this study space is returned.
	 */
	public Preface get_preface() {
		if (preface == null) {// should happen only if there's been some major problem
			if (!loadPreface()) {
				WISEApplication.logInfo("Study Space " + dirName
						+ " failed to load its preface file ");
				return null;
			}
		}
		return preface;
	}

	/**
	 * Returns the User object linked with the message ID.
	 * 
	 * @param 	msgId	Message Id from the URL whose user object is to be initialized.     * 
	 * @return	User	Newly created User Object from the provided message ID.
	 */
	public User getUser(String msgId) {
		return db.makeUserFromMsgID(msgId);
	}

	/**
	 * Method that calls the private send method to send emails to users.
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
	 * @param 	displayMessage	This is used to send the message back as output for the case of 
	 * 							anonymous users
	 * @return	String 			output message or message use ID for the invitee to whom email is sent.
	 */
	public String sendInviteReturnDisplayMessage(String msg_type,
			String message_seq_id, String survey_id, String whereStr,
			boolean isReminder) {
		return sendMessages(msg_type, message_seq_id, survey_id, whereStr,
				isReminder, true);
	}

	/**
	 * Method that calls the private send method to send emails to users.
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
	 * @param 	displayMessage	This is used to send the message back as output for the case of 
	 * 							anonymous users
	 * @return	String 			output message or message use ID for the invitee to whom email is sent.
	 */
	public String sendInviteReturnMsgSeqId(String msg_type,
			String message_seq_id, String survey_id, String whereStr,
			boolean isReminder) {
		return sendMessages(msg_type, message_seq_id, survey_id, whereStr,
				isReminder, false);
	}

	/* // send message to all invitees who match on whereStr
    private String send_messages(String msg_type, String message_seq_id,
	    String survey_id, String whereStr, boolean isReminder,
	    boolean displayMessage) {

	String messageSequenceId = null;
	// look up the correct message sequence in preface
	Message_Sequence msg_seq = this.preface
		.get_message_sequence(message_seq_id);
	if (msg_seq == null) {
	    AdminInfo
		    .log_info("ADMIN INFO - SEND MESSAGES: Can't get the requested  message sequence "
			    + message_seq_id + AdminInfo.class.getSimpleName());
	    return null;
	}
	Message msg = msg_seq.get_type_message(msg_type); // passes thru an
	// integer for
	// 'other'
	// messages
	if (msg == null) {
	    AdminInfo
		    .log_info("ADMIN INFO - SEND MESSAGES: Can't get the message from hash");
	    return null;
	}
	String outputString = "";
	Message_Sender sender = new Message_Sender(msg_seq);
	try {
	    Connection conn = getDBConnection();
	    Statement msgUseQry = conn.createStatement();
	    Statement inviteeQuery = conn.createStatement();
	    Statement usrSteQry = conn.createStatement();

	 * Check this function to make it work if multiple invitees are selected
	 * query = "SELECT id FROM invitee WHERE " + whereStr;
ResultSet rs = stmt.executeQuery(query);
String msgUse_sql = "INSERT INTO survey_message_use (messageId,invitee, survey, message) VALUES ";
String messageId= "";
while (rs.next()) {
            int id = rs.getInt("id");
            messageId= org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(22);
			String temp= "('"+ messageId + "'," +  id + ", '"+ survey_id + "', 'attempt')";
			messageId += temp;
			}
messageId += "\;";
	 * 
	    String messageId = org.apache.commons.lang3.RandomStringUtils
		    .randomAlphanumeric(22);

	    // FIRST, to obtain new IDs, insert pending message_use records
	    // for
	    // each subject
	    String msgUse_sql = "INSERT INTO survey_message_use (messageId,invitee, survey, message) "
		    + "SELECT '"
		    + messageId
		    + "', id, '"
		    + survey_id
		    + "', 'attempt' FROM invitee WHERE " + whereStr;
	    AdminInfo.log_info("The sql query run when sending the mail is "+ msgUse_sql);
	    msgUseQry.execute(msgUse_sql);

	    List<String> success_ids = new ArrayList<String>();
	    outputString += "Sending message '" + msg.subject + "' to:<p>";

	    // Now get back newly-created message IDs and invitee data at
	    // the
	    // same time
	    String invitee_sql = "SELECT firstname, lastname, salutation, AES_DECRYPT(email,'"
		    + this.db.email_encryption_key
		    + "'), invitee.id, survey_message_use.messageId "
		    + "FROM invitee, survey_message_use WHERE invitee.id = survey_message_use.invitee "
		    + "AND message = 'attempt' AND survey = '"
		    + survey_id
		    + "' AND " + whereStr;
	    ResultSet rs = inviteeQuery.executeQuery(invitee_sql);

	    // send email message to each selected invitee
	    while (rs.next()) {
		String firstname = rs.getString(1);
		String lastname = rs.getString(2);
		String salutation = rs.getString(3);
		String email = rs.getString(4);
		String invitee_id = rs.getString(5);
		String message_id = rs.getString(6);
		// This is used when for anonymous user. We want to return the
		// message id to the calling function from save_anno_user so
		// that it can forward the survey request automatically.
		messageSequenceId = message_id;
		// print out the user information
		outputString += salutation + " " + firstname + " " + lastname
			+ " with email address &lt;" + email + "&gt; -&gt; ";
		String msg_result = sender.send_message(msg, message_id, email,
			salutation, lastname, this.id);

		if (msg_result.equalsIgnoreCase("")) {
		    outputString += "message sent.<br>";
		    success_ids.add(invitee_id);
		} else {
		    msgUse_sql = "UPDATE survey_message_use SET message= 'err:"
			    + msg_result
			    + "' WHERE message = 'attempt' AND survey = '"
			    + survey_id + "' AND invitee = " + invitee_id;
		    msgUseQry.execute(msgUse_sql);
		    outputString += msg_result + "<br><br>";
		}
		String state = msg_result.equalsIgnoreCase("") ? "invited"
			: "email_error";
		if (msg_type.equalsIgnoreCase("invite")) {
		    String sql_u = "insert into survey_user_state (invitee, state, survey, message_sequence) "
			    + "values("
			    + invitee_id
			    + ", '"
			    + state
			    + "', '"
			    + survey_id
			    + "', '"
			    + message_seq_id
			    + "') "
			    + "ON DUPLICATE KEY UPDATE state='"
			    + state
			    + "', state_count=1, message_sequence=VALUES(message_sequence)";
		    // note timestamp updates automatically
		    usrSteQry.execute(sql_u);
		}
	    }
	    if (success_ids.size() > 0) {
		String successLst = "(";
		for (int i = 0; i < (success_ids.size() - 1); i++) {
		    successLst += success_ids.get(i) + ",";
		}
		successLst += success_ids.get(success_ids.size() - 1) + ")";
		outputString += successLst + "<br><br>";
		// Update survey message use with successes
		msgUse_sql = "UPDATE survey_message_use SET message= '"
			+ msg.id + "' WHERE message = 'attempt' AND survey = '"
			+ survey_id + "' AND invitee in " + successLst;
		msgUseQry.execute(msgUse_sql);
	    }
	    conn.close();
	} catch (Exception e) {
	    AdminInfo.log_error("ADMIN INFO - SEND MESSAGES: " + e.toString(),
		    e);
	}
	// If the call comes from UI, we return outputString, if the call comes
	// from the anno user trying to take the survey we return messageSeqid
	// to the caller.
	return displayMessage ? outputString : messageSequenceId;
    }
	 */


	//NEW send message method

	/*
    // send message to all invitees who match on whereStr
    private String send_messages(String msg_type, String message_seq_id,
	    String survey_id, String whereStr, boolean isReminder,
	    boolean displayMessage) {

	String messageUseId = null;
	// look up the correct message sequence in preface
	Message_Sequence msg_seq = this.preface
		.get_message_sequence(message_seq_id);
	if (msg_seq == null) {
	    AdminInfo
		    .log_info("ADMIN INFO - SEND MESSAGES: Can't get the requested  message sequence "
			    + message_seq_id + AdminInfo.class.getSimpleName());
	    return null;
	}
	Message msg = msg_seq.get_type_message(msg_type); // passes thru an
	// integer for
	// 'other'
	// messages
	if (msg == null) {
	    AdminInfo
		    .log_info("ADMIN INFO - SEND MESSAGES: Can't get the message from hash");
	    return null;
	}
	String outputString = "";
	Message_Sender sender = new Message_Sender(msg_seq);
	try {
	    Connection conn = getDBConnection();
	    Statement inviteeQuery = conn.createStatement();

	   // FIRST, to obtain new IDs, insert pending message_use records
	    // for
	    // each subject

		List<String> success_ids = new ArrayList<String>();
	    outputString += "Sending message '" + msg.subject + "' to:<p>";

	    // Now get back newly-created message IDs and invitee data at
	    // the
	    // same time
		//SQL to return the data only form invitees table

		 String invitee_sql = "SELECT id, firstname, lastname, salutation, AES_DECRYPT(email,'"
		    + this.db.email_encryption_key
		    + "') FROM invitee WHERE " + whereStr;
		 AdminInfo.log_info("The sql query run when selecting the invitees is "+ invitee_sql);
	    ResultSet rs = inviteeQuery.executeQuery(invitee_sql);

	    // send email message to each selected invitee
	    while (rs.next()) {
			String invitee_id = rs.getString(1);
			String firstname = rs.getString(2);
			String lastname = rs.getString(3);
			String salutation = rs.getString(4);
			String email = rs.getString(5);

			//This is used when for anonymous user. We want to return the
			//message id to the calling function from save_anno_user so
			//that it can forward the survey request automatically.


			// print out the user information
			outputString += salutation + " " + firstname + " " + lastname
				+ " with email address &lt;" + email + "&gt; -&gt; ";
			String msg_result = sender.send_message(msg, invitee_id, message_seq_id, email,
				salutation, lastname, this.id,this.db, msg_type,survey_id);

			if (msg_result.length() == 22) {
				outputString += "message sent.<br>";
				success_ids.add(invitee_id);
				messageUseId = msg_result;
			} else {
				 outputString += msg_result + "<br><br>";
			}
		}
	    if (success_ids.size() > 0) {
		String successLst = "(";
		for (int i = 0; i < (success_ids.size() - 1); i++) {
		    successLst += success_ids.get(i) + ",";
		}
		successLst += success_ids.get(success_ids.size() - 1) + ")";
		outputString += successLst + "<br><br>";
		}
	    conn.close();
	} catch (Exception e) {
	    AdminInfo.log_error("ADMIN INFO - SEND MESSAGES: " + e.toString(),
		    e);
	}
	// If the call comes from UI, we return outputString, if the call comes
	// from the anno user trying to take the survey we return messageSeqid
	// to the caller.
	return displayMessage ? outputString : messageUseId;
    } */

	/**
	 * private method which Prepares the message for email depending on the message sequence then
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
	 * @param 	displayMessage	This is used to send the message back as output for the case of 
	 * 							anonymous users
	 * @return	String 			output message or message use ID for the invitee to whom email is sent.
	 */
	private String sendMessages(String msgType, String messageSeqId,
			String surveyId, String whereStr, boolean isReminder,
			boolean displayMessage) {

		String messageUseId = null;

		/* look up the correct message sequence in preface */
		MessageSequence msgSeq = this.preface
				.getMessageSequence(messageSeqId);
		if (msgSeq == null) {
			AdminApplication
			.logInfo("ADMIN INFO - SEND MESSAGES: Can't get the requested  message sequence "
					+ messageSeqId + AdminApplication.class.getSimpleName());
			return null;
		}
		Message msg = msgSeq.getTypeMessage(msgType); // passes thru an integer for 'other' messages
		if (msg == null) {
			AdminApplication
			.logInfo("ADMIN INFO - SEND MESSAGES: Can't get the message from hash");
			return null;
		}
		String outputString = "";
		MessageSender sender = new MessageSender(msgSeq);
		try {
			Connection conn = getDBConnection();
			Statement inviteeQuery = conn.createStatement();

			List<String> successIds = new ArrayList<String>();
			outputString += "Sending message '" + msg.subject + "' to:<p>";

			String inviteeSql = "SELECT id, firstname, lastname, salutation, AES_DECRYPT(email,'"
					+ this.db.emailEncryptionKey
					+ "') FROM invitee WHERE " + whereStr;
			AdminApplication.logInfo("The sql query run when selecting the invitees is "+ inviteeSql);
			ResultSet rs = inviteeQuery.executeQuery(inviteeSql);

			/* send email message to each selected invitee */
			while (rs.next()) {
				String inviteeId = rs.getString(1);
				String firstname = rs.getString(2);
				String lastname = rs.getString(3);
				String salutation = rs.getString(4);
				String email = rs.getString(5);

				/*
				 * This is used when for anonymous user. We want to return the
				 * message id to the calling function from save_anno_user so
				 * that it can forward the survey request automatically. 
				 */

				/* print out the user information */
				outputString += salutation + " " + firstname + " " + lastname
						+ " with email address &lt;" + email + "&gt; -&gt; ";

				messageUseId = db.recordMessageUse("attempt", inviteeId, surveyId);

				String msgResult = sender.sendMessage(msg,messageUseId,
						email,salutation,lastname,this.id, this.db, inviteeId);

				if (msgResult.equalsIgnoreCase("")) {
					outputString += "message sent.<br>";
					successIds.add(inviteeId);
					db.updateMessageUse(msg.id, inviteeId, surveyId);
				} else {
					db.updateMessageUse("err: "+msgResult, inviteeId, surveyId);    				
				}

				if(msgType.equalsIgnoreCase("invite")){
					String state = msgResult.equalsIgnoreCase("") ? "invited" : "email_error";
					db.recordSurveyState(state, inviteeId, surveyId, messageSeqId);
				}

			}
			if (successIds.size() > 0) {
				String successLst = "(";
				for (int i = 0; i < (successIds.size() - 1); i++) {
					successLst += successIds.get(i) + ",";
				}
				successLst += successIds.get(successIds.size() - 1) + ")";
				outputString += successLst + "<br><br>";
			}
			conn.close();
		} catch (SQLException e) {
			AdminApplication.logError("ADMIN INFO - SEND MESSAGES: " + e.toString(), e);
		}

		/*
		 *  If the call comes from UI, we return outputString, if the call comes
		 *  from the anno user trying to take the survey we return messageSeqid
		 *  to the caller.
		 */
		return displayMessage ? outputString : messageUseId;
	}




	/** parse the config file and load all the study spaces */
	/*
	 * public static void load_all_study_spaces() { try {
	 * DriverManager.registerDriver(new com.mysql.jdbc.Driver());
	 * 
	 * // Get parser and an XML document Document doc =
	 * DocumentBuilderFactory.newInstance
	 * ().newDocumentBuilder().parse(config_loc);
	 * 
	 * // parse all study elements in the config file NodeList nl =
	 * doc.getElementsByTagName("Study"); for (int i = 0; i < nl.getLength();
	 * i++) { // create new Study_Space Study_Space ss = new
	 * Study_Space(nl.item(i)); // put Study_Space in ALL_SPACES
	 * ALL_SPACES.put(ss.id,ss); }
	 * 
	 * } catch (Exception e) {
	 * Study_Util.email_alert("WISE - STUDY SPACE - LOAD ALL STUDY SPACES: "
	 * +e.toString()); return; }
	 * 
	 * }
	 */
	/** prints all the study spaces */
	/*
	 * public static String print_ALL() { Study_Space ss;
	 * 
	 * String s = "ALL Study Spaces:<p>"; Enumeration e1 =
	 * ALL_SPACES.elements(); while (e1.hasMoreElements()) { ss = (Study_Space)
	 * e1.nextElement(); s += ss.print(); } s += "<p>"; return s; }
	 */
	/** look up if the user and password exists in the list of study spaces */
	/*
	 * public static String lookup_study_space(String u, String p) { Study_Space
	 * ss; Enumeration e1 = ALL_SPACES.elements(); while (e1.hasMoreElements())
	 * { ss = (Study_Space) e1.nextElement(); if (ss.dbuser.equalsIgnoreCase(u))
	 * if (ss.dbpwd.equalsIgnoreCase(p)) return ss.id; } return null; }
	 */

	/** returns if a specific Study_Space has been loaded */
	/*
	 * public static boolean space_exists(String id) { Study_Space ss =
	 * (Study_Space) ALL_SPACES.get(id); if (ss == null) return false; else
	 * return true; }
	 * 
	 * /** constructor to initialize the surveys and messages hashtables
	 */
	/*
	 * public Study_Space(Node n) { try { // parse the config node id =
	 * n.getAttributes().getNamedItem("ID").getNodeValue(); location =
	 * n.getAttributes().getNamedItem("Location").getNodeValue(); dbdata =
	 * n.getAttributes().getNamedItem("DB_Data").getNodeValue(); dbuser =
	 * n.getAttributes().getNamedItem("DB_User").getNodeValue(); dbpwd =
	 * n.getAttributes().getNamedItem("DB_Password").getNodeValue(); title =
	 * n.getAttributes().getNamedItem("Title").getNodeValue();;
	 * 
	 * style_path = "/wise_test/file/style/" + location + "/";
	 * 
	 * 
	 * msg = new Message(xml_loc + "/" + location + "/messages.xml", this);
	 * if(msg==null) Study_Util.email_alert("study space msg can't be created");
	 * 
	 * // open database connection Connection conn = getDBConnection();
	 * Statement stmt = conn.createStatement();
	 * 
	 * // load all the surveys surveys = new Hashtable(); String sql =
	 * "SELECT filename from surveys, (select max(internal_id) as maxint from surveys group by id) maxes where maxes.maxint = surveys.internal_id"
	 * ; boolean dbtype = stmt.execute(sql); ResultSet rs = stmt.getResultSet();
	 * while (rs.next()) { String filename = rs.getString("filename"); Survey s
	 * = new Survey(filename,this); surveys.put(s.id,s); }
	 * 
	 * // close database stmt.close(); conn.close(); } catch (Exception e) {
	 * Study_Util
	 * .email_alert("WISE - STUDY SPACE - CONSTRUCTOR: "+e.toString()); return;
	 * } }
	 */

	/** 
	 * Prints a specific study space 
	 */
	@Override
	public String toString() {
		String s = "STUDY SPACE<br>";
		s += "ID: " + id + "<br>";
		s += "Location: " + dirName + "<br>";
		s += "Study Name: " + studyName + "<br>";
		// s += "DB Password: "+dbpwd+"<p>";

		/* print surveys */
		s += "<hr>SURVEYS<BR>";
		Survey svy;
		Enumeration<Survey> e1 = surveys.elements();
		while (e1.hasMoreElements()) {
			svy = e1.nextElement();
			s += svy.toString();
		}

		s += "<hr>PREFACE<BR>";
		s += preface.toString();
		return s;
	}

}
