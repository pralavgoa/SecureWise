package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;

/**
 * ConsentRecordServlet is a class which leads user to the survey if he accepted the consent or
 * lead him to the page to ask for decline reason if he declined the consent.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */

@WebServlet("/survey/consent_record")
public class ConsentRecordServlet extends HttpServlet {
    static final long serialVersionUID = 1000L;

    /**
     * Redirects the page to survey or decline consent reason.
     * 
     * @param 	req	 HTTP Request.
     * @param 	res	 HTTP Response.
     * @throws 	ServletException and IOException. 
     */
    @Override
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
		if (theUser == null) {
		    out.println("<p>Error: Can't find the user info.</p>");
		    return;
		}
	
		/* get user's consent decision */
		String answer = req.getParameter("answer");
	
		String url = "";
		// accepted the consent
		if (answer.equalsIgnoreCase("yes")) {
		    theUser.consent();
		    
		    /* forward to setup_survey servlet */
		    url = "setup_survey";
		} else if (answer.equalsIgnoreCase("no_consent")) {
		    
			/* 
			 * Accepted the consent
			 * forward to setup_survey servlet, which handles all other state changes.
			 */
		    url = "setup_survey";
		} else {
			
			/* declined the consent */
		    theUser.decline();
		    
		    /* forward to decline servlet */
		    url = SurveyorApplication.sharedFileUrl + "decline" + SurveyorApplication.htmlExt;
		}
		res.sendRedirect(url);
		out.close();
    }

}
