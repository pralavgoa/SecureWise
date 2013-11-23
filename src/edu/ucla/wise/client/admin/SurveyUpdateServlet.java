package edu.ucla.wise.client.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.WISEApplication;

/**
 * SurveyUpdateServlet is a class which is used to update the local survey info 
 * Called by the Admin tool's drop_survey.jsp page *assuming* no error
 * SurveyStatus param to indicate change requested.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class SurveyUpdateServlet extends HttpServlet {
    static final long serialVersionUID = 1000L;

    /**
     * Updates the survey info.
     * 
     * @param 	req	 HTTP Request.
     * @param 	res	 HTTP Response.
     * @throws 	ServletException and IOException. 
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res)
    		throws ServletException, IOException {

		/* prepare for writing */
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		
		/* Make sure local app is initialized */
		String initErr = SurveyorApplication.checkInit(req.getContextPath());
		out.println("<p>WISE Surveyor Application "
				+ SurveyorApplication.ApplicationName + " on "
				+ WISEApplication.rootURL + ": ");
		if (initErr != null) {
		    out.println("<p> FAILED to initialize </p>");
		    WISEApplication.logError("WISE Surveyor Init Error from survey_update servlet: "
				    + initErr, null);
		    return;
		}
	
		/* get the survey ID, status and study ID
		 * survey status:
		 * D - delete submitted data from surveys in developing mode
		 * R - remove the surveys in developing mode
		 * P - clean up and archive the data of surveys in production mode
		 */
		String surveyId = (String) req.getParameter("SurveyID");
		String surveyStatus = (String) req.getParameter("SurveyStatus");
		String studyId = (String) req.getParameter("SID");
		if (surveyId == null || studyId == null || surveyStatus == null) {
		    out.println("<tr><td align=center>SURVEY UPDATE ERROR: " +
		    		"can't get the survey id/status or study id from URL</td></tr></table>");
		    return;
		}
		
		/* get the study space */
		StudySpace studySpace = StudySpace.getSpace(studyId);
		if (studySpace == null) {
		    out.println("<tr><td align=center>SURVEY UPDATE ERROR: " +
		    		"can't find the requested study space</td></tr></table>");
		    return;
		}
		
		try {
		    if (surveyStatus.equalsIgnoreCase("R")
		    		|| surveyStatus.equalsIgnoreCase("P")) {
				studySpace.dropSurvey(surveyId);
				out.println("Dropped survey " + surveyId);
		    } else {
		    	out.println("Registered update of survey " + surveyId);
		    }
		} catch (NullPointerException e) {
		    WISEApplication.logError("WISE - DROP SURVEY DATA: " + e.toString(), null);
		    out.println("<tr><td align=center>Survey Update Error: "
		    		+ e.toString() + "</td></tr>");
		}
		return;
	}
}
