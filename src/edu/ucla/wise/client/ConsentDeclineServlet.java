package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;

/**
 * ConsentDeclineServlet is a class used to record declining consent reason.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
@WebServlet("/survey/consent_decline")
public class ConsentDeclineServlet extends HttpServlet {
    static final long serialVersionUID = 1000;
    
    /**
     * Saves the reason into the database for declining the survey by a user.
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
		
		//Surveyor_Application s = (Surveyor_Application) session
		//		.getAttribute("SurveyorInst");
	
		/* if session is new, then show the session expired info */
		if (session.isNew()) {
		    res.sendRedirect(SurveyorApplication.sharedFileUrl + "error"
			    + SurveyorApplication.htmlExt);
		    return;
		}
	
		/* get the user from session */
		User theUser = (User) session.getAttribute("USER");
		if (theUser == null) {
		    out.println("<p>Error: Can't find the user info.</p>");
		    return;
		}
	
		/* save the decline comments */
		theUser.setDeclineReason(req.getParameter("reason"));
	
		/* then show the thank you page to user */
		String newPage = SurveyorApplication.sharedFileUrl + "decline_thanks"
				+ SurveyorApplication.htmlExt;
		out.println("<html><head>");
		out.println("<script LANGUAGE='JavaScript1.1'>");
		out.println("top.location.replace('" + newPage + "');");
		out.println("</script></head>");
		out.println("<body></body>");
		out.println("</html>");
		out.close();
    }
}
