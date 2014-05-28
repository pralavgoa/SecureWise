package edu.ucla.wise.admin.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.Charsets;
import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.admin.AdminUserSession;
import edu.ucla.wise.admin.web.AdminSessionServlet;
import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.FileExtensions;
import edu.ucla.wise.commons.WiseConstants;
import edu.ucla.wise.utils.FileUtils;
import freemarker.template.TemplateException;

@WebServlet("/admin/download_file")
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

        if (Strings.isNullOrEmpty(filename)) {
            response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP + "/error_pages/error.htm");
            return;
        }

        // if the file is the stylesheet
        if (filename.indexOf(".css") != -1) {
            InputStream cssStream = adminUserSession.getMyStudySpace().getDB()
                    .getFileFromDatabase(filename, adminUserSession.getMyStudySpace().studyName);
            outputStr = FileUtils.convertInputStreamToString(cssStream, Charsets.UTF_8);
        } else if (filename.indexOf(".sql") != -1) {
            // if the file is the database backup
            filepath = AdminApplication.getInstance().getDbBackupPath(); // dbase
                                                                         // mysqldump
                                                                         // file
            fileExt = FileExtensions.sql.name();
            outputStr = adminUserSession.buildSql(filepath, filename);
        } else if (filename.indexOf(".csv") != -1) {
            // if the file is the csv file (MS Excel)
            // no more creating the csv file (could be either the survey data or
            // the invitee list)
            outputStr = adminUserSession.buildCsvString(filename);
            fileExt = FileExtensions.csv.name();
        } else {
            InputStream xmlStream = adminUserSession.getMyStudySpace().getDB().getXmlFileFromDatabase(filename);
            outputStr = FileUtils.convertInputStreamToString(xmlStream, Charsets.UTF_8);
        }

        response.setContentType("text/" + fileExt);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        out.write(outputStr);
        out.close();
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
