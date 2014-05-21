/**
 * Copyright (c) 2014, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.ucla.wise.commons;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;
import com.google.gson.Gson;

import edu.ucla.wise.persistence.data.Answer;
import edu.ucla.wise.persistence.data.DBConstants;
import edu.ucla.wise.persistence.data.GeneratedKeysForDataTables;
import edu.ucla.wise.persistence.data.RepeatingItemInstance;

//TODO (low): consider getting rid of STATUS column in data table & just using page_submit; 
// nice thing tho is that STATUS is given back alongside the main data.

/**
 * Class UserDBConnection -- a customized interface to encapsulate single-user
 * interface to data storage.
 */
public class UserDBConnection {
    public User theUser = null;
    private final String surveyID;
    private final String mainTableName;
    private DataBank db;
    private Connection conn = null;
    private static final Logger LOGGER = Logger.getLogger(UserDBConnection.class);

    /**
     * If there is a quote in the string, replace it with double quotes this is
     * necessary for sql to store the quote properly.
     * 
     * @param s
     *            Input string with quotes.
     * @return String Modifies string.
     */
    public static String fixquotes(String s) {
        if (s == null) {
            return "";
        }

        int len = s.length();
        String s1, s2;

        s2 = "";
        for (int i = 0; i < len; i++) {
            s1 = s.substring(i, i + 1);
            s2 = s2 + s1;
            if (s1.equalsIgnoreCase("'")) {
                s2 = s2 + "'";
            }
        }
        return s2;
    }

    /**
     * Constructor for the class.
     * 
     * @param usr
     *            User for which this object is associated to.
     * @param dbk
     *            Data bank for getting details to contact database.
     */
    public UserDBConnection(User usr, DataBank dbk) {
        this.theUser = usr;
        this.db = dbk;
        this.surveyID = usr.getCurrentSurvey().getId();
        this.mainTableName = this.surveyID + DataBank.MainTableExtension;
        try {

            /*
             * open a database connection to hold for the user ultimately closed
             * by finalize() below
             */
            this.conn = this.db.getDBConnection();
        } catch (SQLException e) {
            LOGGER.error("User " + this.theUser.getId() + " unable to make its DB connection. Err: " + e.toString(),
                    null);
        }
    }

    /**
     * constructor version for testing only
     * 
     * @param user
     *            User for which this object is associated to.
     */
    public UserDBConnection(User user) {
        this.theUser = user;
        this.surveyID = user.getCurrentSurvey().getId();
        this.mainTableName = this.surveyID + DataBank.MainTableExtension;
    }

    /**
     * finalize() called by garbage collector to clean up all objects
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            this.conn.close();
        } catch (SQLException e) {
            LOGGER.error("Exception for user " + this.theUser.getId() + " closing DB connection w/: " + e.toString(),
                    null);
        } finally {
            super.finalize();
        }
    }

    /**
     * Retrieves values for a list of fields from the invitee table
     * 
     * @param fieldNames
     *            Array of field name whose values are to be retrieved from the
     *            database.
     * @return String[] Array of the values from the database.
     */
    public String[] getInviteeAttrs(String[] fieldNames) {
        HashSet<String> nonEncodedFieldSet = new HashSet<String>();
        nonEncodedFieldSet.add("firstname");
        nonEncodedFieldSet.add("lastname");
        nonEncodedFieldSet.add("salutation");
        nonEncodedFieldSet.add("phone");
        nonEncodedFieldSet.add("irb_id");

        String userid = this.theUser.getId();
        String[] values = new String[fieldNames.length];
        if (fieldNames.length < 1) {
            return values;
        }
        String fieldString = "";
        for (int i = 0; i < (fieldNames.length - 1); i++) {
            fieldString += (!nonEncodedFieldSet.contains(fieldNames[i].toLowerCase())) ? "AES_DECRYPT("
                    + fieldNames[i].toLowerCase() + ",'" + this.db.emailEncryptionKey + "')" : fieldNames[i];
            fieldString += ",";
        }
        // fieldString += fieldNames[fieldNames.length - 1];
        fieldString += (!nonEncodedFieldSet.contains(fieldNames[fieldNames.length - 1].toLowerCase())) ? "AES_DECRYPT("
                + fieldNames[fieldNames.length - 1].toLowerCase() + ",'" + this.db.emailEncryptionKey + "')"
                : fieldNames[fieldNames.length - 1];

        String sql = "SELECT " + fieldString + " FROM " + DBConstants.INVITEE_TABLE + " WHERE id = " + userid;
        try {

            // TODO: Change to Prepared Statement.
            /* connect to the database */
            Statement stmt = this.conn.createStatement();

            /* get the status' value from survey data table */
            LOGGER.info("\n #####The SQl being executed for the extraction of invitee fields is :" + sql);
            stmt.execute(sql);
            ResultSet rs = stmt.getResultSet();

            /*
             * update the current page by searching with the status' value (page
             * ID)
             */
            if (rs.next()) {
                for (int i = 0; i < fieldNames.length; i++) {
                    values[i] = rs.getString(i + 1);
                    values[i] = values[i].replaceAll("^\"|\"$", "");
                }
            }
            stmt.close();
        } catch (SQLException e) {
            LOGGER.error("DataBank - Invitee attr retrieval fail: " + e.toString(), null);
            return null; // signal failure to retrieve
        }
        return values;
    }

