package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.Interviewer;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;

/**
 * ProgressServlet is a class used to display the sub menus 
 * to the left of the survey to review the completed pages of the survey.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
@WebServlet("/survey/progress")
public class ProgressServlet extends HttpServlet {
    static final long serialVersionUID = 1000;

    public void service(HttpServletRequest req, HttpServletResponse res)
    		throws ServletException, IOException {

		/* prepare for writing */
		PrintWriter out;
		res.setContentType("text/html");
		out = res.getWriter();
	
		HttpSession session = req.getSession(true);
		
		//Surveyor_Application s = (Surveyor_Application) session
		//	.getAttribute("SurveyorInst");
	
		/* if session is new, then show the session expired info */
		if (session.isNew()) {
		    res.sendRedirect(SurveyorApplication.sharedFileUrl + "error"
		    		+ SurveyorApplication.htmlExt);
		    return;
		}
	
		/* get the user from session */
		User theUser = (User) session.getAttribute("USER");
		if (theUser == null || theUser.getId() == null) {
		    out.println("<p>Error: Can't find the user info.</p>");
		    return;
		}
	
		Hashtable<String, String> completedPages = theUser.getCompletedPages();
	
		/* get the interviewer if it is on the interview status */
		Interviewer inv = (Interviewer) session.getAttribute("INTERVIEWER");
		
		/* for interviewer, he can always browse any pages */
		if (inv != null)
		    		    theUser.getCurrentSurvey().allowGoback = true;
	
		/*
		 * check if the allow goback setting is ture, then user could go back to
		 * view the pages that he has went through
		 */
		if (		    theUser.getCurrentSurvey().allowGoback) {
		    out.println(		    theUser.getCurrentSurvey()
		    		.printProgress(theUser.getCurrentPage()));
		} else {
		
			/*
			 * otherwise, print out the page list without linkages to prevent user
			 * from going back
			 */
		    out.println(		    theUser.getCurrentSurvey().printProgress(
		    		theUser.getCurrentPage(), completedPages));
		}
	
		out.close();
    }
}
