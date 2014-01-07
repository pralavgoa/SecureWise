package edu.ucla.wise.client.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucla.wise.commons.DataBank;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.Survey;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.initializer.WiseProperties;

/*
 Load a new survey and set up its Data tables. 
 (Called via URL request from load.jsp in the admin application)
 */

/**
 * SurveyLoaderServlet class is used to load a new survey and set up its Data tables
 * and also archives old tables.
 * (Called via URL request from load.jsp in the admin application)
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class SurveyLoaderServlet extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Archives the old survey and sets up new survey.
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
		WiseProperties properties = new WiseProperties("wise.properties","WISE");
		String initErr = SurveyorApplication.checkInit(req.getContextPath(), properties);
		if (initErr != null) {
		    out.println(initErr + "<p> Servlet called: Survey Loader </p>"
		    		+ SurveyorApplication.initErrorHtmlFoot);
		    WISEApplication.logError("WISE Surveyor Init Error: " + initErr,
		    		null);
		    return;
		}
	
		out.println("<table border=0>");
		
		/* get the survey name and study ID */
		String surveyName = (String) req.getParameter("SurveyName");
		String studyId = (String) req.getParameter("SID");
		if (surveyName == null || studyId == null) {
		    out.println("<tr><td align=center>SURVEY LOADER ERROR: can't " +
		    		"get the survey name or study id from URL</td></tr></table>");
		    return;
		}
	
		out.println("<tr><td align=center>SURVEY Name:" + surveyName
				+ " STUDY ID: " + studyId + "</td></tr>");
	
		/* get the study space */
		StudySpace studySpace = StudySpace.getSpace(studyId);
		if (studySpace == null) {
		    out.println("<tr><td align=center>SURVEY LOADER ERROR: " +
		    		"can't create study space</td></tr></table>");
		    return;
		}
	
		/* get the survey */
		String surveyID = studySpace.loadSurvey(surveyName);
		Survey survey = studySpace.getSurvey(surveyID);
	
		DataBank db = new DataBank(studySpace);
	
		try {
			
		    /* connect to the database */
			String sql = "DELETE FROM interview_assignment WHERE survey = ?";
			Connection conn = studySpace.getDBConnection();
		   	PreparedStatement stmt = conn.prepareStatement(sql);
	
		    /* create data table - archive old data - copy old data */
		    out.println("<tr><td align=center>Creating new data table.<td></tr>");
		    db.setupSurvey(survey);
	
		    /* delete old data*/
		    //out.println("<tr><td align=center>Deleting data from tables" +
		    //		"update_trail and page_submit.</td></tr>");
		    //db.delete_survey_data(survey);
	
		    /* remove the interview records from table - interview_assignment */
		    out.println("<tr><td align=center>Deleting data from tables " +
		    		"of interview_assignment and interview_session.</td><tr>");
		    
		    stmt.setString(1, surveyID);		    
		    stmt.executeUpdate();
	
		    out.println("</table>");
		    stmt.close();
		    conn.close();
		} catch (SQLException e) {
		    WISEApplication.logError("WISE - SURVEY LOADER: " + e.toString(),
		    		null);
		    out.println("<tr><td align=center>survey loader Error: "
		    		+ e.toString() + "</td></tr>");
		}
		return;
    }
}
