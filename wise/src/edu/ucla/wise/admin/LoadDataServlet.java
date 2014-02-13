package edu.ucla.wise.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Strings;
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
 * @author Douglas Bell
 * @version 1.0
 */

@WebServlet("/admin/load_data")
public class LoadDataServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(LoadDataServlet.class);

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
    private String process_survey_file(Document doc, PrintWriter out,
	    Statement stmt) {
	NodeList nodeList;
	Node n, nodeOne;
	NamedNodeMap nnm;

	String id, title;
	String sql;
	String returnVal;

	try {

	    /* parsing the survey node */
	    nodeList = doc.getElementsByTagName("Survey");
	    n = nodeList.item(0);
	    nnm = n.getAttributes();

	    /* get the survey attributes */
	    id = nnm.getNamedItem("ID").getNodeValue();
	    title = nnm.getNamedItem("Title").getNodeValue();
	    nodeOne = nnm.getNamedItem("Version");
	    if (nodeOne != null) {
		title = title + " (v" + nodeOne.getNodeValue() + ")";
	    }

	    /* get the latest survey's internal ID from the table of surveys */
	    sql = "select max(internal_id) from surveys where id = '" + id
		    + "'";
	    stmt.execute(sql);
	    ResultSet rs = stmt.getResultSet();
	    rs.next();
	    String maxId = rs.getString(1);

	    /* initiate the survey status as "N" */
	    String status = "N";

	    /* display processing information */
	    out.println("<table border=0><tr><td align=center>Processing a SURVEY (ID = "
		    + id + ")</td></tr>");

	    /* get the latest survey's status */
	    if (maxId != null) {
		sql = "select status from surveys where internal_id = " + maxId;
		stmt.execute(sql);
		rs = stmt.getResultSet();
		rs.next();
		status = (rs.getString(1)).toUpperCase();
	    }

	    /*
	     * If the survey status is in Developing or Production mode NOTE
	     * this just sets up survey info in surveys table; actual read of
	     * survey is handled by the Surveyor application.
	     */
	    if (status.equalsIgnoreCase("D") || status.equalsIgnoreCase("P")) {

		/* display the processing situation about the status */
		out.println("<tr><td align=center>Existing survey is in "
			+ status + " mode. </td></tr>");

		/* insert a new survey record */
		sql = "INSERT INTO surveys (id, title, status, archive_date) VALUES ('"
			+ id
			+ "',\""
			+ title
			+ "\", '"
			+ status
			+ "', 'current')";
		stmt.execute(sql);

		/* get the new inserted internal ID */
		sql = "SELECT max(internal_id) from surveys";
		stmt.execute(sql);
		rs = stmt.getResultSet();
		rs.next();
		String newId = rs.getString(1);

		/* use the newly created internal ID to name the file */
		String fileName = "file" + newId + ".xml";

		/* update the file name and uploading time in the table */
		sql = "UPDATE surveys SET filename = '" + fileName
			+ "', uploaded = now() WHERE internal_id = " + newId;
		stmt.execute(sql);

		/* display the processing information about the file name */
		out.println("<tr><td align=center>New version becomes the one with internal ID = "
			+ id + "</td></tr>");
		out.println("</table>");
		returnVal = fileName;
	    } else if (status.equalsIgnoreCase("N")
		    || status.equalsIgnoreCase("R")
		    || status.equalsIgnoreCase("C")) {

		/*
		 * If the survey status is in Removed or Closed mode. Or there
		 * is no such survey (keep the default status as N) the survey
		 * will be treated as a brand new survey with the default
		 * Developing status
		 */
		out.println("<tr><td align=center>This is a NEW Survey.  Adding a new survey into DEVELOPMENT mode...</td></tr>");

		/* insert the new survey record */
		sql = "INSERT INTO surveys (id, title, status, archive_date) VALUES ('"
			+ id + "',\"" + title + "\",'D','current')";
		stmt.execute(sql);

		/* get the newly created internal ID */
		sql = "SELECT max(internal_id) from surveys";
		stmt.execute(sql);
		rs = stmt.getResultSet();
		rs.next();
		String newId = rs.getString(1);
		String filename = "file" + newId + ".xml";

		/* update the file name and uploading time */
		sql = "UPDATE surveys SET filename = '" + filename
			+ "', uploaded = now() WHERE internal_id = " + newId;
		stmt.execute(sql);
		out.println("<tr><td align=center>New version becomes the one with internal ID = "
			+ id + "</td></tr>");
		out.println("</table>");
		returnVal = filename;
		rs.close();
	    } else {
		out.println("<tr><td align=center>ERROR!  Unknown STATUS!</td></tr>");
		out.println("<tr><td align=center>status:" + status
			+ "</td></tr>");
		out.println("</table>");
		returnVal = "NONE";
	    }

	} catch (SQLException e) {
	    log.error("WISE ADMIN - PROCESS SURVEY FILE:" + e.toString(), e);
	    returnVal = "ERROR";
	}
	return returnVal;
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
    public void processInviteesCsvFile(File f, PrintWriter out, Statement stmt)
	    throws SQLException, IOException {

	// TODO: Currently, ID column should be deleted from the csv file to
	// Handle
	// Adding Invitees. In future, we want to make sure, that if ID column
	// exists in
	// the csv file then it should be automatically handled up update if
	// exists

	/* Storing the fields that are not encoded into the HashSet. */
	HashSet<String> nonEncodedFieldSet = new HashSet<String>();
	nonEncodedFieldSet.add("firstname");
	nonEncodedFieldSet.add("lastname");
	nonEncodedFieldSet.add("salutation");
	nonEncodedFieldSet.add("phone");
	nonEncodedFieldSet.add("irb_id");

	HashSet<Integer> nonEncodedFieldPositions = new HashSet<Integer>();

	String[] colVal = new String[1000];
	BufferedReader br = null;

	try {

	    String sql = "insert into invitee(";
	    FileReader fr = new FileReader(f);
	    br = new BufferedReader(fr);
	    String line = new String();

	    int colNumb = 0, lineCount = 0;
	    while (!Strings.isNullOrEmpty(line = br.readLine())) {
		line = line.trim();
		if (line.length() != 0) {
		    lineCount++;
		    ArrayList<String> columns = new ArrayList<String>(
			    Arrays.asList(line.split(",")));

		    /*
		     * first row indicates the number of columns in the invitees
		     * csv.
		     */
		    if (lineCount == 1) {
			colNumb = columns.size();
		    } else {
			if (columns.size() < colNumb) {
			    while ((colNumb - columns.size()) != 0) {
				columns.add("");
			    }
			}
		    }

		    /* assign the column values */
		    for (int i = 0, j = 0; i < columns.size(); i++, j++) {
			colVal[j] = columns.get(i);

			/*
			 * mark as the null string if the phrase is an empty
			 * string
			 */
			if ((columns.size() == 0) || columns.get(i).equals("")) {
			    colVal[j] = "NULL";
			} else if ((columns.get(i).charAt(0) == '\"')
				&& (columns.get(i).charAt(
					columns.get(i).length() - 1) != '\"')) {
			    /*
			     * parse the phrase with the comma inside (has the
			     * double-quotation mark) this string is just part
			     * of the entire string, so append with the next one
			     */
			    do {
				i++;
				colVal[j] += "," + columns.get(i);
			    } while ((i < columns.size())
				    && (columns.get(i).charAt(
					    columns.get(i).length() - 1) != '\"'));

			    /*
			     * remove the double-quotation mark at the beginning
			     * and end of the string
			     */
			    colVal[j] = colVal[j].substring(1,
				    colVal[j].length() - 1);
			} else if ((columns.get(i).charAt(0) == '\"')
				&& (columns.get(i).charAt(
					columns.get(i).length() - 1) == '\"')) {

			    /*
			     * there could be double-quotation mark(s) (doubled
			     * by csv format) inside this string keep only one
			     * double-quotation mark(s)
			     */
			    if (columns.get(i).indexOf("\"\"") != -1) {
				colVal[j] = colVal[j].replaceAll("\"\"", "\"");
			    }
			}

			/*
			 * keep only one double-quotation mark(s) if there is
			 * any inside the string
			 */
			if (columns.get(i).indexOf("\"\"") != -1) {
			    colVal[j] = colVal[j].replaceAll("\"\"", "\"");
			}

			/* compose the sql query with the column values */
			if ((lineCount == 1)
				|| colVal[j].equalsIgnoreCase("null")) {
			    if (nonEncodedFieldSet.contains(colVal[j]
				    .toLowerCase())) {
				nonEncodedFieldPositions.add(j);
			    }
			    sql += colVal[j] + ",";
			} else {
			    if (!nonEncodedFieldPositions.contains(j)) {
				colVal[j] = "AES_ENCRYPT('"
					+ colVal[j]
					+ "','"
					+ this.adminUserSession
						.getMyStudySpace().db.emailEncryptionKey
					+ "')";
				sql += colVal[j] + ",";
			    } else {
				sql += "\"" + colVal[j] + "\",";
			    }
			}
		    }
		}

		/* compose the sql query */
		if (lineCount == 1) {
		    sql = sql.substring(0, sql.length() - 1) + ") values (";
		} else {
		    sql = sql.substring(0, sql.length() - 1) + "),(";
		}
	    }

	    /* delete the last "," and "(" */
	    sql = sql.substring(0, sql.length() - 2);
	    log.info("The Sql Executed is" + sql);

	    /* insert into the database */
	    stmt.execute(sql);
	    out.println("The data has been successfully uploaded and input into database");
	} catch (FileNotFoundException err) {

	    /* catch possible file not found errors from FileReader() */
	    log.error("CVS parsing: FileNotFoundException error!");
	    err.printStackTrace();
	} catch (IOException err) {
	    /* catch possible io errors from readLine() */
	    log.error("CVS parsing: IOException error!");
	    err.printStackTrace();
	} finally {
	    if (br != null) {
		br.close();
	    }
	}
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
    public void service(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	String path = request.getContextPath() + "/" + WiseConstants.ADMIN_APP;
	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
	WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(
		request);
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
	    log.error("Not a directory");
	}
	xmlTempLoc = xmlDir.getAbsolutePath()
		+ System.getProperty("file.separator");

	fileLoc = xmlTempLoc;
	try {
	    MultipartRequest multi = new MultipartRequest(request, xmlTempLoc,
		    250 * 1024);
	    File f1;
	    String filename = multi.getFilesystemName("file");
	    xmlTempLoc = xmlTempLoc + multi.getFilesystemName("file");
	    String fileType = multi.getContentType("file");

	    // out.println(file_type);

	    if ((fileType.indexOf("csv") != -1)
		    || (fileType.indexOf("excel") != -1)
		    || (fileType.indexOf("plain") != -1)) {

		/* open database connection */
		Connection con = this.adminUserSession.getDBConnection();
		Statement stm = con.createStatement();
		out.println("<p>Processing an Invitee CSV file...</p>");

		/* parse csv file and put invitees into database */
		File f = multi.getFile("file");
		this.processInviteesCsvFile(f, out, stm);

		/* delete the file */
		f.delete();
		String disp_html = "<p>The CSV named " + filename
			+ " has been successfully uploaded.</p>";
		out.println(disp_html);
	    } else if ((fileType.indexOf("css") != -1)
		    || (fileType.indexOf("jpg") != -1)
		    || (fileType.indexOf("jpeg") != -1)
		    || (fileType.indexOf("gif") != -1)) {

		this.saveFileToDatabase(multi, filename, "wisefiles");

		out.println("<p>The image named " + filename
			+ " has been successfully uploaded.</p>");
	    } else {

		/* open database connection */
		Connection conn = this.adminUserSession.getDBConnection();
		Statement stmt = conn.createStatement();

		/* Get parser and an XML document */
		Document doc = DocumentBuilderFactory.newInstance()
			.newDocumentBuilder()
			.parse(new FileInputStream(xmlTempLoc));

		NodeList nodelist = doc.getChildNodes();
		Node n;
		String fname = "";

		for (int i = 0; i < nodelist.getLength(); i++) {
		    n = nodelist.item(i);
		    if (n.getNodeName().equalsIgnoreCase("Survey")) {
			String fn = this.process_survey_file(doc, out, stmt);
			if (!fn.equalsIgnoreCase("NONE")) {
			    this.saveFileToDatabase(multi, fn, "xmlfiles");
			    File f = multi.getFile("file");
			    f1 = new File(fileLoc
				    + System.getProperty("file.separator") + fn);
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
			    String remoteURL = this.adminUserSession
				    .makeRemoteURL("survey", fname);
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
			String remoteURL = this.adminUserSession.makeRemoteURL(
				"preface", fname);
			response.sendRedirect(remoteURL);
			break;
		    }
		}
		stmt.close();
		conn.close();
	    }
	} catch (SQLException e) {
	    WISEApplication.logError(
		    "WISE - ADMIN load_data.jsp: " + e.toString(), e);
	    out.println("<h3>Upload of the file has failed.  Please try again.</h3>");
	} catch (Exception e) {
	    WISEApplication.logError(
		    "WISE - ADMIN load_data.jsp: " + e.toString(), e);
	    out.println("<h3>Invalid XML document submitted.  Please try again.</h3>");

	    /*
	     * Security feature change. Error should not be printed out on
	     * console
	     */
	    // out.println("<p>Error: " + e.toString() + "</p>");
	}
	out.println("<p><a href= tool.jsp>Return to Administration Tools</a></p>\n"
		+ "		</center>\n" + "		<pre>\n" +
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
    private void saveFileToDatabase(MultipartRequest multi, String filename,
	    String tableName) throws SQLException {
	Connection conn = null;
	PreparedStatement psmnt = null;
	FileInputStream fis = null;
	try {
	    /* open database connection */
	    conn = this.adminUserSession.getDBConnection();

	    String studySpaceName = this.adminUserSession.getStudyName();

	    File f = multi.getFile("file");
	    psmnt = conn.prepareStatement("DELETE FROM " + studySpaceName + "."
		    + tableName + " where filename =" + "'" + filename + "'");
	    psmnt.executeUpdate();
	    psmnt = conn.prepareStatement("INSERT INTO " + studySpaceName + "."
		    + tableName + "(filename,filecontents,upload_date)"
		    + "VALUES (?,?,?)");
	    psmnt.setString(1, filename);
	    fis = new FileInputStream(f);
	    psmnt.setBinaryStream(2, fis, (int) (f.length()));
	    java.util.Date currentDate = new java.util.Date();
	    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
		    "yyyy-MM-dd HH:mm:ss");
	    String currentDateString = sdf.format(currentDate);
	    psmnt.setString(3, currentDateString);
	    psmnt.executeUpdate();
	    psmnt.close();

	} catch (SQLException e) {
	    log.error("Could not save the file to the database", e);
	} catch (FileNotFoundException e) {
	    log.error("Could not find the file to save", e);
	} finally {
	    psmnt.close();
	    conn.close();
	}
    }
}

/*
 * 1/19/2012 - Fixed survey and preface upload failure bugs due to blocked HTTP
 * calls [Doug]
 */
