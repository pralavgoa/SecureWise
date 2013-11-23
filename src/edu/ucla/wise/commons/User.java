package edu.ucla.wise.commons;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

/**
 * The User object takes actions and retains data for a specific user session
 * User_DB_Connection is User's interface to Data_Bank (encapsulates
 * user-specific AND database-specific calls).
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class User {

	/* mandatory fields */
	/*
	 * Enums are used if we have a variable taking only a  fixed number of constants types.
	 * Here in this case INVITEE_FIELDS only have limited number of values to represent.
	 * 
	 */
	public enum INVITEE_FIELDS {
		id(null, false), firstname(null, true), lastname(null, true), salutation(
				null, true), email(null, true), phone(null, false), irb_id(null,
				false), field("columnName", false), textField("columnName",
				false), codedField("columnName", false); // optional

		private String attributeName;
		private boolean shouldDisplay;

		private INVITEE_FIELDS(String attrib, boolean disp) {
			attributeName = attrib;
			shouldDisplay = disp;
		}

		public String getAttributeName() {
			return this.attributeName;
		}
		
		public boolean isShouldDisplay(){
			return shouldDisplay;
		}

		public static boolean contains(String columnName) {
			for (INVITEE_FIELDS column : INVITEE_FIELDS.values()) {
				if (column.name().equalsIgnoreCase(columnName))
					return true;
			}
			return false;
		}
	};

	private static String[] reqInviteeFields = {
			INVITEE_FIELDS.firstname.name(), INVITEE_FIELDS.lastname.name(),
			INVITEE_FIELDS.salutation.name(), INVITEE_FIELDS.email.name(),
			INVITEE_FIELDS.irb_id.name() };

	/** Instance Variables */
	public String id;
	public String email;
	public String firstName;
	public String lastName;
	public String salutation;
	public String irbId;

	public String userSession;
	private String messageID;

	public Survey currentSurvey;
	public Page currentPage;
	public DataBank db;

	private Hashtable<String, Object> allAnswers = new Hashtable<String, Object>();
	private UserDBConnection myDataBank;

	// private String currentState;

	/**
	 * Getter Method.
	 * 
	 * @return UserDBConnection.
	 */
	public UserDBConnection getMyDataBank() {
		return myDataBank;
	}

	private boolean completedSurvey = false;
	Logger log = Logger.getLogger(User.class);
	
	/**
	 * Constructor: Creates user object after setting the variables correctly.
	 * User object is initialized for every new user accessing the survey system, 
	 * and it gets instantiated at in the 
	 * 
	 * @param myID		Invitee id for this particular user.
	 * @param survey
	 * @param msgId
	 * @param db
	 */
	public User(String myID, Survey survey, String msgId, DataBank db) {
		
		/* save the email's message ID as the user's survey message ID */
		try {
			id = myID;
			
			/* get the survey searching by survey ID */
			currentSurvey = survey;
			messageID = msgId;
			this.db=db;
			myDataBank = new UserDBConnection(this, db);

			/* retrieve & fill in required invitee values */
			String[] inviteeAttrs = myDataBank
					.getInviteeAttrs(User.reqInviteeFields);
			firstName = inviteeAttrs[0];
			lastName = inviteeAttrs[1];
			salutation = inviteeAttrs[2];
			email = inviteeAttrs[3];
			irbId = inviteeAttrs[4];
			if (irbId == null) {
				irbId = "";
			}

			/* retrieve & cache values that will be referenced by survey */
			if (currentSurvey.inviteeFields != null
					&& currentSurvey.inviteeFields.length > 0) {
				inviteeAttrs = myDataBank
						.getInviteeAttrs(currentSurvey.inviteeFields);
				if (inviteeAttrs != null) {
					Hashtable<String, String> invAns = new Hashtable<String, String>();
					for (int i = 0; i < currentSurvey.inviteeFields.length; i++) {
						invAns.put(currentSurvey.inviteeFields[i],
								inviteeAttrs[i]);
					}
					allAnswers.putAll(invAns);
				}
			}
			Hashtable<String, String> mainData = myDataBank.getMainData();
			
			/* no data -> empty hash but test for null first just in case */
			if (mainData == null || mainData.size() == 0) {
				currentPage = currentSurvey.pages[0];
			} else {
				
				/* STATUS column contains the current page, or NULL if done */
				String currentPageName = mainData.remove("status");
				if (currentPageName == null) {
					completedSurvey = true;
				} else {
					Page p = currentSurvey.getPage(currentPageName);
					if (p != null) {
						currentPage = p;
					} else {
						
						/* page must've been deleted; start back at 1st page */
						currentPage = currentSurvey.pages[0];
					}
					mainData.remove("id");
					allAnswers.putAll(mainData);
				}
			}
		} catch (Exception e) {
			WISEApplication.logError(
					"USER CONSTRUCTOR failed w/ " + e.toString(), e);
			id = null; // signal an improperly initialized User
		}
	}

	/**
	 * Constructor for testing without database -- NOT DEBUGGED 
	 * 
	 * @param svy	Survey for which the test user object has to be initialized.
	 */
	public User(Survey svy) {
		
		/* save the email's message ID as the user's survey message ID */
		//String[] ids;
		String[] inviteeAttrs = { "2" };
		try {
			id = "1";
			currentSurvey = svy;
			String[] testFields = currentSurvey.inviteeFields;
			if (testFields != null && testFields.length > 0) {
				Hashtable<String, String> invAns = new Hashtable<String, String>();
				for (int i = 0; i < testFields.length; i++) {
					invAns.put(testFields[i], inviteeAttrs[i]);
				}
				allAnswers.putAll(invAns);
			}
			myDataBank = new UserDBConnection(this);
		} catch (Exception e) {
			WISEApplication.logError(
					"USER CONSTRUCTOR can't find MESSAGE_INDEX. :"
							+ e.toString(), e);
		}
	}

	/**
     * Assembles values submitted (in http request params), advance page, store
	 * values in DataBank.
     *  
     * @param 	params	HashTable which contains the questions and corresponding answers.
     * @param	advance	Should the survey be advanced to next page or not.
     */
	public void readAndAdvancePage(Hashtable<String, ?> params, boolean advance) {
		//String debugCheck = "";
		String[] pageMainFields = currentPage.getFieldList();
		char[] pageMainFieldTypes = currentPage.getValueTypeList();
		String[] pageMainVals = new String[pageMainFields.length];
		for (int i = 0; i < pageMainFields.length; i++) {
			if(pageMainFields[i] != null){
				Object theVal = params.get(pageMainFields[i]);
				if (theVal != null) {
					pageMainVals[i] = (String) theVal;
					allAnswers.put(pageMainFields[i], theVal);
					//debugCheck += "{" + pageMainFields[i] + ":" + pageMainVals[i]
					//		+ "}";
				}
			}else{
				//do nothing
			}
		}
		myDataBank.recordPageSubmit();

		/* record state change and send interrupt message, but don't advance page */
		if (advance) {
			currentPage = currentSurvey.nextPage(currentPage.id);
		}

		/* next_page() returns null only if finished; set done conditions immediately */
		if (currentPage == null) {
			setDone();
		}

		/*
		 *  this records new page (or null for completion) so don't have to call
		 *  record_currentPage() from this function
		 */
		myDataBank.storeMainData(pageMainFields, pageMainFieldTypes,
				pageMainVals);

		// TODO: (med) add SubjectSet part: get page's sets & set-questions;
		// read 'em, store 'em
	}

	/**
     * Sets the user state in the data base as interrupted and closes the current session.
     * Also sends an interrupted email to the user.
     */
	public void setInterrupt() {
		myDataBank.closeSurveySession();
		myDataBank.setUserState("interrupted");
		MessageSequence msgSeq = getCurrentMessageSequence();
		Message msg = msgSeq.getTypeMessage("interrupt");
		if (msg != null) {
			String msgUseId = myDataBank.recordMessageUse(msg.id);
			MessageSender sndr = new MessageSender(msgSeq);
			sndr.sendMessage(msg, msgUseId, this,this.db);
		}
	}

	/**
     * Sets variables when done with survey, but if forwarding, hold off setting exercise
	 * as complete for invite purposes.
     */
	public void setDone() {
		completedSurvey = true;
		myDataBank.closeSurveySession();
	}

	// call when current participation complete 
	/**
     * Sets user's state as "completed" in database.
     * Also sends a completed message. 
     * If forwarding, needs to be set by callback from app forwarded to.
     */
	public void setComplete() {
		myDataBank.setUserState("completed");
		
		/* send THANK YOU email, if any */
		MessageSequence msgSeq = getCurrentMessageSequence();
		Message msg = msgSeq.getTypeMessage("done");
		if (msg != null) {
			String msgUseId = myDataBank.recordMessageUse(msg.id);
			MessageSender sndr = new MessageSender(msgSeq);
			sndr.sendMessage(msg, msgUseId, this,this.db);
		}
	}

	// //For offline testing: set page rather than pulling from database
	// public void read_current_form(Hashtable params, int pageIndx)
	// {
	// currentPage = currentSurvey.pages[pageIndx];
	// readAndAdvancePage(params);
	// }
	//

	/**
	 * Returns all current data that the user has stored so far.
	 * 
	 * @return	Hashtable	The data stored in the database as a hashtable.
	 */
	public Hashtable<String, Object> getData() {
		return allAnswers;
	}

	/**
	 * Returns a value of a field from the data related to this user.
	 * 
	 * @param 	fieldName	Name of the field whose value is needed.
	 * @return	Integer 	Value of the fieldName, null is returned if no value is present.
	 */
	public Integer getFieldValue(String fieldName) {
		Integer value = null;
		String valueStr = "";
		try {
			valueStr = (String) allAnswers.get(fieldName);
		} catch (NullPointerException  e) {
			WISEApplication.logError("USER can't GET DATA:" + e.toString()
					+ allAnswers.toString(), e);
		}
		/* check for empty values */
		if (valueStr != null && valueStr.length() > 0) {
			value = new Integer(valueStr);
		}
		return value;
	}

	/**
	 * Output all Field, value pairs answered so far as a JavaScript string of
	 * name:value pairs.
	 * 
	 * @return	String 	Name value pair of the question answered so far.
	 */
	public String getJSValues() {
		String str = "{";
		String fieldName;		
		String fieldValue;
		try {
			
			/* get the user's entered data for the entire survey */
			Hashtable<String, Object> pgAnswers = this.getData();
			// get the specific column from hashtable
			if (!pgAnswers.isEmpty()) {
				Enumeration<String> en = pgAnswers.keys();
				while (en.hasMoreElements()) {
					fieldName = (String) en.nextElement();
					
					/* exclude the fields of invitee & status */
					if ((!fieldName.equalsIgnoreCase("INVITEE"))
							&& (!fieldName.equalsIgnoreCase("STATUS"))) {
						
						/* search by the key to get the value */
						fieldValue = (String) pgAnswers.get(fieldName);
						
						/* exclude the null value and create a string of
						 * NAME:value pair used for JavaScript
						 */
						if (fieldValue != null
								&& !fieldValue.equalsIgnoreCase("null")) {
							if(fieldValue.indexOf("\'")!=-1){
								fieldValue=fieldValue.replace("'", "\\'");	
							}
							str = str + "'" + fieldName.toUpperCase() + "':'"
									+ fieldValue + "',";
						}
					}
				}
				int len = str.length();
				
				/* delete the last comma from the string */
				if (len > 1)
					str = str.substring(0, len - 1);
				str = str + "}";
			} else {
				str += "}";
			}
		} catch (NullPointerException e) {
			WISEApplication
					.logError("USER RECORD EXISTS: " + e.toString(), e);
		}
		return str;
	}

	/**
	 * Pull out answer values just for the current page and for the invitee
	 * fields in use.
	 * 
	 * @return Hashtable	The answer values of current page.
	 */
	public Hashtable<String, String> getPageData() {
		Hashtable<String, String> result = new Hashtable<String, String>();
		if (currentSurvey.inviteeFields != null
				&& currentSurvey.inviteeFields.length > 0) {
			for (int i = 0; i < currentSurvey.inviteeFields.length; i++) {
				String fldnm = currentSurvey.inviteeFields[i];
				String fldval = (String) allAnswers.get(fldnm);
				if (fldval != null)
					result.put(fldnm, fldval);
			}
		}
		for (int i = 0; i < currentPage.allFieldNames.length; i++) {
			String fldnm = currentPage.allFieldNames[i];
			String fldval = (String) allAnswers.get(fldnm);
			if (fldval != null)
				result.put(fldnm, fldval);
		}
		return result;
	}

	/**
	 * Returns the current message sequence for sending emails to the users.
	 * 
	 * @return	MessageSequence.
	 */
	public MessageSequence getCurrentMessageSequence() {
		Preface preface;
		MessageSequence msgSeq = null;
		String msID = myDataBank.getCurrentMessageSequence();
		try {
			preface = currentSurvey.studySpace.get_preface();
			if (preface == null || msID == null) {
				throw new Exception("<p>Error: Can't get the preface file.</p>");
			}
			
			/* get the message sequence */
			msgSeq = preface.getMessageSequence(msID);
			if (msgSeq == null) {
				throw new Exception(
						"<p>Error: Can't find message sequence for the current survey.</p>");
			}
		} catch (Exception e) {
			WISEApplication.logError(
					"USER can't get message sequence: " + e.toString(), e);
		}
		return msgSeq;
	}

	// /** called on entry to a page; save the current page status in the DB
	// * User's next page must be set first */
	// public void set_incomplete()
	// {
	// try
	// {
	// myDataBank.record_currentPage();
	// }
	// catch (Exception e)
	// {
	// WISE_Application.email_alert("USER SET INCOMPLETE: "+e.toString(), e);
	// }
	// }

	/**
	 * create user's survey session and capture the IP address of the user
	 * 
	 * @param browserUseragent	User browser's User Agent.
	 * @param ipAddress			Ip address of the user's machine from where he is accessing the survey.
	 */
	public void startSurveySession(String browserUseragent, String ipAddress) {
		try {
			myDataBank.recordCurrentPage();
			myDataBank.setUserState("started"); // may have changed to interrupted
			userSession = myDataBank.createSurveySession(browserUseragent, ipAddress,
					messageID);
		} catch (Exception e) {
			WISEApplication.logError(
					"USER start_survey_session:" + e.toString(), e);
		}
	}

	/** 
	 * Updates the current page of the user both in the user object and the database.
	 * 
	 * @param	newPgName	Current page to be updated in user object.
	 */
	public void setPage(String newPgName) {
		currentPage = currentSurvey.getPage(newPgName);
		myDataBank.recordCurrentPage();
	}

	/**
	 * Checks if user has entered any survey data.
	 * 
	 * @return	boolean	true if user has some survey data else false.
	 */

	public boolean startedSurvey() {
		if (myDataBank.getCurrentPageName() != null) {
			return true;
		}
		String theState = myDataBank.getUserState();
		if (theState == null) {
			return false;
		}
		
		/* note returns true if consent given but no pages submitted */
		if (theState.equalsIgnoreCase("interrupted")
				|| theState.equalsIgnoreCase("started"))  {
			return true;
		}
		return false;
	}

	/** 
	 * Checks if user has completed the survey.
	 * 
	 * @return boolean 	Did the user complete the survey. 
	 */
	public boolean completedSurvey() {
		return completedSurvey;
	}

	/**
	 * Returns if user has accepted the consent or not.
	 * 
	 * @return	boolean	true if accepted the consent false otherwise.
	 */
	public boolean checkConsent() {
		return myDataBank.checkConsent();
	}

	/**
	 * Saves the user's accept consent as Y in the data base. 
	 */
	public void consent() {
		myDataBank.setConsent("Y");
		// myDataBank.record_currentPage(); begin survey should handle all state updates
		// myDataBank.set_userState("started");
	}

	/**
	 * Saves the user's decline consent as N in the data base.
	 */
	public void decline() {
		myDataBank.setConsent("N");
		myDataBank.setUserState("declined");
	}
	
	/**
	 * gets a hashtable of all the page IDs keyed to "completed" vs currently
	 * working on.
	 * 
	 * @return	Hashtable	all completed pages in the survey so far by this user.
	 */
	public Hashtable<String, String> getCompletedPages() {
		return myDataBank.getCompletedPages(); // pass thru to databank
	}

	// ===============================================================
	// Code separated to here. Not worth continuing for now

	/**
	 * Adds a record into the welcome_hits table, to log that the welcome page
	 * was visited/hit by the user
	 */
	public void recordWelcomeHit() {
		if (!myDataBank.recordWelcomeHit(this.id, currentSurvey.id))
			log.error("Error while recording welcome hit for invitee with ID="
					+ this.id + " survey ID " + currentSurvey.id);
	}

	/**
	 * Records that the decline form was hit form the invitation decline link
	 * 
	 * @param msgId		Message id that the user used to access the system.
	 * @param studyId	Survey Id that the user declined.
	 */
	public void recordDeclineHit(String msgId, String studyId) {
		if (!myDataBank.recordDeclineHit(msgId, studyId, this.id,
				currentSurvey.id))
			log.error("Error while recording decline hit for invitee with ID="
					+ this.id + " survey ID " + currentSurvey.id);
	}

	/**
	 * Sets the decline reason
	 * @param reason	Reason why user has declined the survey.
	 */
	public void setDeclineReason(String reason) {
		if (!myDataBank.setDeclineReason(this.id, reason))
			log.error("Error while recording reason for the decline for invitee with ID="
					+ this.id);
	}

	/**
	 * Gets the current number of completers from the survey data table.
	 * 
	 * @return	int	Number of users who finished the survey.
	 */
	public int checkCompletionNumber() {
		return myDataBank.checkCompletionNumber(currentSurvey.id);
	}

}
