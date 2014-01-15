package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.ConsentForm;
import edu.ucla.wise.commons.IRBSet;
import edu.ucla.wise.commons.Preface;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.Survey;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WelcomePage;

/**
 * WelcomeGenerateServlet generates the welcome page before displaying the consent form.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
@WebServlet("/survey/welcome")
public class WelcomeGenerateServlet extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Generates the welcome page. 
     * Also checks if there is consent form based on the ird ID.
     *  
     * @param 	req	 HTTP Request.
     * @param 	res	 HTTP Response.
     * @throws 	ServletException and IOException. 
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res)
    		throws ServletException, IOException {
	
    	/* prepare for writing */
		PrintWriter out;
		res.setContentType("text/html");
		out = res.getWriter();
	
		HttpSession session = req.getSession(true);
		
		/* if session is new, then show the session expired info */
		if (session.isNew()) {
		    res.sendRedirect(SurveyorApplication.sharedFileUrl + "error"
		    		+ SurveyorApplication.htmlExt);
		    return;
		}
	
		/* get the user from session */
		User theUser = (User) session.getAttribute("USER");
		StudySpace study_space = (StudySpace) session
				.getAttribute("STUDYSPACE");
		if (theUser == null || study_space == null) {
		    out.println("<p>Error: Can't find the user & study space.</p>");
		    return;
		}
	
		String error = null;
		Preface pf = study_space.get_preface();
		Survey currentSurvey = theUser.getCurrentSurvey();
		if (pf != null) {
		    if ((pf.irbSets.size() > 0 && theUser.getIrbId() == null)
		    		|| currentSurvey.id == null) {
				error = "Error: Cannot find your IRB or Survey ID ";
				WISEApplication.logError("WISE - WELCOME GENERATE: " + error, null);
				out.println("<p>" + error + "</p>");
				return;
		    }
	
		    WelcomePage wPage = pf.getWelcomePageSurveyIrb(
		    		currentSurvey.id, theUser.getIrbId());
		    if (wPage == null) {
				error = "Error: Can't find a default Welcome Page in the Preface for survey ID="
						+ currentSurvey.id
						+ " and IRB="
						+ theUser.getIrbId();
				WISEApplication.logError("WISE - WELCOME GENERATE: " + error, null);
				out.println("<p>" + error + "</p>");
				return;
		    }
	
		    // TODO: get a default logo if the IRB is empty
		    String title, banner, logo = "ucla.gif", aprNumb = null, expDate = null;
		    title = wPage.title;
		    banner = wPage.banner;
		    logo = wPage.logo;
	
		    /* check the irb set */
		    if (!theUser.getIrbId().equalsIgnoreCase("")) {
				IRBSet irbSet = pf.getIrbSet(theUser.getIrbId());
				if (irbSet != null) {
				    if (!irbSet.irbLogo.equalsIgnoreCase("")) {
				    	logo = irbSet.irbLogo;
				    }
					if (!irbSet.approvalNumber.equalsIgnoreCase("")) {
						aprNumb = irbSet.approvalNumber;
					}					
				    if (!irbSet.expirDate.equalsIgnoreCase("")) {
				    	expDate = irbSet.expirDate;
				    }					
				} else {
				    out.println("<p>Can't find the IRB with the number sepecified in welcome page</p>");
				    return;
				}
		    }
	
		    /* print out welcome page */
		    String welcomeHtml = "";
		    
		    /* compose the common header */
		    welcomeHtml += "<HTML><HEAD><TITLE>" + title
		    		+ " - Welcome</TITLE>";
		    welcomeHtml += "<META http-equiv=Content-Type content='text/html; charset=iso-8859-1'>";
		    welcomeHtml += "<LINK href='" + "styleRender?app="
		    		+ study_space.studyName + "&css=style.css"
		    		+ "' type=text/css rel=stylesheet>";
		    welcomeHtml += "<META content='MSHTML 6.00.2800.1170' name=GENERATOR></HEAD>";
		    
		    /* compose the top part of the body */
		    welcomeHtml += "<body><center>";
		    // welcome_html += "<body bgcolor=#FFFFCC text=#000000><center>";
		    welcomeHtml += "<table width=100% cellspacing=1 cellpadding=9 border=0>";
		    welcomeHtml += "<tr><td width=98 align=center valign=top><img src='"
		    		+ "imageRender?app="
		    		+ study_space.studyName
		    		+ "&img="
		    		+ logo + "' border=0 align=middle></td>";
		    welcomeHtml += "<td width=695 align=center valign=middle><img src='"
		    		+ "imageRender?app="
		    		+ study_space.studyName
		    		+ "&img="
		    		+ banner + "' border=0 align=middle></td>";
		    welcomeHtml += "<td rowspan=6 align=center width=280>&nbsp;</td></tr>";
		    welcomeHtml += "<tr><td width=98 rowspan=3>&nbsp;</td>";
		    welcomeHtml += "<td class=head>WELCOME</td></tr>";
		    welcomeHtml += "<tr><td width=695 align=left colspan=1>";
		    
		    /* get the welcome contents */
		    welcomeHtml += wPage.pageContents;
		    welcomeHtml += "</td></tr><tr>";
	
		    /*
		     * add the bottom part 
		     * lookup the consent form by user's irb id, otherwise, skip the consent form
		     */
		    ConsentForm cForm = null;
		    if (!theUser.getIrbId().equalsIgnoreCase("")) {
				cForm = pf.getConsentFormSurveyIrb(
						currentSurvey.id, theUser.getIrbId());
		    }	
		    if (cForm != null) {
				welcomeHtml += "<td width=695 align=center colspan=1><a href='"
						+ SurveyorApplication.servletUrl
						+ "consent_generate'><img src='"
						+ "imageRender?img=continue.gif' border=0 align=absmiddle></a></td>";
		    } else {
				welcomeHtml += "<td width=695 align=center colspan=1><a href='"
						+ SurveyorApplication.servletUrl
						+ "consent_record?answer=no_consent'><img src='"
						+ "imageRender?img=continue.gif' border=0 align=absmiddle></a></td>";
		    }
		    welcomeHtml += "</tr>";
	
		    /* if there are the expriation date and approval date found in IRB */
		    if (expDate != null && aprNumb != null) {
				welcomeHtml += "<tr><td><p align=left><font size=2><b>IRB Number: "
						+ aprNumb + "<br>";
				welcomeHtml += "Expiration Date: " + expDate
						+ "</b></font></p>";
				welcomeHtml += "</td></tr>";
		    }
		    welcomeHtml += "</table></center></body></html>";
		    
		    /* print out the html form */
		    out.println(welcomeHtml);
	
		} else {
		    error = "Error: Can't get the preface";
		}
	
		if (error != null) {
		    WISEApplication.logError("WISE - WELCOME GENERATE: " + error,
		    		null);
		    out.println("<p>" + error + "</p>");
		}
		out.close();
		theUser.recordWelcomeHit();
		return;
    }
}
