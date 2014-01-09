package edu.ucla.wise.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.initializer.WiseProperties;

/**
 * ReloadServlet class is used to load a new survey and set up its Data tables.
 * (Called via URL request from load.jsp in the admin application)
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
@WebServlet("/admin/reload")
public class ReloadServlet extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Reloads the new survey and sets up its data tables.
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
		
		/* check if the session is still valid */
		AdminApplication adminInfo = (AdminApplication) session.getAttribute("ADMIN_INFO");
		if (adminInfo == null) {
		    out.println("Wise Admin - Reload Error: Can't get your Admin Info");
		    return;
		}
		WiseProperties properties = new WiseProperties("wise.properties","WISE");
		String initErr = AdminApplication.forceInit(req.getContextPath(), properties);
		out.println("<HTML><HEAD><TITLE>WISE Admin Reloader</TITLE>"
				+ "<LINK href='../file_product/style.css' type=text/css rel=stylesheet>"
				+ "<body text=#000000 bgColor=#ffffcc><center><table>");
		if (initErr != null) {
		    out.println("<tr><td>Sorry, the WISE Administration application failed to initialize. "
		    		+ "Please contact the system administrator with the following information."
		    		+ "<P>" + initErr + "</td></tr>");
		    WISEApplication.logError("WISE Admin Init Error: " + initErr,
		    		null);
		} else {
			out.println("<tr><td align=center>WISE Admin Application Reload succeeded.</td></tr></table>");
		}		    
		out.println("</table></center></body></html>");
    }
}
