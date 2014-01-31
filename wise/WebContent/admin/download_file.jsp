<%@page import="edu.ucla.wise.admin.AdminUserSession"%>
<%@ page contentType="text/html;charset=windows-1252"%><%@ page
	language="java"%><%@ page
	import="edu.ucla.wise.commons.*,java.io.*,java.sql.*,java.util.*"%>
<%
	//get the server path
	String path = request.getContextPath();
	String fileExt = null;
	String filepath = null;
	String outputStr = null;

	//get the admin info object from session
	AdminUserSession adminUserSession = (AdminUserSession) session
	.getAttribute("ADMIN_USER_SESSION");
	//get the download file name from the request
	String filename = request.getParameter("fileName");
	
	//security features changes
    if(SanityCheck.sanityCheck(filename)){
    	response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP + "/sanity_error.html");
	    return;
    }
    
    filename=SanityCheck.onlyAlphaNumeric(filename);
    //End of security changes
	
	//if the session is invalid, display the error
	if (adminUserSession == null || filename == null) {
		response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP
		+ "/error.htm");
		return;
	}

	//if the file is the stylesheet
	if (filename.indexOf(".css") != -1) {
		// TODO: This is path is wrong because the 
		// Upload servlet always uploads into the study_xml_path.
		// Fix load_data to take correct file path to upload, till then 
		// downloading from study_xml_path.
		filepath = adminUserSession.getStudyXmlPath();
		//if (filename.equalsIgnoreCase("print.css"))
		//	filepath = adminUserSession.study_css_path; //print.css
		//else
		//	filepath = adminUserSession.study_css_path; //style.css
		fileExt = FileExtensions.css.name();
		outputStr = adminUserSession.buildXmlCssSql(filepath, filename);
	} else if (filename.indexOf(".sql") != -1) {
		//if the file is the database backup
		filepath = AdminApplication.getInstance().getDbBackupPath(); //dbase mysqldump file
		fileExt = FileExtensions.sql.name();
		outputStr = adminUserSession.buildXmlCssSql(filepath, filename);
	} else if (filename.indexOf(".csv") != -1) {
		//if the file is the csv file (MS Excel)
		//no more creating the csv file (could be either the survey data or the invitee list)
		outputStr = adminUserSession.buildCsvString(filename);
		fileExt = FileExtensions.csv.name();
	} else {
		// the file should be the xml file (survey, message, preface etc.)
		filepath = adminUserSession.getStudyXmlPath(); //xml file
		fileExt = FileExtensions.xml.name();
		outputStr = adminUserSession.buildXmlCssSql(filepath, filename);
	}

	//response.setContentType("APPLICATION/OCTET-STREAM");
	response.setContentType("text/" + fileExt);
	response.setHeader("Content-Disposition", "attachment; filename=\""
	+ filename + "\"");
	out.clearBuffer();
	out.write(outputStr);
	//out.close(); Should not be closing because it is the JSP's outputStream object!
%>
