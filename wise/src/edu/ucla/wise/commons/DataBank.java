package edu.ucla.wise.commons;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.commons.InviteeMetadata.Values;
import edu.ucla.wise.commons.User.INVITEE_FIELDS;
import edu.ucla.wise.initializer.WiseProperties;
import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;

/**
 * This class encapsulates the database interface for a Study Space. The static
 * part represents the MySQL interface in general
 * 
 * Also provides group-level update of the valid survey_user_states: invited,
 * declined, start_reminder_x, non_responder, started, interrupted,
 * completion_reminder_x, incompleter TODO: (low) abstract valid User-state
 * progression into static final strings, either in Data_Bank or User class
 * 
 * @author Douglas Bell
 * @version 1.0
 */
public class DataBank {

    public static String mysqlServer;
    public static final String dbDriver = "jdbc:mysql://";
    public static final String MainTableExtension = "_data";

    public static final char intValueTypeFlag = 'n';
    public static final char textValueTypeFlag = 'a';
    public static final char decimalValueTypeFlag = 'd';

    public static final String intFieldDDL = " int(6),";
    public static final String textFieldDDL = " text,";
    public static final String decimalFieldDDL = " decimal(11,3),";

    // TODO (med) add mechanism to use decimalPlaces and maxSize rather than
    // this default

    private static final Logger LOGGER = Logger.getLogger(DataBank.class);

    /** Instance Variables */
    StudySpace studySpace;
    public String dbdata;
    public String dbuser;
    public String dbpwd;
    public String emailEncryptionKey;

    /**
     * Sets the Database driver from the properties file.
     * 
     * @param props
     *            Resource bundle object from which the details of the data base
     *            driver are obtained.
     */
    public static void SetupDB(WiseProperties properties) {
	mysqlServer = properties.getStringProperty("mysql.server");
	try {
	    DriverManager.registerDriver(new com.mysql.jdbc.Driver());
	} catch (SQLException e) {
	    LOGGER.error("DataBank init Error: " + e, e);
	}
    }

    /**
     * Constructor setting up data storage for a survey session. All the
     * parameters are now obtained from database
     * 
     * @param ss
     *            Study space to which the data bank class is linked to.
     * 
     */
    public DataBank(StudySpace ss, StudySpaceParameters params) {
	this.studySpace = ss;
	this.dbuser = params.getDatabaseUsername();
	this.dbdata = params.getDatabaseName();
	this.dbpwd = params.getDatabasePassword();
	this.emailEncryptionKey = params.getDatabaseEncryptionKey();
    }

    /**
     * Gets list of current survey xml files and request loading of each by
     * studySpace
     */
    public void readSurveys() {
	Connection conn = null;
	PreparedStatement stmt = null;

	/* Read all the surveys in the current database */
	String sql = "SELECT filename from surveys, "
		+ "(SELECT max(internal_id) as maxint FROM surveys group by id) maxes "
		+ "WHERE maxes.maxint = surveys.internal_id";
	try {
	    conn = this.getDBConnection();
	    stmt = conn.prepareStatement(sql);
	    ResultSet rs = stmt.executeQuery();

	    while (rs.next()) {
		String filename = rs.getString("filename");
		this.studySpace.loadSurvey(filename);
	    }
	} catch (SQLException e) {
	    LOGGER.error("DataBank survey file loading error:" + e.toString(),
		    e);
	} finally {
	    try {
		if (conn != null) {
		    conn.close();
		}
		if (stmt != null) {
		    stmt.close();
		}
	    } catch (SQLException e) {
		LOGGER.error(
			"DataBank survey file loading error:" + e.toString(), e);
	    }
	}
    }

    /**
     * Creates a User object form the message ID that is obtained from the URL
     * whenever a user tries to use WISE system.
     * 
     * @param msgId
     * @return
     */
    public User makeUserFromMsgID(String msgId) {
	User theUser = null;
	Connection conn = null;
	PreparedStatement stmt = null;

	/* get the user's ID and the survey ID being responded to */
	String sql = "select invitee, survey from survey_message_use where messageId= ?";
	LOGGER.debug(sql);
	try {
	    conn = this.getDBConnection();
	    stmt = conn.prepareStatement(sql);
	    String usrID, surveyID;
	    Survey survey;
	    stmt.setString(1, msgId);
	    ResultSet rs = stmt.executeQuery();

	    /* if message id not found, result set will be empty */
	    if (rs.next()) {
		usrID = rs.getString("invitee");
		surveyID = rs.getString("survey");
		survey = this.getSurvey(surveyID);
		System.out.println(surveyID);
		if ((usrID == null) || (survey == null)) {
		    throw new Exception("Can't get user " + usrID
			    + " or survey ID " + surveyID);
		}
		theUser = new User(usrID, survey, msgId, this);
	    }
	    rs.close();
	} catch (SQLException e) {
	    LOGGER.error("Data_Bank user creation error:" + e.toString(), e);
	} catch (Exception e) {
	    LOGGER.error("Data_Bank user creation error:" + e.toString(), e);
	} finally {
	    try {
		if (conn != null) {
		    conn.close();
		}
		if (stmt != null) {
		    stmt.close();
		}
	    } catch (SQLException e) {
		LOGGER.error(
			"DataBank survey file loading error:" + e.toString(), e);
	    }
	}
	return theUser;
    }

    /**
     * Establishes data base connection and returns a Connection object for
     * other functions.
     * 
     * @return Connection Database connection object.
     * @throws SQLException
     */
    public Connection getDBConnection() throws SQLException {
	return DriverManager.getConnection(dbDriver + mysqlServer + "/"
		+ this.dbdata + "?user=" + this.dbuser + "&password="
		+ this.dbpwd + "&autoReconnect=true");
    }

    /**
     * Searches by the survey ID and returns a specific survey.
     * 
     * @param surveyId
     *            Survey Id which has to be returned.
     * @return Survey Survey object associated with the surveyId.
     */
    public Survey getSurvey(String sid) {
	return this.studySpace.getSurvey(sid);
    }

    /**
     * Creates the tables for storing the responses for the survey questions.
     * This function is called while loading the survey.
     * 
     * @param survey
     *            Survey for which tables have to be created.
     * @throws SQLException
     */
    public void setupSurvey(Survey survey) throws SQLException {

	/* Pralav- first handle repeating questions */
	ArrayList<RepeatingItemSet> repeatingItemSets = survey
		.getRepeatingItemSets();
	for (RepeatingItemSet repeatSetInstance : repeatingItemSets) {

	    /* generate a table for this instance */
	    this.createRepeatingSetTable(repeatSetInstance);
	}

	/*
	 * "create_string" contains just the core syntax representing the survey
	 * fields; can test for changes by comparing this
	 */
	String newCreatestr = "";// old_create_str;
	String[] fieldList = survey.getFieldList();
	char[] valTypeList = survey.getValueTypeList();
	for (int i = 0; i < fieldList.length; i++) {
	    if (fieldList[i] != null) {
		if (valTypeList[i] == textValueTypeFlag) {
		    newCreatestr += fieldList[i] + textFieldDDL;
		} else if (valTypeList[i] == decimalValueTypeFlag) {
		    newCreatestr += fieldList[i] + decimalFieldDDL;
		} else {
		    newCreatestr += fieldList[i] + intFieldDDL;
		}
	    }
	    /* DON'T chop trailing comma as it precedes rest of DDL string: */
	}

	Connection conn = null;
	Statement stmt = null;
	Statement stmtM = null;

	String createSql = "";
	try {
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();
	    stmtM = conn.createStatement();

	    /*
	     * //if all columns are the same, then keep the old table if( -- OLD
	     * FIELDS SAME AS NEW FIELDS -- ) { //clean up the value of archive
	     * date in table of surveys survey.update_archive_date(conn);
	     * //update the creation syntax for the new record String sql =
	     * "select internal_id, uploaded, archive_date from surveys where internal_id=(select max(internal_id) from surveys where id='"
	     * +id+"')"; stmt.execute(sql); ResultSet rs = stmt.getResultSet();
	     * //keep the uploaded value - (mysql tends to wipe it off by using
	     * the current timestamp value) //and set the archive date to be
	     * current - (it's the current survey, has not been archived yet)
	     * if(rs.next()) { String
	     * sql_m="update surveys set create_syntax='"+
	     * new_create_str+"', uploaded='"
	     * +rs.getString(2)+"', archive_date='current' where internal_id="
	     * +rs.getString(1); boolean dbtype_m = stmt_m.execute(sql_m); }
	     * 
	     * return; //leave the old data table and other relevant tables
	     * alone } else
	     */

	    /*
	     * get the temporary survey record inserted by admin tool in the
	     * SURVEYS table
	     */
	    String sql = "select internal_id, filename, title, uploaded, status "
		    + "from surveys where internal_id=(select max(internal_id) from surveys where id='"
		    + survey.getId() + "')";
	    stmt.execute(sql);
	    ResultSet rs = stmt.getResultSet();
	    String internalId, filename, title, uploaded, status;

	    if (rs.next()) {

		/* save the data of the newly inserted survey record */
		internalId = rs.getString("internal_id");
		filename = rs.getString("filename");
		title = rs.getString("title");
		uploaded = rs.getString("uploaded");
		status = rs.getString("status");

		/* delete the newly inserted survey record */
		String sqlM = "delete from surveys where internal_id="
			+ internalId;
		stmtM.execute(sqlM);

		/* archive the old data table if it exists in the database */
		String oldArchiveDate = this.archiveTable(survey);

		/* create new data table */
		createSql = "CREATE TABLE " + survey.getId()
			+ MainTableExtension
			+ " (invitee int(6) not null, status varchar(64),";
		createSql += newCreatestr;
		createSql += "PRIMARY KEY (invitee),";
		createSql += "FOREIGN KEY (invitee) REFERENCES invitee(id) ON DELETE CASCADE";
		createSql += ") ";

		LOGGER.info("Create table statement is:" + createSql);

		stmtM.execute(createSql);

		/*
		 * add the new survey record back in the table of surveys, and
		 * save the new table creation syntax and set the archive date
		 * to be current - (it's the current survey, has not been
		 * archived yet)
		 */
		sqlM = "insert into surveys(internal_id, id, filename, title, uploaded, status, archive_date, create_syntax) "
			+ "values("
			+ internalId
			+ ",'"
			+ survey.getId()
			+ "','"
			+ filename
			+ "',\""
			+ title
			+ "\",'"
			+ uploaded
			+ "','"
			+ status
			+ "','current','"
			+ newCreatestr
			+ "')";
		stmtM.execute(sqlM);

		/*
		 * append the data from the old data table to the new created
		 * one if in production mode, status.equalsIgnoreCase("P") but
		 * taking that out of criteria for user trust
		 */

		// if(old_archive_date!=null &&
		// !old_archive_date.equalsIgnoreCase("") &&
		// !old_archive_date.equalsIgnoreCase("no_archive") )
		if ((oldArchiveDate != null)
			&& !oldArchiveDate.equalsIgnoreCase("")) {
		    this.appendData(survey, oldArchiveDate);
		}
	    } // end of if
	} catch (SQLException e) {
	    LOGGER.error("SURVEY - CREATE TABLE: " + createSql, e);
	} finally {
	    try {
		if (conn != null) {
		    conn.close();
		}
		if (stmt != null) {
		    stmt.close();
		}
		if (stmtM != null) {
		    stmtM.close();
		}
	    } catch (SQLException e) {
		LOGGER.error(
			"DataBank survey table creation error:" + e.toString(),
			e);
	    }
	}
	return;
    }

