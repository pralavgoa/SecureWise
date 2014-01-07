package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.initializer.WiseProperties;

/**
 * SurveyorTestServlet initializes the surveyor application
 * and displays some of the parameters.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class SurveyorTestServlet extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Loads the surveryor application and study space and prints some of the parameters for testing.
     *  
     * @param 	req	 HTTP Request.
     * @param 	res	 HTTP Response.
     * @throws 	ServletException and IOException. 
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res)
    		throws ServletException, IOException {
		
    	// FIND server's default file location
		// File test_file = new File("whereAmI");
		// FileOutputStream tstout = new FileOutputStream(test_file);
		// tstout.write(100);
		// tstout.close();
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		WiseProperties properties = new WiseProperties("wise.properties","WISE");
		/* Initialize surveyor application if not already started */
		String initErr = SurveyorApplication.checkInit(req.getContextPath(), properties);
		
		if (initErr != null) {
		    out.println("<HTML><HEAD><TITLE>WISE survey system -- Can't identify you</TITLE>"
		    		+ "<LINK href='../file_product/style.css' type=text/css rel=stylesheet>"
		    		+ "<body text=#000000 bgColor=#ffffcc><center><table>"
		    		+ "<tr><td>Sorry, the WISE Surveyor application failed to initialize. "
		    		+ "Please contact the system administrator with the following information."
		    		+ "<P>"
		    		+ initErr
		    		+ "</td></tr>"
		    		+ "</table></center></body></html>");
		    SurveyorApplication.logError("WISE Surveyor Init Error: "
		    		+ initErr, null);// should write to file if no email
		    return;
		}
	
		HttpSession session = req.getSession(true);
		session.getServletContext();
		
		//Surveyor_Application s = (Surveyor_Application) session
		//	.getAttribute("SurveyorInst");
	
		/* get the encoded study space ID */
		String spaceidEncode = req.getParameter("t");
		
		/* get the email message ID */
		String msgidEncode = req.getParameter("msg");
	
		StudySpace myStudySpace = null;
		String thesharedFile = "";
		//String id2 = "";
		if (spaceidEncode != null) {
		    myStudySpace = StudySpace.getSpace(spaceidEncode); // instantiates the study
		    if (myStudySpace != null) {
				//id2 = myStudySpace.id;
				thesharedFile = myStudySpace.sharedFileUrlRoot;
				//Message_Sequence[] msa = myStudySpace.preface
				//		.get_message_sequences("Enrollmt");
				//for (int i = 0; i < msa.length; i++)
				//    id2 += "; " + msa[1].toString();
			    //}
		}
	
		out.println("<HTML><HEAD><TITLE>Begin Page</TITLE>"
			+ "<LINK href='../file_product/style.css' type=text/css rel=stylesheet>"
			+ "<body text=#000000 bgColor=#ffffcc><center><table>"
			+ "<tr><td>Successful test. StudySpace id [t]= "
			+ spaceidEncode + "</td></tr>"
			+ "<tr><td>Root URL= "
			+ SurveyorApplication.rootURL
			+ "</td></tr>"
			+ "<tr><td>XML path = "
			+ SurveyorApplication.wiseProperties.getXmlRootPath()
			+ "</td></tr>"
			+ "<tr><td>SS file path = "
			+ thesharedFile
			+ "</td></tr>"
			// + "<tr><td>Image path = " +
			// Surveyor_Application.image_root_path + "</td></tr>"
			// + "<tr><td>DB backup path = " +
			// Surveyor_Application.db_backup_path + "</td></tr>"
			+ "<tr><td>Context Path= " + SurveyorApplication.ApplicationName + "</td></tr>"
			+ "<tr><td>Servlet Path= " + SurveyorApplication.servletUrl + "</td></tr>"
			+ "<tr><td>message id= " + msgidEncode + "</td></tr>"
			+ "</table></center></body></html>");
		}
    }
}