    /**
     * Writes array of values for a page and also the ID of next page to the
     * user's row in survey's main data table.
     * 
     * @param name
     *            The values of the columns in the survey's data table.
     * @param valTypes
     *            Types of the columns in the survey's data table.
     * @param vlas
     *            Values of the columns in the survey's data table.
     * @return int 1 if successful.
     */
    public int storeMainData(String[] names, char[] valTypes, String[] vals) {
        String sql = "", sqlu = "";
        String colNames = "", values = "", updateStr = "", updateTrailStr = "";

        /* connect to the database */
        Statement stmt = null;
        int numtoStore = 0;
        try {
            stmt = this.conn.createStatement();
        } catch (SQLException e) {
            LOGGER.error("WISE - PAGE Store error: Can't get DB statement for user [" + this.theUser.getId() + "]: "
                    + e.toString(), null);
        }

        // TODO: Change form statement to prepared statement here.
        for (int i = 0; i < names.length; i++) {
            String fieldnm = names[i];
            String newval = vals[i];
            if ((newval == null) || newval.equals("")) {
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
            updateTrailStr += ",(" + this.theUser.getId() + ",'" + this.surveyID + "','" + fieldnm + "', " + newval
                    + ")";
            numtoStore++;
        }
        if (numtoStore > 1) {

            /* chop initial comma */
            updateTrailStr = updateTrailStr.substring(1, updateTrailStr.length());
            sqlu = "insert into update_trail (invitee, survey, ColumnName, CurrentValue)" + " values " + updateTrailStr;
            try {
                stmt.execute(sqlu);
            } catch (SQLException e) {
                LOGGER.error("WISE - PAGE Store [" + this.theUser.getId() + "] query (" + sqlu + "): " + e.toString(),
                        null);
            }
        }

        /*
         * note proper storage of "status" field relies on User object having
         * advanced page before call;
         */
        String nextPage = "null";
        if (this.theUser.getCurrentPage() != null) {

            /* null val means finished */
            nextPage = "'" + this.theUser.getCurrentPage().getId() + "'";
        }
        sql = "INSERT into " + DBConstants.SURVEY_USER_PAGE_STATUS_TABLE + " (invitee, status " + colNames
                + ") VALUES (" + this.theUser.getId() + "," + nextPage + values
                + ") ON DUPLICATE KEY UPDATE status=VALUES(status) " + updateStr;
        LOGGER.info("The data storing sql is " + sql);
        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            LOGGER.error("WISE - PAGE Store error [" + this.theUser.getId() + "] query (" + sql + "): " + e.toString(),
                    null);
        }
        try {
            stmt.close();
        } catch (SQLException e) {
            LOGGER.error("WISE - PAGE Store closing error: " + e.toString(), null);
        }
        return 1;
    }

    public void storeMainDataNew(String[] names, char[] valTypes, String[] vals) {

        int questionLevel = 0;

        if ((names.length != valTypes.length) || (names.length != vals.length)) {
            throw new IllegalArgumentException("Code to store main data failed");
        }

        Map<String, Answer> answers = new HashMap<>();
        for (int i = 0; i < names.length; i++) {

            if (Strings.isNullOrEmpty(names[i]) || Strings.isNullOrEmpty(vals[i])) {
                LOGGER.error("Trying to save null values, INVESTIGATE");
                continue;
            }

            if (valTypes[i] == 'a') {
                answers.put(names[i], new Answer(vals[i], Answer.Type.TEXT));
            } else {
                answers.put(names[i], new Answer(Integer.parseInt(vals[i]), Answer.Type.INTEGER));
            }
        }

        // store data
        this.saveData(answers, questionLevel);

    }