    /**
     * Creating tables for repeating item set questions in the survey.
     * 
     * @param iRepeatingSet
     *            RepeatingItemSet for which table has to be created.
     * 
     */
    public void createRepeatingSetTable(RepeatingItemSet iRepeatingSet) {

	String tableName = iRepeatingSet.getNameForRepeatingSet();

	this.archiveTable("repeat_set_" + tableName);

	String sqlFieldList = "";//
	String[] fieldList = iRepeatingSet.listFieldNames();

	char[] valTypeList = iRepeatingSet.getValueTypeList();
	for (int i = 0; i < fieldList.length; i++) {
	    if (valTypeList[i] == textValueTypeFlag) {
		sqlFieldList += fieldList[i] + textFieldDDL;
	    } else if (valTypeList[i] == decimalValueTypeFlag) {
		sqlFieldList += fieldList[i] + decimalFieldDDL;
	    } else {
		sqlFieldList += fieldList[i] + intFieldDDL;
	    }
	}

	Connection conn = null;
	Statement stmt = null;
	try {
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();

	    String sqlStatement = "";

	    /* create new data table */
	    sqlStatement = "CREATE TABLE "
		    + "repeat_set_"
		    + tableName
		    + " (instance int(6) not null auto_increment, invitee int(6) not null, instance_name text null, ";
	    sqlStatement += sqlFieldList;
	    sqlStatement += "PRIMARY KEY (instance),";
	    sqlStatement += "FOREIGN KEY (invitee) REFERENCES invitee(id) ON DELETE CASCADE";
	    sqlStatement += ")";
	    stmt.execute(sqlStatement);

	} catch (SQLException e) {
	    LOGGER.error("Repeating Set - CREATE TABLE: " + e.toString(), null);
	} finally {
	    try {
		if (conn != null) {
		    conn.close();
		}
		if (stmt != null) {
		    stmt.close();
		}
	    } catch (SQLException e) {
		LOGGER.error("Repeating Set - CREATE TABLE:" + e.toString(), e);
	    }
	}
	return;
    }

    /**
     * Updates the value of archive date in table of surveys
     * 
     * @param survey
     *            Survey for which tables have to be updated.
     */
    public void updateArchiveDate(Survey survey) {
	Connection conn = null;
	Statement stmt = null;
	Statement stmtM = null;
	Statement stmtN = null;
	try {
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();
	    stmtM = conn.createStatement();
	    stmtN = conn.createStatement();

	    /* get the internal id of the old survey record */
	    String sql = "select max(internal_id) from "
		    + "(select * from surveys where id='" + survey.getId()
		    + "' and internal_id <> "
		    + "(select max(internal_id) from surveys where id='"
		    + survey.getId() + "')) as a group by a.id;";
	    stmt.execute(sql);
	    ResultSet rs = stmt.getResultSet();

	    /* get the uploaded date */
	    if (rs.next()) {
		String sqlM = "select internal_id, uploaded from surveys where internal_id="
			+ rs.getString(1);
		stmtM.execute(sqlM);
		ResultSet rsM = stmtM.getResultSet();

		/*
		 * keep the value of uploaded date - (mysql tends to
		 * automatically update this value and set the archive date to
		 * be none - (no need to do the archive since data sets are
		 * identical)
		 */
		if (rsM.next()) {
		    String sqlN = "update surveys set uploaded='"
			    + rsM.getString(2)
			    + "', archive_date='' where internal_id="
			    + rsM.getString(1);
		    stmtN.execute(sqlN);
		}
	    }
	} catch (SQLException e) {
	    LOGGER.error("SURVEY - UPDATE ARCHIVE DATE: " + e.toString(), null);
	} finally {
	    try {
		if (conn != null) {
		    conn.close();
		}
		if (stmt != null) {
		    stmt.close();
		}
		if (stmtM != null) {
		    stmtM.close();
		}
		if (stmtN != null) {
		    stmtN.close();
		}
	    } catch (SQLException e) {
		LOGGER.error(
			"DataBank survey table creation error:" + e.toString(),
			e);
	    }
	}
	return;
    }

    /**
     * Archives old survey tables.
     * 
     * @param tableName
     *            Table that has to be archived.
     * @return String The date when the table is archived.
     */
    public String archiveTable(String tableName) {
	String archiveString = "";
	Connection connection = null;
	Statement statement = null;
	try {
	    connection = this.getDBConnection();
	    statement = connection.createStatement();

	    boolean oldTableFound = false;
	    String archiveDate = "";

	    ResultSet resultSet = statement.executeQuery("show tables");

	    while (resultSet.next()) {
		if (resultSet.getString(1).equalsIgnoreCase(tableName)) {
		    oldTableFound = true;
		    break;
		}
	    }

	    if (oldTableFound) {
		String sqlToCheckIfTableIsEmpty = "select * from " + tableName;
		Statement statementToCheckEmpty = connection.createStatement();
		ResultSet resultSetForTable = statementToCheckEmpty
			.executeQuery(sqlToCheckIfTableIsEmpty);

		if (!resultSetForTable.next()) {
		    String sqlToDropTable = "DROP TABLE IF EXISTS " + tableName;
		    statementToCheckEmpty.execute(sqlToDropTable);

		    /* return empty archive date */
		    archiveDate = "";

		} else {

		    /*
		     * otherwise, archive the table by changing its name with
		     * the current timestamp get the current date
		     */
		    java.util.Date today = new java.util.Date();
		    SimpleDateFormat formatter = new SimpleDateFormat(
			    "yyyyMMddhhmm");
		    archiveDate = formatter.format(today);

		    String sqlToAlterTable = "ALTER TABLE " + tableName
			    + " RENAME " + tableName + "_arch_" + archiveDate;
		    statement.execute(sqlToAlterTable);
		    archiveString = archiveDate;
		}

	    }
	} catch (SQLException e) {
	    LOGGER.error("Error while archiving survey", e);
	} finally {
	    try {
		if (connection != null) {
		    connection.close();
		}
		if (statement != null) {
		    statement.close();
		}
	    } catch (SQLException e) {
		LOGGER.error(
			"DataBank survey table creation error:" + e.toString(),
			e);
	    }
	}
	return archiveString;
    }

    /**
     * archive the old data table -- called both for D and P mode if new survey
     * uploaded or if P survey closed
     * 
     * @param survey
     *            Survey whose tables are to be archived.
     * @return String The date when the table are archived.
     */
    public String archiveTable(Survey survey) {
	String archiveStr = "";
	String archiveDate = "";
	Connection conn = null;
	Statement stmt = null;
	Statement stmtM = null;

	try {
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();
	    stmtM = conn.createStatement();

	    /* check if the old data table exists in the current database */
	    boolean found = false;
	    ResultSet rs = stmt.executeQuery("show tables");
	    while (rs.next()) {
		if (rs.getString(1).equalsIgnoreCase(
			survey.getId() + MainTableExtension)) {
		    found = true;
		    break;
		}
	    }

	    /* if the old data table can be found */
	    if (found) {

		/* then check if the table is empty */
		String sqlM = "select * from " + survey.getId()
			+ MainTableExtension;
		stmtM.execute(sqlM);
		ResultSet rsM = stmtM.getResultSet();

		/*
		 * if the table is empty, simply drop the table - no need to
		 * archive
		 */
		if (!rsM.next()) {
		    String sql = "DROP TABLE IF EXISTS " + survey.getId()
			    + MainTableExtension;
		    stmt.execute(sql);

		    /* return empty archive date */
		    archiveDate = "";

		} else {

		    /*
		     * otherwise, archive the table by changing its name with
		     * the current timestamp get the current date
		     */
		    java.util.Date today = new java.util.Date();
		    SimpleDateFormat formatter = new SimpleDateFormat(
			    "yyyyMMddhhmm");
		    archiveDate = formatter.format(today);

		    String sql = "ALTER TABLE " + survey.getId()
			    + MainTableExtension + " RENAME " + survey.getId()
			    + "_arch_" + archiveDate;
		    stmt.execute(sql);
		}

		/*
		 * update the archive date of this old survey record in the
		 * table of surveys the old survey record should have the max
		 * internal id since the new survey record has been deleted from
		 * the table
		 */
		sqlM = "select internal_id, uploaded from surveys where internal_id=(select max(internal_id) from surveys where id='"
			+ survey.getId() + "')";
		stmtM.execute(sqlM);
		rsM = stmtM.getResultSet();
		if (rsM.next()) {
		    String sql = "update surveys set uploaded='"
			    + rsM.getString(2) + "', archive_date='"
			    + archiveDate + "' where internal_id="
			    + rsM.getString(1);
		    stmt.execute(sql);
		    archiveStr = archiveDate;
		}

	    } // end of if
	} catch (SQLException e) {
	    LOGGER.error("SURVEY - ARCHIVE DATA TABLE: " + e.toString(), null);
	} finally {
	    try {
		if (conn != null) {
		    conn.close();
		}
		if (stmt != null) {
		    stmt.close();
		}
		if (stmtM != null) {
		    stmtM.close();
		}
	    } catch (SQLException e) {
		LOGGER.error("SURVEY - ARCHIVE DATA TABLE:" + e.toString(), e);
	    }
	}
	return archiveStr;
    }

    /**
     * Append the data in the same named column(s) from archived data table to
     * the newly created one.
     * 
     * @param survey
     *            Survey whose tables are to be appended and archived.
     * @param archiveDate
     *            Archive data of the old table.
     * @throws SQLException
     */
    public void appendData(Survey survey, String archiveDate)
	    throws SQLException {
	Connection conn = null;
	Statement stmt = null;
	try {
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();

	    /* get old data set - the columns names from the archived table */
	    String sql = "show columns from " + survey.getId() + "_arch_"
		    + archiveDate;
	    stmt.execute(sql);
	    ResultSet rs = stmt.getResultSet();
	    List<String> oldColumns = new ArrayList<String>();
	    while (rs.next()) {

		/* put Field names into the old data set array list */
		oldColumns.add(rs.getString(1));
	    }

	    /* get new data set - the columns names from new created table */
	    sql = "show columns from " + survey.getId() + MainTableExtension;
	    stmt.execute(sql);
	    rs = stmt.getResultSet();
	    Set<String> newColumns = new HashSet<String>();
	    while (rs.next()) {

		/* put Field names into the new data set array list */
		newColumns.add(rs.getString(1).toUpperCase());
	    }

	    // sort the two array list
	    Collections.sort(oldColumns);

	    int i;

	    /* compare with the two array list */
	    List<String> commonColumns = new ArrayList<String>();

	    /* and put the common columns into the common data set array list */
	    for (String oldStr : oldColumns) {
		if (newColumns.contains(oldStr.toUpperCase())
			&& !oldStr.equalsIgnoreCase("status")
			&& !oldStr.equalsIgnoreCase("invitee")) {
		    commonColumns.add(oldStr);
		}
	    }

	    // A better and efficient of finding common elements betweens two
	    // lists is above.. this is deprecated code.
	    // for (i = 0, j = 0; i < old_columns.size(); i++) {
	    // String old_str = (String) old_columns.get(i);
	    // while (j < new_columns.size()) {
	    // String new_str = (String) new_columns.get(j);
	    // // the common data set doesn't include the columns of status
	    // // & invitee
	    // if (old_str.compareToIgnoreCase(new_str) == 0
	    // && !old_str.equalsIgnoreCase("status")
	    // && !old_str.equalsIgnoreCase("invitee")) {
	    // common_columns.add(old_str);
	    // j++;
	    // break;
	    // } else if (old_str.compareToIgnoreCase(new_str) < 0) {
	    // break;
	    // } else if (old_str.compareToIgnoreCase(new_str) > 0) {
	    // j++;
	    // }
	    // } // end of while
	    // }

	    /* append the data by using <insert...select...> query */
	    sql = "insert into " + survey.getId() + MainTableExtension
		    + " (invitee, status,";
	    for (i = 0; i < commonColumns.size(); i++) {
		sql += commonColumns.get(i);
		if (i != (commonColumns.size() - 1)) {
		    sql += ", ";
		}
	    }
	    sql += ") select ";
	    sql += survey.getId() + "_arch_" + archiveDate + ".invitee, "
		    + survey.getId() + "_arch_" + archiveDate + ".status, ";
	    for (i = 0; i < commonColumns.size(); i++) {
		sql += survey.getId() + "_arch_" + archiveDate + ".";
		sql += commonColumns.get(i);
		if (i != (commonColumns.size() - 1)) {
		    sql += ", ";
		}
	    }
	    sql += " from " + survey.getId() + "_arch_" + archiveDate;
	    // Study_Util.email_alert("SURVEY - APPEND DATA debug: "+sql);
	    stmt.execute(sql);
	    stmt.close();

	} catch (SQLException e) {
	    LOGGER.error("SURVEY - APPEND DATA: " + e.toString(), null);
	} finally {
	    try {
		if (conn != null) {
		    conn.close();
		}
		if (stmt != null) {
		    stmt.close();
		}
	    } catch (SQLException e) {
		LOGGER.error("SURVEY - APPEND DATA:" + e.toString(), e);
	    }
	}
	return;
    }

