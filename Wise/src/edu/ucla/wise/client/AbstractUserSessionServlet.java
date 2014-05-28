package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WiseConstants;

public abstract class AbstractUserSessionServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AbstractUserSessionServlet.class);

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
        /* prepare for writing */
        PrintWriter out;
        res.setContentType("text/html");
        out = res.getWriter();

        HttpSession session = req.getSession(true);

        if (session.isNew()) {
            res.sendRedirect(SurveyorApplication.getInstance().getSharedFileUrl() + "error"
                    + WiseConstants.HTML_EXTENSION);
            return;
        }

        User theUser = (User) session.getAttribute("USER");

        /* if the user can't be created, send error info */
        if (theUser == null) {
            out.println("<HTML><HEAD><TITLE>Begin Page</TITLE>"
                    + "<LINK href='"
                    + SurveyorApplication.getInstance().getSharedFileUrl()
                    + "style.css' type=text/css rel=stylesheet>"
                    + "<body><center><table>"
                    // + "<body text=#000000 bgColor=#ffffcc><center><table>"
                    + "<tr><td>Error: WISE can't seem to store your identity in the browser. You may have disabled cookies.</td></tr>"
                    + "</table></center></body></html>");
            LOGGER.error("WISE BEGIN - Error: Can't create the user.", null);
            return;
        }

        out.println(this.serviceMethod(theUser, session));
    }

    public abstract String serviceMethod(User user, HttpSession session);

}
