package edu.ucla.wise.client.interview;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.Interviewer;
import edu.ucla.wise.commons.WiseConstants;

/* 
 Handle the interviewer's log out
 */

/**
 * InterviewLogoutServlet is a class which is called when interviewer 
 * tries to log out.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class InterviewLogoutServlet extends HttpServlet {
   	private static final long serialVersionUID = 100L;

    /**
     * Logs out the interviewer and removes Interviewer object from the session 
     * and redirects login page accordingly.
     *  
     * @param 	req	 HTTP Request.
     * @param 	res	 HTTP Response.
     * 
     * @throws 	ServletException and IOException. 
     */
    @Override
	public void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
	
	res.setContentType("text/html");
	
	String url = null;
	HttpSession session = req.getSession(true);
	
	//Surveyor_Application s = (Surveyor_Application) session
	//	.getAttribute("SurveyorInst");

	if (session.isNew()) {
	    url = "interview/expired.htm";
	} else if (session != null) {
	    Interviewer inv = (Interviewer) session.getAttribute("INTERVIEWER");
	    
	    /* get the URL of the forwarding page */
	    url = inv.studySpace.appUrlRoot + WiseConstants.SURVEY_APP
	    		+ "/interview/expired.htm";
	    
	    /* remove the interviewer from the session */
	    session.removeAttribute("INTERVIEWER");
	    
	    /* end the session */
	    session.invalidate();
	}
	res.sendRedirect(url);
    }

}