    /**
     * Drop data tables including the survey data table & subject set data
     * tables; update surveys table Remove a survey -- should only be called for
     * Development mode surveys.
     * 
     * @param survey
     *            Survey which has to be removed.
     * @return String HTML string of all the dropped tables.
     */
    public String deleteSurvey(Survey survey) {
	String useResult = "";
	Connection conn = null;
	Statement stmt = null;
	Statement stmtM = null;

	try {

	    /* connect to the database */
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();
	    stmtM = conn.createStatement();

	    /* pick up all the related data tables */
	    String sqlM = "show tables";
	    stmtM.execute(sqlM);
	    ResultSet rsM = stmtM.getResultSet();
	    while (rsM.next()) {
		String tableName = rsM.getString(1);
		if ((tableName.indexOf(survey.getId() + "_") != -1)
			&& (tableName.indexOf(MainTableExtension) != -1)) {

		    /* drop this table */
		    String sql = "DROP TABLE IF EXISTS " + tableName;
		    stmt.execute(sql);
		}
	    }
	    useResult = this.clearSurveyUseData(survey);
	    sqlM = "Update surveys set status='R', uploaded=uploaded, archive_date='no_archive' "
		    + "WHERE id ='" + survey.getId() + "'";
	    stmtM.execute(sqlM);
	    return "<p align=center>Survey " + survey.getId()
		    + " successfully dropped & old survey files archived.</p>"
		    + useResult;
	} catch (SQLException e) {
	    LOGGER.error("SURVEY - DROP Table error: " + e.toString(), e);
	    return "<p align=center>ERROR deleting survey " + survey.getId()
		    + ".</p>" + useResult
		    + "Please discuss with the WISE Administrator.</p>";
	} finally {
	    try {
		if (conn != null) {
		    conn.close();
		}
		if (stmt != null) {
		    stmt.close();
		}
		if (stmtM != null) {
		    stmtM.close();
		}
	    } catch (SQLException e) {
		LOGGER.error("SURVEY - DROP Table error: " + e.toString(), e);
	    }
	}
    }

    /**
     * Deletes survey references - those related data tables while survey is in
     * production mode.
     * 
     * @param survey
     *            Survey whose table references are to be removed.
     * @return String HTML output saying the survey has been closed.
     */
    public String archiveProdSurvey(Survey survey) {
	Connection conn = null;
	Statement stmt = null;
	try {
	    String archiveDate = this.archiveTable(survey);

	    /*
	     * change the survey mode from P to C in table surveys C - survey
	     * closed
	     */
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();
	    String sql = "update surveys set status='C', uploaded=uploaded, archive_date='"
		    + archiveDate + "' " + "WHERE id ='" + survey.getId() + "'";
	    stmt.execute(sql);

	    /* remove the interview records from table - interview_assignment */
	    sql = "DELETE FROM interview_assignment WHERE survey = '"
		    + survey.getId() + "' and pending=-1";
	    stmt.execute(sql);

	    /*
	     * delete the survey data from *some* related tables -- not sure why
	     * necessary
	     */
	    // String sql = "DELETE FROM update_trail WHERE survey = '" +
	    // survey.getId() + "'";
	    // stmt.execute(sql);
	    // sql = "DELETE FROM page_submit WHERE survey = '" + survey.getId()
	    // +
	    // "'";
	    // stmt.execute(sql);
	    return "<p align=center>Survey "
		    + survey.getId()
		    + " successfully closed archived. Discuss with WISE database Admin if you need access to old data.</p>";
	} catch (Exception e) {
	    LOGGER.error("Error - Closing PRODUCTION SURVEY: " + e.toString(),
		    e);
	    return "<p align=center>ERROR Closing survey " + survey.getId()
		    + ".</p>"
		    + "Please discuss with the WISE Administrator.</p>";

	} finally {
	    try {
		if (conn != null) {
		    conn.close();
		}
		if (stmt != null) {
		    stmt.close();
		}
	    } catch (SQLException e) {
		LOGGER.error(
			"Error - Closing PRODUCTION SURVEY: " + e.toString(), e);
	    }
	}
    }

    /**
     * Clears data from data tables including the survey data table & subject
     * set data tables.
     * 
     * @param survey
     *            Survey whose table references are to be cleared.
     * @return String HTML response of the status of clearing the table data.
     */
    public String clearSurveyData(Survey survey) {
	String useResult = "";
	Connection conn = null;
	Statement stmt = null;
	Statement stmtM = null;
	try {
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();
	    stmtM = conn.createStatement();

	    /* pick up all the related data tables */
	    ResultSet rs = stmt.executeQuery("show tables");
	    while (rs.next()) {
		String tableName = rs.getString(1);
		if ((tableName.indexOf(survey.getId() + "_") != -1)
			&& (tableName.indexOf(MainTableExtension) != -1)) {

		    /* delete data from this table */
		    String sqlM = "delete from " + tableName;
		    stmtM.execute(sqlM);
		}
	    }
	    stmtM.close();
	    stmt.close();
	    conn.close();
	    useResult = this.clearSurveyUseData(survey);
	    return "<p align=center>Submitted data for survey "
		    + survey.getId()
		    + " successfully cleared from database.</p>" + useResult;
	} catch (Exception e) {
	    LOGGER.error("Error clearing survey data : " + e.toString(), e);
	    return "<p align=center>ERROR clearing data for survey "
		    + survey.getId() + " from database.</p>" + useResult
		    + "Please discuss with the WISE Administrator.</p>";
	} finally {
	    try {
		if (conn != null) {
		    conn.close();
		}
		if (stmt != null) {
		    stmt.close();
		}
		if (stmtM != null) {
		    stmtM.close();
		}
	    } catch (SQLException e) {
		LOGGER.error("Error clearing survey data :" + e.toString(), e);
	    }
	}
    }

    /**
     * Delete associated "use" data for the survey -- should only be enabled in
     * Development mode.
     * 
     * @param survey
     *            Survey whose all the associated table data is to be cleared.
     * @return String HTML response of the status of clearing the table data.
     */
    public String clearSurveyUseData(Survey survey) {
	Connection conn = null;
	Statement stmt = null;
	try {
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();
	    String sql = "DELETE FROM update_trail WHERE survey = '"
		    + survey.getId() + "'";
	    stmt.execute(sql);
	    sql = "DELETE FROM survey_message_use WHERE survey = '"
		    + survey.getId() + "'";
	    stmt.execute(sql);

	    /*
	     * delete above cascades to survey_user_session
	     * 
	     * //welcome hits let's keep for now: sql =
	     * "DELETE FROM welcome_hits WHERE survey = '" + survey.getId() +
	     * "'"; stmt.execute(sql);
	     */
	    sql = "DELETE FROM consent_response WHERE survey = '"
		    + survey.getId() + "'";
	    stmt.execute(sql);
	    sql = "DELETE FROM survey_user_state WHERE survey = '"
		    + survey.getId() + "'";
	    stmt.execute(sql);
	    sql = "DELETE FROM page_submit WHERE survey = '" + survey.getId()
		    + "'";
	    stmt.execute(sql);
	    sql = "DELETE FROM interview_assignment WHERE survey = \""
		    + survey.getId() + "\"";
	    stmt.execute(sql);
	    stmt.close();
	    conn.close();
	    return "<p align=center>Associated use data for survey "
		    + survey.getId()
		    + " successfully cleared "
		    + "(tables survey_user_state, survey_message_use, page_submit, update_trail, consent_response & for interviews).</p>";
	} catch (SQLException e) {
	    LOGGER.error(e.toString(), e);
	    return "<p align=center>ERROR clearing Associated use data for survey "
		    + survey.getId()
		    + " from "
		    + "(one or more of the tables "
		    + "survey_user_state, survey_message_use, page_submit, update_trail, consent_response).";
	} finally {
	    try {
		if (conn != null) {
		    conn.close();
		}
		if (stmt != null) {
		    stmt.close();
		}

	    } catch (SQLException e) {
		LOGGER.error("ERROR clearing Associated use data for survey"
			+ e.toString(), e);
	    }
	}
    }

    /**
     * ITERATE over all study spaces and send email invitations to any subject
     * due for a reminder or pending for initial invite.
     * 
     * @return String Outputs when the process of sending emails end.
     */
    public String sendReminders() {
	MessageSequence msgSeq = null;
	String selectSql = "", outputStr = "";
	String surveyId;
	String msID;
	Connection conn = null;
	try {

	    /*
	     * connect to the database Move users in the "started" state more
	     * than 6 hrs to be "interrupted", regardless of survey
	     */
	    conn = this.getDBConnection();
	    selectSql = "UPDATE survey_user_state SET state='interrupted', state_count=1, entry_time=entry_time "
		    + "WHERE state='started' AND entry_time <= date_sub(now(), interval 6 hour)";
	    Statement outrStmt = conn.createStatement();
	    outrStmt.executeUpdate(selectSql);
	    outrStmt.close();

	    // TODO: pending table needs separate handling -- doesn't belong in
	    // loop below but how to get survey id otherwise

	    /*
	     * Send initial invitations to users due for one according to the
	     * pending table
	     */
	    outputStr += ("\nChecking for pending initial invitations");

	    /*
	     * also Survey was not previously included in WHERE. Check: Could
	     * this obviate need for separate function??
	     */

	    /* run the sql query to update the user group's states */
	    this.invitePendingUsers();

	    /*
	     * send initial invitation & reminders by going through each survey
	     * - message_seq pair currently in use
	     */
	    String sql1 = "SELECT distinct survey, message_sequence FROM survey_user_state order by survey";
	    Statement svyStmt = conn.createStatement();
	    svyStmt.execute(sql1);
	    ResultSet rsSurvey = svyStmt.getResultSet();
	    while (rsSurvey.next()) {
		surveyId = rsSurvey.getString("survey");
		msID = rsSurvey.getString("message_sequence");
		msgSeq = this.studySpace.get_preface().getMessageSequence(msID);
		if (msgSeq == null) {
		    continue;
		}

		outputStr += "\n\nStart checks for survey_id=" + surveyId
			+ ", message sequence id=" + msID;

		/* 1. send the start reminders */
		outputStr += this.advanceReminders("start", msgSeq, surveyId,
			conn);

		/* 2. send the completion reminders */
		outputStr += this.advanceReminders("completion", msgSeq,
			surveyId, conn);
	    }// end of while
	    svyStmt.close();
	    outputStr += ("\nEnd emailing at "
		    + Calendar.getInstance().getTime().toString() + "\n");
	} catch (SQLException e) {
	    outputStr += ("\nReminder generation ERROR! w/ select sql ("
		    + selectSql + "): " + e.toString());
	    e.printStackTrace();
	} finally {
	    try {
		conn.close();
	    } catch (SQLException e) {
	    }
	}
	return outputStr;
    }

