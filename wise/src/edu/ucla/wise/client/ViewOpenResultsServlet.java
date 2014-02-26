package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.Page;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.Survey;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;

/**
 * ViewOpenResultsServlet creates a summary report for each individual open
 * question
 * 
 * @author Douglas Bell
 * @version 1.0
 */

@WebServlet("/survey/view_open_results")
public class ViewOpenResultsServlet extends HttpServlet {
    public static final Logger LOGGER = Logger
	    .getLogger(ViewOpenResultsServlet.class);
    static final long serialVersionUID = 1000;

    /**
     * Displays results of an open question while viewing the results from wise
     * admin page.
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

	/* prepare to write */
	PrintWriter out;
	res.setContentType("text/html");
	out = res.getWriter();
	String path = req.getContextPath();

	HttpSession session = req.getSession(true);

	// Surveyor_Application s = (Surveyor_Application) session
	// .getAttribute("SurveyorInst");

	/* if session is new, then show the session expired info */
	if (session.isNew()) {
	    res.sendRedirect(SurveyorApplication.getInstance()
		    .getSharedFileUrl() + "error" + SurveyorApplication.htmlExt);
	    return;
	}

	/* get the user or the user group whose results will get presented */
	String whereclause = (String) session.getAttribute("WHERECLAUSE");
	if (whereclause == null) {
	    whereclause = "";
	}

	/* get the unanswered question number */
	String unanswered = req.getParameter("u");

	/* get the question id */
	String question = req.getParameter("q");

	/* get the page id */
	String page = req.getParameter("t");

	if (SanityCheck.sanityCheck(unanswered)
		|| SanityCheck.sanityCheck(question)
		|| SanityCheck.sanityCheck(page)) {
	    res.sendRedirect(path + "/admin/sanity_error.html");
	    return;
	}
	unanswered = SanityCheck.onlyAlphaNumeric(unanswered);
	question = SanityCheck.onlyAlphaNumeric(question);
	page = SanityCheck.onlyAlphaNumeric(page);

	StudySpace studySpace;
	Survey survey;

	/* get the user from session */
	User theUser = (User) session.getAttribute("USER");
	Survey currentSurvey = theUser.getCurrentSurvey();
	if (theUser == null) {

	    /* theUser is null means this view came from admin */
	    studySpace = (StudySpace) session.getAttribute("STUDYSPACE");
	    survey = (Survey) session.getAttribute("SURVEY");
	} else {
	    studySpace = currentSurvey.getStudySpace();
	    survey = currentSurvey;
	}

	/* get the question stem */
	String qStem = "";
	Page pg = survey.getPage(page);
	if (pg != null) {
	    qStem = pg.title;
	}

	// find the question stem
	// for(int i=0; i<pg.items.length; i++)
	// {
	// if(pg.items[i].name!=null &&
	// pg.items[i].name.equalsIgnoreCase(question))
	// {
	// Question theQ = (Question) pg.items[i];
	// q_stem = theQ.stem;
	// break;
	// }
	// }

	/* display the report */
	out.println("<html><head>");
	out.println("<title>VIEW RESULTS - QUESTION:" + question.toUpperCase()
		+ "</title>");
	out.println("<LINK href='"
		+ SurveyorApplication.getInstance().getSharedFileUrl()
		+ "style.css' rel=stylesheet>");
	out.println("<style>");
	out.println(".tth {	border-color: #CC9933;}");
	out.println(".sfon{	font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 8pt; font-weight: bold; color: #996633;}");
	out.println("</style>");
	out.println("<script type='text/javascript' language='javascript' src=''></script>");
	out.println("</head><body text=#333333><center>");
	// out.println("</head><body text=#333333 bgcolor=#FFFFCC><center>");
	out.println("<table class=tth border=1 cellpadding=2 cellspacing=2 bgcolor=#FFFFF5>");
	out.println("<tr bgcolor=#BA5D5D>");
	out.println("<td align=left><font color=white>");
	out.println("<b>Question:</b> " + qStem + " <font size=-2><i>("
		+ question + ")</i></font>");
	out.println("</font>");
	out.println("</tr><tr>");
	out.println("<th width=200 class=sfon align=left><b>Answer:</b></th>");
	out.println("</tr>");

	try {

	    /* open database connection */
	    // TODO: Change to prepared Statement.
	    Connection conn = studySpace.getDBConnection();
	    Statement stmt = conn.createStatement();

	    if (page != null) {

		/*
		 * get all the answers from data table regarding to this
		 * question
		 */
		String sql = "select invitee, firstname, lastname, status, "
			+ question + " from " + survey.getId()
			+ "_data, invitee where ";
		sql += "id=invitee and (status not in (";

		for (int k = 0; k < survey.getPages().length; k++) {
		    if (!page.equalsIgnoreCase(survey.getPages()[k].id)) {
			sql += "'" + survey.getPages()[k].id + "', ";
		    } else {
			break;
		    }
		}
		sql += "'" + page + "') or status is null) and " + question
			+ " is not null and " + question + " !=''";
		if (!whereclause.equalsIgnoreCase("")) {
		    sql += " and " + whereclause;
		}

		stmt.execute(sql);
		ResultSet rs = stmt.getResultSet();

		String text;
		while (rs.next()) {
		    text = rs.getString(question);
		    if ((text == null) || text.equalsIgnoreCase("")) {
			text = "null";
		    }
		    out.println("<tr>");
		    out.println("<td align=left>" + text + "</td>");
		    out.println("</tr>");
		}
	    } // end of if

	    /* display unanswered question number */
	    if ((unanswered != null) && !unanswered.equalsIgnoreCase("")) {
		out.println("<tr><td align=left>Number of unanswered:"
			+ unanswered + "</td></tr>");
		out.println("</table></center><br><br>");
	    }
	    stmt.close();
	    conn.close();
	} catch (SQLException e) {
	    LOGGER.error("WISE - VIEW OPEN RESULT: " + e.toString(), null);
	    return;
	}
	out.println("<center><a href='javascript: history.go(-1)'>");
	out.println("<img src='" + "imageRender?img=back.gif' /></a></center>");
	out.close();
    }
}
