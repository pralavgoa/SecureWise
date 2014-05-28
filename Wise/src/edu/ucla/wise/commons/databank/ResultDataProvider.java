package edu.ucla.wise.commons.databank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import edu.ucla.wise.commons.WiseConstants;
import edu.ucla.wise.persistence.data.Answer;
import edu.ucla.wise.persistence.data.DBConstants;
import edu.ucla.wise.persistence.data.RepeatingItemInstance;

/**
 * A subordinate of the DataBank class to isolate functionality related to
 * providing results.
 * 
 */
public class ResultDataProvider {

    private final DataBankInterface databank;

    private static final Logger LOGGER = Logger.getLogger(ResultDataProvider.class);

    public ResultDataProvider(DataBankInterface databank) {
        this.databank = databank;
    }

    /**
     * Table with all the answers for all invitees who have provided them.
     * 
     * @param surveyId
     * @param questionLevel
     * @return a csv string that can be opened in a spreadsheet.
     */
    public String getInviteeAnswersTable(String surveyId, int questionLevel) {

        StringBuilder resultBuilder = new StringBuilder();

        List<String> invitees = this.getDistinctInviteeIdsFromDataTable(surveyId, questionLevel);
        List<String> questions = this.getDistinctQuestionIds(surveyId, questionLevel);

        // print header row

        resultBuilder.append("inviteeId").append(WiseConstants.COMMA);
        resultBuilder.append(DBStringUtils.convertListToCSVString(questions)).append(WiseConstants.NEWLINE);
        for (String invitee : invitees) {
            resultBuilder.append(invitee);
            Map<String, String> answers = this.getAnswersForInvitee(surveyId, Integer.parseInt(invitee), questionLevel);
            for (String question : questions) {
                resultBuilder.append(WiseConstants.COMMA);
                resultBuilder.append(answers.get(question));

            }
            resultBuilder.append(WiseConstants.NEWLINE);
        }
        return resultBuilder.toString();
    }

    public Map<String, String> getAnswersForInvitee(String surveyId, int userId, int questionLevel) {
        String sqlForText = "SELECT questionId,answer FROM (SELECT * FROM " + DBConstants.MAIN_DATA_TEXT_TABLE
                + " WHERE level=" + questionLevel + " AND inviteeId=" + userId + " AND survey='" + surveyId
                + "' ORDER BY id DESC) AS x GROUP BY questionId";
        String sqlForInteger = "SELECT questionId,answer FROM (SELECT * FROM " + DBConstants.MAIN_DATA_INTEGER_TABLE
                + " WHERE level=" + questionLevel + " AND inviteeId=" + userId + " AND survey='" + surveyId
                + "' ORDER BY id DESC) AS x GROUP BY questionId";

        LOGGER.debug("SQL:" + sqlForText);
        LOGGER.debug("SQL:" + sqlForInteger);

        Map<String, String> answerMap = new HashMap<>();
        try {
            Connection connection = this.databank.getDBConnection();
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
            LOGGER.error("Exception while getting invitee data: id '" + userId + "'", e);
        }
        LOGGER.debug(answerMap);
        return answerMap;

    }

    public List<String> getDistinctInviteeIdsFromDataTable(String surveyId, int level) {
        return this.getListFromDataTable("inviteeId", surveyId, level);
    }

    public List<String> getDistinctQuestionIds(String surveyId, int level) {
        return this.getListFromDataTable("questionId", surveyId, level);
    }

    private List<String> getListFromDataTable(String columnName, String surveyId, int level) {
        String sql = "select distinct(" + columnName + ") from wisedev.data_text where survey=? AND level=?";

        List<String> distinctQuestionIds = new ArrayList<>();

        try {
            Connection connection = this.databank.getDBConnection();

            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, surveyId);
            stmt.setInt(2, level);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                distinctQuestionIds.add(rs.getString(1));
            }

        } catch (SQLException e) {
            LOGGER.error("Error while fetching distinctQuestionIds for survey '" + surveyId + "' for level " + level, e);
        }
        if (distinctQuestionIds.size() == 0) {
            LOGGER.info("No question ids found for '" + surveyId + "' at level '" + level + "'");
        }
        return distinctQuestionIds;

    }

    public String getAnswersInRepeatingSetForInvitee(String surveyId, int userId, String repeatingSetName) {

        String sqlToGetData = "SELECT instance_pseudo_id,questionId,answer FROM "
                + DBConstants.DATA_REPEAT_SET_INSTANCE_TABLE + "," + DBConstants.DATA_RPT_INS_TO_QUES_ID_TABLE + ","
                + DBConstants.MAIN_DATA_TEXT_TABLE + " WHERE " + "data_repeat_set_instance.survey='" + surveyId
                + "' AND " + "data_repeat_set_instance.id = data_rpt_ins_id_to_ques_id.rpt_ins_id" + " AND "
                + "data_rpt_ins_id_to_ques_id.ques_id = data_text.id" + " AND "
                + "data_repeat_set_instance.repeat_set_name='repeat_set_" + repeatingSetName + "'" + " AND "
                + "data_repeat_set_instance.inviteeId=" + userId;

        LOGGER.debug("SQL:" + sqlToGetData);

        java.util.List<RepeatingItemInstance> repeatingItemInstances = new ArrayList<>();

        try {
            PreparedStatement stmtToGetData = this.databank.getDBConnection().prepareStatement(sqlToGetData);
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
}
