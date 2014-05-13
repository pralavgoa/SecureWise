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

public class ReassignWati extends AdminSessionServlet {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ReassignWati.class);

    @Override
    public void getMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException, TemplateException {
        String path = request.getContextPath();
        HttpSession session = request.getSession();
        PrintWriter out = response.getWriter();

        String interviewerId = (String) session.getAttribute("INTERVIEWER_ID");
        String surveyId = (String) session.getAttribute("SURVEY_ID");
        session.removeAttribute("INTERVIEWER_ID");
        session.removeAttribute("SURVEY_ID");

        String inviteeReassign[] = request.getParameterValues("inviteereassign");
        String inviteePending[] = request.getParameterValues("inviteepend");

        String output = adminUserSession.getMyStudySpace().reassignWati(inviteePending, inviteeReassign, interviewerId,
                surveyId, request.getParameterMap());

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
