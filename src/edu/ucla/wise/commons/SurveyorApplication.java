package edu.ucla.wise.commons;

import java.io.IOException;

/**
 * Class to represent common elements for *this* local installation of the 
 * wise surveyor java application NOT the possibly-remote surveyor information 
 * for a given StudySpace. Never instantiated; just initialize static variables.
 * 
 * @author Douglas Bell
 * @version 1.0
 */
public class SurveyorApplication extends WISEApplication {

    public static final String sharedFileUrlRef = "#SHAREDFILEURL#";
    public static final String sharedImageUrlRef = "#SHAREDIMAGEURL#";

    /* 08-Nov-2009
     * Pushed down into the children classes like Surveyor_Application
     * and AdminInfo as static in the children
     * Make Surveyor_Application a singleton class per JBOSS
     */
    public static String ApplicationName = "WISE";
    public static String sharedFileUrl;
    public static String sharedImageUrl;
    public static String servletUrl;
    // public static final String html_ext = ".htm";
    
    /**
     * Initializing the static variables. Default of the variables are declared
     * in WiseApplication but can override for a given Surveyor app if necessary
     * 
     * @param appContext	Name of the application.
     */
    public static void initStaticFields(String appContext) {
		if (ApplicationName == null) {
		    SurveyorApplication.ApplicationName = appContext;
		}
		sharedFileUrl = WISEApplication.rootURL + "/"
				+ SurveyorApplication.ApplicationName + "/"
				+ WISEApplication.sharedFilesLink + "/";
		sharedImageUrl = SurveyorApplication.sharedFileUrl + "images/";
		servletUrl = WISEApplication.rootURL + "/"
				+ SurveyorApplication.ApplicationName + "/" + "survey" + "/";
    }

    /**
     * Checks if the static variables are initialized or not.
     * If not the it initializes then in case of error it responds with error message.
     * 
     * @param  appContext  	Name of the application.
     * @return String		Error message in case of error or null 
     * @throws IOException
     */
    public static String checkInit(String appContext) throws IOException {
		if ((SurveyorApplication.ApplicationName == null)
				|| (SurveyorApplication.servletUrl == null)
				|| (SurveyorApplication.sharedFileUrl == null)) {
		    String initErr = null;
		    
		    // Surveyor_Application.ApplicationName = appContext;
		    WISEApplication.initialize();
		    SurveyorApplication.initStaticFields(appContext);
	
		    if (SurveyorApplication.ApplicationName == null) {
		    	// *still* null means uninitialized
		    	initErr = "Wise Surveyor Application -- uncaught initialization error";
		    }
		    return initErr;
		} else
		    return null;
		/*
		 * String retVal = initErrorHtmlHead+"<P>"+ appContext +"</p>"; return
		 * retVal; } else {
		 */
    }

    // else
    // {
    // Surveyor_Application newApp = new Surveyor_Application(appContext);
    // /*
    // String initErr = WISE_Application.initialize(appContext);
    // newApp.ApplicationName = appContext;
    // if(newApp.ApplicationName == null)
    // {
    // initErr = "Wise Surveyor Application -- uncaught initialization error";
    // }
    // else
    // {
    // initErr = initErrorHtmlHead+"<P>"+ initErr +"</p>";
    // }
    // //Manoj
    // newApp.servlet_url = rootURL + newApp.ApplicationName + "/";
    // //Manoj
    // newApp.shared_file_url = rootURL + newApp.ApplicationName + "/" +
    // localProps.getProperty("default.sharedFiles_linkName") + "/";
    // System.out.println("Manoj:Shared File URL is initialised to [" +
    // newApp.shared_file_url + "]");
    // newApp.shared_image_url = newApp.shared_file_url + "images/";
    // */
    // WISE_Application.AppInstanceTbl.put(appContext, newApp);
    // String initErr = newApp.ApplicationName;
    // if(newApp.ApplicationName == null)
    // {
    // //initErr +=
    // "Wise Surveyor Application -- uncaught initialization error";
    // initErr += initErrorHtmlHead+"<P>"+ initErr +"</p>";
    // }
    // else
    // {
    // initErr = null;
    // }
    //
    // return initErr;
    // //String initErr = initialize(appContext);
    // }
    //
    // if()
    // return null;
    /*
     * String initErr = null; if (ApplicationName == null) initErr =
     * initialize(appContext); if (ApplicationName == null) // *still* null
     * means uninitialized initErr =
     * "Wise Surveyor Application -- uncaught initialization error"; if (initErr
     * != null) //wrap error message, leaving page open for calling servlet to
     * add info initErr = initErrorHtmlHead+"<P>"+ initErr +"</p>"; return
     * initErr;
     */
    // }

    /**
     * Forces the initialization of the static variables.
     * If not the it initializes then in case of error it responds with error message.
     * 
     * @param  appContext  	Name of the application.
     * @return String		Error message in case of error or null 
     * @throws IOException
     */
    public static String forceInit(String appContext) throws IOException {
		String initErr = null;
		WISEApplication.initialize();
		SurveyorApplication.initStaticFields(appContext);
		
		// Surveyor_Application insApp =
		// WISE_Application.AppInstanceTbl.get(appContext);
		
		/* *still* null means uninitialized */
		if (SurveyorApplication.ApplicationName == null) {
		    initErr = "Wise Surveyor Application -- uncaught initialization error";
		}
		return initErr;
	 }
	
	 /**
	  * Constructor for the class. This won't be called probably as this class is never instantiated.
	  * @param 	appContext	Name of the application.
	  * @throws IOException
	  */
	 public SurveyorApplication(String appContext) throws IOException {
	
        /*String initErr = WISEApplication.initialize();
		Surveyor_Application.initStaticFields(appContext);
		this.ApplicationName = appContext;
		if (this.ApplicationName == null) {
		    initErr = "Wise Surveyor Application -- uncaught initialization error";
		} else {
		    initErr = initErrorHtmlHead + "<P>" + initErr + "</p>";
		}
		// Manoj
		// WISE_Application.servlet_url = rootURL + this.ApplicationName + "/";
		// Manoj
		// WISE_Application.shared_file_url = rootURL +
		// WISE_Application.ApplicationName + "/" +
		// localProps.getProperty("default.sharedFiles_linkName") + "/";
		Surveyor_Application.sharedImageUrl = Surveyor_Application.sharedFileUrl
			+ "images/";*/
	 }

    /*
     * public static String initialize(String appContext) { String initErr =
     * WISE_Application.initialize(appContext); //Manoj servlet_url = rootURL +
     * ApplicationName + "/"; //Manoj shared_file_url = rootURL +
     * ApplicationName + "/" +
     * localProps.getProperty("default.sharedFiles_linkName") + "/";
     * System.out.println("Manoj:Shared File URL is initialised to [" +
     * shared_file_url + "]"); shared_image_url = shared_file_url + "images/";
     * return initErr; }
     */

    public static String initErrorHtmlHead = "<HTML><HEAD><TITLE>WISE survey system -- Startup error</TITLE>"
    		+ "<LINK href='"
    		+ SurveyorApplication.sharedFileUrl
    		+ "style.css' type=text/css rel=stylesheet>"
    		+ "<body text=#000000 bgColor=#ffffcc><center><table>"
    		+ "<tr><td>Sorry, the WISE Surveyor application failed to initialize. "
    		+ "Please contact the system administrator with the following information.";
    
    public static String initErrorHtmlFoot = "</td></tr></table></center></body></html>";

}
