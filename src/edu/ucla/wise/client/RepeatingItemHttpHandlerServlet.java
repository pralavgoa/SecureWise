package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.UserDBConnection;

/**
 * RepeatingItemHttpHandlerServlet will handle deleting a repeating item from the database.
 * 
 * Sample request expected ?request_type=DELETE&table_name=repeat_set_project&instance_name=hi&invitee_id=31
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
@WebServlet("/survey/repeating_set_control")
public class RepeatingItemHttpHandlerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    public static final String DELETE_REQUEST = "DELETE";
    public static final String REQUEST_TYPE = "request_type";
    public static final String TABLE_NAME = "table_name";
    public static final String INSTANCE_NAME = "instance_name";
    public static final String INVITEE_ID = "invitee_id";

    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";

    /**
     * Deletes a repeating item set from repeating item table.
     * 
     * @param 	req	 HTTP Request.
     * @param 	res	 HTTP Response.
     * @throws 	ServletException and IOException. 
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res)
    		throws ServletException, IOException {
		res.setContentType("text");
		PrintWriter out = res.getWriter();

		User user = BrowserRequestChecker.getUserFromSession(req, res, out);
		if (user == null) {
		    out.close();
		    return;
		}
	
		String requestType = req.getParameter(REQUEST_TYPE);
		if (DELETE_REQUEST.equalsIgnoreCase(requestType)) {
	
		    String inviteeId = req.getParameter(INVITEE_ID);
		    String tableName = req.getParameter(TABLE_NAME);
		    String instanceName = req.getParameter(INSTANCE_NAME);
	
		    /* get database connection */
		    UserDBConnection userDbConnection = user.getMyDataBank();	
		    if (userDbConnection.deleteRowFromTable(inviteeId, tableName,
		    		instanceName)) {
		    	out.println(SUCCESS);
		    } else {
		    	out.println(FAILURE);
		    }
	
		} else {
		    out.println("Please specify a request type");
		    return;
		}	
		out.close();
    }
}
