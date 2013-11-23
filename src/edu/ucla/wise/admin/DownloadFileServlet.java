package edu.ucla.wise.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.FileExtensions;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.WiseConstants;

/**
 * DownloadFileServlet is a class which is used when user tries to download 
 * survey data or preface or the survey questions from wise admin system.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class DownloadFileServlet extends HttpServlet {
    static final long serialVersionUID = 1000;
    
    /**
     * Checks the type of file to download based on the extensions 
     * and calls the appropriate methods.
     *  
     * @param 	req	 HTTP Request.
     * @param 	res	 HTTP Response.
     * @throws 	ServletException and IOException. 
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res)
    		throws ServletException, IOException {
	
    	/* prepare for writing */
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		HttpSession session = req.getSession(true);
	
		AdminApplication adminInfo = (AdminApplication) session.getAttribute("ADMIN_INFO");
		String fileName = req.getParameter("fileName");
		
		String path = req.getContextPath();
	    if(SanityCheck.sanityCheck(fileName)){
	    	res.sendRedirect(path + "/" + WiseConstants.ADMIN_APP + "/sanity_error.html");
		    return;
	    }
	    fileName=SanityCheck.onlyAlphaNumeric(fileName);
	    
	    /* if the session is invalid, display the error */
		if (adminInfo == null) {
		    out.println("Wise Admin - Download function can't ID you as a valid admin");
		    return;
		}
		if (fileName == null) {
		    out.println("Wise Admin - File download Error: Can't get the file name");
		    return;
		}
	
		String outputStr = null;
		String fileExt = null;
	
		String filePath = "";
		    
		/* if the file is the stylesheet */
		if (fileName.indexOf(".css") != -1) {
			if (fileName.equalsIgnoreCase("print.css")) {
				filePath = adminInfo.studyCssPath; // print.css
			} else {
				filePath = adminInfo.studyCssPath; // style.css
			}
			fileExt = FileExtensions.css.name();
			outputStr = adminInfo.buildXmlCssSql(filePath, fileName);
		} else if (fileName.indexOf(".sql") != -1) {
		    	
		   	/* if the file is the database backup */
			filePath = AdminApplication.dbBackupPath; // dbase mysqldump file
			fileExt = FileExtensions.sql.name();
			outputStr = adminInfo.buildXmlCssSql(filePath, fileName);
		} else if (fileName.indexOf(".csv") != -1) {
		
			/* if the file is the csv file (MS Excel)
			 * create the csv file (could be either the survey data or the
			 * invitee list)
			 */
		    outputStr = adminInfo.buildCsvString(fileName);
			fileExt = FileExtensions.csv.name();
		} else {
		
			/*
		     * for else, the file should be the xml file (survey, message, preface, etc.)
		     * the file should be the xml file (survey, message, preface, etc.)
		     */
			filePath = adminInfo.studyXmlPath; // xml file
			fileExt = FileExtensions.xml.name();
			outputStr = adminInfo.buildXmlCssSql(filePath, fileName);
		}
	
		res.setContentType("text/" + fileExt);
		res.setHeader("Content-Disposition", "attachment; filename=\""
		   		+ fileName + "\"");
		out.write(outputStr);
		out.close();
    }

}
