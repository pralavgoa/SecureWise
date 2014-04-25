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

        healthInfo.dbCellColor = HealthStatus.getInstance().isDbIsAlive() ? "#008000" : "#FF0000";
        healthInfo.dbStatus = HealthStatus.getInstance().isDbIsAlive() ? "OK" : "Fail";
        healthInfo.smtpCellColor = HealthStatus.getInstance().isSmtpIsAlive() ? "#008000" : "#FF0000";
        healthInfo.smtpStatus = HealthStatus.getInstance().isSmtpIsAlive() ? "OK" : "Fail";
        SURVEY_STATUS studyServerStatus = HealthStatus.getInstance().isSurveyAlive(studySpace.studyName, studySpace.db);
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

    public List<SurveyInformation> getCurrentSurveys(Connection conn, StudySpace studySpace) throws SQLException {

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

            surveyInformation.anonymousInviteUrl = Message.buildInviteUrl(studySpace.appUrlRoot, null, studySpace.id,
                    surveyInformation.id);
            currentSurveysList.add(surveyInformation);
        }
        return currentSurveysList;
    }

}
