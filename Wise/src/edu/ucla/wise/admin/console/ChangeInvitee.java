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
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.web.WebResponseMessage;
import freemarker.template.TemplateException;

@WebServlet("/admin/change_invitee.jsp")
public class ChangeInvitee extends AdminSessionServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    final Logger LOGGER = Logger.getLogger(this.getClass());

    @Override
    public void getMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException, TemplateException {

        PrintWriter out = response.getWriter();

        String path = request.getContextPath();
        // get the edit type - add, delete, update etc from the request
        String editType = request.getParameter("cedit");
        // Security features Changes
        if (SanityCheck.sanityCheck(editType)) {
            response.sendRedirect("/admin/error_pages/sanity_error.html");
            return;
        }
        editType = SanityCheck.onlyAlphaNumeric(editType);

        String colOName, colName, colValue, colDef;
        String column_name, column_type, column_default, column_key;

        // get the new column name, data type, default value and old column
        // name
        if (editType != null) {
            colName = request.getParameter("cname");
            colValue = request.getParameter("ctype");
            colDef = request.getParameter("cdefault");
            colOName = request.getParameter("coname");
            if (SanityCheck.sanityCheck(colName) || SanityCheck.sanityCheck(colValue)
                    || SanityCheck.sanityCheck(colDef) || SanityCheck.sanityCheck(colOName)) {
                response.sendRedirect(path + "/admin/error_pages/sanity_error.html");
                return;
            }
            colName = SanityCheck.onlyAlphaNumericandSpecial(colName);
            colValue = SanityCheck.onlyAlphaNumericandSpecial(colValue);
            colDef = SanityCheck.onlyAlphaNumericandSpecial(colDef);
            colOName = SanityCheck.onlyAlphaNumericandSpecial(colOName);
            // end of security features changes

            WebResponseMessage crudWebResponseMessage = adminUserSession.getMyStudySpace().modifyInviteeTable(editType,
                    colName, colValue, colDef, colOName);

            WebResponseMessage describeInviteeResponse = adminUserSession.getMyStudySpace().describeInviteeTable();

            if (crudWebResponseMessage.isSuccess() && describeInviteeResponse.isSuccess()) {
                Map<String, Object> parametersForChangeInviteePage = new HashMap<>();
                parametersForChangeInviteePage.put("crudMessage", crudWebResponseMessage.getResponse());
                parametersForChangeInviteePage.put("inviteeTableDescription", describeInviteeResponse.getResponse());
                TemplateUtils.getHtmlFromTemplateForAdmin(parametersForChangeInviteePage, "changeInviteeTemplate.ftl");
            }
        }
    }

    @Override
    public void postMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession) {
        // do nothing

    }

    @Override
    public Logger getLogger() {
        return this.LOGGER;
    }
}
