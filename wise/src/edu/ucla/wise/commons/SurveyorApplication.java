package edu.ucla.wise.commons;

import java.io.IOException;

import edu.ucla.wise.initializer.WiseProperties;

/**
 * Class to represent common elements for *this* local installation of the 
 * wise surveyor java application NOT the possibly-remote surveyor information 
 * for a given StudySpace. Never instantiated; just initialize static variables.
 * 
 * @author Douglas Bell
 * @version 1.0
 */
public class SurveyorApplication extends WISEApplication {

	private static SurveyorApplication surveyorApplication;

	public static final String sharedFileUrlRef = "#SHAREDFILEURL#";
	public static final String sharedImageUrlRef = "#SHAREDIMAGEURL#";

	public static String ApplicationName = "WISE";
	private final String sharedFileUrl;
	private final String sharedImageUrl;
	private final String servletUrl;

	public final String initErrorHtmlHead;

	public static final String initErrorHtmlFoot = "</td></tr></table></center></body></html>";
	


	public SurveyorApplication(String appContext, WiseProperties properties){
		super(properties);

		if (ApplicationName == null) {
			SurveyorApplication.ApplicationName = appContext;
		}
		sharedFileUrl = WISEApplication.rootURL + "/"
				+ SurveyorApplication.ApplicationName + "/"
				+ WISEApplication.sharedFilesLink + "/";
		sharedImageUrl = sharedFileUrl + "images/";
		servletUrl = WISEApplication.rootURL + "/"
				+ SurveyorApplication.ApplicationName + "/" + "survey" + "/";

		initErrorHtmlHead = "<HTML><HEAD><TITLE>WISE survey system -- Startup error</TITLE>"
				+ "<LINK href='"
				+ sharedFileUrl
				+ "style.css' type=text/css rel=stylesheet>"
				+ "<body text=#000000 bgColor=#ffffcc><center><table>"
				+ "<tr><td>Sorry, the WISE Surveyor application failed to initialize. "
				+ "Please contact the system administrator with the following information.";
		
		if ((ApplicationName == null)
				|| (servletUrl == null)
				|| (sharedFileUrl == null)) {
			throw new IllegalArgumentException("They survey application is not initialized correctly");
		}
	}


	/**
	 * Checks if the static variables are initialized or not.
	 * If not the it initializes then in case of error it responds with error message.
	 * 
	 * @param  appContext  	Name of the application.
	 * @return String		Error message in case of error or null 
	 * @throws IOException
	 */
	public static void initialize(String appContext, WiseProperties properties) throws IOException {
		if(surveyorApplication == null){
			surveyorApplication = new SurveyorApplication(appContext,properties);
		}
	}
	
	public static void reload(String appContext, WiseProperties properties){
		surveyorApplication = new SurveyorApplication(appContext,properties);
	}
	
	public static SurveyorApplication getInstance(){
		return surveyorApplication;
	}


	public static String getSharedfileurlref() {
		return sharedFileUrlRef;
	}


	public static String getSharedimageurlref() {
		return sharedImageUrlRef;
	}


	public String getSharedFileUrl() {
		return sharedFileUrl;
	}


	public String getSharedImageUrl() {
		return sharedImageUrl;
	}


	public String getServletUrl() {
		return servletUrl;
	}

}
