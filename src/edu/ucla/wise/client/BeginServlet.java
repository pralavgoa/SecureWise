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

import edu.ucla.wise.client.web.WiseHttpRequestParameters;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WiseConstants;

/**
 * BeginServlet is a class which is used to direct the user coming 
 * from email URL or interviewers to appropriate next step or page.
 */
@WebServlet("/survey")
public class BeginServlet extends HttpServlet {
	static final long serialVersionUID = 1000;
	Logger log = Logger.getLogger(this.getClass());

	/**
	 * Checks call the passed parameters and initializes the survey for the user. 
	 * Also checks number of users currently accessing the system and
	 * if it more then 100000 new users are not allowed to access the survey.
	 * 
	 * @param 	req	 HTTP Request.
	 * @param 	res	 HTTP Response.
	 * @throws 	ServletException and IOException. 
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException {
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();		

		HttpSession session = createNewSessionForAnonymousUser(req);	

		WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(req);

		String spaceIdEncode = parameters.getEncodedStudySpaceId();
		String msgId = parameters.getEncodedMessageId();
		String surveyIdEncode = parameters.getEncodedSurveyId();

		if (Strings.isNullOrEmpty(spaceIdEncode)) {
			res.sendRedirect(SurveyorApplication.sharedFileUrl
					+ "incorrectUrl"
					+ edu.ucla.wise.commons.SurveyorApplication.htmlExt);
			return;
		}

		/* This is a general email address without any specific invitee
		 * information. Hence, ask the user to enter his details so that 
		 * invitee shall be created.
		 */
		if (Strings.isNullOrEmpty(msgId)) {
			StringBuffer destination = new StringBuffer();
			destination.append("/WISE/survey/").append(
					WiseConstants.NEW_INVITEE_JSP_PAGE);
			if (Strings.isNullOrEmpty(surveyIdEncode)) {
				res.sendRedirect(SurveyorApplication.sharedFileUrl
						+ "link_error"
						+ edu.ucla.wise.commons.SurveyorApplication.htmlExt);
				return;
			}
			res.sendRedirect(destination.toString() + "?s=" + surveyIdEncode
					+ "&t=" + spaceIdEncode);
			return;
		}

		String spaceId;
		User theUser;

		/* decode study space ID */
		spaceId = WISEApplication.decode(spaceIdEncode);

		/* initiate the study space ID and put it into the session */
		session.removeAttribute("STUDYSPACE");
		StudySpace theStudy = StudySpace.getSpace(spaceId);
		session.setAttribute("STUDYSPACE", theStudy);

		if (theStudy == null) {
			res.sendRedirect(SurveyorApplication.sharedFileUrl
					+ "link_error"
					+ edu.ucla.wise.commons.SurveyorApplication.htmlExt);
			return;
		}

		/* get the user ID */
		theUser = (User) session.getAttribute("USER");

		/* create a new User if none is already found in the session */
		if (theUser == null) {
			theUser = theStudy.getUser(msgId);
		}


		/*
		 * might double-check user's validity otherwise but need to write
		 * new fn 'cause all we have is msgid not userid
		 */

		/* if the user can't be retrieved or created, send error info */
		if (theUser == null || theUser.getId() == null) {
			req.getRequestDispatcher("./survey/error_jsps/UserNotIdentified.jsp?studySpaceName="+theStudy.studyName).forward(req, res);
			log.error("WISE Error: Begin servlet failed for message id " + msgId,
					null);
			return;
		}

		/* put the user into the session */
		session.setAttribute("USER", theUser);

		/* checks the URL and redirects to triage servlet */	
		String mainUrl;
		if ((SurveyorApplication.sharedFileUrl != null)
				|| (SurveyorApplication.sharedFileUrl.length() != 0)) {
			mainUrl = "" + SurveyorApplication.sharedFileUrl
					+ "browser_check"
					+ edu.ucla.wise.commons.SurveyorApplication.htmlExt
					+ "?w=" + SurveyorApplication.servletUrl + "start"; // pass
		} else {
			System.err.println("servlet URL is "
					+ SurveyorApplication.servletUrl);
			mainUrl = "file_test/" + "browser_check"
					+ edu.ucla.wise.commons.SurveyorApplication.htmlExt
					+ "?w=" + SurveyorApplication.servletUrl + "start"; // pass
			log.error("Main URL is [" + mainUrl + "]", null);
		}

		out.println("<HTML><HEAD><SCRIPT LANGUAGE=\"JavaScript1.1\">");
		out.println("<!--");
		out.println("top.location.replace('" + mainUrl + "');");
		out.println("// -->");
		out.println("</SCRIPT>");
		out.println("</HEAD>");
		out.println("<frameset rows='1,*' frameborder='NO' border=0 framespacing=0>");
		out.println("<frame name='topFrame' scrolling='NO' noresize src=''>");
		out.println("<frame name='mainFrame' src='"
				+ SurveyorApplication.sharedFileUrl
				+ "error_javascript.htm'>");
		out.println("</frameset><noframes></noframes></HTML>");
		out.close();
	}

	private HttpSession createNewSessionForAnonymousUser(HttpServletRequest request){
		HttpSession session = null;	
		String newSession=request.getParameter("n");	
		/*code for checking if the user is from anonymous survey page or directly from the mail */
		if (!Strings.isNullOrEmpty(newSession)){
			if (newSession.equalsIgnoreCase("true")){
				WISEApplication.logInfo("New session created for anonymous user");
				session = request.getSession();
				session.invalidate();
				session = request.getSession(true);
			}
		} else {
			session = request.getSession(true);
		}
		return session;
	}

	private boolean checkIfInterviewer(){
		/*
		 * TODO: check if the user is actually a interviewer (i=interview)
		 * String interview_begin = req.getParameter("i");
		 * if (interview_begin != null) {
		 * session.setAttribute("INTERVIEW", interview_begin);
		 *
		 * If it is not an interview, but can't get sufficient information,
		 * then the email URL maybe broken into lines.
		 */	
		return false;
	}
}
