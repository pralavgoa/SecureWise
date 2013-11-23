package edu.ucla.wise.commons;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.log4j.Logger;

//TODO (low): consider getting rid of STATUS column in data table & just using page_submit; 
// nice thing tho is that STATUS is given back alongside the main data.

/**
 * Class UserDBConnection -- a customized interface to encapsulate single-user interface to data storage.
 * 
 * @author Doulas Bell
 * @author Pralav
 * @author Vijay 
 * @version 1.0
 *
 */
public class UserDBConnection {
    public User theUser = null;
    private final String surveyID;
    private final String mainTableName;
    private DataBank db;
    private Connection conn = null;
    Logger log = Logger.getLogger(UserDBConnection.class);

    /**
     * If there is a quote in the string, replace it with double quotes this is
     * necessary for sql to store the quote properly.
     * 
     * @param 	s		Input string with quotes.
     * @return	String	Modifies string.
     */
    public static String fixquotes(String s) {
		if (s == null)
		    return "";
	
		int len = s.length();
		String s1, s2;
	
		s2 = "";
		for (int i = 0; i < len; i++) {
		    s1 = s.substring(i, i + 1);
		    s2 = s2 + s1;
		    if (s1.equalsIgnoreCase("'"))
			s2 = s2 + "'";
		}
		return s2;
    }

    /**
     * Constructor for the class.
     * 
     * @param usr	User for which this object is associated to.
     * @param dbk	Data bank for getting details to contact database.
     */
    public UserDBConnection(User usr, DataBank dbk) {
		theUser = usr;
		db = dbk;
		surveyID = usr.currentSurvey.id;
		mainTableName = surveyID + DataBank.MainTableExtension;
		try {
			
			/* 
			 * open a database connection to hold for the user
		     * ultimately closed by finalize() below
		     */
		    conn = db.getDBConnection();
		} catch (SQLException e) {
		    WISEApplication.logError(
			    "User " + theUser.id
				    + " unable to make its DB connection. Err: "
				    + e.toString(), null);
		}
    }

    /**
     * constructor version for testing only
     * 
     * @param user User for which this object is associated to.
     */
    public UserDBConnection(User user) {
		theUser = user;
		surveyID = user.currentSurvey.id;
		mainTableName = surveyID + DataBank.MainTableExtension;
    }

    /**
     * finalize() called by garbage collector to clean up all objects
     */
    @Override
    protected void finalize() throws Throwable {
	try {
	    conn.close();
	} catch (SQLException e) {
	    WISEApplication.logError("Exception for user " + theUser.id
	    		+ " closing DB connection w/: " + e.toString(), null);
	} finally {
	    super.finalize();
	}
    }


    /**
     * Retrieves values for a list of fields from the invitee table 
     * 
     * @param 	fieldNames	Array of field name whose values are to be retrieved from the database. 	
     * @return	String[]	Array of the values from the database.
     */
    public String[] getInviteeAttrs(String[] fieldNames) {    	
	    HashSet<String> nonEncodedFieldSet = new HashSet<String>();
		nonEncodedFieldSet.add("firstname");
		nonEncodedFieldSet.add("lastname");
		nonEncodedFieldSet.add("salutation");
		nonEncodedFieldSet.add("phone");
		nonEncodedFieldSet.add("irb_id");
		
		String userid = theUser.id;
		String[] values = new String[fieldNames.length];
		if (fieldNames.length < 1) {
		    return values;
		}
		String fieldString = "";
		for (int i = 0; i < fieldNames.length - 1; i++) {
		    fieldString += (!nonEncodedFieldSet.contains(fieldNames[i]
			    .toLowerCase()))
			    ? "AES_DECRYPT("
			    + fieldNames[i].toLowerCase() + ",'"
			    + db.emailEncryptionKey + "')" : fieldNames[i];
		    fieldString += ",";
		}
		//fieldString += fieldNames[fieldNames.length - 1];
		fieldString += (!nonEncodedFieldSet.contains(fieldNames[fieldNames.length - 1]
			    .toLowerCase()))
			    ? "AES_DECRYPT("
			    + fieldNames[fieldNames.length - 1].toLowerCase() + ",'"
			    + db.emailEncryptionKey + "')" : fieldNames[fieldNames.length - 1];
		
		String sql = "SELECT " + fieldString + " FROM invitee WHERE id = "
			+ userid;
		try {
			
			//TODO: Change to Prepared Statement.
		    /* connect to the database */
		    Statement stmt = conn.createStatement();
		    
		    /* get the status' value from survey data table */
		    WISEApplication.logInfo("\n #####The SQl being executed for the extraction of invitee fields is :"+ sql);
		    stmt.execute(sql);
		    ResultSet rs = stmt.getResultSet();
		    
		    /* update the current page by searching with the status' value (page ID) */
		    if (rs.next()) {
				for (int i = 0; i < fieldNames.length; i++)
				{
					values[i] = rs.getString(i + 1);
					values[i]=values[i].replaceAll("^\"|\"$", "");
				}
		    }
		    stmt.close();
		} catch (SQLException e) {
		    WISEApplication.logError(
		    		"DataBank - Invitee attr retrieval fail: " + e.toString(), null);
		    return null; // signal failure to retrieve
		}
		return values;
    }

