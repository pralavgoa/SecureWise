package edu.ucla.wise.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.WiseConstants;

/**
 * LogoutServlet is a class which is called when admin user clicks on logout button,
 * wise logs out of the admin page and goes back to log in page.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */

@WebServlet("/admin/logout")
public class LogoutServlet extends HttpServlet {
    
	private static final long serialVersionUID = 1L;
	
	/**
	 * Invalidates the user session and redirects to log in page.
	 *   
     * @param 	req	 HTTP Request.
     * @param 	res  HTTP Response.
     * @throws 	ServletException and IOException. 
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res)
    		throws ServletException, IOException {
		HttpSession session = req.getSession(true);
	
		/* prepare for writing */
		//PrintWriter out;
		//out = res.getWriter();
		res.setContentType("text/html");
		session.invalidate();
		res.sendRedirect(req.getContextPath() + "/" + WiseConstants.ADMIN_APP
				+ "/index.html");
    }
}
