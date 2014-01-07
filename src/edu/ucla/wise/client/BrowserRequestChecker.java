package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.initializer.WiseProperties;

/**
 * BrowserRequestChecker is used to initialize the survey 
 * and check the User associated with the session.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class BrowserRequestChecker {
	static Logger log = Logger.getLogger("BrowserRequestChecker Class");
	
	/**
     * Returns a boolean value about the correct initialization of the survey.
     * 
     * @param 	req	 		HTTP Request.
     * @param 	res	 		HTTP Response.
     * @param 	PrintWriter	The output writer.
     * @return 	boolean		If the survey has been initialized properly or not.
     * @throws 	IOException. 
     */
    public static boolean checkRequest(HttpServletRequest req,
    		HttpServletResponse res, PrintWriter out) throws IOException {
    	WiseProperties properties = new WiseProperties("wise.properties","WISE");
		String initErr = SurveyorApplication.checkInit(req.getContextPath(), properties);
		if (initErr != null) {
		    out.println("<HTML><HEAD><TITLE>WISE survey system -- Can't identify you</TITLE>"
		    		+ "<LINK href='"
		    		+ SurveyorApplication.sharedFileUrl
		    		+ "style.css' type=text/css rel=stylesheet>"
		    		+ "<body><center><table>"
		    		// + "<body text=#000000 bgColor=#ffffcc><center><table>"
		    		+ "<tr><td>Sorry, the WISE Surveyor application failed to initialize. "
		    		+ "Please contact the system administrator with the following information."
		    		+ "<P>"
		    		+ initErr
		    		+ "</td></tr>"
		    		+ "</table></center></body></html>");
		    log.error("WISE Surveyor Init Error: " + initErr, null);
		    return false;
		}
		return true;
    }

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
		    res.sendRedirect(SurveyorApplication.sharedFileUrl + "error"
		    		+ SurveyorApplication.htmlExt);
		    return null;
		}
		
		/* get the user from session */
		User user = (User) session.getAttribute("USER");
		
		/* latter signals an improperly-initialized User */
		if (user == null || user.id == null) {
		    out.println("<p>Error: Can't find the user info.</p>");
		    return null;
		}	
		return user;
    }
}
