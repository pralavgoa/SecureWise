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
import edu.ucla.wise.commons.UserDBConnection;

/**
 * RepeatingItemSetIOServlet will handle retrieving survey page values sent through AJAX calls
 * currently implemented only for the repeating item set. 
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
@WebServlet("/survey/repeating_item_io")
public class RepeatingItemSetIOServlet extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Gets all the data that is there in the repeating item table and prepares a Json object.
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
		res.setContentType("application/json");
		out = res.getWriter();
		HttpSession session = req.getSession(true);
	
		/*
		 * if session is new, then it must have expired since begin; show the
		 * session expired info
		 */
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
	
		/* get database connection */
		UserDBConnection userDbConnection = theUser.getMyDataBank();
	
		/* get the table name from request */
		String repeatTableName = req.getParameter("repeat_table_name");
	
		/* get the table values as a string */
		String repeatTableValues = userDbConnection
				.getAllDataForRepeatingSet(repeatTableName);
		out.println(repeatTableValues);
    }
}
