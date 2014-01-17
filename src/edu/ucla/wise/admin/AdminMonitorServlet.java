package edu.ucla.wise.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucla.wise.commons.AdminApplication;

/**
 * AdminMonitorServlet class is used to load a new survey and set up its Data tables.
 * (Called via URL request from load.jsp in the admin application)
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class AdminMonitorServlet extends HttpServlet {
	static final long serialVersionUID = 1000;

	/**
	 * Reloads the new survey and sets up its data tables.
	 *  
	 * @param 	req	 HTTP Request.
	 * @param 	res	 HTTP Response.
	 * @throws 	ServletException and IOException. 
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		/* prepare for writing */
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();

		out.println("<HTML><HEAD><TITLE>WISE Admin Reloader</TITLE>"
				+ "<LINK href='../file_product/style.css' type=text/css rel=stylesheet>"
				+ "<body text=#000000 bgColor=#ffffcc><center><table>");
		out.println("<tr><td align=center>WISE Admin Application Currently being used by:");
		String theList = AdminUserSession.listAdminsOnNow();
		if (theList.equals(""))
			out.println("Nobody");
		else
			out.println(theList);
		out.println("</td></tr></table>");
		out.println("</table></center></body></html>");
	}

}
