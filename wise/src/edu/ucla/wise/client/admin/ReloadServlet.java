package edu.ucla.wise.client.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.initializer.WiseProperties;

/**
 * ReloadServlet class is used to load a new survey and set up its Data tables.
 * (Called via URL request from load.jsp in the admin application)
 * 
 * @author Douglas Bell
 * @version 1.0
 */
@WebServlet("/survey/admin_reload")
public class ReloadServlet extends HttpServlet {
    public static final Logger LOGGER = Logger.getLogger(ReloadServlet.class);
    static final long serialVersionUID = 1000;

    /**
     * Reloads the new survey and sets up its data tables.
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
	/* prepare for writing */
	res.setContentType("text/html");
	PrintWriter out = res.getWriter();

	try {
	    /* Make sure local app is initialized */
	    WiseProperties properties = new WiseProperties("wise.properties",
		    "WISE");
	    SurveyorApplication.reload(req.getContextPath(), properties);
	    out.println("<table border=0>");
	    out.println("<tr><td align=center>SURVEY Application Reload succeeded.</td></tr></table>");
	} catch (IllegalArgumentException e) {
	    out.println(e + "<p> Servlet called: Application Reloader </p>"
		    + SurveyorApplication.initErrorHtmlFoot);
	    LOGGER.error("WISE Surveyor Init Error: " + e, null);
	}

    }

}