    /**
     * sets up user's status entry in survey data table
     * 
     * @param pageID
     *            Page ID whose status has to be updated to
     */
    public void beginSurvey(String pageID) {

        String sql = "SELECT status FROM " + DBConstants.SURVEY_USER_PAGE_STATUS_TABLE + " WHERE invitee = ?";
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        try {

            /* connect to database */
            stmt1 = this.conn.prepareStatement(sql);
            stmt1.setInt(1, Integer.parseInt(this.theUser.getId()));

            ResultSet rs = stmt1.executeQuery();
            boolean exists = rs.next();

            /*
             * if the user doesn't exist, insert a new user record in to the
             * data table and set the status value to be the ID of the 1st
             * survey page - (starting from the beginning)
             */
            if (!exists) {
                sql = "INSERT INTO " + DBConstants.SURVEY_USER_PAGE_STATUS_TABLE + " (invitee, status) VALUES ( ?, ?)";
                stmt2 = this.conn.prepareStatement(sql);
                stmt2.setInt(1, Integer.parseInt(this.theUser.getId()));
                stmt2.setString(2, pageID);
                stmt2.executeUpdate();
            }

            /* update user state to be started (consented) */
            sql = "update survey_user_state set state='started', state_count=1, entry_time=now() where invitee= ?"
                    + " AND survey= ?";
            stmt3 = this.conn.prepareStatement(sql);
            stmt3.setInt(1, Integer.parseInt(this.theUser.getId()));
            stmt3.setString(2, this.surveyID);
            stmt3.executeUpdate();

        } catch (SQLException e) {
            LOGGER.error("Databank SETUP STATUS:" + e.toString(), null);
        } catch (NumberFormatException e) {
            LOGGER.error("Databank SETUP STATUS:" + e.toString(), null);
        } finally {
            try {
                if (stmt1 != null) {
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
     * @return String The Page on which user is on. Returns null if none
     */
    public String getCurrentPageName() {
        String sql = "SELECT status FROM " + DBConstants.SURVEY_USER_PAGE_STATUS_TABLE + " WHERE invitee = ?";
        PreparedStatement stmt = null;

        /* Assumes user/survey has a state */
        String status = null;
        try {

            /* connect to database */
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(this.theUser.getId()));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                status = rs.getString(1);
            }
            rs.close();
        } catch (SQLException e) {
            LOGGER.error("UDB getCurrentPageName:" + e.toString(), e);
        } catch (NumberFormatException e) {
            LOGGER.error("UDB getCurrentPageName:" + e.toString(), null);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return status;
    }

    /**
     * Reads all previously-submitted main values for the user; returns null if
     * not started the survey
     * 
     * @return Hashtable All the previously submitted answers in the form of
     *         fieldName-->response.
     */
    public HashMap<String, String> getMainData() {
        HashMap<String, String> h = new HashMap<String, String>();
        int i = 0;
        String sql = "SELECT * from " + this.mainTableName + " WHERE invitee = " + this.theUser.getId();
        PreparedStatement stmt = null;
        try {
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(this.theUser.getId()));

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

                    /*
                     * leave out of the hashtable if null value (hashes can't
                     * hold nulls)
                     */
                    if (ans != null) {
                        h.put(colName, ans);
                    }
                }
            } else {
                return null;
            }
        } catch (SQLException e) {
            LOGGER.error("UDB getCurrentPageName:" + e.toString(), e);
        } catch (NumberFormatException e) {
            LOGGER.error("UDB getCurrentPageName:" + e.toString(), null);
        } catch (Exception e) {
            LOGGER.error("USER_DB SETUP DATA after " + i + " cols read: " + e.toString(), e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return h;
    }

    public Map<String, String> getMainDataNew(int questionLevel) {
        String sqlForText = "SELECT questionId,answer FROM (SELECT * FROM " + DBConstants.MAIN_DATA_TEXT_TABLE
                + " WHERE level=" + questionLevel + " AND inviteeId=" + this.theUser.getId() + " AND survey='"
                + this.theUser.getCurrentSurvey().getId() + "' ORDER BY id DESC) AS x GROUP BY questionId";
        String sqlForInteger = "SELECT questionId,answer FROM (SELECT * FROM " + DBConstants.MAIN_DATA_INTEGER_TABLE
                + " WHERE level=" + questionLevel + " AND inviteeId=" + this.theUser.getId() + " AND survey='"
                + this.theUser.getCurrentSurvey().getId() + "' ORDER BY id DESC) AS x GROUP BY questionId";

        LOGGER.debug("SQL:" + sqlForText);
        LOGGER.debug("SQL:" + sqlForInteger);

        Map<String, String> answerMap = new HashMap<>();
        try {
            Connection connection = this.conn;
            PreparedStatement stmtForText = connection.prepareStatement(sqlForText);

            ResultSet rs = stmtForText.executeQuery();

            while (rs.next()) {
                String questionId = rs.getString(1);
                String answerId = rs.getString(2);
                answerMap.put(questionId, answerId);
            }

            PreparedStatement stmtForInteger = connection.prepareStatement(sqlForInteger);

            rs = stmtForInteger.executeQuery();

            while (rs.next()) {
                String questionId = rs.getString(1);
                String answerId = rs.getString(2);
                answerMap.put(questionId, answerId);
            }

        } catch (SQLException e) {
            LOGGER.error("Exception while getting invitee data: id '" + this.theUser.getId() + "'", e);
        }

        LOGGER.debug(answerMap);
        return answerMap;
    }

    /**
     * 
     * @param questionAnswer
     * @param questionLevel
     * @return Set of generated keys.
     */
    public GeneratedKeysForDataTables saveData(Map<String, Answer> questionAnswer, int questionLevel) {
        String sqlForText = "INSERT INTO `" + DBConstants.MAIN_DATA_TEXT_TABLE
                + "` (`survey`, `inviteeId`, `questionId`, `answer`, `level`) VALUES (?,?,?,?,?)";
        String sqlForInteger = "INSERT INTO `" + DBConstants.MAIN_DATA_INTEGER_TABLE
                + "` (`survey`, `inviteeId`, `questionId`, `answer`, `level`) VALUES (?,?,?,?,?)";

        if (questionAnswer.isEmpty()) {
            LOGGER.info("An empty Map provided as input. INVESTIGATE");
        }

        Set<Integer> generatedKeysForIntegerTable = new HashSet<>();
        Set<Integer> generatedKeysForTextTable = new HashSet<>();
        try {
            Connection connection = this.conn;

            PreparedStatement stmtForText = connection.prepareStatement(sqlForText, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement stmtForInteger = connection.prepareStatement(sqlForInteger,
                    Statement.RETURN_GENERATED_KEYS);

            for (Entry<String, Answer> entry : questionAnswer.entrySet()) {
                Answer answer = entry.getValue();
                if (answer.getType() == Answer.Type.TEXT) {
                    stmtForText.setString(1, this.theUser.getCurrentSurvey().getId());
                    stmtForText.setString(2, this.theUser.getId());
                    stmtForText.setString(3, entry.getKey());
                    stmtForText.setString(4, answer.toString());
                    stmtForText.setInt(5, questionLevel);
                    stmtForText.execute();
                    ResultSet keySet = stmtForText.getGeneratedKeys();

                    while (keySet.next()) {
                        generatedKeysForTextTable.add(keySet.getInt(1));
                    }

                } else {
                    stmtForInteger.setString(1, this.theUser.getCurrentSurvey().getId());
                    stmtForInteger.setString(2, this.theUser.getId());
                    stmtForInteger.setString(3, entry.getKey());
                    stmtForInteger.setInt(4, (int) answer.getAnswer());
                    stmtForInteger.setInt(5, questionLevel);
                    stmtForInteger.execute();
                    ResultSet keySet = stmtForInteger.getGeneratedKeys();

                    while (keySet.next()) {
                        generatedKeysForIntegerTable.add(keySet.getInt(1));
                    }
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Could not save user answers for invitee " + this.theUser.getId());
            LOGGER.error(questionAnswer, e);
        }

        GeneratedKeysForDataTables generatedKeys = new GeneratedKeysForDataTables();
        generatedKeys.setIntegerTableKeys(generatedKeysForIntegerTable);
        generatedKeys.setTextTableKeys(generatedKeysForTextTable);

        return generatedKeys;
    }

    public String getAllDataForRepeatingSet(String repeatingSetName) {

        String sqlToGetData = "SELECT instance_pseudo_id,questionId,answer FROM "
                + DBConstants.DATA_REPEAT_SET_INSTANCE_TABLE + "," + DBConstants.DATA_RPT_INS_TO_QUES_ID_TABLE + ","
                + DBConstants.MAIN_DATA_TEXT_TABLE + " WHERE "
                + "data_repeat_set_instance.id = data_rpt_ins_id_to_ques_id.rpt_ins_id" + " AND "
                + "data_rpt_ins_id_to_ques_id.ques_id = data_text.id" + " AND "
                + "data_repeat_set_instance.repeat_set_name='repeat_set_" + repeatingSetName + "'" + " AND "
                + "data_repeat_set_instance.inviteeId=" + this.theUser.getId();

        LOGGER.debug("SQL:" + sqlToGetData);

        java.util.List<RepeatingItemInstance> repeatingItemInstances = new ArrayList<>();

        try {
            PreparedStatement stmtToGetData = this.conn.prepareStatement(sqlToGetData);
            ResultSet rs = stmtToGetData.executeQuery();
            RepeatingItemInstance currentInstance = null;
            while (rs.next()) {
                String instancePseudoId = rs.getString(1);
                String questionId = rs.getString(2);
                String answer = rs.getString(3);

                if (currentInstance == null) {
                    currentInstance = new RepeatingItemInstance(repeatingSetName, instancePseudoId);
                } else if (currentInstance.getInstanceName().equals(instancePseudoId)) {
                    currentInstance.addAnswer(questionId, new Answer(answer, Answer.Type.TEXT));
                } else {
                    repeatingItemInstances.add(currentInstance);
                    currentInstance = new RepeatingItemInstance(repeatingSetName, instancePseudoId);
                    currentInstance.addAnswer(questionId, new Answer(answer, Answer.Type.TEXT));
                }
            }
            if (currentInstance != null) {
                repeatingItemInstances.add(currentInstance);
            }
        } catch (SQLException e) {
            LOGGER.error("Could not get data for repeating set", e);
        }
        String response = new Gson().toJson(repeatingItemInstances);
        LOGGER.debug("Repeating Set Name='" + repeatingSetName + "' Response:" + response);
        return response;
    }

    public void insertRepeatSetInstance(String repeatSetName, String instanceName, Map<String, Answer> answers) {

        String sqlForRepeatSetIdToInstance = "INSERT INTO data_repeat_set_instance"
                + "(repeat_set_name,instance_pseudo_id,inviteeId) VALUES (?,?,?)";
        String sqlForRepeatSetInstanceToQuestionId = "INSERT INTO data_rpt_ins_id_to_ques_id"
                + "(rpt_ins_id, ques_id, type) VALUES (?,?,?)";

        try {
            Connection connection = this.conn;

            PreparedStatement stmtRptIdToInstance = connection.prepareStatement(sqlForRepeatSetIdToInstance,
                    Statement.RETURN_GENERATED_KEYS);

            stmtRptIdToInstance.setString(1, repeatSetName);
            stmtRptIdToInstance.setString(2, instanceName);
            stmtRptIdToInstance.setString(3, this.theUser.getId());

            stmtRptIdToInstance.execute();

            ResultSet keySet = stmtRptIdToInstance.getGeneratedKeys();

            int repeatInstanceId = -1;
            while (keySet.next()) {
                repeatInstanceId = keySet.getInt(1);
            }

            if (repeatInstanceId == -1) {
                throw new IllegalStateException("Insert id was not retrieved for the update statement");
            }

            GeneratedKeysForDataTables generatedKeys = this.saveData(answers, 1);

            PreparedStatement stmtForRptInsToQuesId = connection.prepareStatement(sqlForRepeatSetInstanceToQuestionId);

            for (int foreignKey : generatedKeys.getTextTableKeys()) {
                stmtForRptInsToQuesId.setInt(1, repeatInstanceId);
                stmtForRptInsToQuesId.setInt(2, foreignKey);
                stmtForRptInsToQuesId.setString(3, "A");
                stmtForRptInsToQuesId.execute();
            }

            for (int foreignKey : generatedKeys.getIntegerTableKeys()) {
                stmtForRptInsToQuesId.setInt(1, repeatInstanceId);
                stmtForRptInsToQuesId.setInt(2, foreignKey);
                stmtForRptInsToQuesId.setString(3, "N");
            }

        } catch (SQLException e) {
            LOGGER.error("Could not save user answers for invitee " + this.theUser.getId());
            LOGGER.error(answers, e);
        }
    }

    /**
     * Saves the data from the repeating item set questions into the database
     * 
     * @param tableName
     *            Repeating item set table name.
     * @param rowId
     *            Row id to which data has to be stored.
     * @param rowName
     *            Row name
     * @param nameValue
     *            Answers for the repeating item set.
     * @param nameType
     *            Types of the repeating item set table columns.
     * @return int returns the inserted key.
     */
    public int insertUpdateRowRepeatingTableOld(String tableName, String rowId, String rowName,
            Hashtable<String, String> nameValue, Hashtable<String, String> nameType) {

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

            commaSepdUpdateString.append(columnName + "=VALUES(" + columnName + "),");

            String column_value = nameValue.get(columnName);
            if (nameType.get(columnName).equals("text") || nameType.get(columnName).equals("textarea")) {
                if ("".equals(column_value)) {
                    commaSepdColumnValues.append(" NULL,");
                } else {
                    commaSepdColumnValues.append("'" + fixquotes(column_value) + "'" + ",");
                }
            } else {
                commaSepdColumnValues.append(column_value + ",");
            }

        }

        /* remove the last commas */
        if (commaSepdColumnNames.charAt(commaSepdColumnNames.length() - 1) == ',') {
            commaSepdColumnNames.setCharAt(commaSepdColumnNames.length() - 1, ' ');
        }
        if (commaSepdColumnValues.charAt(commaSepdColumnValues.length() - 1) == ',') {
            commaSepdColumnValues.setCharAt(commaSepdColumnValues.length() - 1, ' ');
        }
        if (commaSepdUpdateString.charAt(commaSepdUpdateString.length() - 1) == ',') {
            commaSepdUpdateString.setCharAt(commaSepdUpdateString.length() - 1, ' ');
        }
        /* --end of remove last commas */
        // TODO: change from statement to prepared statement.
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
        sqlStatement.append(this.theUser.getId() + ",");
        sqlStatement.append("'" + rowName + "',");
        sqlStatement.append(commaSepdColumnValues.toString() + ") ");
        sqlStatement.append("ON DUPLICATE KEY UPDATE ");
        sqlStatement.append(commaSepdUpdateString);
        sqlStatement.append("");
        sqlStatement.append("");
        sqlStatement.append("");
        sqlStatement.append("");
        sqlStatement.append("");

        LOGGER.info(sqlStatement.toString());

        Statement statement = null;
        try {
            statement = this.conn.createStatement();
        } catch (SQLException e) {
            LOGGER.error("WISE - Repeat Item Store error: Can't get DB statement for user [" + this.theUser.getId()
                    + "]: " + e.toString(), null);
        }

        try {
            statement.execute(sqlStatement.toString(), Statement.RETURN_GENERATED_KEYS);

            ResultSet generatedKeySet = statement.getGeneratedKeys();
            if (generatedKeySet.first()) {
                insertedKeyValue = generatedKeySet.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.error(
                    "WISE - Repeat Item Store error [" + this.theUser.getId() + "] query (" + sqlStatement.toString()
                            + "): " + e.toString(), null);
        }
        try {
            statement.close();
        } catch (SQLException e) {
            LOGGER.error("WISE - Repeat Item Store closing error: " + e.toString(), null);
        }
        return insertedKeyValue;
    }

    /**
     * Returns the data stored in the given repeating item table in the form of
     * a json.
     * 
     * @param repeatingSetName
     *            Name of the table whose data is to be read.
     * @return String The data form the table in the form of json.
     */
    public String getAllDataForRepeatingSetOld(String repeatingSetName) {
        String tableName = "repeat_set_" + repeatingSetName;
        int columnIndex = 0;

        StringBuffer javascriptArrayResponse = new StringBuffer();
        PreparedStatement stmt = null;

        try {

            /* pull all from current repeating set table */
            String sql = "SELECT * from " + tableName + " WHERE invitee = ?";

            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
            LOGGER.info("The sql statement is:" + sql);
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
                        throw new SQLException("can't get column name " + columnIndex);
                    }
                    ans = rs.getString(colName);

                    if (columnIndex == 1) {
                        javascriptArrayResponse.append("\"" + ans + "\"" + ":[{");
                    }
                    if (ans != null) {
                        javascriptArrayResponse.append("\"" + colName + "\"");
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
                        if (javascriptArrayResponse.charAt(javascriptArrayResponse.length() - 1) == ',') {
                            javascriptArrayResponse.deleteCharAt(javascriptArrayResponse.length() - 1);
                        }
                        javascriptArrayResponse.append("}],");
                    }
                }
            }
            if (javascriptArrayResponse.length() > 2) {

                /* remove the last comma */
                javascriptArrayResponse.deleteCharAt(javascriptArrayResponse.length() - 1);
            }
            javascriptArrayResponse.append("}");
        } catch (SQLException e) {
            LOGGER.error("USER_DB REPEATING SET after " + columnIndex + " cols read: " + e.toString(), e);
        } catch (NumberFormatException e) {
            LOGGER.error("USER_DB REPEATING Invalid User ID" + e.toString(), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.error("USER_DB REPEATING SET error:" + e.toString(), null);
                }
            }
        }
        return javascriptArrayResponse.toString();
    }

    /**
     * Returns all the data that the user has stored in the survey data table.
     * 
     * @return Hashtable Hash table which contains all the data of a user in all
     *         survey tables.
     */
    public Hashtable<String, String> getAllData() {
        Hashtable<String, String> h = new Hashtable<String, String>();
        String sql = "select ColumnName, CurrentValue from UPDATE_TRAIL " + "where invitee = " + this.theUser.getId()
                + " AND survey = " + this.surveyID + " Order by Modified asc";
        PreparedStatement stmt = null;

        try {

            /* connect to the database */
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
            stmt.setString(2, this.surveyID);

            /* get data from database for subject */
            ResultSet rs = stmt.executeQuery();

            // ResultSetMetaData metaData = rs.getMetaData();
            // int columns = metaData.getColumnCount();

            /*
             * The data hash table takes the column name as the key and the
             * user's anwser as its value
             */
            while (rs.next()) {
                String colName, ans;
                colName = rs.getString(2);
                ans = rs.getString(2);

                /*
                 * input a string called null if the column value is null to
                 * avoid the hash table has the null value
                 */
                if (ans == null) {
                    ans = "null";
                }
                h.put(colName, ans); // old, overwritten values will be
                // overwritten here
            }
            rs.close();
        } catch (SQLException e) {
            LOGGER.error("UDB getAllData:" + e.toString(), e);
        } catch (NumberFormatException e) {
            LOGGER.error("UDB getAllData:" + e.toString(), null);
        } finally {
            try {
                if (stmt != null) {
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
        String sql = "INSERT INTO " + DBConstants.SURVEY_USER_PAGE_STATUS_TABLE + " (invitee, status) "
                + "VALUES (?,?) on duplicate key update status=values(status)";
        PreparedStatement stmt = null;
        try {
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
            stmt.setString(2, this.theUser.getCurrentPage().getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Record page STATUS:" + e.toString(), null);
        } catch (NumberFormatException e) {
            LOGGER.error("Record page STATUS Invalid User ID" + e.toString(), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.error("Record page STATUS:" + e.toString(), null);
                }
            }
        }
    }

    /**
     * Updates the users current submitted page into the database.
     */
    public void recordPageSubmit() {
        String sql = "INSERT INTO page_submit (invitee, survey, page) " + "VALUES (?,?,?)";
        PreparedStatement stmt = null;
        try {
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
            stmt.setString(2, this.surveyID);
            stmt.setString(3, this.theUser.getCurrentPage().getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Record page submit error:" + e.toString(), null);
        } catch (NumberFormatException e) {
            LOGGER.error("Record page submit Invalid User ID" + e.toString(), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.error("Record page submit error:" + e.toString(), null);
                }
            }
        }
    }

    /**
     * Generates a random string which is used while link generation for sending
     * email to users. This link is used by the users to get access to the
     * survey system.
     * 
     * @param messageID
     *            The message name that is send to the user as email.
     * @return String New random message Id that is used for link generation
     *         while sending emails.
     */
    public String recordMessageUse(String messageId) {
        String uid = this.theUser.getId();
        String randMessageId = org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(22);
        String sql = "INSERT INTO survey_message_use(messageId,invitee, survey, message) VALUES (?,?,?,?)";
        PreparedStatement stmt = null;
        try {

            /* connect to database */
            stmt = this.conn.prepareStatement(sql);

            stmt.setString(1, randMessageId);
            stmt.setInt(2, Integer.parseInt(uid));
            stmt.setString(3, this.surveyID);
            stmt.setString(4, messageId);

            /* check if the user has already existed in the survey data table */
            stmt.executeUpdate();

            stmt.close();
        } catch (SQLException e) {
            LOGGER.error("Error recording new message using " + sql + ": " + e.toString(), null);
        } catch (NumberFormatException e) {
            LOGGER.error("Error recording new message Invalid User ID" + e.toString(), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.error("Error recording new message" + e.toString(), null);
                }
            }
        }
        return randMessageId;
    }

    /**
     * Saves the user's new state into database.
     * 
     * @param newState
     *            New state to which user is to be updated.
     */
    public void setUserState(String newState) {
        String sql = "update survey_user_state set state = ?" + ", state_count=1 " + // reset
                                                                                     // to
                                                                                     // 1
                                                                                     // on
                                                                                     // entering
                                                                                     // new
                                                                                     // state
                "where invitee = ? AND survey = ?";
        PreparedStatement stmt = null;

        /* Assumes user/survey has a state */
        try {

            /* connect to database */
            stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, newState);
            stmt.setInt(2, Integer.parseInt(this.theUser.getId()));
            stmt.setString(3, this.surveyID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("UDB setUserState:" + e.toString(), null);
        } catch (NumberFormatException e) {
            LOGGER.error("UDB setUserState Invalid User ID" + e.toString(), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.error("UDB setUserState:" + e.toString(), null);
                }
            }
        }
    }

    /**
     * Returns users current state of the user in the survey weather it is
     * completed/interrupted.
     * 
     * @return String Current state of the user.
     */
    public String getUserState() {
        String sql = "SELECT state FROM survey_user_state " + "where invitee= ?" + " AND survey= ?";

        PreparedStatement stmt = null;

        /* Assumes user/survey has a state */
        String theState = null;
        try {

            /* connect to database */
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
            stmt.setString(2, this.surveyID);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                theState = rs.getString(1);
            }
        } catch (SQLException e) {
            LOGGER.error("UDB getUserState:" + e.toString(), null);
        } catch (NumberFormatException e) {
            LOGGER.error("UDB getUserState Invalid User ID" + e.toString(), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.error("UDB getUserState:" + e.toString(), null);
                }
            }
        }
        return theState;
    }

