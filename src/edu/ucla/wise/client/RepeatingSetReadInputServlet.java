package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.UserDBConnection;

/**
 * RepeatingSetReadInputServlet will handle saving survey page values sent through AJAX calls
 * currently implemented only for the repeating item set. 
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
@WebServlet("/survey/repeating_set_read_input")
public class RepeatingSetReadInputServlet extends HttpServlet {
    static final long serialVersionUID = 1000;
    static Logger log = Logger.getLogger(RepeatingSetReadInputServlet.class);

    /**
     * saves the data into repeating item set tables.
     * 
     * @param 	req	 HTTP Request.
     * @param 	res	 HTTP Response.
     * @throws 	ServletException and IOException. 
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) 
    		throws ServletException, IOException{
		try {
			
			/* prepare for writing */
			PrintWriter out;
			res.setContentType("text/html");
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
			if (theUser == null || theUser.getId() == null) {
				/* latter signals an improperly-initialized User */
			    out.println("FAILURE");
			    return;
			}
		
			String repeatTableName = req.getParameter("repeat_table_name");
			String repeatTableRow = req.getParameter("repeat_table_row");
		
			if (!Strings.isNullOrEmpty(repeatTableRow)) {
			    if ("null".equals(repeatTableRow)) {
			    	repeatTableRow = null;
			    }
			}
		
			String repeatTableRowName = req
					.getParameter("repeat_table_row_name");
			
			/* get all the fields values from the request and save them in the hash table */
			Hashtable<String, String> params = new Hashtable<String, String>();
			Hashtable<String, String> types = new Hashtable<String, String>();
		
			String name, value;
			Enumeration e = req.getParameterNames();
			while (e.hasMoreElements()) {
			    name = (String) e.nextElement();
			    value = req.getParameter(name);
			    if (!name.contains("repeat_table_name")
			    		&& !name.contains("repeat_table_row")
			    		&& !name.contains("repeat_table_row_name")) {
				
			    	/* 
			    	 * Parse out the proper name here 
			    	 * here split the value into its constituents
			    	 */

					String[] typeAndValue = value.split(":::");
					if (typeAndValue.length == 2) {
					    params.put(name, typeAndValue[1]);
					    types.put(name, typeAndValue[0]);
					} else {
					    if (typeAndValue.length == 1) {
						params.put(name, "");
						types.put(name, typeAndValue[0]);
					    }
			
					}
			    } else {
				;// do nothing
			    }
			}
		
			int generatedKeyValue = putValuesInDatabase(repeatTableName,
					repeatTableRow,
					repeatTableRowName, theUser, params, types);
			out.print(generatedKeyValue);
			out.flush();
			out.close();
		} catch (NullPointerException e) {
		    log.error(e);
		} catch (PatternSyntaxException e) {
			log.error(e);
		}
	
	}
    
    /**
     * Saves the data from the repeating item set questions into the database
     * by calling method from user DB connection. 
     * 
     * @param tableName		Repeating item set table name.
     * @param rowId			Row id to which data has to be stored.
     * @param rowName		Row name 
     * @param theUser		User's object whose data has to be saved.
     * @param params		Answers for the repeating item set.
     * @param paramTypes	Types of the repeating item set table columns.
     * @return int			returns the inserted key.
     */	
	private int putValuesInDatabase(String tableName, String rowId,
		    String rowName, User theUser, Hashtable<String, String> params,
		    Hashtable<String, String> paramTypes) {
	
		/* get database connection */
		UserDBConnection userDbConnection = theUser.getMyDataBank();
	
		/* send the table name and values to the database */
		int insertedKeyValue = userDbConnection
				.insertUpdateRowRepeatingTable(tableName,
						rowId, rowName, params, paramTypes);
	
		return insertedKeyValue;
    }
}
