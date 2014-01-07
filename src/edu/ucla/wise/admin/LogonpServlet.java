package edu.ucla.wise.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.admin.healthmon.HealthMonitoringManager;
import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.WiseConstants;

/**
 * LogonpServlet is a class which is called when user tries to log into
 * wise admin system, it also sets up AdminInfo in the admin user's session
 * in case the user has logged in with valid credentials.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */

@WebServlet("/admin/logonp")
public class LogonpServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	Logger log = Logger.getLogger(this.getClass());

	/**
	 * Checks if the user has entered proper credentials and also verifies if he is 
	 * blocked and initializes AdminInfo object or redirects to error page accordingly.
	 *  
	 * @param 	request	 HTTP Request.
	 * @param 	response	 HTTP Response.
	 * @throws 	ServletException and IOException. 
	 */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		String path = request.getContextPath();

		String userName = request.getParameter("username");
		String password = request.getParameter("password");

		if(Strings.isNullOrEmpty(userName)){
			response.getWriter().println("Please provide a valid username");
			return;
		}

		if(Strings.isNullOrEmpty(password)){
			response.getWriter().println("Please provide a valid password");
			return;
		}
		
		userName = userName.toLowerCase();

		//HttpSession session = req.getSession(true);
		/* security features changes */
		HttpSession session = request.getSession();
		if(session!=null)
			session.invalidate();
		session = request.getSession(true);
		/* end of security features changes */

		try {

			/* initialize AdminInfo instance and store in session */
			AdminApplication adminInfo = new AdminApplication(userName, password);
			HealthMonitoringManager.monitor(adminInfo);

			/* Verify the username and password */
			if (!adminInfo.pwValid) {

				/* updating number of attempts made to login.*/
				int numberOfAttempts=0;
				if (AdminApplication.getLoginAttemptNumbers().containsKey(userName)) {
					numberOfAttempts = AdminApplication.getLoginAttemptNumbers().get(userName);
				}
				numberOfAttempts++;
				AdminApplication.getLoginAttemptNumbers().put(userName, numberOfAttempts);

				/*checking if the user is blocked*/
				if (isUserBlocked(userName)) {
					log.error("User is blocked due to too many login attempts:"
							+ userName);
					response.sendRedirect(path +"/"+ WiseConstants.ADMIN_APP + "/error.htm");
				} else {
					log.error("Incorrect input: Username or password was entered wrong.");
					response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP
							+ "/error.htm");
				}
			} else {
				if (isUserBlocked(userName)) {
					log.error("User is blocked due to too many login attempts:"
							+ userName);
					response.sendRedirect(path +"/"+ WiseConstants.ADMIN_APP + "/error.htm");
				} else {
					session.setAttribute("ADMIN_INFO", adminInfo);

					/* send HTTP request to create study space and admin user */
					response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP
							+ "/tool.jsp");
					log.info("Admin login Success!");
				}
			}
		} catch (IllegalArgumentException e) {
			log.error("Could not get the parameters for study space "
					+ userName);
			response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP
					+ "/error.htm");
		}
	}

	/**
	 * Returns a boolean value after verifying if user has 
	 * exceeded 5 failed login attempts within last 30 mins
	 * 
	 * @param 	username Username who is trying to login.
	 * @return 	If the user trying to login is blocked or not. 
	 */
	private boolean isUserBlocked(String username) {

		boolean userIsBlocked = false;
		if (AdminApplication.getLoginAttemptNumbers().containsKey(username)) {
			int numberOfAttempts = AdminApplication.getLoginAttemptNumbers()
					.get(username);
			if (numberOfAttempts > 5) {
				if (System.currentTimeMillis()
						- AdminApplication.getLastlogintime().get(username) < (30 * 60 * 1000)) {
					userIsBlocked = true;
				} 
			}
		}

		/* updating the last login time */
		AdminApplication.getLastlogintime().put(username, System.currentTimeMillis());
		return userIsBlocked;
	}
}