    /**
     * Finds all the invitees who are due to get a reminder either start or
     * completion and sends them the message.
     * 
     * @param reminderType
     *            Weather it is a start or completion reminder
     * @param msgSeq
     *            Message sequence form which reminders are obtained and sent to
     *            the users.
     * @param surveyId
     *            Survey whose emails are to be sent.
     * @param conn
     *            Data base connection.
     * @return String Output string.
     */
    private String advanceReminders(String reminderType,
	    MessageSequence msgSeq, String surveyId, Connection conn) {
	Reminder remMsg;
	int remCount;
	String selectSql = "", updateSql = "", outputStr = "", entryState, lastState;
	MessageSender sender = new MessageSender(msgSeq); // sets up
							  // properly-authenticated
							  // mail session
	if (reminderType.equals("start")) {
	    remMsg = msgSeq.getStartReminder(0);
	    entryState = "invited";
	    lastState = "non_responder";
	    remCount = msgSeq.totalStartReminders();
	} else {
	    remMsg = msgSeq.getCompletionReminder(0);
	    entryState = "interrupted";
	    lastState = "incompleter";
	    remCount = msgSeq.totalCompletionReminders();
	}
	if (remMsg == null) {
	    return "No " + reminderType + " reminders\n";
	}
	int maxCount = 1; // max in 1st entry state is 1
	int entryTrigDays = remMsg.triggerDays;

	for (int i = 0; i < remCount; i++) {
	    int n = i + 1;

	    /*
	     * i represents 0-based index for current reminder; n represents the
	     * number that administrators see
	     */
	    outputStr += "\nChecking for those needing a new " + reminderType
		    + "_reminder " + n + " from entry state " + entryState;
	    selectSql = "SELECT id, AES_DECRYPT(email,\""
		    + this.emailEncryptionKey
		    + "\") as email, salutation, firstname, lastname "
		    + "FROM invitee, survey_user_state WHERE survey='"
		    + surveyId + "' AND state='" + entryState + "' "
		    + " AND entry_time <= date_sub(now(), interval "
		    + entryTrigDays + " day) " + " AND state_count >= "
		    + maxCount + " AND id=invitee AND message_sequence='"
		    + msgSeq.id + "'";
	    updateSql = "UPDATE survey_user_state SET state='" + reminderType
		    + "_reminder_" + n + "', state_count=1 WHERE survey='"
		    + surveyId + "' AND invitee=";
	    outputStr += this.sendReminders(surveyId, sender, remMsg,
		    selectSql, updateSql, conn);

	    outputStr += ("\nChecking for those needing another "
		    + reminderType + " reminder " + n);

	    /* Select users NOT at max */
	    selectSql = "SELECT id, AES_DECRYPT(email,\""
		    + this.emailEncryptionKey
		    + "\") as email, salutation, firstname, lastname "
		    + "FROM invitee, survey_user_state WHERE state='"
		    + reminderType + "_reminder_" + n + "' AND survey='"
		    + surveyId + "'"
		    + " AND entry_time <= date_sub(now(), interval "
		    + remMsg.triggerDays + " day)" + " AND state_count < "
		    + remMsg.maxCount
		    + " AND id=invitee AND message_sequence='" + msgSeq.id
		    + "'";
	    updateSql = "UPDATE survey_user_state SET state_count=state_count+1 "
		    + "WHERE survey='" + surveyId + "' AND invitee=";
	    outputStr += this.sendReminders(surveyId, sender, remMsg,
		    selectSql, updateSql, conn);
	    entryState = reminderType + "_reminder_" + n;
	    entryTrigDays = remMsg.triggerDays;
	    if (n < remCount) // need to keep last for final tag-out, below
	    {
		if (reminderType.equals("start")) {
		    remMsg = msgSeq.getStartReminder(n);
		} else {
		    remMsg = msgSeq.getCompletionReminder(n);
		}
	    }

	    // selectSql = "SELECT id, email, salutation, firstname, lastname "
	    // +
	    // "FROM invitee, survey_user_state WHERE state='"+reminderType+"_reminder_"+i+"' "
	    // +
	    // "AND survey='"+survey_id+"' AND state_count >= "+priorMsg.max_count+" "
	    // + "AND entry_time <= date_sub(now(), interval "+
	    // priorMsg.trigger_days + " day) " +
	    // "AND id=invitee AND message_sequence='"+msg_seq.id+"'";
	    // updateSql =
	    // "UPDATE survey_user_state SET state='"+reminderType+"_reminder_"
	    // +(i+1)+ "', state_count=1 "
	    // + "WHERE survey='"+survey_id+"' AND invitee=";
	    // outputStr+=send_reminders(survey_id, fromStr, remMsg, selectSql,
	    // updateSql, conn);

	}

	/* Move users at max of last reminder to to final state */
	selectSql = "UPDATE survey_user_state SET state='" + lastState
		+ "', state_count=1 " + "WHERE state='"
		+ reminderType
		+ "_reminder_"
		+ remCount
		+ "' " // same as entryState
		+ "AND state_count = " + remMsg.maxCount + " "
		+ "AND entry_time <= date_sub(now(), interval "
		+ remMsg.triggerDays + " day) " + "AND survey='" + surveyId
		+ "' AND message_sequence='" + msgSeq.id + "'";

	/* (No message to send; run UPDATE on all at once) */
	try {
	    Statement statement = conn.createStatement();
	    statement.execute(selectSql);
	    statement.close();
	} catch (SQLException e) {
	    outputStr += ("\nadvanceReminder ERROR! w/ select sql ("
		    + selectSql + "): " + e.toString());
	}
	return outputStr;
    }

    /**
     * This function will read and update the pending table, sending messages
     * that are due.
     * 
     * @return String Output string.
     */
    private String invitePendingUsers() {

	String sql = "", outputStr = "";
	String selectSql = "SELECT id, AES_DECRYPT(email,'"
		+ this.studySpace.db.emailEncryptionKey
		+ "') as email, salutation, firstname, lastname, survey, message_sequence FROM invitee, pending "
		+ "WHERE DATE(send_time) <= DATE(now()) AND pending.completed = 'N' AND invitee.id = pending.invitee";
	Statement statement = null;
	Connection conn = null;

	try {
	    conn = this.getDBConnection();
	    statement = conn.createStatement();
	    statement.execute(selectSql);
	    ResultSet rs = statement.getResultSet();
	    while (rs.next()) {
		String inviteeId = rs.getString("id");
		String email = rs.getString("email");
		String salutation = rs.getString("salutation");
		String lastname = rs.getString("lastname");
		String surveyId = rs.getString("survey");
		String msID = rs.getString("message_sequence");
		MessageSequence msgSeq = this.studySpace.get_preface()
			.getMessageSequence(msID);
		MessageSender messageSender = new MessageSender(msgSeq);
		Message invMsg = msgSeq.getTypeMessage("invite");
		if (invMsg == null) {
		    LOGGER.error("Failed to get the initial invitation", null);
		    return "Failed";
		}

		outputStr += ("Sending invitation to invitee = " + inviteeId);
		Statement statement2 = conn.createStatement();
		String messageId = org.apache.commons.lang3.RandomStringUtils
			.randomAlphanumeric(22);

		sql = "INSERT INTO survey_message_use(messageId,invitee, survey, message) VALUES ('"
			+ messageId
			+ "',"
			+ inviteeId
			+ ",'"
			+ surveyId
			+ "', '" + invMsg.id + "')";
		statement2.execute(sql);
		String msgIndex = "";
		if (invMsg.hasLink) {
		    msgIndex = messageId;
		}
		String emailResponse = messageSender.sendMessage(invMsg,
			msgIndex, email, salutation, lastname,
			this.studySpace.id, this, inviteeId);
		if (emailResponse.equalsIgnoreCase("")) {
		    outputStr += (" --> Email Sent");
		    // TODO: I have Fixed insertion of message_sequence in the
		    // reminder code, because it was not inserting any
		    // message_sequence in the survey_user_state table.
		    String updateSql = "INSERT INTO survey_user_state(invitee, survey, message_sequence, state, state_count) "
			    + "values ("
			    + inviteeId
			    + ", '"
			    + surveyId
			    + "', '"
			    + msID
			    + "','invited', 1) ON DUPLICATE KEY UPDATE state='invited', state_count=1";
		    statement2.execute(updateSql);
		    // update the pending table
		    String sql3 = "update pending set completed='Y', completed_time = now() where invitee="
			    + inviteeId
			    + " and survey ='"
			    + surveyId
			    + "' and message_sequence ='" + msID + "'";
		    statement2.execute(sql3);
		} else {
		    outputStr += (" --> ERROR SENDING EMAIL (" + emailResponse + ")");
		    LOGGER.error("Error sending invitation email to invitee = "
			    + inviteeId, null);
		}// if
	    }// while
	} catch (SQLException e) {
	    LOGGER.error("Pending initial invite ERROR:", e);
	} finally {
	    if (conn != null) {
		try {
		    conn.close();
		} catch (SQLException e) {
		}
	    }
	    if (statement != null) {
		try {
		    statement.close();
		} catch (SQLException e) {
		}
	    }
	}
	return outputStr;
    }

    /**
     * Finds all the invitees who are due to get a reminder either start or
     * completion and sends them the message.
     * 
     * @param surveyId
     *            Survey whose emails are to be sent.
     * @param messageSender
     *            Message sender object used for sending Emails.
     * @param r
     *            Message object which contains actual email to send.
     * @param selQry
     *            Query to select to the people who needs email to be sent.
     * @param updQry
     *            Query to update the information of the email sent into the
     *            table
     * @param conn
     *            Data base connection.
     * @return String Output string.
     */
    private String sendReminders(String surveyId, MessageSender messageSender,
	    Message r, String selQry, String updQry, Connection conn) {
	String sql = "", outputStr = "";
	try {
	    Statement statement = conn.createStatement();
	    statement.execute(selQry);
	    ResultSet rs = statement.getResultSet();
	    while (rs.next()) {
		String iid = rs.getString("id");
		String email = rs.getString("email");
		String salutation = rs.getString("salutation");
		String lastname = rs.getString("lastname");
		outputStr += ("\nSending reminder invitation to invitee = " + iid);
		Statement statement2 = conn.createStatement();
		String messageId = org.apache.commons.lang3.RandomStringUtils
			.randomAlphanumeric(22);
		sql = "INSERT INTO survey_message_use(messageId,invitee, survey, message) VALUES ('"
			+ messageId
			+ "',"
			+ iid
			+ ",'"
			+ surveyId
			+ "', '"
			+ r.id + "')";
		statement2.execute(sql);
		String msgIndex = "";
		if (r.hasLink) {
		    msgIndex = messageId;
		}
		/*
		 * args: send_message(Message msg, String from_str, String
		 * message_useID, String toEmail, String salutation, String
		 * lastname, String ssid)
		 */
		String emailResponse = messageSender.sendMessage(r, msgIndex,
			email, salutation, lastname, this.studySpace.id, this,
			iid);
		if (emailResponse.equalsIgnoreCase("")) {
		    outputStr += (" --> Email Sent");
		    statement2.execute(updQry + iid);
		} else {
		    outputStr += (" --> ERROR SENDING EMAIL (" + emailResponse + ")");
		    LOGGER.error("Error sending invitation email to invitee = "
			    + iid, null);
		}
	    }// while
	} catch (SQLException e) {
	    e.printStackTrace();
	    LOGGER.error("Reminder sending ERROR: " + e.toString(), null);
	}
	return outputStr;
    }

