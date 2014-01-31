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
import edu.ucla.wise.commons.WiseConstants;

/**
 * SurveyResultServlet is a class used when user tries to check the responses 
 * of people who have taken the survey.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
@WebServlet("/admin/survey_result")
public class SurveyResultServlet extends HttpServlet {
   	private static final long serialVersionUID = 1000L;

    /**
     * Checks the validity of the sessions and redirects, so that 
     * results of the question in the first page can be viewed.
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
	
		WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(req);
		/* get the survey ID from request and the admin info object from session */
		String surveyId = parameters.getEncodedSurveyId();
		
		
		AdminUserSession adminUserSession = parameters.getAdminUserSessionFromHttpSession();
	
		/* if the session is invalid, display the error */
		if (adminUserSession == null || surveyId == null) {
		    out.println("Wise Admin - Survey Result Error: Can't get the Admin Info");
		    return;
		}
		
		/* get the selected users and the where clause of the query to select users */
		String whereclauseV = req.getParameter("whereclause");
		String alluserV = req.getParameter("alluser");
		String userV[] = req.getParameterValues("user");
	
		/* initiate the user ID list */
	    String userList = "";
	    
	    /* put each user ID into the list, seperated by comma */
	    if (userV != null) {
			int lastI = userV.length - 1;
			for (int i = 0; i < lastI; i++) {
				userList += userV[i] + ",";
			}	    
			userList += userV[lastI];
	    }
	    
	    /* compose the forwarding URL to review the survey data conducted by the selected users */
	    res.sendRedirect(adminUserSession.getStudyServerPath()
	    		+ WiseConstants.SURVEY_APP + "/admin_view_results?SID="
	    		+ adminUserSession.getStudyId() + "&a=FIRSTPAGE&s=" + surveyId
	    		+ "&whereclause=" + whereclauseV + "&alluser=" + alluserV
	    		+ "&user=" + userList);
	    out.close();
    }
}
