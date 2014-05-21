package edu.ucla.wise.admin.console;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.ucla.wise.admin.AdminUserSession;
import edu.ucla.wise.admin.web.AdminSessionServlet;
import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.FileExtensions;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.WiseConstants;
import freemarker.template.TemplateException;

@WebServlet("/admin/download_file.jsp")
public class DownloadFile extends AdminSessionServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DownloadFile.class);

    @Override
    public void getMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException, TemplateException {

        PrintWriter out = response.getWriter();

        // get the server path
        String path = request.getContextPath();
        String fileExt = null;
        String filepath = null;
        String outputStr = null;

        // get the download file name from the request
        String filename = request.getParameter("fileName");

        // security features changes
        if (SanityCheck.sanityCheck(filename)) {
            response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP + "/sanity_error.html");
            return;
        }

        filename = SanityCheck.onlyAlphaNumeric(filename);
        // End of security changes

        // if the session is invalid, display the error
        if ((filename == null)) {
            response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP + "/error_pages/error.htm");
            return;
        }

        // if the file is the stylesheet
        if (filename.indexOf(".css") != -1) {
            // TODO: This is path is wrong because the
            // Upload servlet always uploads into the study_xml_path.
            // Fix load_data to take correct file path to upload, till then
            // downloading from study_xml_path.
            filepath = adminUserSession.getStudyXmlPath();
            // if (filename.equalsIgnoreCase("print.css"))
            // filepath = adminUserSession.study_css_path; //print.css
            // else
            // filepath = adminUserSession.study_css_path; //style.css
            fileExt = FileExtensions.css.name();
            outputStr = adminUserSession.buildXmlCssSql(filepath, filename);
        } else if (filename.indexOf(".sql") != -1) {
            // if the file is the database backup
            filepath = AdminApplication.getInstance().getDbBackupPath(); // dbase
                                                                         // mysqldump
                                                                         // file
            fileExt = FileExtensions.sql.name();
            outputStr = adminUserSession.buildXmlCssSql(filepath, filename);
        } else if (filename.indexOf(".csv") != -1) {
            // if the file is the csv file (MS Excel)
            // no more creating the csv file (could be either the survey data or
            // the invitee list)
            outputStr = adminUserSession.buildCsvString(filename);
            fileExt = FileExtensions.csv.name();
        } else {
            // the file should be the xml file (survey, message, preface etc.)
            filepath = adminUserSession.getStudyXmlPath(); // xml file
            fileExt = FileExtensions.xml.name();
            outputStr = adminUserSession.buildXmlCssSql(filepath, filename);
        }

        // response.setContentType("APPLICATION/OCTET-STREAM");
        response.setContentType("text/" + fileExt);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        out.write(outputStr);
        // out.close(); Should not be closing because it is the JSP's
        // outputStream object!

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
