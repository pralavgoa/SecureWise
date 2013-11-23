package edu.ucla.wise.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WiseConstants;

/**
 * ViewSurveyServlet is a class used when user tries to check  
 * the survey form wise admin system.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
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
		
		/* Make sure local app is initialized */
		String initErr = SurveyorApplication.checkInit(req.getContextPath());
		if (initErr != null) {
		    out.println("<HTML><HEAD><TITLE>WISE survey system -- Can't identify you</TITLE>"
		    		+ "<LINK href='../file_product/style.css' type=text/css rel=stylesheet>"
		    		+ "<body text=#000000 bgColor=#ffffcc><center><table>"
		    		+ "<tr><td>Sorry, the WISE Surveyor application failed to initialize. "
		    		+ "Please contact the system administrator with the following information."
		    		+ "<P>"
		    		+ initErr
		    		+ "</td></tr>"
		    		+ "</table></center></body></html>");
		    WISEApplication.logError("WISE Surveyor Init Error: " + initErr,
		    		null);
		    return;
		}
		HttpSession session = req.getSession(true);
		
		String surveyId = req.getParameter("s");
			 
	    if(SanityCheck.sanityCheck(surveyId)){
	    	String path = req.getContextPath() + "/" + WiseConstants.ADMIN_APP;
			res.sendRedirect(path + "/sanity_error.html");
		    return;
	    }
	    surveyId=SanityCheck.onlyAlphaNumeric(surveyId);
		
		AdminApplication adminInfo = (AdminApplication) session.getAttribute("ADMIN_INFO");
		
		/* check if the session is still valid */
		if (adminInfo == null || surveyId == null) {
		    out.println("Wise Admin - View Survey Error: Can't get the Admin Info");
		    return;
		}
	
		/* Changing URL pattern */
	    String newUrl = adminInfo.getStudyServerPath()
	    		+ WiseConstants.ADMIN_APP + "/admin_view_form?SID="
	    		+ adminInfo.studyId + "&a=FIRSTPAGE&s=" + surveyId;
	    res.sendRedirect(newUrl);
		out.close();
    }
}
