package edu.ucla.wise.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.client.web.WiseHttpRequestParameters;

/**
 * Dev2ProdServlet class is used for converting the survey system from
 * Development to production mode. Once converted from Development mode to
 * Production mode you cannot come back.
 * 
 * @author Douglas Bell
 * @version 1.0
 */
@WebServlet("/admin/dev2prod")
public class Dev2ProdServlet extends HttpServlet {
    public static final Logger LOGGER = Logger.getLogger(Dev2ProdServlet.class);
    private static final long serialVersionUID = 1L;

    /**
     * Converts the survey system from Development to Production mode.
     * 
     * @param req
     *            HTTP Request.
     * @param res
     *            HTTP Response.
     * @throws ServletException
     *             and IOException.
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res)
	    throws ServletException, IOException {

	WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(
		req);

	/* prepare to write */
	PrintWriter out;
	res.setContentType("text/html");
	out = res.getWriter();

	/* get the server path */
	String path = req.getContextPath();
	out.println("<html><head>");
	out.println("<link rel='stylesheet' href='" + path
		+ "/style.css' type='text/css'>");
	out.println("<title>WISE CHANGE SURVEY MODE</title>");
	out.println("</head><body text=#333333 bgcolor=#FFFFCC>");
	out.println("<center><table cellpadding=2 cellpadding=0 cellspacing=0 border=0>");
	out.println("<tr><td>");
	HttpSession session = parameters.getSession(true);
	if (session.isNew()) {
	    out.println("<h2>Your session has timed out.</h2><p>");
	    out.println("<h3>Please return to the <a href='../'>admin logon page</a> and try again.</h3>");
	    out.println("</td></tr></table></center></body></html>");
	    out.close();
	    return;
	}
	AdminUserSession adminUserSession = parameters
		.getAdminUserSessionFromHttpSession();
	String internalId = parameters.getEncodedStudySpaceId();

	/* if session does not exists */
	if ((adminUserSession == null) || (internalId == null)) {
	    out.println("Wise Admin - Dev to Prod Error: Can't get the Admin Info");
	    return;
	}

	try {

	    /* open database connection */
	    Connection conn = adminUserSession.getDBConnection();

	    out.println("Changing status from DEVELOPMENT to PRODUCTION...<br>");
	    String sql = "SELECT id, filename, title FROM surveys WHERE internal_id = ?";

	    PreparedStatement stmt1 = conn.prepareStatement(sql);
	    stmt1.setInt(1, Integer.parseInt(internalId));
	    ResultSet rs = stmt1.executeQuery();
	    rs.next();
	    String sId = rs.getString(1);
	    String fileName = rs.getString(2);
	    String title = rs.getString(3);

	    sql = "INSERT INTO surveys (id, filename, title, status) ";
	    sql += "VALUES ('" + sId + "','" + fileName + "',\"" + title
		    + "\", 'P')";
	    sql = "INSERT INTO surveys (id, filename, title, status) "
		    + "VALUES(?, ?, ?, ?)";

	    PreparedStatement stmt2 = conn.prepareStatement(sql);
	    stmt2.setString(1, sId);
	    stmt2.setString(2, fileName);
	    stmt2.setString(3, title);
	    stmt2.setString(4, "P");

	    stmt2.executeUpdate();

	    stmt1.close();
	    stmt2.close();
	    conn.close();
	} catch (NumberFormatException e) {
	    LOGGER.error("Wise Admin - Dev to Prod Error: " + e.toString(), e);
	    return;
	} catch (SQLException e) {
	    LOGGER.error("Wise Admin - Dev to Prod Error: " + e.toString(), e);
	    return;
	}
	out.println("<p><a href='../tool.jsp'>Return to Administration Tools</a>");
	out.println("</td></tr></table></center></body></html>");
	out.close();
    }

}
