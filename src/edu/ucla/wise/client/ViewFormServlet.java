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
import edu.ucla.wise.commons.WISEApplication;

/**
 * ViewFormServlet displays a single survey page as a form to be filled out.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
@WebServlet("/survey/view_form")
public class ViewFormServlet extends HttpServlet {
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
	
		HttpSession session = req.getSession(true);
		
		// Surveyor_Application s =
		// (Surveyor_Application)session.getAttribute("SurveyorInst");
	
		/* if session is new, then show the session expired info */
		if (session.isNew()) {
		    res.sendRedirect(SurveyorApplication.getInstance().getSharedFileUrl() + "error"
			    + WISEApplication.htmlExt);
		    return;
		}
	
		/* get the user from session */
		User theUser = (User) session.getAttribute("USER");
		if (theUser == null) {
		    out.println("<p>Error: Can't find the user info.</p>");
		    return;
		}
	
		/* check if it is an interview process */
		Interviewer inv = (Interviewer) session.getAttribute("INTERVIEWER");
		if (inv != null) {
		    
			/* get the current page */
		    String pageid = req.getParameter("p");
		    
		    /* set the current page */
		    theUser.setCurrentPage(theUser.getCurrentSurvey().getPage(pageid));
		}
		
		/*
		 * //get the output string of the current page String p_output =
		 * theUser.getCurrentPage().render_page(theUser);
		 * 
		 * // display the current page only if it returns output
		 * if(p_output!=null && !p_output.equalsIgnoreCase("")) {
		 * out.println("<html><head>" + "<link rel='stylesheet' href="+
		 * "'styleRender?css=style.css' type='text/css'>\n"+
		 * "<script type='text/javascript' language='JavaScript1.1' src='"+
		 * Surveyor_Application.shared_file_url +"survey.js'></script>" +
		 * "<script type='text/javascript' src='"
		 * +Surveyor_Application.shared_file_url
		 * +"jquery-1.7.1.min.js'></script>"); out.println(p_output); } //
		 * otherwise, skip the current page else { //redirect to the next page
		 * by outputting hidden field values and running JS submit()
		 * out.println("<html>"); out.println("<head></head>");
		 * out.println("<body>");
		 * out.println("<form name='mainform' method='post' action='readform'>"
		 * ); out.println("<input type='hidden' name='action' value=''>"); if (
		 * (theUser.getCurrentSurvey().is_last_page(theUser.getCurrentPage().id)) ||
		 * (theUser.getCurrentPage().final_page) )
		 * out.println("<input type='hidden' name='nextPage' value='DONE'>");
		 * else { //if the id of the next page is not set in the survey xml file
		 * (with value=NONE), //then get its id from the page hash table in the
		 * survey class. if (
		 * theUser.getCurrentPage().next_page.equalsIgnoreCase("NONE"))
		 * out.println("<input type='hidden' name='nextPage' value='"+
		 * theUser.getCurrentSurvey().next_page(theUser.getCurrentPage().id).id+"'>");
		 * else //otherwise, assign the page id directly to the form
		 * out.println("<input type='hidden' name='nextPage' value='"+
		 * theUser.getCurrentPage().next_page+"'>"); } out.println("</form>");
		 * out.println
		 * ("<script LANGUAGE='JavaScript1.1'>document.mainform.submit();</script>"
		 * ); out.println("</body></html>"); } out.close();
		 */
	
		// Pralav code for printing page
		StringBuffer htmlContent = new StringBuffer("");
	
		// html_content.append("<!DOCTYPE html>");
		htmlContent
				.append("<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/strict.dtd'>");
		htmlContent.append("<html>");
		htmlContent.append("<head>");
		htmlContent
				.append("<title>Web-based Interactive Survey Environment (WISE)</title>");
		htmlContent
				.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>"
				+ "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\"/>"
				+ "<script type='text/javascript' language='javascript' src='"
				+ SurveyorApplication.getInstance().getSharedFileUrl()
				+ "/js/main.js'></script>"
				+ "<script type='text/javascript' language='javascript' SRC='"
				+ SurveyorApplication.getInstance().getSharedFileUrl()
				+ "/js/survey.js'></script>"
				+ "<script type='text/javascript' language='javascript'>"
				+ "	top.fieldVals = null;"
				+ "	top.requiredFields = null;"
				+ "     var userId = "
				+ theUser.getId() + ";" + "</script>");
		htmlContent.append("</head>");
		htmlContent
				.append("<body onload='javascript: setFields();check_preconditions();'>");
		htmlContent.append("<div id = 'progress_bar'>");
		htmlContent.append(getProgressDivContent(theUser, session));
		htmlContent.append("</div>");
		htmlContent.append("<div id='content'>");
		htmlContent.append(getPageHTML(theUser));
		htmlContent.append("</div>");
		htmlContent
				.append("<div class='modal'><!-- Place at bottom of page --></div>");
		htmlContent.append("</body>");
		htmlContent.append("</html>");	
		out.println(htmlContent.toString());
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
		    theUser.getCurrentSurvey().setAllowGoback(true);
		}	
		if (theUser.getCurrentSurvey().isAllowGoback()) {
		    progressBar.append(theUser.getCurrentSurvey()
			    .printProgress(theUser.getCurrentPage()));
		} else {
		    progressBar.append(theUser.getCurrentSurvey().printProgress(
			    theUser.getCurrentPage(), completedPages));
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
		String pOutput = theUser.getCurrentPage().renderPage(theUser);
	
		if (pOutput != null && !pOutput.equalsIgnoreCase("")) {
		    pageHtml.append("<script type='text/javascript' language='JavaScript1.1' src='"
				    + SurveyorApplication.getInstance().getSharedFileUrl()
				    + "/js/survey.js'></script>");
		    pageHtml.append("<script type='text/javascript' src='"
				    + SurveyorApplication.getInstance().getSharedFileUrl()
				    + "../js/jquery-1.7.1.min.js'></script>"
				    + "<script type='text/javascript' language='javascript' SRC='"
				    + SurveyorApplication.getInstance().getSharedFileUrl()
				    + "/js/survey_form_values_handler.js'></script>");
		    pageHtml.append(pOutput);
		} else {
		    
			/* redirect to the next page by outputting hidden field values and running JS submit() */
		    pageHtml.append("<form name='mainform' method='post' action='readform'>");
		    pageHtml.append("<input type='hidden' name='action' value=''>");
		    if ((theUser.getCurrentSurvey().isLastPage(theUser.getCurrentPage().id))
		    		|| (theUser.getCurrentPage().finalPage)) {
				pageHtml.append("<input type='hidden' name='nextPage' value='DONE'>");
		    } else {
				
		    	/*
		    	 * if the id of the next page is not set in the survey xml file
		    	 * (with value=NONE),
				 * then get its id from the page hash table in the survey class.
		    	 */
				if (theUser.getCurrentPage().nextPage.equalsIgnoreCase("NONE")) {
				    pageHtml.append("<input type='hidden' name='nextPage' value='"
						    + theUser.getCurrentSurvey()
							.nextPage(theUser.getCurrentPage().id).id
						    + "'>");
				} else {
				    
					/* otherwise, assign the page id directly to the form */
				    pageHtml.append("<input type='hidden' name='nextPage' value='"
						    + theUser.getCurrentPage().nextPage + "'>");
				}
		    }
		    pageHtml.append("</form>");
		    pageHtml.append("<script LANGUAGE='JavaScript1.1'>document.mainform.submit();</script>");
		}	
		return pageHtml.toString();
    }
}