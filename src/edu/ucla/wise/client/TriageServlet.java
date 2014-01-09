package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.CommonUtils;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WiseConstants.STATES;

/**
 * TriageServlet is used to direct the user after browser check to appropriate next step or page.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
@WebServlet("/survey/start")
public class TriageServlet extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Replaces the current page with the new page.
     * @param 	newPage	Url of the new page	
     * @return	String html to replace the current page.
     */
    public String pageReplaceHtml(String newPage) {
		return "<html>" + "<head><script LANGUAGE='javascript'>"
			+ "top.location.replace('" + newPage + "');"
			+ "</script></head>" + "<body></body>" + "</html>";
    }

    /**
     * Forwards the user to correct page based on his status which 
     * could be a new or a returning user.
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
	
		if (session.isNew()) {
		    res.sendRedirect(SurveyorApplication.sharedFileUrl + "error"
			    + SurveyorApplication.htmlExt);
		    return;
		}
	
		User theUser = (User) session.getAttribute("USER");
	
		/* if the user can't be created, send error info */
		if (theUser == null) {
		    out.println("<HTML><HEAD><TITLE>Begin Page</TITLE>"
		    		+ "<LINK href='"
		    		+ SurveyorApplication.sharedFileUrl
		    		+ "style.css' type=text/css rel=stylesheet>"
		    		+ "<body><center><table>"
		    		// + "<body text=#000000 bgColor=#ffffcc><center><table>"
		    		+ "<tr><td>Error: WISE can't seem to store your identity in the browser. You may have disabled cookies.</td></tr>"
		    		+ "</table></center></body></html>");
		    WISEApplication.logError(
		    		"WISE BEGIN - Error: Can't create the user.", null);
		    return;
		}
	
		String interviewBegin = (String) session.getAttribute("INTERVIEW");
		String mainUrl = "";
	
		/* check if user already completed the survey */
		if (theUser.completedSurvey()) {
		    if (theUser.getMyDataBank().getUserState()
		    		.equalsIgnoreCase(STATES.incompleter.name())) {
		    	theUser.getMyDataBank().setUserState(STATES.started.name());
		    }
		    if (interviewBegin != null) {
		    	
		    	/* then IS an interview, always direct interviewer 
		    	 * to the survey page. This previously *just* recorded
		    	 *  the current page in the db; not sure why if interviewing and done
		    	 */ 
				mainUrl = SurveyorApplication.servletUrl
	 					+ "setup_survey";
		    } else {
		    	
		    	/* 
		    	 * not an interview
				 * forward to another application's URL, if specified in survey xml file.
				 */
				if (theUser.currentSurvey.forwardUrl != null
						&& !theUser.currentSurvey.forwardUrl
						.equalsIgnoreCase("")) {
				    mainUrl = theUser.currentSurvey.forwardUrl;
				    
				    /*
				     * if an educational module ID is specified in the survey
				     * xml, then add it to the URL
				     */
				    if (!CommonUtils.isEmpty(theUser.currentSurvey.eduModule)) {
						mainUrl += "/"
								+ theUser.currentSurvey.studySpace.dirName
								+ "/survey?t="
								+ WISEApplication.encode(theUser.currentSurvey.eduModule)
								+ "&r=" + WISEApplication.encode(theUser.id);
				    } else {
				    	
				    	/*
				    	 * otherwise the link will be the URL plus the user ID
				    	 * Added Study Space ID and Survey ID, was sending just
						 * the UserID earlier
				    	 */
						mainUrl = mainUrl
							+ "?s="
							+ WISEApplication.encode(theUser.id)
							+ "&si="
							+ theUser.currentSurvey.id
							+ "&ss="
							+ WISEApplication.encode(theUser.currentSurvey.studySpace.id);
				    }
				} else if (theUser.currentSurvey.minCompleters == -1) {
					
					/*
					 * if the min completers is not set in survey xml, then direct
					 * to Thank You page
					 */
				    mainUrl = SurveyorApplication.sharedFileUrl
					    + "thank_you";
				} else if (theUser.currentSurvey.minCompleters != -1) {
				    
					/* this link may come from the invitation email for results
				     * review or user reclicked the old invitation link
				     * check if the number of completers has reached the minimum
				     * number set in survey xml,
				     * then redirect the user to the review result page
				     */
				    if (theUser.checkCompletionNumber() < theUser.currentSurvey.minCompleters) {
						mainUrl = SurveyorApplication.sharedFileUrl
							+ "thank_you" + "?review=false";
				    } else {
						mainUrl = SurveyorApplication.servletUrl
							+ "view_results";
				    }
				}	
		    }
		} else if (theUser.startedSurvey()) {
		    
			/* for either user or interviewer, redirect to start the current page. */
		    mainUrl = SurveyorApplication.servletUrl + "setup_survey";
		} else {
			
			/* forward to the welcome page */
		    // main_url =
		    // WISE_Application.retrieveAppInstance(session).servlet_url +
		    // "welcome_generate";
		    mainUrl = SurveyorApplication.servletUrl + "welcome";
		}
	
		/* output javascript to forward */
		out.println(pageReplaceHtml(mainUrl));
		out.close();
    }
}