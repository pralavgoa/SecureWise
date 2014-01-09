package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

import edu.ucla.wise.commons.MessageSequence;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WiseConstants;

/**
 * SaveAnnoUserServlet  is used to give anonymous users access to the survey.
 * User has to enter all the needed details to take up the survey, once done 
 * user will be redirected to the survey welcome page.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
@WebServlet("/survey/save_anno_user")
public class SaveAnnoUserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new invitee with all the entered details and then forwards 
     * the user to welcome page to take up the survey. 
     *  
     * @param 	req	 HTTP Request.
     * @param 	res	 HTTP Response.
     * @throws 	ServletException and IOException. 
     */
    @Override
    protected void doPost(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {
       	String sessionId=request.getSession().getId();
    	String sID = request.getParameter("SID");
    	
    	PrintWriter pw = response.getWriter();
    	/* condition to check the session ID added to check the CSRF issue */ 
    	//pw.write("<html> <p>The session parameter form the session is " +	sessionId + "</p>");
    	//pw.write("<p>the session parameter coming from the new_invitee.jsp file is " + sID + "</p>");
    	if (sessionId.equals(sID)) {
    		
    		/* get the ecoded study space ID */
			String spaceidEncode = request.getParameter("t");
			
			/* Sanity check of the input variables */
			String path = request.getContextPath() + "/" + WiseConstants.ADMIN_APP;	
			if(SanityCheck.sanityCheck(spaceidEncode)) {
				response.sendRedirect(path + "/sanity_error.html");
				return;
			}
			spaceidEncode= SanityCheck.onlyAlphaNumeric(spaceidEncode);
			
			if(spaceidEncode==null || spaceidEncode.isEmpty()){
				response.sendRedirect(path + "/parameters_error.html");
				return;
			}	
						
			/* decode study space ID */
			String spaceidDecode = WISEApplication.decode(spaceidEncode);
			StudySpace theStudy = StudySpace.getSpace(spaceidDecode);
		
			/* adding new user */
			Map<String, String> parametersMap = new HashMap<String, String>();
		
			Enumeration<String> parametersNames = request.getParameterNames();
			
			ArrayList<String> inputs = new ArrayList<String>();		
			while (parametersNames.hasMoreElements()) {		
			    String parameterName = parametersNames.nextElement();		
			    String[] parameterValues = request.getParameterValues(parameterName);			    
			    inputs.add(parameterValues[0]);		
			    parametersMap.put(parameterName, parameterValues[0]);		
			}
			
			/* Sanity of input parameters. */
			if (SanityCheck.sanityCheck(inputs)) {
				response.sendRedirect(path + "/sanity_error.html");
			    return;
			}
						
			if (Strings.isNullOrEmpty(parametersMap.get("lastname"))) {
			    pw.write("<html><body>The 'Last Name' field cannot be left blank</body><html>");
			    pw.close();
			    return;		
			}
		
			int userId = theStudy.db.addInviteeAndReturnUserId(parametersMap);		
			
			/*  
			 * Sending the New User initial invite 
			 * Get the Message Sequence associated with invite.
			 */
			String surveyIdString = theStudy.db.getCurrentSurveyIdString();
			MessageSequence[] msgSeqs = theStudy.preface
					.getMessageSequences(surveyIdString);
			if (msgSeqs.length == 0) {
			    pw.println("No message sequences found in Preface file for selected Survey");
			}
			String msgUseId = theStudy.sendInviteReturnMsgSeqId("invite",
					msgSeqs[0].id, surveyIdString, " invitee.id in ( " + userId
					+ " )", false);
			request.setAttribute("msg", msgUseId);
			StringBuffer destination = new StringBuffer();
			destination.append("/WISE/survey").append("?msg=").append(msgUseId)
					.append("&t=" + WISEApplication.encode(theStudy.id)).append("&n=true");
			response.sendRedirect(destination.toString());		
		} else {
			
			/* code added so that each session is considered as different session. */
			pw.write("<html><body>There is something wrong with the session please try again</body><html>");
		    pw.close();
		    return;
		}
    }
}
