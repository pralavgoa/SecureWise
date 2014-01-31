package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;

/**
 * BrowserRequestChecker is used to check the User associated with the session.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class BrowserRequestChecker {
	static Logger log = Logger.getLogger(BrowserRequestChecker.class);
	
    /**
     * Returns the user object that is linked with the current session if 
     * present else returns null.
     * 
     * @param 	req	 		HTTP Request.
     * @param 	res	 		HTTP Response.
     * @param 	PrintWriter	The output writer.
     * @return 	User		The user objected present in the session.
     * @throws 	IOException. 
     */
    public static User getUserFromSession(HttpServletRequest req,
    		HttpServletResponse res, PrintWriter out) throws IOException {
		HttpSession session = req.getSession(true);
	
		/*
		 *  if session is new, then it must have expired since begin; show the
		 *  session expired info
		 */
		if (session.isNew()) {
		    res.sendRedirect(SurveyorApplication.getInstance().getSharedFileUrl() + "error"
		    		+ SurveyorApplication.htmlExt);
		    return null;
		}
		
		/* get the user from session */
		User user = (User) session.getAttribute("USER");
		
		/* latter signals an improperly-initialized User */
		if (user == null || user.getId() == null) {
		    out.println("<p>Error: Can't find the user info.</p>");
		    return null;
		}	
		return user;
    }
}
