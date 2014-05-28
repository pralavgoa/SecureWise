package edu.ucla.wise.admin.console;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.ucla.wise.admin.AdminUserSession;
import edu.ucla.wise.admin.web.AdminSessionServlet;
import edu.ucla.wise.client.web.TemplateUtils;
import freemarker.template.TemplateException;

@WebServlet("/admin/list_interviewer.jsp")
public class ListInterviewer extends AdminSessionServlet {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ListInterviewer.class);

    @Override
    public void getMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException, TemplateException {
        // get the path
        String path = request.getContextPath();
        PrintWriter out = response.getWriter();

        String listInterviewer = adminUserSession.getMyStudySpace().listInterviewer();

        Map<String, Object> mapOfParameters = new HashMap<>();

        mapOfParameters.put("output", listInterviewer);

        out.println(TemplateUtils.getHtmlFromTemplateForAdmin(mapOfParameters, "listInterviewerTemplate.ftl"));

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
