package edu.ucla.wise.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.initializer.WiseProperties;

/**
 * CompleteServlet Class is used to accept a hidden request from LOFTS
 * to declare completion.
 * 
 * @author Ka Cheung Sia
 * @version 1.0  
 */

@WebServlet("/admin/complete")
public class CompleteServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void service(HttpServletRequest req, HttpServletResponse res)
    		throws ServletException, IOException {
	PrintWriter out;
	PreparedStatement stmt = null;
	Connection conn = null;
	res.setContentType("text/html");
	out = res.getWriter();

	String userID = req.getParameter("u");
	String surveyID = req.getParameter("si");
	String studySpaceID = req.getParameter("ss");

	if (userID != null && surveyID != null & studySpaceID != null) {
		WiseProperties properties = new WiseProperties("wise.properties","WISE");
	    AdminApplication.checkInit(req.getContextPath(), properties);
	    String user = WISEApplication.decode(userID);
	    String ss = WISEApplication.decode(studySpaceID);
	    StudySpace studySpace = StudySpace.getSpace(ss);

	    try {
	    	String sql = "update survey_user_state set state='completed', state_count=1, entry_time=now()" +
					" where invitee= ? AND survey= ?";
			conn = studySpace.getDBConnection();
			stmt = conn.prepareStatement(sql);
			int userId = Integer.parseInt(user);
			stmt.setInt(1, userId);
			stmt.setString(2, surveyID);
			stmt.executeUpdate();
	    } catch (NumberFormatException e) {
	    	e.printStackTrace();
	    } catch (SQLException e) {
	    	e.printStackTrace();
	    } finally {
			if (stmt != null) {
			    try {
				stmt.close();
			    } catch (SQLException e) {
			    }
			}
			if (conn != null) {
			    try {
				conn.close();
			    } catch (SQLException e) {
			    }
			}
	    }
	}
	out.println("OK");
	out.close();
	return;
    }
}