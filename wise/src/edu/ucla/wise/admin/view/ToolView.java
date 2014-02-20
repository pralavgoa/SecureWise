package edu.ucla.wise.admin.view;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.ucla.wise.admin.healthmon.HealthStatus;
import edu.ucla.wise.commons.Message;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.WiseConstants.SURVEY_STATUS;

public class ToolView {

    public SurveyHealthInformation healthStatusInfo(StudySpace studySpace) {

	SurveyHealthInformation healthInfo = new SurveyHealthInformation();

	healthInfo.dbCellColor = HealthStatus.getInstance().isDbIsAlive() ? "#008000"
		: "#FF0000";
	healthInfo.dbStatus = HealthStatus.getInstance().isDbIsAlive() ? "OK"
		: "Fail";
	healthInfo.smtpCellColor = HealthStatus.getInstance().isSmtpIsAlive() ? "#008000"
		: "#FF0000";
	healthInfo.smtpStatus = HealthStatus.getInstance().isSmtpIsAlive() ? "OK"
		: "Fail";
	SURVEY_STATUS studyServerStatus = HealthStatus.getInstance()
		.isSurveyAlive(studySpace.studyName, studySpace.db);
	switch (studyServerStatus) {
	case OK:
	    healthInfo.surveyCellColor = "#008000";
	    healthInfo.surveyStatus = "OK";
	    break;
	case FAIL:
	    healthInfo.surveyCellColor = "#FF0000";
	    healthInfo.surveyStatus = "Fail";
	    break;
	case NOT_AVAIL:
	    healthInfo.surveyCellColor = "#FF6F00";
	    healthInfo.surveyStatus = "Not Available";
	    break;
	}

	return healthInfo;
    }

    public List<SurveyInformation> getCurrentSurveys(Connection conn,
	    StudySpace studySpace) throws SQLException {

	List<SurveyInformation> currentSurveysList = new ArrayList<>();
	Statement stmt2 = conn.createStatement();
	String sql2 = "select internal_id, id, filename, title, status, uploaded from surveys where status in ('P', 'D') and internal_id in"
		+ "(select max(internal_id) from surveys group by id) order by uploaded DESC";
	boolean dbtype = stmt2.execute(sql2);
	ResultSet rs2 = stmt2.getResultSet();

	while (rs2.next()) {
	    SurveyInformation surveyInformation = new SurveyInformation();
	    surveyInformation.internalId = rs2.getString(1);
	    surveyInformation.id = rs2.getString(2);
	    surveyInformation.filename = rs2.getString(3);
	    surveyInformation.title = rs2.getString(4);
	    surveyInformation.status = rs2.getString(5);
	    surveyInformation.uploaded = rs2.getString(6);
	    if (surveyInformation.status.equalsIgnoreCase("D")) {
		surveyInformation.surveyMode = "Development";
	    }
	    if (surveyInformation.status.equalsIgnoreCase("P")) {
		surveyInformation.surveyMode = "Production";
	    }

	    surveyInformation.anonymousInviteUrl = Message.buildInviteUrl(
		    studySpace.appUrlRoot, null, studySpace.id,
		    surveyInformation.id);
	    currentSurveysList.add(surveyInformation);
	}
	return currentSurveysList;
    }

}
