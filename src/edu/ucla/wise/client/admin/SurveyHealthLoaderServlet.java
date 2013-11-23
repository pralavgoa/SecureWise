package edu.ucla.wise.client.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucla.wise.client.healthmon.SurveyHealth;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.WISEApplication;

/*
 Load a new survey and set up its Data tables. 
 (Called via URL request from load.jsp in the admin application)
 */

/**
 * SurveyHealthLoaderServlet class is used to laod the method 
 * which checks the status of the survey system.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class SurveyHealthLoaderServlet extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Checks for the parameters and calls the status monitor method.
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
		if (initErr != null) {
		    out.println(initErr + "<p> Servlet called: Survey Loader </p>"
		    		+ SurveyorApplication.initErrorHtmlFoot);
		    WISEApplication.logError("WISE Surveyor Init Error: " + initErr,
		    		null);
		    return;
		}
	
		/* get the survey name and study ID */
		String surveyName = (String) req.getParameter("SurveyName");
		String studyId = (String) req.getParameter("SID");
		if (surveyName == null || studyId == null) {
		    out.println("<tr><td align=center>SURVEY LOADER ERROR: can't get the survey " +
		    		"name or study id from URL</td></tr></table>");
		    return;
		}
		
		/* get the study space */
		StudySpace studySpace = StudySpace.getSpace(studyId);
		if (studySpace == null) {
		    out.println("<tr><td align=center>SURVEY LOADER ERROR: can't create study space</td></tr></table>");
		    return;
		}
		SurveyHealth.monitor(studySpace);
		return;
    }

}