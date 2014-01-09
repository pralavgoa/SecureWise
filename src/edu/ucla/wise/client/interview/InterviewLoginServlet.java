package edu.ucla.wise.client.interview;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.Interviewer;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.WiseConstants;

/**
 * InterviewLoginServlet is a class which is called when interviewer 
 * tries to directly log in wise system, it also sets up Interviewer 
 * object in the session, in case the interviewer has logged in with valid credentials.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
@WebServlet("/survey/interview_login")
public class InterviewLoginServlet extends HttpServlet {
  	private static final long serialVersionUID = 1000L;

    /**
     * Checks if the interviewer has entered proper credentials and 
     * initializes Interviewer object or redirects to error page accordingly.
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
			
		/* get the interviewer login info from the login form */
		String interviewerName = req.getParameter("interviewername");
		String interviewerId = req.getParameter("interviewerid");
		String studyId = req.getParameter("studyid");
		String path = req.getContextPath() + "/" + WiseConstants.ADMIN_APP;
		
		if (SanityCheck.sanityCheck(interviewerName) 
				|| SanityCheck.sanityCheck(interviewerId)
				|| SanityCheck.sanityCheck(studyId)) {
	    	res.sendRedirect(path + "/sanity_error.html");
		    return;
	    }
		
		interviewerName = SanityCheck.onlyAlphaNumeric(interviewerName);
		interviewerId = SanityCheck.onlyAlphaNumeric(interviewerId);
		studyId = SanityCheck.onlyAlphaNumeric(studyId);
	    
	    if (interviewerName == null || interviewerName.isEmpty()
	    		|| interviewerId == null || interviewerId.isEmpty()
	    		|| studyId == null || studyId.isEmpty()) {
			res.sendRedirect(path + "/admin/parameters_error.html");
			return;
		}
	
		HttpSession session = req.getSession(true);
		
		/* get the study space and create the interviewer object */
		StudySpace theStudy = StudySpace.getSpace(studyId);
		Interviewer inv = new Interviewer(theStudy);
		String url;
		
		/* check the interviewer's verification and assign the attributes */
		if (inv.verifyInterviewer(interviewerId, interviewerName)) {
		    session.setAttribute("INTERVIEWER", inv);
		    url = SurveyorApplication.sharedFileUrl + "interview/Show_Assignment.jsp";
		} else {
		    url = theStudy.appUrlRoot + theStudy.dirName + "/interview/error"
			    + SurveyorApplication.htmlExt;
		}
		res.sendRedirect(url);
    }
}
