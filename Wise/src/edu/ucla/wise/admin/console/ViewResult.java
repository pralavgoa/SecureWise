package edu.ucla.wise.admin.console;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.ucla.wise.admin.AdminUserSession;
import edu.ucla.wise.admin.web.AdminSessionServlet;
import edu.ucla.wise.client.web.TemplateUtils;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.WiseConstants;
import freemarker.template.TemplateException;

@WebServlet("/admin/view_results.jsp")
public class ViewResult extends AdminSessionServlet {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ViewResult.class);

    @Override
    public void getMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException, TemplateException {

        String path = request.getContextPath();

        // get the survey ID
        String surveyId = request.getParameter("s");
        // security feature changes
        if (SanityCheck.sanityCheck(surveyId)) {
            response.sendRedirect(path + "/admin/error_pages/sanity_error.html");
            return;
        }
        surveyId = SanityCheck.onlyAlphaNumeric(surveyId);
        // if the session is invalid, display the error
        if ((surveyId == null)) {
            response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP + "/error_pages/error.htm");
            return;
        }

        String adminResults = adminUserSession.getMyStudySpace().printAdminResults(surveyId);

        Map<String, Object> parametersForView = new HashMap<>();
        parametersForView.put("path", path);
        parametersForView.put("surveyId", surveyId);
        parametersForView.put("results", adminResults);
        TemplateUtils.getHtmlFromTemplateForAdmin(parametersForView, "viewResultsTemplate.ftl");

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