    /**
     * Gets information about a question from the survey data table.
     * 
     * @param surveyId
     *            Survey Id for which results are to be obtained.
     * @param pgName
     *            Page name which contains this question.
     * @param itemName
     *            Question name that needs results.
     * @param whereclause
     *            Selection of the invitees.
     * @return Hashtable Results of the item.
     */
    public Hashtable<String, Integer> getDataForItem(String surveyId,
	    String pgName, String itemName, String whereclause) {
	Hashtable<String, Integer> h1 = new Hashtable<String, Integer>();

	/* connect to the database */
	Connection conn = null;
	Statement stmt = null;
	try {
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();
	    // count the total number of invitees for each distinct answer;
	    // join to page_submit prevents counting nulls from people who never
	    // submitted the item
	    String sql = "select " + itemName
		    + ", count(distinct s.invitee) from " + surveyId
		    + MainTableExtension + " as s, page_submit as p where "
		    + "p.invitee=s.invitee and p.survey='" + surveyId + "'"
		    + " and p.page='" + pgName + "'";
	    if (!whereclause.equals("")) {
		sql += " and s." + whereclause;
	    }
	    sql += " group by " + itemName;
	    stmt.execute(sql);
	    ResultSet rs = stmt.getResultSet();
	    while (rs.next()) {

		/* if the answer is null */
		if (rs.getString(1) == null) {
		    h1.put("null", new Integer(rs.getInt(2)));
		} else {
		    h1.put(rs.getString(1), new Integer(rs.getInt(2)));
		}
	    }
	    rs.close();
	    stmt.close();
	    conn.close();
	} catch (Exception e) {
	    LOGGER.error("WISE - CLOSED QUESTION RENDER RESULTS EXCLUSIVE: "
		    + e.toString(), e);
	}
	return h1;
    }

    /**
     * Returns the maximum and minimum for an item in the survey data.
     * 
     * @param page
     *            Page name which contains this item.
     * @param itemName
     *            Question whose max and min values are asked for.
     * @param whereclause
     *            The invitees from whom these values are to be selected.
     * @return HashMap Result
     */
    public HashMap<String, Float> getMinMaxForItem(Page page, String itemName,
	    String whereclause) {

	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;
	String sql = null;
	HashMap<String, Float> retMap = new HashMap<String, Float>();
	try {
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();
	    sql = "select "
		    + "min("
		    + itemName
		    + "), max("
		    + itemName
		    + ") from "
		    + page.survey.getId()
		    + "_data as s, page_submit as p where s.invitee=p.invitee and p.survey='"
		    + page.survey.getId() + "' and p.page='" + page.id + "'";
	    if (!whereclause.equalsIgnoreCase("")) {
		sql += " and s." + whereclause;
	    }

	    stmt.execute(sql);
	    rs = stmt.getResultSet();
	    if (rs.next()) {
		retMap.put("min", rs.getFloat(1));
		retMap.put("max", rs.getFloat(2));
	    }
	} catch (SQLException ex) {
	    LOGGER.error("SQL Query Error", ex);
	    return retMap;
	} finally {
	    if (conn != null) {
		try {
		    conn.close();
		} catch (SQLException e) {
		    LOGGER.error(e);
		}
	    }
	    if (stmt != null) {
		try {
		    stmt.close();
		} catch (SQLException e) {
		    LOGGER.error(e);
		}
	    }
	    try {
		rs.close();
	    } catch (SQLException e) {
		LOGGER.error(e);
	    }
	}
	return retMap;
    }

    /**
     * Generates a histogram of a particular question for all the response set
     * for that Item.
     * 
     * @param page
     *            Page name which contains this item.
     * @param itemName
     *            Question whose max and min values are asked for.
     * @param scaleStart
     *            Start scale of histogram.
     * @param binWidthFinal
     *            Width of the histogram.
     * @param whereclause
     *            The invitees from whom these values are to be selected.
     * @return HashMap Result of the histogram
     */
    public HashMap<String, String> getHistogramForItem(Page page,
	    String itemName, float scaleStart, float binWidthFinal,
	    String whereclause) {
	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;
	String sql = null;
	HashMap<String, String> retMap = new HashMap<String, String>();
	try {
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();

	    /* get bins on that question from database */
	    sql = "select floor(("
		    + itemName
		    + "-"
		    + scaleStart
		    + ")/"
		    + binWidthFinal
		    + "), count(*) from "
		    + page.survey.getId()
		    + "_data as s, page_submit as p where s.invitee=p.invitee and p.survey='"
		    + page.survey.getId() + "' and p.page='" + page.id + "'";
	    if (!whereclause.equalsIgnoreCase("")) {
		sql += " and s." + whereclause;
	    }
	    sql += " group by floor((" + itemName + "-" + scaleStart + ")/"
		    + binWidthFinal + ")";
	    stmt.execute(sql);
	    rs = stmt.getResultSet();
	    String name, count;
	    while (rs.next()) {
		name = rs.getString(1);
		if (name == null) {
		    name = "null";
		}
		count = rs.getString(2);
		if (count == null) {
		    count = "null";
		    retMap.put("unanswered", String.valueOf(1));
		}
		retMap.put(name, count);
	    }

	} catch (SQLException ex) {
	    LOGGER.error("SQL Query Error", ex);
	    return retMap;
	} finally {
	    if (conn != null) {
		try {
		    conn.close();
		} catch (SQLException e) {
		    LOGGER.error(e);
		}
	    }
	    if (stmt != null) {
		try {
		    stmt.close();
		} catch (SQLException e) {
		    LOGGER.error(e);
		}
	    }
	    try {
		rs.close();
	    } catch (SQLException e) {
		LOGGER.error(e);
	    }
	}
	return retMap;
    }

    /**
     * Updates the status of the survey system into data base.
     * 
     * @param surveyName
     *            Name of the survey whose status is updated.
     */
    public void updateSurveyHealthStatus(String surveyName) {
	long currentTimeMillis = System.currentTimeMillis();
	StringBuffer query = new StringBuffer(
		"insert into survey_health (survey_name, last_update_time) values ('");
	query.append(surveyName).append("',").append(currentTimeMillis)
		.append(")");
	query.append(" on duplicate key update last_update_time=")
		.append(currentTimeMillis).append(";");
	Connection conn = null;
	Statement stmt = null;

	try {
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();
	    stmt.executeUpdate(query.toString());
	} catch (SQLException e) {
	    LOGGER.error("Could not update survey_health table", e);
	} finally {
	    if (stmt != null) {
		try {
		    stmt.close();
		} catch (SQLException e) {
		    LOGGER.error(e);
		}
	    }
	    if (conn != null) {
		try {
		    conn.close();
		} catch (SQLException e) {
		    LOGGER.equals(e);
		}
	    }
	}

    }

    /**
     * Returns the time when the status of the survey system has been updated in
     * the database in milli seconds.
     * 
     * @param studyName
     *            Name of the whose value is needed.
     * @return long Time in milli seconds.
     */
    public long lastSurveyHealthUpdateTime(String studyName) {

	long lastUpdateTime = 0;

	Connection conn = null;
	PreparedStatement stmt = null;
	String sqlQuery = "select * from survey_health where survey_name= ? ";
	try {
	    conn = this.getDBConnection();
	    stmt = conn.prepareStatement(sqlQuery);
	    stmt.setString(1, studyName);
	    ResultSet rs = stmt.executeQuery();
	    if (rs.next()) {
		lastUpdateTime = rs.getLong(2);
	    }

	} catch (SQLException e) {
	    LOGGER.error("Could not update survey_health table", e);
	} finally {
	    if (stmt != null) {
		try {
		    stmt.close();
		} catch (SQLException e) {
		    LOGGER.error(e);
		}
	    }
	    if (conn != null) {
		try {
		    conn.close();
		} catch (SQLException e) {
		    LOGGER.equals(e);
		}
	    }
	}
	return lastUpdateTime;

    }

    /**
     * Returns the HTML format of the fields in invitees. This is used by admin
     * page to add new invitee information
     * 
     * @return String HTML format of the invitee fields.
     */
    public String displayAddInvitee() {
	StringBuffer strBuff = new StringBuffer();
	Connection conn = null;
	Statement stmt = null;
	try {

	    /* connect to the database */
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();
	    stmt.execute("describe invitee");
	    ResultSet rs = stmt.getResultSet();
	    while (rs.next()) {

		/*
		 * read col name from database, matching col value from input
		 * request
		 */
		String columnName = rs.getString("Field");
		if (columnName.equalsIgnoreCase("id")) {
		    continue;
		}
		strBuff.append("<tr><td width=400 align=left>").append(
			columnName);

		/* check for required field values */
		if (columnName.equalsIgnoreCase("lastname")) {
		    strBuff.append(" (required)");
		}
		strBuff.append(": <input type='text' name='")
			.append(columnName).append("' ");
		if (columnName.equalsIgnoreCase("salutation")) {
		    strBuff.append("maxlength=5 size=5 ");
		} else {
		    strBuff.append("maxlength=64 size=40 ");
		}
		strBuff.append("></td></tr>");
	    }

	    /* display the submit button */
	    strBuff.append("<tr><td align=center>")
		    .append("<input type='image' alt='submit' src='admin_images/submit.gif' border=0>")
		    .append("</td></tr>");

	} catch (SQLException e) {
	    e.printStackTrace();
	} finally {
	    if (conn != null) {
		try {
		    conn.close();
		} catch (SQLException e) {
		}
	    }
	    if (stmt != null) {
		try {
		    stmt.close();
		} catch (SQLException e) {
		}
	    }
	}
	return strBuff.toString();
    }

    /**
     * Returns the HTML format of the fields in invitees for a particular
     * survey. This is used by admin page to add new invitee information
     * 
     * @param surveyId
     *            Name of the survey whose invitee fields are to be displayed.
     * @return String HTML format of the invitee fields.
     */
    public String displayAddInvitee(String surveyId) {

	Survey survey = this.studySpace.getSurvey(surveyId);
	Map<String, Values> inviteeMap = new HashMap<String, Values>(
		survey.getInviteeMetadata().fieldMap);
	StringBuffer strBuff = new StringBuffer();

	for (INVITEE_FIELDS field : INVITEE_FIELDS.values()) {
	    if (!field.isShouldDisplay()) {
		continue;
	    }
	    strBuff.append(this.getInviteeEntry(field.name(),
		    inviteeMap.get(field.name())));
	    inviteeMap.remove(field.name());
	}

	for (Map.Entry<String, Values> map : inviteeMap.entrySet()) {
	    strBuff.append(this.getInviteeEntry(map.getKey(), map.getValue()));
	}

	/* display the submit button */
	strBuff.append("<tr><td align=center>")
		.append("<input type='image' alt='submit' src='admin_images/submit.gif' onmousedown='submit_inv();return true;' border=0>")
		.append("</td></tr>");
	return strBuff.toString();
    }