    /**
     * Writes array of values for a page and also the ID of next page to the
     * user's row in survey's main data table.
     *  
     * @param 	name		The values of the columns in the survey's data table.
     * @param	valTypes	Types of the columns in the survey's data table.
     * @param	vlas		Values of the columns in the survey's data table.
     * @return	int			1 if successful.
     */
	public int storeMainData(String[] names, char[] valTypes, String[] vals) {
		String sql = "", sqlu = "";
		String colNames = "", values = "", updateStr = "", updateTrailStr = "";
		
		/* connect to the database */
		Statement stmt = null;
		int numtoStore = 0;
		try {
		    stmt = conn.createStatement();
		} catch (SQLException e) {
		    WISEApplication.logError(
			    "WISE - PAGE Store error: Can't get DB statement for user ["
				    + theUser.id + "]: " + e.toString(), null);
		}
	
		//TODO: Change form statement to prepared statement here.
		for (int i = 0; i < names.length; i++) {
		    String fieldnm = names[i];
		    String newval = vals[i];
		    if (newval == null || newval.equals("")) {
		    	continue;
		    }
			
		    /*
		     * convert string (ascii) values for sql storage; may need to
		     * abstract this out if more datatypes
		     */
		    if (valTypes[i] == 'a') {
			newval = "'" + fixquotes(newval) + "'";
		    }
		    colNames += "," + fieldnm;
		    values += "," + newval;
		    updateStr += "," + fieldnm + "=VALUES(" + fieldnm + ")";
		    updateTrailStr += ",(" + theUser.id + ",'" + surveyID + "','"
			    + fieldnm + "', " + newval + ")";
		    numtoStore++;
		}
		if (numtoStore > 1) {
			
		    /* chop initial comma */
		    updateTrailStr = updateTrailStr.substring(1,
		    		updateTrailStr.length());
		    sqlu = "insert into update_trail (invitee, survey, ColumnName, CurrentValue)"
		    		+ " values " + updateTrailStr;
		    try {
		    	stmt.execute(sqlu);
		    } catch (SQLException e) {
				WISEApplication.logError("WISE - PAGE Store [" + theUser.id
						+ "] query (" + sqlu + "): " + e.toString(), null);
		    }
		}
		
		/*
		 * note proper storage of "status" field relies on User object having
		 * advanced page before call;
		 */
		String nextPage = "null";
		if (theUser.currentPage != null) {
			
			/* null val means finished */
		    nextPage = "'" + theUser.currentPage.id + "'";
		}		
		sql = "INSERT into " + mainTableName + " (invitee, status " + colNames
				+ ") VALUES (" + theUser.id + "," + nextPage + values
				+ ") ON DUPLICATE KEY UPDATE status=VALUES(status) "
				+ updateStr;
		log.info("The data storing sql is " + sql);
		try {
		    stmt.execute(sql);
		} catch (SQLException e) {
		    WISEApplication.logError("WISE - PAGE Store error [" + theUser.id
		    		+ "] query (" + sql + "): " + e.toString(), null);
		}
		try {
		    stmt.close();
		} catch (SQLException e) {
		    WISEApplication.logError(
			    "WISE - PAGE Store closing error: " + e.toString(), null);
		}	
		return 1;
	}

