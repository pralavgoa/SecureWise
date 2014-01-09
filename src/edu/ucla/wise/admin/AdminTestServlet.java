package edu.ucla.wise.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.WISEApplication;

/**
 * AdminTestServlet class directs the user coming from email
 * URL or interviewers to appropriate next step or page.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class AdminTestServlet extends HttpServlet {
    static final long serialVersionUID = 1000;
    
    /**
     * Tests mailing part of the code.
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
	session.getServletContext();

	// get the encoded study space ID 
	//String spaceid = req.getParameter("t");	
	// get the email message ID
	// String msgid_encode = req.getParameter("msg");
	// Study_Space myStudySpace = null;
	// String id2="", thesharedFile="";
	// if (spaceid != null)
	// {
	// myStudySpace = Study_Space.get_Space(spaceid); //instantiates the
	// study
	// if (myStudySpace !=null) {
	// id2=myStudySpace.id;
	// thesharedFile = myStudySpace.sharedFile_urlRoot;
	// Message_Sequence[] msa =
	// myStudySpace.preface.get_message_sequences("Enrollmt");
	// for (int i=0; i<msa.length; i++)
	// id2+= "; "+msa[1].toString();
	// }
	// }
	// AdminInfo.email_alert("Initialized OK");
	String fromStr = "";
	try {
	    
		/* Define message */
	    MimeMessage message = new MimeMessage(AdminApplication.mailSession);
	    
	    if (req.getParameter("froma") != null) {
	    	fromStr += "<" + req.getParameter("froma") + ">";
	    } else {
	    	fromStr += "<merg@mednet.ucla.edu>";
	    }
		
	    if (req.getParameter("from") != null) {
			fromStr = req.getParameter("from") + fromStr;
		}
		
	    InternetAddress ia = new InternetAddress(fromStr);
	    message.setFrom(ia);
	    
	    if (req.getParameter("repa") != null) {
			fromStr = "<" + req.getParameter("repa") + ">";
			InternetAddress ib = new InternetAddress(fromStr);
			message.setReplyTo(new InternetAddress[] { ib });
	    }
	    if (req.getParameter("senda") != null) {
			fromStr = "<" + req.getParameter("senda") + ">";
			InternetAddress ib = new InternetAddress(fromStr);
			message.setSender(ib);
	    }

	    message.addRecipient(javax.mail.Message.RecipientType.TO,
	    		new InternetAddress("<merg@mednet.ucla.edu>"));
	    message.setSubject("This is a test");
	    message.setText("this is a test body");

	    /* Send message */
	    Transport.send(message);
	} catch (AddressException e) {
		WISEApplication.logError("Error in AdminTest:", e);
	} catch (MessagingException e) {
	    String initError = e.toString();
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    e.printStackTrace(pw);
	    initError += sw.toString();
	    WISEApplication.logError("Error in begin_test:" + initError, e);
	} 

	out.println("<HTML><HEAD><TITLE>Begin Page</TITLE>"
		+ "<LINK href='../file_product/style.css' type=text/css rel=stylesheet>"
		+ "<body text=#000000 bgColor=#ffffcc><center><table>"
		
		// + "<tr><td>Successful test. StudySpace id [t]= " +
		// id2 + "</td></tr>"
		+ "<tr><td>Root URL= "
		+ AdminApplication.rootURL
		+ "</td></tr>"
		+ "<tr><td>XML path = "
		+ AdminApplication.wiseProperties.getXmlRootPath()
		+ "</td></tr>"
		
		// + "<tr><td>SS file path = " + thesharedFile +
		// "</td></tr>"
		+ "<tr><td>Image path = "
		+ AdminApplication.imageRootPath
		+ "</td></tr>"
		+ "<tr><td>DB backup path = "
		+ AdminApplication.dbBackupPath
		+ "</td></tr>"
		+ "<tr><td>Context Path= "
		+ AdminApplication.ApplicationName
		+ "</td></tr>"
		+ "<tr><td>Servlet Path= "
		+ AdminApplication.servletUrl
		+ "</td></tr>"
		
		// + "<tr><td>message id= " + msgid_encode +
		// "</td></tr>"
		+ "<tr><td>Default email_from= "
		+ WISEApplication.wiseProperties.getEmailFrom()
		+ "</td></tr>"
		+ "<tr><td>constructed fromstr= "
		+ fromStr
		+ "</td></tr>" + "</table></center></body></html>");
    }
}