    /**
     * Helper function to display invitee fields of a particular study space.
     * 
     * @param columnName
     *            Column name which has to be displayed.
     * @param value
     *            Value in case the field is a drop down.
     * @return String HTML format of the string.
     */
    private String getInviteeEntry(String columnName, Values value) {
	StringBuffer strBuff = new StringBuffer();
	strBuff.append("<tr><td width=450 align=left>").append(value.label);

	/* not a drop down */
	if (value.values.size() == 0) {
	    strBuff.append(": <input type='text' name='").append(columnName)
		    .append("' ");
	    if (columnName.equalsIgnoreCase(INVITEE_FIELDS.salutation.name())) {
		strBuff.append("maxlength=5 size=5 ");
	    } else {
		strBuff.append("maxlength=64 size=40 ");
	    }
	    strBuff.append("></td></tr>");
	} else {
	    strBuff.append(": <select name='").append(columnName).append("'>");
	    for (Map.Entry<String, String> valueNode : value.values.entrySet()) {
		strBuff.append("<option value='").append(valueNode.getKey())
			.append("'>");
		strBuff.append(valueNode.getValue() == null ? valueNode
			.getKey() : valueNode.getValue());
		strBuff.append("</option>");
	    }
	    strBuff.append("</select>");
	}
	return strBuff.toString();
    }

    /**
     * Adds a new invitee and then displays the page.
     * 
     * @param requestParameters
     *            parameters need for adding a new invitee into the tables.
     * @return String HTML format of the further Page.
     */
    public String addInviteeAndDisplayPage(Map<String, String> requestParameters) {
	return this.handleAddInviteeAndDisplayPage(requestParameters);
    }