  	/**
	 * sets up user's status entry in survey data table
	 * 
	 * @param pageID	Page ID whose status has to be updated to 
	 */
    public void beginSurvey(String pageID) {
		
    	String sql = "SELECT status FROM " + mainTableName
			    + " WHERE invitee = ?";
    	PreparedStatement stmt1 = null;
    	PreparedStatement stmt2 = null;
    	PreparedStatement stmt3 = null;
    	try {
    		
		    /* connect to database */
		    stmt1 = conn.prepareStatement(sql);
		    stmt1.setInt(1, Integer.parseInt(theUser.id));

		    ResultSet rs = stmt1.executeQuery();
		    boolean exists = rs.next();
		    
		    /* if the user doesn't exist, insert a new user record in to the
		     * data table and set the status value to be the ID of the 1st survey page -
		     * (starting from the beginning)
		     */
		    if (!exists) {
				sql = "INSERT INTO " + mainTableName
					+ " (invitee, status) VALUES ( ?, ?)";				
				stmt2 = conn.prepareStatement(sql);
				stmt2.setInt(1, Integer.parseInt(theUser.id));
				stmt2.setString(2, pageID);			
				stmt2.executeUpdate();
		    }
	
		    /* update user state to be started (consented) */
		    sql = "update survey_user_state set state='started', state_count=1, entry_time=now() where invitee= ?"
			    + " AND survey= ?";
		    stmt3 = conn.prepareStatement(sql);
		    stmt3.setInt(1, Integer.parseInt(theUser.id));
		    stmt3.setString(2, surveyID);
		    stmt3.executeUpdate();
		    
		} catch (SQLException e) {
		    WISEApplication.logError("Databank SETUP STATUS:" + e.toString(),
			    null);
		} catch (NumberFormatException e) {
		    WISEApplication.logError("Databank SETUP STATUS:" + e.toString(),
				    null);
		}finally {
    		try {
    			if(stmt1 != null) {
    				stmt1.close();
    			}
    			if (stmt2 != null) {
    				stmt2.close();
    			}
    			if (stmt3 != null) {
        			stmt3.close();
        		}
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
    }

    /**
     * Returns the current page the user is on from the database.
     * 
     * @return	String	The Page on which user is on. Returns null if none
     */
    public String getCurrentPageName() {
    	String sql = "SELECT status FROM " + mainTableName
    			+ " WHERE invitee = ?";
    	PreparedStatement stmt = null;
    	
    	/* Assumes user/survey has a state */
    	String status = null;
    	try {
    		
    		/* connect to database */
    		stmt = conn.prepareStatement(sql);
    		stmt.setInt(1, Integer.parseInt(theUser.id));    		
    		
    		ResultSet rs = stmt.executeQuery();
    		if (rs.next())
    			status = rs.getString(1);
    		rs.close();
    	} catch (SQLException e) {
    		WISEApplication.logError(
    				"UDB getCurrentPageName:" + e.toString(), e);
    	} catch (NumberFormatException e) {
		    WISEApplication.logError("UDB getCurrentPageName:" + e.toString(),
				    null);
		}finally {
    		try {
    			if(stmt != null) {
    				stmt.close();
    			}    			
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
    	return status;
    }

    /**
     * Reads all previously-submitted main values for the user;
     * returns null if not started the survey
     * 
     * @return	Hashtable	All the previously submitted answers in the 
     * 						form of fieldName-->response.
     */
    public Hashtable<String, String> getMainData() {
    	Hashtable<String, String> h = new Hashtable<String, String>();
    	int i = 0;
		String sql = "SELECT * from " + mainTableName
				+ " WHERE invitee = " + theUser.id;
    	PreparedStatement stmt = null;    	
    	try {
    		stmt = conn.prepareStatement(sql);
    		stmt.setInt(1, Integer.parseInt(theUser.id));   
    		
    		/* pull all from current survey data table */
    		ResultSet rs = stmt.executeQuery();
    		
    		if (rs.next()) {
    			ResultSetMetaData metaData = rs.getMetaData();
    			if (metaData == null) {
    				throw new Exception("can't get meta data");
    			}
    			int columns = metaData.getColumnCount();
    			String colName, ans;
    			for (i = 1; i <= columns; i++) {
    				colName = metaData.getColumnName(i);
    				if (colName == null) {
    					throw new Exception("can't get column name " + i);
    				}
    				ans = rs.getString(colName);
    				
    				/* leave out of the hashtable if null value (hashes can't hold nulls) */
    				if (ans != null) {
    					h.put(colName, ans);
    				}
    			}
    		} else {
    			return null;
    		}
    	} catch (SQLException e) {
    		WISEApplication.logError(
    				"UDB getCurrentPageName:" + e.toString(), e);
    	} catch (NumberFormatException e) {
		    WISEApplication.logError("UDB getCurrentPageName:" + e.toString(),
				    null);
		} catch (Exception e) {
    		WISEApplication.logError("USER_DB SETUP DATA after " + i
    				+ " cols read: " + e.toString(), e);
    	} finally {
    		try {
    			if(stmt != null) {
    				stmt.close();
    			}    			
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
    	return h;
    }

    /**
     * Saves the data from the repeating item set questions into the database
     * 
     * @param tableName		Repeating item set table name.
     * @param rowId			Row id to which data has to be stored.
     * @param rowName		Row name 
     * @param nameValue		Answers for the repeating item set.
     * @param nameType		Types of the repeating item set table columns.
     * @return int			returns the inserted key.
     */	
    public int insertUpdateRowRepeatingTable(String tableName,
    		String rowId, String rowName,
    		Hashtable<String, String> nameValue,
    		Hashtable<String, String> nameType) {

		int insertedKeyValue = -1;
	
		StringBuffer sqlStatement = new StringBuffer("");
		StringBuffer commaSepdColumnNames = new StringBuffer("");
		StringBuffer commaSepdColumnValues = new StringBuffer("");
		StringBuffer commaSepdUpdateString = new StringBuffer("");
	
		/* iterate through hashtable to get column names and types */
		Enumeration<String> eIterator = nameValue.keys();
		while (eIterator.hasMoreElements()) {
		    String columnName = eIterator.nextElement();
		    commaSepdColumnNames.append(columnName + ",");
	
		    commaSepdUpdateString.append(columnName + "=VALUES("
			    + columnName + "),");
	
		    String column_value = nameValue.get(columnName);
		    if (nameType.get(columnName).equals("text")
		    		|| nameType.get(columnName).equals("textarea")) {
				if ("".equals(column_value)) {
				    commaSepdColumnValues.append(" NULL,");
				} else {
				    commaSepdColumnValues.append("'"
					    + fixquotes(column_value) + "'" + ",");
				}	
		    } else {
		    	commaSepdColumnValues.append(column_value + ",");
		    }
	
		}
	
		/* remove the last commas */
		if (commaSepdColumnNames
				.charAt(commaSepdColumnNames.length() - 1) == ',') {
		    commaSepdColumnNames.setCharAt(
		    		commaSepdColumnNames.length() - 1, ' ');
		}
		if (commaSepdColumnValues
				.charAt(commaSepdColumnValues.length() - 1) == ',') {
		    commaSepdColumnValues.setCharAt(
		    		commaSepdColumnValues.length() - 1, ' ');
		}
		if (commaSepdUpdateString
				.charAt(commaSepdUpdateString.length() - 1) == ',') {
		    commaSepdUpdateString.setCharAt(
		    		commaSepdUpdateString.length() - 1, ' ');
		}		
		/* --end of remove last commas */
		//TODO: change from statement to prepared statement.
		sqlStatement.append("INSERT INTO ");
		sqlStatement.append(tableName);
		if (rowId != null) {
		    sqlStatement.append(" (instance,invitee,instance_name, ");
		} else {
		    sqlStatement.append("(invitee,instance_name, ");
		}
		sqlStatement.append(commaSepdColumnNames.toString() + ") ");
		sqlStatement.append("VALUES (");
		if (rowId != null) {
		    sqlStatement.append(rowId + ",");
		}
		sqlStatement.append(theUser.id + ",");
		sqlStatement.append("'" + rowName + "',");
		sqlStatement.append(commaSepdColumnValues.toString() + ") ");
		sqlStatement.append("ON DUPLICATE KEY UPDATE ");
		sqlStatement.append(commaSepdUpdateString);
		sqlStatement.append("");
		sqlStatement.append("");
		sqlStatement.append("");
		sqlStatement.append("");
		sqlStatement.append("");
	
		log.info(sqlStatement.toString());
	
		Statement statement = null;
		try {
		    statement = conn.createStatement();
		} catch (SQLException e) {
		    WISEApplication.logError(
		    		"WISE - Repeat Item Store error: Can't get DB statement for user ["
		    				+ theUser.id + "]: " + e.toString(), null);
		}
	
		try {
		    statement.execute(sqlStatement.toString(),
		    		Statement.RETURN_GENERATED_KEYS);
	
		    ResultSet generatedKeySet = statement.getGeneratedKeys();
		    if (generatedKeySet.first()) {
		    	insertedKeyValue = generatedKeySet.getInt(1);
		    }
		} catch (SQLException e) {
		    WISEApplication.logError("WISE - Repeat Item Store error ["
		    		+ theUser.id + "] query (" + sqlStatement.toString()
		    		+ "): " + e.toString(), null);
		}
		try {
		    statement.close();
		} catch (SQLException e) {
		    WISEApplication.logError(
		    		"WISE - Repeat Item Store closing error: " + e.toString(), null);
		}
		return insertedKeyValue;
    }

    /**
     * Returns the data stored in the given repeating item table in the form of a json.
     * 
     * @param 	repeatingSetName	Name of the table whose data is to be read.
     * @return 	String 				The data form the table in the form of json.
     */
    public String getAllDataForRepeatingSet(String repeatingSetName) {
    	String tableName = "repeat_set_" + repeatingSetName;
    	int columnIndex = 0;

    	StringBuffer javascriptArrayResponse = new StringBuffer();
    	PreparedStatement stmt = null; 

    	try {

    		/* pull all from current repeating set table */
    		String sql = "SELECT * from " + tableName + " WHERE invitee = ?";

    		stmt = conn.prepareStatement(sql);
    		stmt.setInt(1, Integer.parseInt(theUser.id));			
    		log.info("The sql statement is:" + sql);
    		ResultSet rs = stmt.executeQuery();	     

    		javascriptArrayResponse.append("{");

    		while (rs.next()) {
    			ResultSetMetaData metaData = rs.getMetaData();
    			if (metaData == null) {
    				throw new SQLException("can't get meta data");
    			}
    			int columns = metaData.getColumnCount();
    			String colName, ans;
    			for (columnIndex = 1; columnIndex <= columns; columnIndex++) {

    				colName = metaData.getColumnName(columnIndex);
    				if (colName == null) {
    					throw new SQLException("can't get column name "
    							+ columnIndex);
    				}
    				ans = rs.getString(colName);

    				if (columnIndex == 1) {
    					javascriptArrayResponse.append("\"" + ans + "\""
    							+ ":[{");
    				}
    				if (ans != null) {
    					javascriptArrayResponse
    					.append("\"" + colName + "\"");
    					javascriptArrayResponse.append(":");
    					ans = ans.replaceAll("(\r\n|\n\r|\r|\n)", "\\\\n");
    					javascriptArrayResponse.append("\"" + ans + "\"");
    				} else {// dont add;
    				}

    				if (!(columnIndex == columns)) {
    					if (ans != null) {
    						javascriptArrayResponse.append(",");
    					} else {// dont add;
    					}
    				} else {

    					/* remove the last comma */
    					if (javascriptArrayResponse
    							.charAt(javascriptArrayResponse.length() - 1) == ',') {
    						javascriptArrayResponse
    						.deleteCharAt(javascriptArrayResponse
    								.length() - 1);
    					}
    					javascriptArrayResponse.append("}],");
    				}
    			}		
    		}
    		if (javascriptArrayResponse.length() > 2) {

    			/* remove the last comma */
    			javascriptArrayResponse
    			.deleteCharAt(javascriptArrayResponse.length() - 1);
    		}
    		javascriptArrayResponse.append("}");	
    	} catch (SQLException e) {
    		WISEApplication.logError("USER_DB REPEATING SET after "
    				+ columnIndex + " cols read: " + e.toString(), e);
    	} catch (NumberFormatException e){
    		WISEApplication.logError("USER_DB REPEATING Invalid User ID"
    				+ e.toString(), e);
    	} finally {
    		if (stmt != null) {
    			try {
    				stmt.close();
    			} catch (SQLException e) {
    				WISEApplication.logError(
    						"USER_DB REPEATING SET error:" + e.toString(), null);
    			}
    		}
    	}
    	return javascriptArrayResponse.toString();
    }

    /**
     * Returns all the data that the user has stored in the survey data table.
     * 
     * @return	Hashtable	Hash table which contains all the data of a user in all survey tables.
     */
    public Hashtable<String, String> getAllData() {
    	Hashtable<String, String> h = new Hashtable<String, String>();
    	String sql = "select ColumnName, CurrentValue from UPDATE_TRAIL "
				+ "where invitee = " + theUser.id + " AND survey = "
				+ surveyID + " Order by Modified asc";
    	PreparedStatement stmt = null;
    	
    	try {
    		
    		/* connect to the database */
    		stmt = conn.prepareStatement(sql);
    		stmt.setInt(1,Integer.parseInt(theUser.id));
    		stmt.setString(2, surveyID);
    		
    		/* get data from database for subject */    		
    		ResultSet rs = stmt.executeQuery();

    		// ResultSetMetaData metaData = rs.getMetaData();
    		// int columns = metaData.getColumnCount();
    		
    		/* The data hash table takes the column name as the key
    		 * and the user's anwser as its value
    		 */    		
    		while (rs.next()) {
    			String colName, ans;
    			colName = rs.getString(2);
    			ans = rs.getString(2);
    			
    			/* input a string called null if the column value is null
    			 * to avoid the hash table has the null value
    			 */
    			if (ans == null)
    				ans = "null";
    			h.put(colName, ans); // old, overwritten values will be overwritten here
    		}
    		rs.close();
    	} catch (SQLException e) {
    		WISEApplication.logError(
    				"UDB getAllData:" + e.toString(), e);
    	} catch (NumberFormatException e) {
		    WISEApplication.logError("UDB getAllData:" + e.toString(),
				    null);
		} finally {
    		try {
    			if(stmt != null) {
    				stmt.close();
    			}    			
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
    	return h;
    }

    /**
     * Updates the database to record user's current page.
     */
    public void recordCurrentPage() {
		String sql = "INSERT INTO " + mainTableName + " (invitee, status) "
				+ "VALUES (?,?) on duplicate key update status=values(status)";
		PreparedStatement stmt = null;
		try {
		    stmt = conn.prepareStatement(sql);
		    stmt.setInt(1, Integer.parseInt(theUser.id));
		    stmt.setString(2, theUser.currentPage.id);
		    stmt.executeUpdate();
		} catch (SQLException e) {
		    WISEApplication.logError("Record page STATUS:" + e.toString(),
		    		null);
		} catch (NumberFormatException e){
			 WISEApplication.logError("Record page STATUS Invalid User ID"
					    + e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					WISEApplication.logError(
				    		"Record page STATUS:" + e.toString(), null);
				}
			}			
		}
    }

    /**
     * Updates the users current submitted page into the database.
     */
    public void recordPageSubmit() {
		String sql = "INSERT INTO page_submit (invitee, survey, page) "
				+ "VALUES (?,?,?)";
		PreparedStatement stmt = null;
		try {
		    stmt = conn.prepareStatement(sql);
		    stmt.setInt(1, Integer.parseInt(theUser.id));
		    stmt.setString(2,surveyID);
		    stmt.setString(3,theUser.currentPage.id);
		    stmt.executeUpdate();
		} catch (SQLException e) {
		    WISEApplication.logError(
		    		"Record page submit error:" + e.toString(), null);
		} catch (NumberFormatException e){
			 WISEApplication.logError("Record page submit Invalid User ID"
					 + e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					WISEApplication.logError(
				    		"Record page submit error:" + e.toString(), null);
				}
			}
		}
    }

    /**
     * Generates a random string which is used while link generation 
     * for sending email to users. This link is used by the users to 
     * get access to the survey system.
     * 
     * @param 	messageID	The message name that is send to the user as email.
     * @return	String 		New random message Id that is used for link generation while sending emails.
     */
    public String recordMessageUse(String messageId) {
		String uid = theUser.id;
		String randMessageId = org.apache.commons.lang3.RandomStringUtils
				.randomAlphanumeric(22);
		String sql = "INSERT INTO survey_message_use(messageId,invitee, survey, message) VALUES ('"
				+ "?,?,?,?)";
		PreparedStatement stmt = null;
		try {
		    
			/* connect to database */
		    stmt = conn.prepareStatement(sql);
		    
		    stmt.setString(1, randMessageId);
		    stmt.setInt(2,Integer.parseInt(uid));
		    stmt.setString(3, surveyID);
		    stmt.setString(4, messageId);
		    
		    /* check if the user has already existed in the survey data table */
		    stmt.executeUpdate();
	
		    stmt.close();
		} catch (SQLException e) {
		    WISEApplication.logError("Error recording new message using "
		    		+ sql + ": " + e.toString(), null);
		} catch (NumberFormatException e){
			 WISEApplication.logError("Error recording new message Invalid User ID"
					+ e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					WISEApplication.logError(
							"Error recording new message" + e.toString(), null);
				}
			}
		}
		return randMessageId;
	}

    /**
     * Saves the user's new state into database.
     * 
     * @param 	newState	New state to which user is to be updated.
     */
    public void setUserState(String newState) {
		String sql = "update survey_user_state set state = ?"
				+ ", state_count=1 "
				+ // reset to 1 on entering new state
				"where invitee = ? AND survey = ?";
		PreparedStatement stmt = null;
		
		/* Assumes user/survey has a state */
		try {
		    
			/* connect to database */
		    stmt = conn.prepareStatement(sql);		    
		    stmt.setString(1, newState);
		    stmt.setInt(2, Integer.parseInt(theUser.id));
		    stmt.setString(3, surveyID);
		    stmt.executeUpdate();
		} catch (SQLException e) {
		    WISEApplication.logError("UDB setUserState:" + e.toString(),
		    		null);
		} catch (NumberFormatException e){
			 WISEApplication.logError("UDB setUserState Invalid User ID"
					+ e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					WISEApplication.logError(
							"UDB setUserState:" + e.toString(), null);
				}
			}
		}
    }

    /**
     * Returns users current state of the user in the survey weather
     *  it is completed/interrupted. 
     * 
     * @return	String	Current state of the user.
     */
    public String getUserState() {
    	String sql = "SELECT state FROM survey_user_state " + "where invitee= ?"
    			+ " AND survey= ?";
    	
    	PreparedStatement stmt = null;
    	
    	/* Assumes user/survey has a state */
    	String theState = null;
    	try {
    		
    		/* connect to database */
    		stmt = conn.prepareStatement(sql);
    		stmt.setInt(1, Integer.parseInt(theUser.id));
    		stmt.setString(2, surveyID);
  	
       		ResultSet rs = stmt.executeQuery();
    		if (rs.next()) {
    			theState = rs.getString(1);
    		}
    	} catch (SQLException e) {
		    WISEApplication.logError("UDB getUserState:" + e.toString(),
		    		null);
		} catch (NumberFormatException e){
			 WISEApplication.logError("UDB getUserState Invalid User ID"
					+ e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					WISEApplication.logError(
							"UDB getUserState:" + e.toString(), null);
				}
			}
		}
    	return theState;
    }

    /**
     * Returns current message sequence of the user this is used to know 
     * which email has to be sent to the user.
     * 
     * @return	String	The user's current message sequence.
     */
    public String getCurrentMessageSequence() {
    	String sql = "SELECT message_sequence FROM survey_user_state "
    			+ "where invitee= ? AND survey= ?";
    	PreparedStatement stmt = null;
    	
    	/* Assumes user/survey has a sequence */
    	String theSeq = null;
    	try {
    		
    		/* connect to database */
    		stmt = conn.prepareStatement(sql);
    		stmt.setInt(1, Integer.parseInt(theUser.id));
    		stmt.setString(2, surveyID);
    		
    		ResultSet rs = stmt.executeQuery();
    		if (rs.next())
    			theSeq = rs.getString(1);
      	} catch (SQLException e) {
		    WISEApplication.logError("UDB getCurrentMessageSequence:" + e.toString(),
		    		null);
		} catch (NumberFormatException e){
			 WISEApplication.logError("UDB getCurrentMessageSequence Invalid User ID"
					+ e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					WISEApplication.logError(
							"UDB getCurrentMessageSequence:" + e.toString(), null);
				}
			}
		}
    	return theSeq;
    }

    /**
     * Saves the user's consent answer - accept or decline 
     * 
     * @param answer	accept or decline.
     */
    public void setConsent(String answer) {
    	String sql = "INSERT INTO consent_response (invitee, answer, survey) VALUES (?, ?, ?)";
    	PreparedStatement stmt = null;
    	try {
    		
    		/* connect to database */
    		stmt = conn.prepareStatement(sql);
    		stmt.setInt(1, Integer.parseInt(theUser.id));
    		stmt.setString(2, answer);
    		stmt.setString(3, surveyID);    		
    		stmt.executeUpdate();
     	} catch (SQLException e) {
		    WISEApplication.logError("UDB setConsent:" + e.toString(),
		    		null);
		} catch (NumberFormatException e){
			 WISEApplication.logError("UDB setConsent Invalid User ID"
					+ e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					WISEApplication.logError(
							"UDB setConsent:" + e.toString(), null);
				}
			}
		}
    }

    /**
     * Checks if user has consented - accepted the consent form.
     * 
     * @return	boolean		If user has accepted the consent or not.
     */
    public boolean checkConsent() {
    	boolean resultp = false;
    	String sql ="SELECT * FROM consent_response WHERE invitee = ?"
				+ " AND survey= ? AND answer = 'Y'";
    	PreparedStatement stmt = null;
    	
    	try {
    		stmt = conn.prepareStatement(sql);
    		stmt.setInt(1, Integer.parseInt(theUser.id));
    		stmt.setString(2, surveyID);
    		
    		/* if user accepted the consent form, the record can be found from
    		 * the consent_response table
    		 */
    		ResultSet rs = stmt.executeQuery();
    		if (rs.next()) {
    			resultp = true;
    		}
    		rs.close();
    	}  catch (SQLException e) {
		    WISEApplication.logError("UDB checkConsent:" + e.toString(),
		    		null);
		} catch (NumberFormatException e){
			 WISEApplication.logError("UDB checkConsent Invalid User ID"
					+ e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					WISEApplication.logError(
							"UDB checkConsent:" + e.toString(), null);
				}
			}
		}
    	return resultp;
    }

