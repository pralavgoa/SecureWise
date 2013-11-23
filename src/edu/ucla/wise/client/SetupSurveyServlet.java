package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.Interviewer;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WISEApplication;

/**
 * SetupSurveyServlet sets up session for user to begin completing survey.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class SetupSurveyServlet extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Sets up the session for the user accessing the survey and also 
     * redirects the survey to correct page for returning users.
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
		String initErr = SurveyorApplication.checkInit(req.getContextPath());
		HttpSession session = req.getSession(true);
	
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
		    WISEApplication.logError("WISE Surveyor Init Error: " + initErr,
		    		null);// should write to file if no email
		    return;
		}
	
		/* if session is new, then it must have expired since begin; show the session expired info */
		if (session.isNew()) {
		    res.sendRedirect(SurveyorApplication.sharedFileUrl + "error"
			    + SurveyorApplication.htmlExt);
		    return;
		}
		
		/* get the user from session */
		User theUser = (User) session.getAttribute("USER");
		if (theUser == null || theUser.id == null) {
			
			/* latter signals an improperly-initialized User */
		    out.println("<p>Error: Can't find the user info.</p>");
		    return;
		}
		
		/* get the interviewer if it exists (set by interview_login) */
		Interviewer inv = (Interviewer) session.getAttribute("INTERVIEWER");
		if (theUser.completedSurvey()) {
			
			/* triage should prevent this but in case it fails, bail out to "thanks" page */
		    res.sendRedirect(SurveyorApplication.sharedFileUrl + "thank_you");
		}
	
		/* Initialize survey session, passing the browser information */		
		String browserInfo = req.getHeader("user-agent");
		
		/* Add Ip address for Audit logs. */
		String ipAddress = req.getHeader("X-FORWARDED-FOR");  
		   if (ipAddress == null) {  
			   ipAddress = req.getRemoteAddr();  
		   }		
		theUser.startSurveySession(browserInfo,ipAddress);
		
		/* check if it is an interview process */
		if (inv != null) {
		    
			/* start the interview session */
		    inv.beginSession(theUser.userSession);
		}
	
		/* display the current survey page */
	
		/*
		 * out.println("<html>"); out.println(
		 * "<head><script LANGUAGE='JavaScript1.1'>top.mainFrame.instruct.location.reload();</script></head>"
		 * ); //if( (WISE_Application.retrieveAppInstance(session).servlet_url
		 * != null) || (theUser.currentPage != null)) if( (theUser.currentPage
		 * != null)) out.println("<body ONLOAD=\"self.location = '" +
		 * Surveyor_Application.servlet_url +
		 * "view_form?p="+theUser.currentPage.id+"';\">&nbsp;</body>"); else
		 * out.println("<body> Setup Survey Failure! </body>");
		 * out.println("</html>");
		 * 
		 * out.close();
		 */
		
		/* pralav modifications */
		if (theUser.currentPage != null) {
		    
			/* Pralav code for printing page */
		    StringBuffer htmlContent = new StringBuffer("");
	
		    /* html_content.append("<!DOCTYPE html>"); */
		    htmlContent.append("<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' " +
		    		"'http://www.w3.org/TR/html4/strict.dtd'>");
		    htmlContent.append("<html>");
		    htmlContent.append("<head>");
		    htmlContent.append("<title>Web-based Interactive Survey Environment (WISE)</title>");
		    htmlContent.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>"
				    + "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\"/>"
				    + "<script type='text/javascript' language='javascript' src='"
				    + SurveyorApplication.sharedFileUrl
				    + "main.js'></script>"
				    + "<script type='text/javascript' language='javascript' SRC='"
				    + SurveyorApplication.sharedFileUrl
				    + "survey.js'></script>"
				    + "<script type='text/javascript' language='javascript'>"
				    + "	top.fieldVals = null;"
				    + "	top.requiredFields = null;"
				    + "     var userId = "
				    + theUser.id
				    + ";"
				    + "</script>");
		    htmlContent.append("</head>");
		    htmlContent.append("<body onload='javascript: setFields();check_preconditions();'>");
		    htmlContent.append("<div id='content'>");
		    htmlContent.append(getPageHTML(theUser));
		    htmlContent.append("</div>");
		    htmlContent.append("<div id = 'progress_bar'>");
		    htmlContent.append(getProgressDivContent(theUser, session));
		    htmlContent.append("</div>");
		    htmlContent.append("<div class='modal'><!-- Place at bottom of page --></div>");
		    htmlContent.append("</body>");
		    htmlContent.append("</html>");
	
		    out.println(htmlContent.toString());
		} else {
		    out.println("<html>");
		    out.println("<head><script LANGUAGE='JavaScript1.1'>top.mainFrame.instruct.location.reload();</script></head>");
		    out.println("<body> Setup Survey Failure! </body>");
		    out.println("</html>");
		    out.close();
		}
    }

  
    /**
     * Returns the elements to go inside the progress div
     * 
     * @param 	theUser	The user whose survey page is being displayed.
     * @param 	session	Session under which survey is being displayed.
     * @return	String	html version of the progress bar
     */
    public String getProgressDivContent(User theUser, HttpSession session) {
		StringBuffer progressBar = new StringBuffer("");
		Hashtable<String, String> completedPages = theUser.getCompletedPages();
	
		/* get the interviewer if it is on the interview status */
		Interviewer intv = (Interviewer) session.getAttribute("INTERVIEWER");
	
		/* Interviewer can always browse any pages */
		if (intv != null) {
		    theUser.currentSurvey.allowGoback = true;
		}	
		if (theUser.currentSurvey.allowGoback) {
		    progressBar.append(theUser.currentSurvey
		    		.printProgress(theUser.currentPage));
		} else {
		    progressBar.append(theUser.currentSurvey.printProgress(
		    		theUser.currentPage, completedPages));
		}
		return progressBar.toString();
    }

    /**
     * Returns the html version of current page.
     * 
     * @param 	theUser	The user whose survey page is being displayed.
     * @return	String	html version of current page.
     */
    public String getPageHTML(User theUser) {
		StringBuffer pageHtml = new StringBuffer("");
	
		/* get the output string for the current page */
		String pOutput = theUser.currentPage.renderPage(theUser);
	
		if (pOutput != null && !pOutput.equalsIgnoreCase("")) {
		    pageHtml.append("<script type='text/javascript' language='JavaScript1.1' src='"
				    + SurveyorApplication.sharedFileUrl
				    + "survey.js'></script>");
		    pageHtml.append("<script type='text/javascript' src='"
				    + SurveyorApplication.sharedFileUrl
				    + "jquery-1.7.1.min.js'></script>"
				    + "<script type='text/javascript' language='javascript' SRC='"
				    + SurveyorApplication.sharedFileUrl
				    + "survey_form_values_handler.js'></script>");
		    pageHtml.append(pOutput);
		} else {
			
		    /* redirect to the next page by outputting hidden field values and running JS submit() */
		    pageHtml.append("<form name='mainform' method='post' action='readform'>");
		    pageHtml.append("<input type='hidden' name='action' value=''>");
		    if ((theUser.currentSurvey.isLastPage(theUser.currentPage.id))
		    		|| (theUser.currentPage.finalPage)) {
		    	pageHtml.append("<input type='hidden' name='nextPage' value='DONE'>");
		    } else {
				
		    	/* 
				 * if the id of the next page is not set in the survey xml file
				 * (with value=NONE),
				 * then get its id from the page hash table in the survey class.
				 */
				if (theUser.currentPage.nextPage.equalsIgnoreCase("NONE")) {
				    pageHtml.append("<input type='hidden' name='nextPage' value='"
						    + theUser.currentSurvey
							    .nextPage(theUser.currentPage.id).id
						    + "'>");
				} else {
				    // otherwise, assign the page id directly to the form
				    pageHtml.append("<input type='hidden' name='nextPage' value='"
						    + theUser.currentPage.nextPage + "'>");
			    }
			    pageHtml.append("</form>");
			    pageHtml.append("<script LANGUAGE='JavaScript1.1'>document.mainform.submit();</script>");
		    }
		}
		return pageHtml.toString();
	}
}