    private String handleAddInviteeAndDisplayPage(
	    Map<String, String> requestParameters) {
	String errStr = "", resStr = "";
	/* connect to the database */
	Connection conn = null;
	Statement stmt = null;
	try {
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();
	    String sql, sqlIns = "insert into invitee(", sqlVal = "values(";

	    /* get the column names of the table of invitee */
	    stmt.execute("describe invitee");
	    ResultSet rs = stmt.getResultSet();

	    boolean submit = (requestParameters.get("submit") != null);
	    while (rs.next()) {

		/*
		 * read col name from database, matching col value from input
		 * request
		 */
		String columnName = rs.getString("Field");
		if (columnName.equalsIgnoreCase("id")) {
		    continue;
		}
		String columnVal = requestParameters.get(columnName);
		if ((columnVal != null) && !columnVal.equalsIgnoreCase("null")
			&& (columnVal.indexOf("\'") != -1)) {
		    columnVal = columnVal.replace("'", "\\'");
		}

		String columnType = rs.getString("Type");
		resStr += "<tr><td width=400 align=left>" + columnName;

		/* check for required field values */
		if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.lastname
			.name())
			|| (columnName
				.equalsIgnoreCase(User.INVITEE_FIELDS.email
					.name()))) {
		    resStr += " (required)";
		    if (submit && Strings.isNullOrEmpty(columnVal)) {
			errStr += "<b>" + columnName + "</b> ";
		    }
		}
		resStr += ": <input type='text' name='" + columnName + "' ";
		if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.salutation
			.name())) {
		    resStr += "maxlength=5 size=5 ";
		} else {
		    resStr += "maxlength=64 size=40 ";
		}
		if (submit) {

		    /*
		     * tempValue=column_val; if(tempValue.indexOf("\'")!=-1){
		     * tempValue=tempValue.replace("'", "&#x27"); }
		     */

		    resStr += "value='" + columnVal + "'"; // add submitted
		    sqlIns += columnName + ",";

		    if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.email
			    .name())) {
			if (Strings.isNullOrEmpty(columnVal)
				|| columnVal.equalsIgnoreCase("null")) {
			    columnVal = WISEApplication.wiseProperties
				    .getAlertEmail();
			}
			sqlVal += "AES_ENCRYPT('" + columnVal + "','"
				+ this.emailEncryptionKey + "'),";
		    } else if (columnName
			    .equalsIgnoreCase(User.INVITEE_FIELDS.irb_id.name())) {
			sqlVal += "\""
				+ (Strings.isNullOrEmpty(columnVal) ? ""
					: columnVal) + "\",";
		    } else if (columnName
			    .equalsIgnoreCase(User.INVITEE_FIELDS.salutation
				    .name())) {
			sqlVal += "\""
				+ (Strings.isNullOrEmpty(columnVal) ? "Mr."
					: columnVal) + "\",";
		    } else if (columnName
			    .equalsIgnoreCase(User.INVITEE_FIELDS.firstname
				    .name())) {
			sqlVal += "\""
				+ (Strings.isNullOrEmpty(columnVal) ? ""
					: columnVal) + "\",";
		    } else if (columnName
			    .equalsIgnoreCase(User.INVITEE_FIELDS.lastname
				    .name())) {
			sqlVal += "\""
				+ (Strings.isNullOrEmpty(columnVal) ? ""
					: columnVal) + "\",";
		    } else if (columnName
			    .equalsIgnoreCase(User.INVITEE_FIELDS.phone.name())) {
			sqlVal += "\""
				+ (Strings.isNullOrEmpty(columnVal) ? "0"
					: columnVal) + "\",";
		    } else if (columnName.equalsIgnoreCase("subj_type")) {
			sqlVal += "\""
				+ (Strings.isNullOrEmpty(columnVal) ? "1"
					: columnVal) + "\",";
		    } else {
			/* code added by Vijay */
			String temp = "";
			if (columnType.toLowerCase().contains("int")) {
			    temp = "\""
				    + (Strings.isNullOrEmpty(columnVal) ? "0"
					    : columnVal) + "\"";
			} else if (columnType.toLowerCase().contains("date")) {
			    temp = "\""
				    + (Strings.isNullOrEmpty(columnVal) ? "2012-12-31"
					    : columnVal) + "\"";
			} else {
			    temp = "\""
				    + (Strings.isNullOrEmpty(columnVal) ? ""
					    : columnVal) + "\"";
			}
			sqlVal += "AES_ENCRYPT('" + temp + "','"
				+ this.emailEncryptionKey + "'),";
			;

		    }
		}

		resStr += "></td></tr>";
	    }

	    /*
	     * run the insertion if all the required fields have been filled in
	     * with values
	     */
	    if (!errStr.equals("")) {
		resStr += "<tr><td align=center>Required fields " + errStr
			+ " not filled out </td></tr>";
	    } else if (submit) {
		sql = sqlIns.substring(0, sqlIns.length() - 1) + ") "
			+ sqlVal.substring(0, sqlVal.length() - 1) + ")";
		LOGGER.info("The sql trying to execute is " + sql);
		stmt.execute(sql);
		resStr += "<tr><td align=center>New invitee "
			+ requestParameters.get("last_name")
			+ " has been added</td></tr>";
	    }

	    /* display the submit button */
	    resStr += "<tr><td align=center>"
		    + "<input type='hidden' name='submit' value='true' >"
		    + "<input type='image' alt='submit' src='admin_images/submit.gif' border=0>"
		    + "</td></tr>";
	} catch (RuntimeException e) {
	    LOGGER.error("Runtime exception while loading invitee", e);
	} catch (SQLException e) {
	    LOGGER.error("WISE ADMIN - LOAD INVITEE: " + e.toString(), e);
	    // Security feature fix Exception should not be displayed
	    // resStr += "<p>Error: " + e.toString() + "</p>";
	    resStr += "<p>Error: Problem while entering data into database</p>";
	    return resStr;
	} finally {
	    try {
		stmt.close();
	    } catch (SQLException e) {
	    }
	    try {
		conn.close();
	    } catch (SQLException e) {
	    }
	}
	return resStr;
    }

    /**
     * Adds a new invitee and then returns the id.
     * 
     * @param requestParameters
     *            parameters need for adding a new invitee into the tables.
     * @return int Id of the new invitee added.
     */
    public int addInviteeAndReturnUserId(Map<String, String> requestParameters) {
	return Integer.parseInt(this.handleAddInvitees(requestParameters));
    }

    /**
     * Runs database to handle input and also print table for adding invitees.
     * 
     * @param requestParameters
     *            parameters need for adding a new invitee into the tables.
     * @param showNextPage
     *            Should next page be displayed or not.
     * @return String Returns the id of the new user added or
     */
    private String handleAddInvitees(Map<String, String> requestParameters) {
	String errStr = "", resStr = "";
	int userId = 0;

	/* connect to the database */
	Connection conn = null;
	Statement stmt = null;
	try {
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();
	    String sql, sqlIns = "insert into invitee(", sqlVal = "values(";

	    /* get the column names of the table of invitee */
	    stmt.execute("describe invitee");
	    ResultSet rs = stmt.getResultSet();

	    boolean submit = (requestParameters.get("save") != null);
	    while (rs.next()) {

		/*
		 * read col name from database, matching col value from input
		 * request
		 */
		String columnName = rs.getString("Field");
		if (columnName.equalsIgnoreCase("id")) {
		    continue;
		}
		String columnVal = requestParameters.get(columnName);
		if ((columnVal != null) && !columnVal.equalsIgnoreCase("null")
			&& (columnVal.indexOf("\'") != -1)) {
		    columnVal = columnVal.replace("'", "\\'");
		}

		String columnType = rs.getString("Type");
		resStr += "<tr><td width=400 align=left>" + columnName;

		/* check for required field values */
		if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.lastname
			.name())) {
		    resStr += " (required)";
		    if (submit && Strings.isNullOrEmpty(columnVal)) {
			errStr += "<b>" + columnName + "</b> ";
		    }
		}
		resStr += ": <input type='text' name='" + columnName + "' ";
		if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.salutation
			.name())) {
		    resStr += "maxlength=5 size=5 ";
		} else {
		    resStr += "maxlength=64 size=40 ";
		}
		if (submit) {

		    /*
		     * tempValue=column_val; if(tempValue.indexOf("\'")!=-1){
		     * tempValue=tempValue.replace("'", "&#x27"); }
		     */

		    resStr += "value='" + columnVal + "'"; // add submitted
		    sqlIns += columnName + ",";

		    if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.email
			    .name())) {
			if (Strings.isNullOrEmpty(columnVal)
				|| columnVal.equalsIgnoreCase("null")) {
			    columnVal = WISEApplication.wiseProperties
				    .getAlertEmail();
			}
			sqlVal += "AES_ENCRYPT('" + columnVal + "','"
				+ this.emailEncryptionKey + "'),";
		    } else if (columnName
			    .equalsIgnoreCase(User.INVITEE_FIELDS.irb_id.name())) {
			sqlVal += "\""
				+ (Strings.isNullOrEmpty(columnVal) ? ""
					: columnVal) + "\",";
		    } else if (columnName
			    .equalsIgnoreCase(User.INVITEE_FIELDS.salutation
				    .name())) {
			sqlVal += "\""
				+ (Strings.isNullOrEmpty(columnVal) ? "Mr."
					: columnVal) + "\",";
		    } else if (columnName
			    .equalsIgnoreCase(User.INVITEE_FIELDS.firstname
				    .name())) {
			sqlVal += "\""
				+ (Strings.isNullOrEmpty(columnVal) ? ""
					: columnVal) + "\",";
		    } else if (columnName
			    .equalsIgnoreCase(User.INVITEE_FIELDS.lastname
				    .name())) {
			sqlVal += "\""
				+ (Strings.isNullOrEmpty(columnVal) ? ""
					: columnVal) + "\",";
		    } else if (columnName
			    .equalsIgnoreCase(User.INVITEE_FIELDS.phone.name())) {
			sqlVal += "\""
				+ (Strings.isNullOrEmpty(columnVal) ? "0"
					: columnVal) + "\",";
		    } else if (columnName.equalsIgnoreCase("subj_type")) {
			sqlVal += "\""
				+ (Strings.isNullOrEmpty(columnVal) ? "1"
					: columnVal) + "\",";
		    } else {
			/* code added by Vijay */
			String temp = "";
			if (columnType.toLowerCase().contains("int")) {
			    temp = "\""
				    + (Strings.isNullOrEmpty(columnVal) ? "0"
					    : columnVal) + "\"";
			} else if (columnType.toLowerCase().contains("date")) {
			    temp = "\""
				    + (Strings.isNullOrEmpty(columnVal) ? "2012-12-31"
					    : columnVal) + "\"";
			} else {
			    temp = "\""
				    + (Strings.isNullOrEmpty(columnVal) ? ""
					    : columnVal) + "\"";
			}
			sqlVal += "AES_ENCRYPT('" + temp + "','"
				+ this.emailEncryptionKey + "'),";
			;

		    }
		}

		resStr += "></td></tr>";
	    }

	    /*
	     * run the insertion if all the required fields have been filled in
	     * with values
	     */
	    if (!errStr.equals("")) {
		resStr += "<tr><td align=center>Required fields " + errStr
			+ " not filled out </td></tr>";
	    } else if (submit) {
		sql = sqlIns.substring(0, sqlIns.length() - 1) + ") "
			+ sqlVal.substring(0, sqlVal.length() - 1) + ")";
		LOGGER.info("The sql trying to execute is " + sql);
		stmt.execute(sql);
		resStr += "<tr><td align=center>New invitee "
			+ requestParameters.get("last_name")
			+ " has been added</td></tr>";
	    }

	    /* display the submit button */
	    resStr += "<tr><td align=center>"
		    + "<input type='hidden' name='submit' value='true' >"
		    + "<input type='image' alt='submit' src='admin_images/submit.gif' border=0>"
		    + "</td></tr>";
	    stmt.execute("select last_insert_id()");
	    rs = stmt.getResultSet();
	    if (rs.next()) {
		userId = rs.getInt(1);
	    }

	} catch (RuntimeException e) {
	    LOGGER.error("Runtime exception while loading invitee", e);
	} catch (SQLException e) {
	    LOGGER.error("WISE ADMIN - LOAD INVITEE: " + e.toString(), e);
	    // Security feature fix Exception should not be displayed
	    // resStr += "<p>Error: " + e.toString() + "</p>";
	    resStr += "<p>Error: Problem while entering data into database</p>";
	    return resStr;
	} finally {
	    try {
		stmt.close();
	    } catch (SQLException e) {
	    }
	    try {
		conn.close();
	    } catch (SQLException e) {
	    }
	}
	return String.valueOf(userId);
    }

    /*
     * Pralav public String addInviteeAndDisplayPage(HttpServletRequest request)
     * { return handle_addInvitees(request, true); }
     * 
     * public int addInviteeAndReturnUserId(HttpServletRequest request) { return
     * Integer.parseInt(handle_addInvitees(request, false)); }
     * 
     * /** run database to handle input and also print table for adding invitees
     */
    /*
     * Pralav private String handle_addInvitees(HttpServletRequest request,
     * boolean showNextPage) { String errStr = "", resStr = ""; int userId = 0;
     * // connect to the database Connection conn = null; Statement stmt = null;
     * try { conn = getDBConnection(); stmt = conn.createStatement(); String
     * sql, sql_ins = "insert into invitee(", sql_val = "values(";
     * 
     * // get the column names of the table of invitee
     * stmt.execute("describe invitee"); ResultSet rs = stmt.getResultSet();
     * boolean submit = (request.getParameter("submit") != null); while
     * (rs.next()) { // read col name from database, matching col value from
     * input // request String column_name = rs.getString("Field"); if
     * (column_name.equalsIgnoreCase("id")) continue; String column_val =
     * request.getParameter(column_name); String column_type =
     * rs.getString("Type"); resStr += "<tr><td width=400 align=left>" +
     * column_name; // check for required field values if
     * (column_name.equalsIgnoreCase(User.INVITEE_FIELDS.lastname .name()) ||
     * (showNextPage && column_name .equalsIgnoreCase(User.INVITEE_FIELDS.email
     * .name()))) { resStr += " (required)"; if (submit &&
     * Strings.isNullOrEmpty(column_val)) errStr += "<b>" + column_name +
     * "</b> "; } resStr += ": <input type='text' name='" + column_name + "' ";
     * if (column_name.equalsIgnoreCase(User.INVITEE_FIELDS.salutation .name()))
     * resStr += "maxlength=5 size=5 "; else resStr += "maxlength=64 size=40 ";
     * if (submit) { resStr += "value='" + column_val + "'"; // add submitted
     * sql_ins += column_name + ","; if
     * (column_name.equalsIgnoreCase(User.INVITEE_FIELDS.email .name())) { if
     * (Strings.isNullOrEmpty(column_val) ||
     * column_val.equalsIgnoreCase("null")) { column_val =
     * WISE_Application.alert_email; } sql_val += "AES_ENCRYPT('" + column_val +
     * "','" + email_encryption_key + "'),"; } else if (column_name
     * .equalsIgnoreCase(User.INVITEE_FIELDS.irb_id.name())) { sql_val += "\"" +
     * (Strings.isNullOrEmpty(column_val) ? "" : column_val) + "\","; } else if
     * (column_name .equalsIgnoreCase(User.INVITEE_FIELDS.salutation .name())) {
     * sql_val += "\"" + (Strings.isNullOrEmpty(column_val) ? "Mr." :
     * column_val) + "\","; } else { if
     * (column_type.toLowerCase().contains("int")) { sql_val += "\"" +
     * (Strings.isNullOrEmpty(column_val) ? "0" : column_val) + "\","; } else {
     * sql_val += "\"" + (Strings.isNullOrEmpty(column_val) ? "" : column_val) +
     * "\","; } } } resStr += "></td></tr>"; } // run the insertion if all the
     * required fields have been filled in // with values if
     * (!errStr.equals("")) resStr += "<tr><td align=center>Required fields " +
     * errStr + " not filled out </td></tr>"; else if (submit) { sql =
     * sql_ins.substring(0, sql_ins.length() - 1) + ") " + sql_val.substring(0,
     * sql_val.length() - 1) + ")"; stmt.execute(sql); resStr +=
     * "<tr><td align=center>New invitee " + request.getParameter("lastname") +
     * " has been added</td></tr>"; } // display the submit button resStr +=
     * "<tr><td align=center>" +
     * "<input type='hidden' name='submit' value='true' >" +
     * "<input type='image' alt='submit' src='admin_images/submit.gif' border=0>"
     * + "</td></tr>"; if (!showNextPage) {
     * stmt.execute("select last_insert_id()"); rs = stmt.getResultSet(); if
     * (rs.next()) { userId = rs.getInt(1); } }
     * 
     * } catch (Exception e) { AdminInfo
     * .log_error("WISE ADMIN - LOAD INVITEE: " + e.toString(), e); resStr +=
     * "<p>Error: " + e.toString() + "</p>"; return resStr; } finally { try {
     * stmt.close(); } catch (SQLException e) { } try { conn.close(); } catch
     * (SQLException e) { } } return showNextPage ? resStr :
     * String.valueOf(userId); }
     */
    /**
     * Returns the ID of the current survey.
     * 
     * @return String Name of the current survey.
     */
    public String getCurrentSurveyIdString() {

	Connection conn = null;
	PreparedStatement stmt = null;
	String surveyIdString = null;
	String sql = "select id from surveys where status in ('P', 'D') and internal_id in"
		+ "(select max(internal_id) from surveys group by id) order by uploaded DESC";

	try {
	    conn = this.getDBConnection();
	    stmt = conn.prepareStatement(sql);
	    ResultSet rs = stmt.executeQuery();
	    if (rs.next()) {
		surveyIdString = rs.getString(1);
	    }
	} catch (SQLException ex) {

	} finally {
	    try {
		stmt.close();
	    } catch (SQLException e) {
	    }
	    try {
		conn.close();
	    } catch (SQLException e) {
	    }
	}
	return surveyIdString;
    }

    // TODO: (low) continue moving functionality from User and Study_Space to
    // Data_Bank

    /**
     * If a new survey is uploaded and there is changes the invitee_fields, this
     * function will make sure that database and survey fields remain in sync.
     * This function ensures that it does not touches the default mandatory
     * arguments in the invitee table, even if they are tampered in the survey
     * XML.
     * 
     * @param inviteeMetadata
     *            Object which contains the metadata of the invitees populated
     *            from the survey table.
     */
    public void syncInviteeTable(InviteeMetadata inviteeMetadata) {
	Connection conn = null;
	Statement stmt = null;
	Set<String> dbColumns = new HashSet<String>();
	Set<String> columnsToBeRemoved = new HashSet<String>();
	Set<String> columnsToBeAdded = new HashSet<String>();
	try {
	    conn = this.getDBConnection();
	    stmt = conn.createStatement();
	    stmt.execute("describe invitee");
	    ResultSet rs = stmt.getResultSet();
	    while (rs.next()) {
		dbColumns.add(rs.getString("Field"));
	    }
	    for (Map.Entry<String, Values> map : inviteeMetadata.fieldMap
		    .entrySet()) {
		String columnName = map.getKey();
		Values columnValue = map.getValue();
		if (!columnValue.userNode || dbColumns.contains(columnName)) {
		    continue;
		}
		columnsToBeAdded.add(columnName);
	    }
	    Iterator<String> it = dbColumns.iterator();
	    while (it.hasNext()) {
		String columnName = it.next();
		Values columnValue = inviteeMetadata.fieldMap.get(columnName);

		/* present in the DB, but not in the Xml file. */
		if (columnValue == null) {
		    if (INVITEE_FIELDS.contains(columnName)) {
			continue;
		    }
		    columnsToBeRemoved.add(columnName);
		}
	    }

	    it = columnsToBeAdded.iterator();
	    while (it.hasNext()) {
		String columnName = it.next();
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("alter table invitee add column ")
			.append(columnName)
			.append("")
			.append(inviteeMetadata.fieldMap.get(columnName).type
				.substring(0, inviteeMetadata.fieldMap
					.get(columnName).type.length() - 1));
		LOGGER.info("@@@@@@ Columns being added are : "
			+ strBuff.toString());
		stmt.execute(strBuff.toString());
	    }

	    it = columnsToBeRemoved.iterator();
	    while (it.hasNext()) {
		String columnName = it.next();
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("alter table invitee drop column ").append(
			columnName);
		LOGGER.info("@@@@@@ Columns being removed are : "
			+ strBuff.toString());
		stmt.execute(strBuff.toString());
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	} finally {
	    if (conn != null) {
		try {
		    conn.close();
		} catch (SQLException e) {
		}
	    }
	    if (stmt != null) {
		try {
		    stmt.close();
		} catch (SQLException e) {
		}
	    }
	}
    }

    /*
     * public InputStream getFileFromDatabase(String cssFileName) { Connection
     * conn = null; PreparedStatement pstmnt = null; InputStream is = null;
     * 
     * try { conn = getDBConnection(); String querySQL =
     * "SELECT filecontents FROM wisefiles WHERE filename = '" + cssFileName +
     * "'"; pstmnt = conn.prepareStatement(querySQL); ResultSet rs =
     * pstmnt.executeQuery();
     * 
     * while (rs.next()) { is = rs.getBinaryStream(1); } } catch (SQLException
     * e) { e.printStackTrace();
     * LOGGER.error("Error while retrieving file from database"); } catch
     * (Exception e) { e.printStackTrace(); } finally { try { conn.close(); }
     * catch (SQLException e) { e.printStackTrace(); } } return is; }
     */

    /**
     * Loads file from the data base.
     * 
     * @param fileName
     *            File name that is to be loaded from the database.
     * @param studySpaceName
     *            StudySpace from the file has to be loaded.
     * @return InputStream The file read form the data base is sent as the
     *         stream to the caller functions.
     */
    public InputStream getFileFromDatabase(String fileName,
	    String studySpaceName) {
	Connection conn = null;
	PreparedStatement pstmnt = null;
	InputStream is = null;

	try {
	    conn = this.getDBConnection();
	    String querySQL = "SELECT filecontents FROM " + studySpaceName
		    + ".wisefiles WHERE filename = '" + fileName + "'";
	    pstmnt = conn.prepareStatement(querySQL);
	    ResultSet rs = pstmnt.executeQuery();

	    while (rs.next()) {
		is = rs.getBinaryStream(1);
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	    LOGGER.error("Error while retrieving file from database");
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    try {
		conn.close();
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	}
	return is;
    }

    /**
     * Loads file from the data base. These are the survey xml files.
     * 
     * @param fileName
     *            File name that is to be loaded from the database.
     * @param studySpaceName
     *            StudySpace from the file has to be loaded.
     * @return InputStream The file read form the data base is sent as the
     *         stream to the caller functions.
     */
    public InputStream getXmlFileFromDatabase(String fileName,
	    String studySpaceName) {
	Connection connection = null;
	PreparedStatement prepStmt = null;
	InputStream inputStream = null;

	if (Strings.isNullOrEmpty(studySpaceName)) {
	    LOGGER.error("No study space name  provided");
	    return null;
	}

	try {
	    connection = this.getDBConnection();
	    String querySQL = "SELECT filecontents FROM " + studySpaceName
		    + ".xmlfiles WHERE filename='" + fileName + "'";
	    prepStmt = connection.prepareStatement(querySQL);
	    ResultSet resultSet = prepStmt.executeQuery();
	    while (resultSet.next()) {
		inputStream = resultSet.getBinaryStream(1);
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	    LOGGER.error("Error while retrieving file from database");
	}
	return inputStream;
    }

    /**
     * This Method is used to replace the patterns in the email body.
     * 
     * @param msg
     *            Message whose content has to be matched and replaced.
     * @param inviteeId
     *            Invitee for whom the message is sent as email.
     * @return String The modified Message.
     */
    @SuppressWarnings("rawtypes")
    public String replacePattern(String msg, String inviteeId) {
	if (msg != null) {
	    Connection connect = null;
	    Statement stmt1 = null;
	    Statement stmt2 = null;
	    String fieldName = "";
	    String actualValue = "";
	    try {
		connect = this.getDBConnection();
		stmt1 = connect.createStatement();
		stmt2 = connect.createStatement();
		stmt1.execute("describe invitee");
		ResultSet rs1 = stmt1.getResultSet();
		rs1.last();
		int columnCount = rs1.getRow();

		rs1.beforeFirst();
		Map<String, String> mMap = new HashMap<String, String>();
		String myStatement = "SELECT";
		int tempCount = 1;
		while (rs1.next()) {
		    fieldName = rs1.getString("Field");
		    mMap.put(fieldName, "");

		    if (fieldName.equalsIgnoreCase("id")
			    || fieldName.equalsIgnoreCase("lastname")
			    || fieldName.equalsIgnoreCase("firstname")
			    || fieldName.equalsIgnoreCase("salutation")
			    || fieldName.equalsIgnoreCase("irb_id")
			    || fieldName.equalsIgnoreCase("phone")
			    || fieldName.equalsIgnoreCase("subj_type")) {
			if (tempCount == columnCount) {
			    myStatement = myStatement + " " + fieldName;
			} else {
			    myStatement = myStatement + " " + fieldName + ",";
			}
		    } else {
			if (tempCount == columnCount) {
			    myStatement = myStatement + " CAST(AES_DECRYPT("
				    + fieldName + ", '"
				    + this.emailEncryptionKey
				    + "') AS CHAR) as " + fieldName;
			} else {
			    myStatement = myStatement + " CAST(AES_DECRYPT("
				    + fieldName + ", '"
				    + this.emailEncryptionKey
				    + "') AS CHAR) as " + fieldName + ",";
			}
		    }
		    tempCount++;
		}
		myStatement += " FROM invitee WHERE id = " + inviteeId;
		stmt2.execute(myStatement);
		ResultSet rs2 = stmt2.getResultSet();
		Iterator iter = mMap.entrySet().iterator();

		rs2.next();
		while (iter.hasNext()) {
		    Map.Entry mEntry = (Map.Entry) iter.next();
		    fieldName = (String) mEntry.getKey();
		    mEntry.setValue(rs2.getString(fieldName));
		}
		Pattern p;
		Matcher m;
		Iterator iter1 = mMap.entrySet().iterator();

		while (iter1.hasNext()) {
		    Map.Entry mEntry = (Map.Entry) iter1.next();
		    fieldName = (String) mEntry.getKey();
		    if (fieldName.equalsIgnoreCase("id")
			    || fieldName.equalsIgnoreCase("lastname")
			    || fieldName.equalsIgnoreCase("firstname")
			    || fieldName.equalsIgnoreCase("email")
			    || fieldName.equalsIgnoreCase("salutation")
			    || fieldName.equalsIgnoreCase("irb_id")
			    || fieldName.equalsIgnoreCase("phone")
			    || fieldName.equalsIgnoreCase("subj_type")) {
			continue;
		    }
		    actualValue = (String) mEntry.getValue();
		    p = Pattern.compile("(@" + fieldName + "@)");
		    m = p.matcher(msg);
		    LOGGER.error("The ActualValue that is being replaced is "
			    + actualValue);
		    if (actualValue == null) {
			actualValue = "";
		    }
		    actualValue = actualValue.replaceAll("^\"|\"$", "");
		    LOGGER.error("The ActualValue that is being replaced  after removing the quotes is "
			    + actualValue);
		    // if (actualValue.equalsIgnoreCase(""))
		    msg = m.replaceAll(actualValue);
		}
		connect.close();
	    } catch (SQLException e) {
		e.printStackTrace();
		LOGGER.error("Error while Replacing the email message patterns");
	    }
	}
	return msg;
    }

    /**
     * Enters new record in survey_message_use table for the new URL.
     * 
     * @param messageId
     *            Message id of the new record
     * @param inviteeId
     *            Invitee Id for whom the mail is sent.
     * @param surveyID
     *            Survey name for which this user is linked to.
     * @return String New randomly generated number.
     */
    public String recordMessageUse(String messageId, String inviteeId,
	    String surveyID) {
	String messageUseId = org.apache.commons.lang3.RandomStringUtils
		.randomAlphanumeric(22);
	String sql = "INSERT INTO survey_message_use(messageId,invitee, survey, message) VALUES (?, ?, ?, ?) ";
	String newid = messageUseId;
	Connection connect = null;
	PreparedStatement stmt = null;
	try {
	    connect = this.getDBConnection();
	    stmt = connect.prepareStatement(sql);
	    stmt.setString(1, messageUseId);
	    stmt.setInt(2, Integer.parseInt(inviteeId));
	    stmt.setString(3, surveyID);
	    stmt.setString(4, messageId);

	    // check if the user has already existed in the survey data table
	    stmt.executeUpdate();
	} catch (SQLException e) {
	    LOGGER.error(
		    "Error recording new message using " + sql + ": "
			    + e.toString(), null);
	} catch (NumberFormatException e) {
	    LOGGER.error(
		    "Error recording new message using " + sql + ": "
			    + e.toString(), null);
	} finally {
	    try {
		if (connect != null) {
		    connect.close();
		}
		if (stmt != null) {
		    stmt.close();
		}
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	}
	return newid;
    }

    /**
     * Updates the message id in survey_message_use table.
     * 
     * @param messageId
     *            Message id of the new record
     * @param inviteeId
     *            Invitee Id for whom the mail is sent.
     * @param surveyID
     *            Survey name for which this user is linked to.
     * @return String Returns success in case of correct update.
     */
    public String updateMessageUse(String messageId, String inviteeId,
	    String surveyID) {

	String msgUseSql = "UPDATE survey_message_use SET message= ?"
		+ " WHERE message = 'attempt' AND survey = ?"
		+ " AND invitee = ? ";
	Connection connect = null;
	PreparedStatement stmt = null;
	try {
	    connect = this.getDBConnection();
	    stmt = connect.prepareStatement(msgUseSql);

	    stmt.setString(1, messageId);
	    stmt.setString(2, surveyID);
	    stmt.setInt(3, Integer.parseInt(inviteeId));

	    stmt.executeUpdate();
	} catch (SQLException e) {
	    LOGGER.error("Error updating new message using " + msgUseSql + ": "
		    + e.toString(), null);
	} catch (NumberFormatException e) {
	    LOGGER.error("Error recording new message using:" + e.toString(),
		    null);
	} finally {
	    try {
		if (connect != null) {
		    connect.close();
		}
		if (stmt != null) {
		    stmt.close();
		}
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	}
	return "success";
    }

    /**
     * Record an entry into table survey_user_state.
     * 
     * @param state
     *            State to be record.
     * @param inviteeId
     *            Invitee id .
     * @param surveyID
     *            Survey name.
     * @param messageSeqId
     *            Name of the message sequence.
     * @return String Sucess is returned if true.
     */
    public String recordSurveyState(String state, String inviteeId,
	    String surveyID, String messageSeqId) {

	String sqlU = "INSERT INTO survey_user_state (invitee, state, survey, message_sequence) "
		+ "VALUES( ?, ?, ?, ? ) "
		+ "ON DUPLICATE KEY UPDATE state = ?, state_count=1, message_sequence=VALUES(message_sequence)";
	Connection connect = null;
	PreparedStatement stmt = null;

	try {
	    connect = this.getDBConnection();
	    /* connect to database */
	    stmt = connect.prepareStatement(sqlU);
	    stmt.setInt(1, Integer.parseInt(inviteeId));
	    stmt.setString(2, state);
	    stmt.setString(3, surveyID);
	    stmt.setString(4, messageSeqId);
	    stmt.setString(5, state);

	    /* check if the user has already existed in the survey data table */
	    stmt.executeUpdate();
	    stmt.close();
	} catch (SQLException e) {
	    LOGGER.error(
		    "Error recording new message using " + sqlU + ": "
			    + e.toString(), null);
	} catch (NumberFormatException e) {
	    LOGGER.error("Error recording new message using:" + e.toString(),
		    null);
	} finally {
	    try {
		if (connect != null) {
		    connect.close();
		}
		if (stmt != null) {
		    stmt.close();
		}
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	}
	return "success";
    }
}