    /**
     * Creates user's survey session and captures the ip address of user into the survey_user_session
     * @param 	browserUseragent	User browser's User Agent.
     * @param 	ipAddress			Ip address of the user's machine from where he is accessing the survey.
     * @param 	surveyMsgId			Message Id.
     * @return 	String				Session ID.
     */
    public String createSurveySession(String browserUseragent, String ipAddress,
    		String surveyMsgId) {
		String sessionid = "";
		String sql1 = "INSERT INTO survey_user_session (from_message, endtime, starttime, browser_info, ip_address) "
			    + "VALUES (?, 0, now(), ?, ?)";
		String sql2 = "SELECT LAST_INSERT_ID()";
		PreparedStatement statement1 = null;
		PreparedStatement statement2 = null;
		try {
		    
			/* connect to the database */
		    statement1 = conn.prepareStatement(sql1);		    
		    statement1.setString(1, surveyMsgId);
		    statement1.setString(2, browserUseragent);
		    statement1.setString(3, ipAddress);
		    
		    /* add a new session record and save the startime & user's browser info and ip address */
		    statement1.executeUpdate();
		    
		    /* get the new session id */
		    statement2 = conn.prepareStatement(sql2);		    
		    ResultSet rs = statement2.executeQuery();
		    if (rs.next()) {
		    	sessionid = rs.getString(1);
		    }
		} catch (SQLException e) {
		    WISEApplication.logError(
		    		"USER CREATE DB SESSION:" + e.toString(), null);
		} finally {
			if (statement1 != null || statement2 != null) {
				try {
					if (statement1 != null) {
						statement1.close();
					}
					if (statement2 != null) {
						statement2.close();
					}
				} catch (SQLException e) {
					WISEApplication.logError(
							"USER CREATE DB SESSION:" + e.toString(), null);
				}
			}
		}
		return sessionid;
    }

