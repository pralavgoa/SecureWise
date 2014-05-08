/**
 * Copyright (c) 2014, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.ucla.wise.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oreilly.servlet.MultipartRequest;

import edu.ucla.wise.client.web.WiseHttpRequestParameters;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WiseConstants;

/**
 * LoadDataServlet is a class which is used to load data into the system. Wise
 * admin can upload a csv containing the details of the invitees. Wise admin can
 * upload a xml file containing survey questions. Wise admin can upload image
 * file that are used in survey.
 * 
 */

@WebServlet("/admin/load_data")
public class LoadDataServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    public static final Logger LOGGER = Logger.getLogger(LoadDataServlet.class);

    private AdminUserSession adminUserSession = null;

    /**
     * Updates the survey information into the database when uploading the
     * survey xml file
     * 
     * @param doc
     *            Parsed xml file document.
     * @param out
     *            Output to the screen.
     * @param stmt
     *            SQL statement for execution of queries.
     * @return Returns the filename of the uploaded survey xml into the
     *         database.
     */
    private String process_survey_file(Document doc, PrintWriter out) {
        return this.adminUserSession.getMyStudySpace().processSurveyFile(doc);

    }

    /**
     * Updates the invitee information in the database when uploading the
     * invitee csv file
     * 
     * @param f
     *            csv file to load.
     * @param out
     *            Output to the screen.
     * @param stmt
     *            SQL statement for execution of queries.
     * @return Returns the filename of the uploaded survey xml into the
     *         database.
     * @throws SQLException
     *             , IOException.
     */
    public void processInviteesCsvFile(File f, PrintWriter out) throws SQLException, IOException {

        this.adminUserSession.getMyStudySpace().processInviteesCsvFile(f);

    }

    /**
     * Uploads the csv/xml/image files into database from Wise admin.
     * 
     * @param req
     *            HTTP Request.
     * @param res
     *            HTTP Response.
     * @throws ServletException
     *             and IOException.
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getContextPath() + "/" + WiseConstants.ADMIN_APP;
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(request);
        HttpSession session = request.getSession(true);
        if (session.isNew()) {
            response.sendRedirect(path + "/index.html");
            return;
        }

        /* get the AdminInfo object */
        this.adminUserSession = parameters.getAdminUserSessionFromHttpSession();
        if (this.adminUserSession == null) {
            response.sendRedirect(path + "/error.htm");
            return;
        }

        String fileLoc = WISEApplication.wiseProperties.getXmlRootPath();
        String xmlTempLoc = fileLoc;

        File xmlDir = new File(xmlTempLoc);
        if (!xmlDir.isDirectory()) {
            LOGGER.error("Not a directory");
        }
        xmlTempLoc = xmlDir.getAbsolutePath() + System.getProperty("file.separator");

        fileLoc = xmlTempLoc;
        try {
            MultipartRequest multi = new MultipartRequest(request, xmlTempLoc, 250 * 1024);
            File f1;
            String filename = multi.getFilesystemName("file");
            xmlTempLoc = xmlTempLoc + multi.getFilesystemName("file");
            String fileType = multi.getContentType("file");

            // out.println(file_type);

            if ((fileType.indexOf("csv") != -1) || (fileType.indexOf("excel") != -1)
                    || (fileType.indexOf("plain") != -1)) {

                out.println("<p>Processing an Invitee CSV file...</p>");

                /* parse csv file and put invitees into database */
                File f = multi.getFile("file");
                this.processInviteesCsvFile(f, out);

                /* delete the file */
                f.delete();
                String disp_html = "<p>The CSV named " + filename + " has been successfully uploaded.</p>";
                out.println(disp_html);
            } else if ((fileType.indexOf("css") != -1) || (fileType.indexOf("jpg") != -1)
                    || (fileType.indexOf("jpeg") != -1) || (fileType.indexOf("gif") != -1)) {

                this.saveFileToDatabase(multi, filename, "wisefiles");

                out.println("<p>The image named " + filename + " has been successfully uploaded.</p>");
            } else {
                /* Get parser and an XML document */
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(new FileInputStream(xmlTempLoc));

                NodeList nodelist = doc.getChildNodes();
                Node n;
                String fname = "";

                for (int i = 0; i < nodelist.getLength(); i++) {
                    n = nodelist.item(i);
                    if (n.getNodeName().equalsIgnoreCase("Survey")) {
                        String fn = this.process_survey_file(doc, out);
                        if (!fn.equalsIgnoreCase("NONE")) {
                            this.saveFileToDatabase(multi, fn, "xmlfiles");
                            File f = multi.getFile("file");
                            f1 = new File(fileLoc + System.getProperty("file.separator") + fn);
                            // f1.delete();
                            if (!f.renameTo(f1)) {
                                System.err.println("Renaming File changed");
                                throw new Exception();
                            }
                            fname = fn;

                            /*
                             * send URL request to create study space and survey
                             * in remote server commenting this out for Apache
                             * admin on same machine -- seems to be blocked
                             * String remoteResult =
                             * adminUserSession.load_remote("survey", fname);
                             * out.println(remoteResult);
                             */
                            String remoteURL = this.adminUserSession.makeRemoteURL("survey", fname);
                            response.sendRedirect(remoteURL);
                        } else {
                            /* delete the file */
                            File f = multi.getFile("file");
                            f.delete();
                        }
                        break;
                    } else if (n.getNodeName().equalsIgnoreCase("Preface")) {
                        fname = "preface.xml";

                        this.saveFileToDatabase(multi, fname, "xmlfiles");

                        File f = multi.getFile("file");
                        f1 = new File(fileLoc + fname);
                        f.renameTo(f1);
                        String dispHtml = null;
                        if (this.adminUserSession.parseMessageFile()) {
                            dispHtml = "<p>PREFACE file is uploaded with name changed to be preface.xml</p>";
                        } else {
                            dispHtml = "<p>PREFACE file upload failed.</p>";
                        }
                        out.println(dispHtml);

                        /*
                         * send URL request to create study space and survey in
                         * remote server commenting this out for Apache admin on
                         * same machine -- seems to be blocked String
                         * remoteResult =
                         * adminUserSession.load_remote("preface", fname);
                         * out.println(remoteResult);
                         */
                        String remoteURL = this.adminUserSession.makeRemoteURL("preface", fname);
                        response.sendRedirect(remoteURL);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("WISE - ADMIN load_data.jsp: " + e.toString(), e);
            out.println("<h3>Upload of the file has failed.  Please try again.</h3>");
        } catch (Exception e) {
            LOGGER.error("WISE - ADMIN load_data.jsp: " + e.toString(), e);
            out.println("<h3>Invalid XML document submitted.  Please try again.</h3>");

            /*
             * Security feature change. Error should not be printed out on
             * console
             */
            // out.println("<p>Error: " + e.toString() + "</p>");
        }
        out.println("<p><a href= tool.jsp>Return to Administration Tools</a></p>\n" + "		</center>\n" + "		<pre>\n" +
        // file_loc [adminUserSession.study_xml_path]: file_loc%>
        // css_path [adminUserSession.study_css_path]: <%=css_path%>
        // image_path [adminUserSession.study_image_path]:
        // <%=image_path%>
                "		</pre>\n" + "</p>\n" + "</body>\n" + "</html>");
    }

    /**
     * Uploads the csv/xml/image files into database.
     * 
     * @param multi
     *            Multi part Request to get the file saved on disk.
     * @param filename
     *            file name to uplaod.
     * @param tableName
     *            database table into which the file has to be saved into.
     */
    private void saveFileToDatabase(MultipartRequest multi, String filename, String tableName) throws SQLException {

        this.adminUserSession.getMyStudySpace().saveFileToDatabase(multi, filename, tableName,
                this.adminUserSession.getStudyName());

    }
}