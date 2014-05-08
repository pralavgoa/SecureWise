package edu.ucla.wise.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.admin.view.SurveyHealthInformation;
import edu.ucla.wise.admin.view.SurveyInformation;
import edu.ucla.wise.admin.view.ToolView;
import edu.ucla.wise.client.web.WiseHttpRequestParameters;
import edu.ucla.wise.commons.WiseConstants;

@WebServlet("/admin/console")
public class AdminConsole extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(true);

        ToolView toolView = new ToolView();
        // get the server path
        String path = request.getContextPath();
        path = path + "/";
        Date today1 = new Date();
        DateFormat f = new SimpleDateFormat("E");
        String wkday = f.format(today1);
        AdminUserSession adminUserSession;
        WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(request);
        try {
            session = request.getSession(true);
            // if the session is expired, go back to the logon page
            if (session.isNew()) {
                response.sendRedirect(path + WiseConstants.ADMIN_APP + "/index.html");
                return;
            }
            // get the admin info object from session
            adminUserSession = parameters.getAdminUserSessionFromHttpSession();
            if (adminUserSession == null) {
                response.sendRedirect(path + WiseConstants.ADMIN_APP + "/error.htm");
                return;
            }
            adminUserSession.loadRemote(WiseConstants.SURVEY_HEALTH_LOADER, adminUserSession.getStudyName());
            // get the weekday format of today to name the data backup file
        } catch (Exception e) {
            // WISE_Application.log_error("WISE ADMIN - TOOL init: ", e);

            PrintWriter out2 = response.getWriter();
            out2.print("******There has Been and exception********");
            return;
        }
        SurveyHealthInformation healthInfo = toolView.healthStatusInfo(adminUserSession.getMyStudySpace());
        List<SurveyInformation> currentSurveysInfo = adminUserSession.getMyStudySpace().getCurrentSurveys();

        for (SurveyInformation currentSurveyInfo : currentSurveysInfo) {

            // if the survey is in the developing mode
            if (currentSurveyInfo.status.equalsIgnoreCase("D")) {
                // if the survey is in the production mode
                if (currentSurveyInfo.status.equalsIgnoreCase("P")) {
                }
            }

        } // end of for loop

    }
}
