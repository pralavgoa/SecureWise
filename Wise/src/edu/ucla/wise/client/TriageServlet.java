package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.commons.Survey;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WiseConstants.STATES;

/**
 * TriageServlet is used to direct the user after browser check to appropriate
 * next step or page.
 * 
 * @author Douglas Bell
 * @version 1.0
 */
@WebServlet("/survey/start")
public class TriageServlet extends HttpServlet {
    public static final Logger LOGGER = Logger.getLogger(TriageServlet.class);
    static final long serialVersionUID = 1000;

    /**
     * Replaces the current page with the new page.
     * 
     * @param newPage
     *            Url of the new page
     * @return String html to replace the current page.
     */
    public String pageReplaceHtml(String newPage) {
	return "<html>" + "<head><script LANGUAGE='javascript'>"
		+ "top.location.replace('" + newPage + "');"
		+ "</script></head>" + "<body></body>" + "</html>";
    }

    /**
     * Forwards the user to correct page based on his status which could be a
     * new or a returning user.
     * 
     * @param req
     *            HTTP Request.
     * @param res
     *            HTTP Response.
     * @throws ServletException
     *             and IOException.
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
	    res.sendRedirect(SurveyorApplication.getInstance()
		    .getSharedFileUrl() + "error" + SurveyorApplication.htmlExt);
	    return;
	}

	User theUser = (User) session.getAttribute("USER");

	/* if the user can't be created, send error info */
	if (theUser == null) {
	    out.println("<HTML><HEAD><TITLE>Begin Page</TITLE>"
		    + "<LINK href='"
		    + SurveyorApplication.getInstance().getSharedFileUrl()
		    + "style.css' type=text/css rel=stylesheet>"
		    + "<body><center><table>"
		    // + "<body text=#000000 bgColor=#ffffcc><center><table>"
		    + "<tr><td>Error: WISE can't seem to store your identity in the browser. You may have disabled cookies.</td></tr>"
		    + "</table></center></body></html>");
	    LOGGER.error("WISE BEGIN - Error: Can't create the user.", null);
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

		/*
		 * then IS an interview, always direct interviewer to the survey
		 * page. This previously *just* recorded the current page in the
		 * db; not sure why if interviewing and done
		 */
		mainUrl = SurveyorApplication.getInstance().getServletUrl()
			+ "setup_survey";
	    } else {

		/*
		 * not an interview forward to another application's URL, if
		 * specified in survey xml file.
		 */
		Survey currentSurvey = theUser.getCurrentSurvey();
		if ((currentSurvey.getForwardUrl() != null)
			&& !currentSurvey.getForwardUrl().equalsIgnoreCase("")) {
		    mainUrl = currentSurvey.getForwardUrl();

		    /*
		     * if an educational module ID is specified in the survey
		     * xml, then add it to the URL
		     */
		    if (!Strings.isNullOrEmpty(currentSurvey.getEduModule())) {
			mainUrl += "/"
				+ currentSurvey.getStudySpace().dirName
				+ "/survey?t="
				+ WISEApplication.encode(currentSurvey
					.getEduModule()) + "&r="
				+ WISEApplication.encode(theUser.getId());
		    } else {

			/*
			 * otherwise the link will be the URL plus the user ID
			 * Added Study Space ID and Survey ID, was sending just
			 * the UserID earlier
			 */
			mainUrl = mainUrl
				+ "?s="
				+ WISEApplication.encode(theUser.getId())
				+ "&si="
				+ currentSurvey.getId()
				+ "&ss="
				+ WISEApplication.encode(currentSurvey
					.getStudySpace().id);
		    }
		} else if (currentSurvey.getMinCompleters() == -1) {

		    /*
		     * if the min completers is not set in survey xml, then
		     * direct to Thank You page
		     */
		    mainUrl = SurveyorApplication.getInstance()
			    .getSharedFileUrl() + "thank_you";
		} else if (currentSurvey.getMinCompleters() != -1) {

		    /*
		     * this link may come from the invitation email for results
		     * review or user reclicked the old invitation link check if
		     * the number of completers has reached the minimum number
		     * set in survey xml, then redirect the user to the review
		     * result page
		     */
		    if (theUser.checkCompletionNumber() < currentSurvey
			    .getMinCompleters()) {
			mainUrl = SurveyorApplication.getInstance()
				.getSharedFileUrl()
				+ "thank_you"
				+ "?review=false";
		    } else {
			mainUrl = SurveyorApplication.getInstance()
				.getServletUrl() + "view_results";
		    }
		}
	    }
	} else if (theUser.startedSurvey()) {

	    /*
	     * for either user or interviewer, redirect to start the current
	     * page.
	     */
	    mainUrl = SurveyorApplication.getInstance().getServletUrl()
		    + "setup_survey";
	} else {

	    /* forward to the welcome page */
	    // main_url =
	    // WISE_Application.retrieveAppInstance(session).servlet_url +
	    // "welcome_generate";
	    mainUrl = SurveyorApplication.getInstance().getServletUrl()
		    + "welcome";
	}

	/* output javascript to forward */
	out.println(this.pageReplaceHtml(mainUrl));
	out.close();
    }
}