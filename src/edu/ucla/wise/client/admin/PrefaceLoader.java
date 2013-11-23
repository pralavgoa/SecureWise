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
 * PrefaceLoader is a class, which includes both welcome page and consent form (optional)
 * (continue running the URL request from the admin - load.jsp)
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class PrefaceLoader extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Checks if the user has entered proper credentials and also verifies if he is 
     * blocked and initializes AdminInfo object or redirects to error page accordingly.
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
	    out.println(initErr + "<p> Servlet called: Preface Loader </p>"
	    		+ SurveyorApplication.initErrorHtmlFoot);
	    WISEApplication.logError("WISE Surveyor Init Error: " + initErr,
	    		null);// should write to file if no email
	    return;
	}

	out.println("<table border=0>");

	/* get the survey name and study ID */
	String studyId = (String) req.getParameter("SID");
	if (studyId == null) {
	    out.println("<tr><td align=center>PREFACE LOADER ERROR: can't get the preface name or study id from URL</td></tr></table>");
	    return;
	}

	/* get the study space */
	StudySpace studySpace = StudySpace.getSpace(studyId);
	if (studySpace == null) {
	    out.println("<tr><td align=center>SURVEY LOADER ERROR: can't create study space</td></tr></table>");
	    return;
	}

	/* get the preface */
	if (studySpace.loadPreface()) {
	    out.println("<tr><td align=center>The preface has been successfully loaded for the study space.<td></tr>");
	} else {
	    out.println("<tr><td align=center>Failed to load the preface for the study space.<td></tr>");
	}
	out.println("</table>");
	return;
    }

}
