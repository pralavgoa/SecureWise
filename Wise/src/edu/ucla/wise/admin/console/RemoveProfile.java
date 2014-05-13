package edu.ucla.wise.admin.console;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.admin.AdminUserSession;
import edu.ucla.wise.admin.web.AdminSessionServlet;
import freemarker.template.TemplateException;

public class RemoveProfile extends AdminSessionServlet {

    private static final Logger LOGGER = Logger.getLogger(RemoveProfile.class);

    @Override
    public void getMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException, TemplateException {

        HttpSession session = request.getSession();
        PrintWriter out = response.getWriter();

        String[] interviewer = (String[]) session.getAttribute("INVLIST");

        if (interviewer != null) {

            String output = adminUserSession.getMyStudySpace().removeProfile(interviewer);

        } else {
            out.println("Can not get the interviewer list that is ready to remove");
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
