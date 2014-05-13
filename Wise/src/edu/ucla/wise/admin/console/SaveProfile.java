package edu.ucla.wise.admin.console;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.admin.AdminUserSession;
import edu.ucla.wise.admin.web.AdminSessionServlet;
import edu.ucla.wise.client.interview.InterviewManager;
import edu.ucla.wise.commons.Interviewer;
import freemarker.template.TemplateException;

public class SaveProfile extends AdminSessionServlet {

    private static final Logger LOGGER = Logger.getLogger(SaveProfile.class);

    @Override
    public void getMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException, TemplateException {

        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();

        // get the path
        String path = request.getContextPath();
        String newId = null;

        Interviewer[] inv = (Interviewer[]) session.getAttribute("INTERVIEWER");
        if (inv == null) {
            response.sendRedirect(path + "/error_pages/error.htm");
            return;
        }

        if (inv != null) {
            if (session.getAttribute("EditType") != null) {

                String userName = request.getParameter("username_" + inv[0].getId());
                String firstName = request.getParameter("firstname_" + inv[0].getId()).toLowerCase();
                String lastName = request.getParameter("lastname_" + inv[0].getId()).toLowerCase();
                String salutation = request.getParameter("salutation_" + inv[0].getId());
                String email = request.getParameter("email_" + inv[0].getId());

                inv[0] = new Interviewer(adminUserSession.getMyStudySpace(), newId, userName, email, firstName,
                        lastName, salutation, "" + System.currentTimeMillis());
                newId = InterviewManager.getInstance().addInterviewer(adminUserSession.getMyStudySpace(), inv[0]);

                // remove the session attributes
                session.removeAttribute("EditType");

            } else {
                for (int i = 0; i < inv.length; i++) {
                    String userName = request.getParameter("username_" + inv[0].getId());
                    String firstName = request.getParameter("firstname_" + inv[0].getId()).toLowerCase();
                    String lastName = request.getParameter("lastname_" + inv[0].getId()).toLowerCase();
                    String salutation = request.getParameter("salutation_" + inv[0].getId());
                    String email = request.getParameter("email_" + inv[0].getId());

                    inv[i] = new Interviewer(adminUserSession.getMyStudySpace(), newId, userName, email, firstName,
                            lastName, salutation, "" + System.currentTimeMillis());

                    // record the changes of profile
                    newId = InterviewManager.getInstance().saveProfile(adminUserSession.getMyStudySpace(), inv[i]);

                }
            }
        } else {
            out.println("Error - can not get the interviewer object.");
        }
        if (newId != null) {
            out.println("Record Added/Updated successfully!");
        } else {
            out.println("Record Add/Update failed!</td>");
        }

    }

    @Override
    public void postMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException {
        // do nothing
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

}
