package edu.ucla.wise.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.client.web.WiseHttpRequestParameters;
import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.WiseConstants;

/**
 * ViewSurveyServlet is a class used when user tries to check  
 * the survey form wise admin system.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
@WebServlet("/admin/view_survey")
public class ViewSurveyServlet extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Checks the validity of the sessions and the parameters and 
     * redirects, so that first page can be viewed.
     *  
     * @param 	req	 HTTP Request.
     * @param 	res	 HTTP Response.
     * @throws 	ServletException and IOException. 
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res)
    		throws ServletException, IOException {
		
    	/* prepare for writing */
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		HttpSession session = req.getSession(true);
		
		WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(req);
		/* get the survey ID from request and the admin info object from session */
		String surveyId = parameters.getEncodedSurveyId();
		
		AdminUserSession adminUserSession = parameters.getAdminUserSessionFromHttpSession();
		
		/* check if the session is still valid */
		if (adminUserSession == null || surveyId == null) {
		    out.println("Wise Admin - View Survey Error: Can't get the Admin Info");
		    return;
		}
	
		/* Changing URL pattern */
	    String newUrl = adminUserSession.getStudyServerPath()
	    		+ WiseConstants.ADMIN_APP + "/admin_view_form?SID="
	    		+ adminUserSession.getStudyId() + "&a=FIRSTPAGE&s=" + surveyId;
	    res.sendRedirect(newUrl);
		out.close();
    }
}