    /**
     * close the user's survey session by setting endtime to now().
     */
    public void closeSurveySession() {
		String sql = "UPDATE survey_user_session SET endtime = now() WHERE id = ?";
		PreparedStatement stmt = null;
		try {
		    stmt = conn.prepareStatement(sql);
		    stmt.setInt(1, Integer.parseInt(theUser.userSession));
		    stmt.executeUpdate();
		    
		} catch (SQLException e) {
		    WISEApplication.logError(
		    		"USER CLOSE SURVEY SESSION :" + e.toString(), null);
		} catch (NumberFormatException e){
    		WISEApplication.logError("UDB setDone Invalid User ID"
    				+ e.toString(), e);
    	} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					WISEApplication.logError(
				    		"USER CLOSE SURVEY SESSION :" + e.toString(), null);
				}
			}
		}
    }

    /** 
     * update the user's status to be done in the survey data table
     */
    public void setDone() {
    	
    	/* set endtime for the current survey session */
    	String sql1 = "UPDATE survey_user_session SET endtime = now() WHERE id = ?";

    	/* set status = null, which means the user has completed the survey */
    	String sql2 = "UPDATE " + mainTableName
    			+ " SET status = null WHERE invitee = " + theUser.id;

    	PreparedStatement stmt1 = null;
    	PreparedStatement stmt2 = null;
    	try {
    		stmt1 = conn.prepareStatement(sql1);    		
    		stmt1.setInt(1, Integer.parseInt(theUser.userSession));
    		stmt1.executeUpdate();

    		stmt2 = conn.prepareStatement(sql2);
    		stmt2.setInt(1, Integer.parseInt(theUser.id));
    		stmt2.executeUpdate();
    	} catch (SQLException e) {
    		WISEApplication.logError("UDB setDone:" + e.toString(),
    				null);
    	} catch (NumberFormatException e){
    		WISEApplication.logError("UDB setDone Invalid User ID"
    				+ e.toString(), e);
    	} finally {
    		if (stmt1 != null) {
    			try {
    				stmt1.close();
    			} catch (SQLException e) {
    				WISEApplication.logError(
    						"UDB setDone:" + e.toString(), null);
    			}
    		}
    		if (stmt2 != null) {
    			try {
    				stmt2.close();
    			} catch (SQLException e) {
    				WISEApplication.logError(
    						"UDB setDone:" + e.toString(), null);
    			}
    		}
    	}
    }

    /**
     * Gets a hashtable of all the page IDs which the user has completed and
     * currently working on
     * 
     * @return	Hashtable	Status of user with respect to each page.
     */
    public Hashtable<String, String> getCompletedPages() {
		Hashtable<String, String> pages = new Hashtable<String, String>();
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;
		String sql1 = "select status from " + mainTableName
			    + " where invitee = ?";
		String sql2 = "select distinct page from page_submit where invitee = ?"
			    + " and survey = ?";		
		
		try {
		    stmt1 = conn.prepareStatement(sql1);
		    
		    stmt1.setInt(1, Integer.parseInt(theUser.id));
		    
		    /* get the status' value from the survey data table */		    
		    ResultSet rs = stmt1.executeQuery();
		    
		    /* add it into the hashtable */
		    while (rs.next()) {
		    	pages.put(rs.getString(1), "Current");
		    }
		    
		    // get the submitted page IDs from page submit table
		    stmt2 = conn.prepareStatement(sql2);
		    stmt2.setInt(1, Integer.parseInt(theUser.id));
		    stmt2.setString(2, surveyID);

		    rs = stmt2.executeQuery();
		    
		    /* input them into the hashtable */
		    while (rs.next()) {
		    	pages.put(rs.getString(1), "Completed");
		    }
		    
		} catch (SQLException e) {
		    WISEApplication.logError(
			    "USER DB getCompletedPages:" + e.toString(), e);
		} catch (NumberFormatException e){
			 WISEApplication.logError("getCompletedPages Invalid User ID"
						+ e.toString(), e);
		} finally {
			if (stmt1 != null || stmt2 != null) {
				try {
					if (stmt1 != null) {
						stmt1.close();
					}
					if (stmt2 != null) {
						stmt2.close();
					}
				} catch (SQLException e) {
					WISEApplication.logError(
							"getCompletedPages:" + e.toString(), null);
				}
			}
		}
		return pages;
    }

    /**
     * Records the new invitee who has accessed the survey for the first time.
     * 
     * @param 	inviteeId	Id of the new invitee.
     * @param 	surveyId	Survey that the user is accessing.
     * @return	boolean		If the record is successful or not.
     */
    public boolean recordWelcomeHit(String inviteeId, String surveyId) {

    	/* insert a new accessment record */
		String sql = "INSERT INTO welcome_hits (invitee, survey) VALUES (?, ?)";
		
		PreparedStatement stmt = null;
		boolean resultp = false;
		try {
		    stmt = conn.prepareStatement(sql);
		    stmt.setInt(1, Integer.parseInt(inviteeId));
		    stmt.setString(2, surveyID);
		    int temp = stmt.executeUpdate();		    
		    if (temp>0) {
		    	resultp = true;
		    }		    
    		
    	} catch (NumberFormatException e) {
    		WISEApplication.logError(
    				"USER RECORD WELCOME HIT:" + e.toString(), e);
    	} catch (SQLException e){
			 WISEApplication.logError("USER RECORD WELCOME HIT:"
						+ e.toString(), e);
		} finally {
    		try {
    			stmt.close();
    		} catch (SQLException e) {
    			log.error(e);
    		}
    	}
    	return resultp;
    }

    /**
     * Records the details about a user who has declined taking the survey.
     * 
     * @param msgId		The Id in the URL which is used to access the system.
     * @param studyId	Study space name which the user is declining.
     * @param inviteeId	The Id of user.
     * @param surveryId	Survey name which the user is declining.
     * @return boolean	If the operation of recording was successful or not.
     */
    public boolean recordDeclineHit(String msgId, String studyId,
    		String inviteeId, String surveryId) {

    	boolean resultp = false;
    	//Statement stmt = null;
    	String sql1 = "INSERT INTO decline_hits (msg_id, survey) VALUES (?, ?)";
    	String sql2 = "update survey_user_state set state='declined', state_count=1, " +
    			"entry_time=now() where invitee= ? AND survey= ?";
    	PreparedStatement stmt1 = null;
    	PreparedStatement stmt2 = null;
    	try {
    		/* connect to the database*/
    		stmt1 = conn.prepareStatement(sql1);
    		stmt2 = conn.prepareStatement(sql2);
    		
    		/* add a new decline hits record */
    		stmt1.setString(1, msgId);
    		stmt1.setString(2, studyId);
    		int out1 = stmt1.executeUpdate();
    		
    		/* update the user state */
    		stmt2.setInt(1, Integer.parseInt(inviteeId));
    		stmt2.setString(2, surveryId);
    		int out2 = stmt2.executeUpdate();
    		if (out1 > 0 && out2 > 0 ) {
    			resultp = true;
    		}
    	}  catch (NumberFormatException e) {
    		WISEApplication.logError(
    				"USER RECORD DECLINE HIT:" + e.toString(), e);
    	} catch (SQLException e){
			 WISEApplication.logError("USER RECORD DECLINE HIT:"
						+ e.toString(), e);
		} finally {
    		try {
    			if (stmt1 != null) {
    				stmt1.close();
    			}
    			if (stmt2 != null) {
    				stmt2.close();
    			}    			
    		} catch (SQLException e) {
    			log.error(e);
    		}
    	}
    	return resultp;
    }

    /**
     * Records the reason for declining to taking the survey.
     * 
     * @param 	inviteeId	User who wants to decline the survey.
     * @param 	reason		Reason for declining
     * @return	boolean		If the reason has successfully updated or not.
     */
    public boolean setDeclineReason(String inviteeId, String reason) {

    	PreparedStatement stmt = null;
    	String sql = "INSERT INTO decline_reason (invitee, reason) VALUES (?,?)";
    	boolean retVal = false;
    	try {

    		/* connect to the database */
    		stmt = conn.prepareStatement(sql);
    		stmt.setInt(1,Integer.parseInt(inviteeId));
    		stmt.setString(2, reason);

    		/* save the user's decline reason */
    		int out = stmt.executeUpdate();	    
    		if(out > 0) {
    			retVal = true;
    		}
    	} catch (NumberFormatException e) {
    		WISEApplication.logError(
    				"USER SET DECLINE REASON:" + e.toString(), e);
    	} catch (SQLException e){
    		WISEApplication.logError("USER SET DECLINE REASON:"
    				+ e.toString(), e);
    	} finally {
    		try {
    			if (stmt != null) {
    				stmt.close();
    			}		
    		} catch (SQLException e) {
    			log.error(e);
    		}
    	}
    	return retVal;
    }

    /**
     * Outputs number of users who have finished taking the survey as of now.
     * 
     * @param 	surveyId	Survey Name whose status has to be checked.
     * @return	int 		Number of people who have finished taking the survey.
     */
    public int checkCompletionNumber(String surveyId) {
    	int numCompleters = 0;
    	String surveyDataTable = surveyId + "_data";
    	String sql = "SELECT count(distinct invitee) FROM " + surveyDataTable +  "WHERE status IS NULL";
    	PreparedStatement stmt = null;
    	try {
    		
    		/* connect to the database */
    		stmt = conn.prepareStatement(sql);
    		
    		/* count the completers */
    		ResultSet rs = stmt.executeQuery();
    		if (rs.next()) {
    			numCompleters = rs.getInt("count(distinct invitee)");
    		}
    		rs.close();
    	} catch (SQLException e) {
    		WISEApplication.logError(
    				"USER CHECK COMPLETION NUMBER:" + e.toString(), e);
    	} finally {
    		try {
    			if (stmt != null) {
    				stmt.close();
    			}
    		} catch (SQLException e) {
    			log.error(e);
    		}
    	}
    	return numCompleters;
    }

    /**
     * Deletes repeating item sets.
     *  
     * @param inviteeId		Invitee whose repeating item has to be deleted.
     * @param tableName		Repeating item table name from which the item has to be deleted.
     * @param instanceName	Row's instance name which has to be deleted.
     * @return boolean		If the delete was successful or not.
     */
    public boolean deleteRowFromTable(String inviteeId, String tableName,
    		String instanceName) {

		String sqlStatement = "DELETE FROM " + tableName + " WHERE invitee= ?"
			     + " AND instance_name=?";
		PreparedStatement statement = null;
	
		try {    
		    statement = conn.prepareStatement(sqlStatement);
		    statement.setInt(1, Integer.parseInt(theUser.id));
		    statement.setString(2, instanceName);
		    statement.executeUpdate(sqlStatement);	
		} catch (SQLException e) {
		    log.error("Error for SQL statement: " + sqlStatement, e);
		    return false;
		} catch (NumberFormatException e){
			 WISEApplication.logError("deleteRowFromTable Invalid User ID"
						+ e.toString(), e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					WISEApplication.logError(
							"deleteRowFromTable:" + e.toString(), null);
				}
			}
		}
		return true;
    }
    
    /*
     * TODO: (med) implement subject set storage public int
     * storeSubjectSetData(Hashtable h, String subjSet_name, String pageID) {
     * String sql=""; int storecount=0; try { //connect to the database
     * Connection conn = getDBConnection(); Statement stmt =
     * conn.createStatement();
     * 
     * //then check if a user record exists in table of subject set for (int i =
     * 0; i < stems.length; i++) { sql =
     * "SELECT * from "+page.survey.id+"_"+SubjectSet_name+"_data where "; sql
     * += "invitee = " +theUser.id+" and subject="; sql +=
     * stem_fieldNames[i].substring((stem_fieldNames[i].lastIndexOf("_")+1));
     * dbtype = stmt.execute(sql); rs = stmt.getResultSet(); user_data_exists =
     * rs.next();
     * 
     * Statement stmt2 = conn.createStatement(); //read out the user's new data
     * from the hashtable params String s_new = (String)
     * params.get(stem_fieldNames[i].toUpperCase());
     * 
     * //note that s_new could be null - seperate the null value with the 0
     * value s_new = Study_Util.fixquotes(s_new); if
     * (s_new.equalsIgnoreCase("")) s_new = "NULL";
     * 
     * //if both tables (page_submit & subject set) have the user's data if
     * (user_data_exists) { String s = rs.getString(name); //compare with the
     * new user data, update the subject set data if the old value has been
     * changed if ((s==null && !s_new.equalsIgnoreCase("NULL")) || (s!=null &&
     * !s.equalsIgnoreCase(s_new))) { //create UPDATE statement sql =
     * "update "+page.survey.id+"_"+SubjectSet_name+"_data set "; sql += name +
     * " = " + s_new; sql += " where invitee = "+theUser.id+" and subject="; sql
     * += stem_fieldNames[i].substring((stem_fieldNames[i].lastIndexOf("_")+1));
     * dbtype = stmt2.execute(sql);
     * 
     * String s1; if (s != null) s1 = Study_Util.fixquotes(s); else s1 = "null";
     * //check if the user's record exists in the table of update_trail, update
     * the data there as well sql =
     * "select * from update_trail where invitee="+theUser
     * .id+" and survey='"+page.survey.id; sql +=
     * "' and page='"+page.id+"' and ColumnName='"
     * +stem_fieldNames[i].toUpperCase()+"'"; dbtype = stmt2.execute(sql);
     * ResultSet rs2 = stmt2.getResultSet(); if(rs2.next()) { //update the
     * records in the update trail if(!s1.equalsIgnoreCase(s_new)) { sql =
     * "update update_trail set OldValue='"+s1+"', CurrentValue='"+s_new; sql
     * +="', Modified=now() where invitee="
     * +theUser.id+" and survey='"+page.survey.id; sql
     * +="' and page='"+page.id+"' and ColumnName='"
     * +stem_fieldNames[i].toUpperCase()+"'"; } } //insert new record if it
     * doesn't exist in the table of update_trail else { sql =
     * "insert into update_trail (invitee, survey, page, ColumnName, OldValue, CurrentValue)"
     * ; sql += " values ("+theUser.id+",'"+page.survey.id+"','"+page.id+"','";
     * sql += stem_fieldNames[i].toUpperCase()+"','"+s1+"', '"+s_new+"')"; }
     * dbtype = stmt2.execute(sql); } } //if no user's record exists in both
     * tables else { //create a insert statement to insert this record in the
     * table of subject set sql =
     * "insert into "+page.survey.id+"_"+SubjectSet_name+"_data "; sql +=
     * "(invitee, subject, "+name+") "; sql += "values ("+theUser.id+",'"; sql
     * += Study_Util.fixquotes(stem_fieldNames[i].substring((stem_fieldNames[i].
     * lastIndexOf("_")+1))); sql += "', "+s_new+")"; dbtype =
     * stmt2.execute(sql); //and insert record into the table of update_trail as
     * well sql =
     * "insert into update_trail (invitee, survey, page, ColumnName, OldValue, CurrentValue)"
     * ; sql += " values ("+theUser.id+",'"+page.survey.id+"','"+page.id+"','";
     * sql += stem_fieldNames[i].toUpperCase()+"','null', '"+s_new+"')"; dbtype
     * = stmt2.execute(sql); } stmt2.close(); } //end of for loop stmt.close();
     * conn.close(); } //end of try catch (Exception e) {
     * Study_Util.email_alert(
     * "WISE - QUESTION BLOCK ["+page.id+"] READ FORM ("+sql
     * +"): "+e.toString()); } } //end of else return index_len;
     */
}
