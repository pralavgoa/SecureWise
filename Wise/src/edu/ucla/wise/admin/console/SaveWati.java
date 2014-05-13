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

public class SaveWati extends AdminSessionServlet {

    private static final Logger LOGGER = Logger.getLogger(SaveWati.class);
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void getMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException, TemplateException {

        HttpSession session = request.getSession();
        PrintWriter out = response.getWriter();
        // get the path
        String path = request.getContextPath();

        String surveyId = request.getParameter("survey");
        String interviewerId = request.getParameter("interviewer");
        if ((interviewerId == null) || interviewerId.equals("")) {
            out.write("<p>");
            out.write("Error: You must select one interviewer");
            out.write("</p>");
            return;
        }
        session.setAttribute("SURVEY_ID", surveyId);
        session.setAttribute("INTERVIEWER_ID", interviewerId);

        String url = null;// adminUserSession.study_server+
        // "file_test/interview/Show_Assignment.jsp?SID="+adminUserSession.study_id+"&InterviewerID="+interviewer_id;

        String whereStr = request.getParameter("whereclause");
        if ((whereStr == null) || whereStr.equals("")) {
            String allUser = request.getParameter("alluser");
            if ((allUser == null) || allUser.equals("")) {
                String nonResp = request.getParameter("nonresp");
                if ((nonResp == null) || nonResp.equals("")) {
                    String user[] = request.getParameterValues("user");
                    whereStr = "id in (";
                    for (int i = 0; i < user.length; i++) {
                        whereStr += user[i] + ",";
                    }
                    whereStr = whereStr.substring(0, whereStr.lastIndexOf(',')) + ")";
                } else {
                    whereStr = "id not in (select distinct invitee from survey_subject)";
                }
            } else {
                whereStr = "id in (select distinct id from invitee)";
            }
        }

        String output = adminUserSession.getMyStudySpace().saveWati(whereStr, interviewerId, surveyId);

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