    /**
     * Returns current message sequence of the user this is used to know which
     * email has to be sent to the user.
     * 
     * @return String The user's current message sequence.
     */
    public String getCurrentMessageSequence() {
        String sql = "SELECT message_sequence FROM survey_user_state " + "where invitee= ? AND survey= ?";
        PreparedStatement stmt = null;

        /* Assumes user/survey has a sequence */
        String theSeq = null;
        try {

            /* connect to database */
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
            stmt.setString(2, this.surveyID);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                theSeq = rs.getString(1);
            }
        } catch (SQLException e) {
            LOGGER.error("UDB getCurrentMessageSequence:" + e.toString(), null);
        } catch (NumberFormatException e) {
            LOGGER.error("UDB getCurrentMessageSequence Invalid User ID" + e.toString(), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.error("UDB getCurrentMessageSequence:" + e.toString(), null);
                }
            }
        }
        return theSeq;
    }

    /**
     * Saves the user's consent answer - accept or decline
     * 
     * @param answer
     *            accept or decline.
     */
    public void setConsent(String answer) {
        String sql = "INSERT INTO consent_response (invitee, answer, survey) VALUES (?, ?, ?)";
        PreparedStatement stmt = null;
        try {

            /* connect to database */
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
            stmt.setString(2, answer);
            stmt.setString(3, this.surveyID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("UDB setConsent:" + e.toString(), null);
        } catch (NumberFormatException e) {
            LOGGER.error("UDB setConsent Invalid User ID" + e.toString(), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.error("UDB setConsent:" + e.toString(), null);
                }
            }
        }
    }

    /**
     * Checks if user has consented - accepted the consent form.
     * 
     * @return boolean If user has accepted the consent or not.
     */
    public boolean checkConsent() {
        boolean resultp = false;
        String sql = "SELECT * FROM consent_response WHERE invitee = ?" + " AND survey= ? AND answer = 'Y'";
        PreparedStatement stmt = null;

        try {
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
            stmt.setString(2, this.surveyID);

            /*
             * if user accepted the consent form, the record can be found from
             * the consent_response table
             */
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                resultp = true;
            }
            rs.close();
        } catch (SQLException e) {
            LOGGER.error("UDB checkConsent:" + e.toString(), null);
        } catch (NumberFormatException e) {
            LOGGER.error("UDB checkConsent Invalid User ID" + e.toString(), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.error("UDB checkConsent:" + e.toString(), null);
                }
            }
        }
        return resultp;
    }

    /**
     * Creates user's survey session and captures the ip address of user into
     * the survey_user_session
     * 
     * @param browserUseragent
     *            User browser's User Agent.
     * @param ipAddress
     *            Ip address of the user's machine from where he is accessing
     *            the survey.
     * @param surveyMsgId
     *            Message Id.
     * @return String Session ID.
     */
    public String createSurveySession(String browserUseragent, String ipAddress, String surveyMsgId) {
        String sessionid = "";
        String sql1 = "INSERT INTO survey_user_session (from_message, endtime, starttime, browser_info, ip_address) "
                + "VALUES (?, 0, now(), ?, ?)";
        String sql2 = "SELECT LAST_INSERT_ID()";
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        try {

            /* connect to the database */
            statement1 = this.conn.prepareStatement(sql1);
            statement1.setString(1, surveyMsgId);
            statement1.setString(2, browserUseragent);
            statement1.setString(3, ipAddress);

            /*
             * add a new session record and save the startime & user's browser
             * info and ip address
             */
            statement1.executeUpdate();

            /* get the new session id */
            statement2 = this.conn.prepareStatement(sql2);
            ResultSet rs = statement2.executeQuery();
            if (rs.next()) {
                sessionid = rs.getString(1);
            }
        } catch (SQLException e) {
            LOGGER.error("USER CREATE DB SESSION:" + e.toString(), null);
        } finally {
            if ((statement1 != null) || (statement2 != null)) {
                try {
                    if (statement1 != null) {
                        statement1.close();
                    }
                    if (statement2 != null) {
                        statement2.close();
                    }
                } catch (SQLException e) {
                    LOGGER.error("USER CREATE DB SESSION:" + e.toString(), null);
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
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.error("USER CLOSE SURVEY SESSION :" + e.toString(), null);
        } catch (NumberFormatException e) {
            LOGGER.error("UDB setDone Invalid User ID" + e.toString(), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.error("USER CLOSE SURVEY SESSION :" + e.toString(), null);
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
        String sql2 = "UPDATE " + DBConstants.SURVEY_USER_PAGE_STATUS_TABLE + " SET status = null WHERE invitee = "
                + this.theUser.getId();

        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        try {
            stmt1 = this.conn.prepareStatement(sql1);
            stmt1.setInt(1, Integer.parseInt(this.theUser.getSession()));
            stmt1.executeUpdate();

            stmt2 = this.conn.prepareStatement(sql2);
            stmt2.setInt(1, Integer.parseInt(this.theUser.getId()));
            stmt2.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("UDB setDone:" + e.toString(), null);
        } catch (NumberFormatException e) {
            LOGGER.error("UDB setDone Invalid User ID" + e.toString(), e);
        } finally {
            if (stmt1 != null) {
                try {
                    stmt1.close();
                } catch (SQLException e) {
                    LOGGER.error("UDB setDone:" + e.toString(), null);
                }
            }
            if (stmt2 != null) {
                try {
                    stmt2.close();
                } catch (SQLException e) {
                    LOGGER.error("UDB setDone:" + e.toString(), null);
                }
            }
        }
    }

    /**
     * Gets a hashtable of all the page IDs which the user has completed and
     * currently working on
     * 
     * @return Hashtable Status of user with respect to each page.
     */
    public Hashtable<String, String> getCompletedPages() {
        Hashtable<String, String> pages = new Hashtable<String, String>();
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        String sql1 = "select status from " + DBConstants.SURVEY_USER_PAGE_STATUS_TABLE + " where invitee = ?";
        String sql2 = "select distinct page from page_submit where invitee = ?" + " and survey = ?";

        try {
            stmt1 = this.conn.prepareStatement(sql1);

            stmt1.setInt(1, Integer.parseInt(this.theUser.getId()));

            /* get the status' value from the survey data table */
            ResultSet rs = stmt1.executeQuery();

            /* add it into the hashtable */
            while (rs.next()) {
                pages.put(rs.getString(1), "Current");
            }

            // get the submitted page IDs from page submit table
            stmt2 = this.conn.prepareStatement(sql2);
            stmt2.setInt(1, Integer.parseInt(this.theUser.getId()));
            stmt2.setString(2, this.surveyID);

            rs = stmt2.executeQuery();

            /* input them into the hashtable */
            while (rs.next()) {
                pages.put(rs.getString(1), "Completed");
            }

        } catch (SQLException e) {
            LOGGER.error("USER DB getCompletedPages:" + e.toString(), e);
        } catch (NumberFormatException e) {
            LOGGER.error("getCompletedPages Invalid User ID" + e.toString(), e);
        } finally {
            if ((stmt1 != null) || (stmt2 != null)) {
                try {
                    if (stmt1 != null) {
                        stmt1.close();
                    }
                    if (stmt2 != null) {
                        stmt2.close();
                    }
                } catch (SQLException e) {
                    LOGGER.error("getCompletedPages:" + e.toString(), null);
                }
            }
        }
        return pages;
    }

    /**
     * Records the new invitee who has accessed the survey for the first time.
     * 
     * @param inviteeId
     *            Id of the new invitee.
     * @param surveyId
     *            Survey that the user is accessing.
     * @return boolean If the record is successful or not.
     */
    public boolean recordWelcomeHit(String inviteeId, String surveyId) {

        /* insert a new accessment record */
        String sql = "INSERT INTO welcome_hits (invitee, survey) VALUES (?, ?)";

        PreparedStatement stmt = null;
        boolean resultp = false;
        try {
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(inviteeId));
            stmt.setString(2, this.surveyID);
            int temp = stmt.executeUpdate();
            if (temp > 0) {
                resultp = true;
            }

        } catch (NumberFormatException e) {
            LOGGER.error("USER RECORD WELCOME HIT:" + e.toString(), e);
        } catch (SQLException e) {
            LOGGER.error("USER RECORD WELCOME HIT:" + e.toString(), e);
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOGGER.error(e);
            }
        }
        return resultp;
    }

    /**
     * Records the details about a user who has declined taking the survey.
     * 
     * @param msgId
     *            The Id in the URL which is used to access the system.
     * @param studyId
     *            Study space name which the user is declining.
     * @param inviteeId
     *            The Id of user.
     * @param surveryId
     *            Survey name which the user is declining.
     * @return boolean If the operation of recording was successful or not.
     */
    public boolean recordDeclineHit(String msgId, String studyId, String inviteeId, String surveryId) {

        boolean resultp = false;
        // Statement stmt = null;
        String sql1 = "INSERT INTO decline_hits (msg_id, survey) VALUES (?, ?)";
        String sql2 = "update survey_user_state set state='declined', state_count=1, "
                + "entry_time=now() where invitee= ? AND survey= ?";
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        try {
            /* connect to the database */
            stmt1 = this.conn.prepareStatement(sql1);
            stmt2 = this.conn.prepareStatement(sql2);

            /* add a new decline hits record */
            stmt1.setString(1, msgId);
            stmt1.setString(2, studyId);
            int out1 = stmt1.executeUpdate();

            /* update the user state */
            stmt2.setInt(1, Integer.parseInt(inviteeId));
            stmt2.setString(2, surveryId);
            int out2 = stmt2.executeUpdate();
            if ((out1 > 0) && (out2 > 0)) {
                resultp = true;
            }
        } catch (NumberFormatException e) {
            LOGGER.error("USER RECORD DECLINE HIT:" + e.toString(), e);
        } catch (SQLException e) {
            LOGGER.error("USER RECORD DECLINE HIT:" + e.toString(), e);
        } finally {
            try {
                if (stmt1 != null) {
                    stmt1.close();
                }
                if (stmt2 != null) {
                    stmt2.close();
                }
            } catch (SQLException e) {
                LOGGER.error(e);
            }
        }
        return resultp;
    }

    /**
     * Records the reason for declining to taking the survey.
     * 
     * @param inviteeId
     *            User who wants to decline the survey.
     * @param reason
     *            Reason for declining
     * @return boolean If the reason has successfully updated or not.
     */
    public boolean setDeclineReason(String inviteeId, String reason) {

        PreparedStatement stmt = null;
        String sql = "INSERT INTO decline_reason (invitee, reason) VALUES (?,?)";
        boolean retVal = false;
        try {

            /* connect to the database */
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(inviteeId));
            stmt.setString(2, reason);

            /* save the user's decline reason */
            int out = stmt.executeUpdate();
            if (out > 0) {
                retVal = true;
            }
        } catch (NumberFormatException e) {
            LOGGER.error("USER SET DECLINE REASON:" + e.toString(), e);
        } catch (SQLException e) {
            LOGGER.error("USER SET DECLINE REASON:" + e.toString(), e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                LOGGER.error(e);
            }
        }
        return retVal;
    }

    /**
     * Outputs number of users who have finished taking the survey as of now.
     * 
     * @param surveyId
     *            Survey Name whose status has to be checked.
     * @return int Number of people who have finished taking the survey.
     */
    public int checkCompletionNumber(String surveyId) {
        int numCompleters = 0;
        String surveyDataTable = surveyId + "_data";
        String sql = "SELECT count(distinct invitee) FROM " + surveyDataTable + "WHERE status IS NULL";
        PreparedStatement stmt = null;
        try {

            /* connect to the database */
            stmt = this.conn.prepareStatement(sql);

            /* count the completers */
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                numCompleters = rs.getInt("count(distinct invitee)");
            }
            rs.close();
        } catch (SQLException e) {
            LOGGER.error("USER CHECK COMPLETION NUMBER:" + e.toString(), e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                LOGGER.error(e);
            }
        }
        return numCompleters;
    }

    /**
     * Deletes repeating item sets.
     * 
     * @param inviteeId
     *            Invitee whose repeating item has to be deleted.
     * @param tableName
     *            Repeating item table name from which the item has to be
     *            deleted.
     * @param instanceName
     *            Row's instance name which has to be deleted.
     * @return boolean If the delete was successful or not.
     */
    public boolean deleteRowFromTable(String itemSetName, String instanceName) {

        String sqlStatement = "DELETE FROM " + DBConstants.DATA_REPEAT_SET_INSTANCE_TABLE + " WHERE invitee= ?"
                + " AND instance_pseudo_id=? AND repeat_set_name=?";
        PreparedStatement statement = null;

        try {
            statement = this.conn.prepareStatement(sqlStatement);
            statement.setInt(1, Integer.parseInt(this.theUser.getId()));
            statement.setString(2, instanceName);
            statement.setString(3, itemSetName);
            statement.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            LOGGER.error("Error for SQL statement: " + sqlStatement, e);
            return false;
        } catch (NumberFormatException e) {
            LOGGER.error("deleteRowFromTable Invalid User ID" + e.toString(), e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.error("deleteRowFromTable:" + e.toString(), null);
                }
            }
        }
        return true;
    }
}
