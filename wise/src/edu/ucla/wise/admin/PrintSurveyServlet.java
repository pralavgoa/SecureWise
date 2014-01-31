package edu.ucla.wise.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.client.web.WiseHttpRequestParameters;
import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.WiseConstants;

/**
 * PrintSurveyServlet is a class used when user tries to print 
 * the survey form wise admin system.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */

@WebServlet("/admin/print_survey")
public class PrintSurveyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    Logger log = Logger.getLogger(PrintSurveyServlet.class);

    /**
     * Checks the validity of the sessions and the parameters and 
     * redirects, so that first page can be printable
     *  
     * @param 	req	 HTTP Request.
     * @param 	res	 HTTP Response.
     * @throws 	ServletException and IOException. 
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) {
		log.info("Admin print survey called");
		WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(req);
		try {
		    
			/* prepare for writing */
		    PrintWriter out;
		    res.setContentType("text/html");
		    out = res.getWriter();
		    HttpSession session = req.getSession(true);
		    
		    String surveyId = parameters.getEncodedSurveyId();
		    String path = req.getContextPath() + "/" + WiseConstants.ADMIN_APP;
		    if (surveyId==null || surveyId.isEmpty()) {
				res.sendRedirect(path + "/admin/parameters_error.html");
				return;
			}
					    
		    AdminUserSession adminUserSession = parameters.getAdminUserSessionFromHttpSession();
		    
		    /* check if the session is still valid */
		    if (adminUserSession == null) {
				out.println("Wise Admin - Print Survey Error: Can't get the Admin Info");
				return;
		    }
		    	
		    /* Changing the URL pattern */
		    String newUrl = adminUserSession.getStudyServerPath() + "/"
		    		+ WiseConstants.ADMIN_APP + "/" + "admin_print_survey?SID="
		    		+ adminUserSession.getStudyId() + "&a=FIRSTPAGE&s=" + surveyId;
		    log.error("The URL built is: "+newUrl);
		    res.sendRedirect(newUrl);
		    out.close();
		} catch (IOException e) {
		    log.error("IO Exception  while printing survey", e);
		}
    }
}
