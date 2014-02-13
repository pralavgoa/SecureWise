package edu.ucla.wise.commons;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import edu.ucla.wise.initializer.WiseProperties;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

/**
 * Class to represent common elements for *this* local installation of the wise
 * surveyor java application NOT the possibly-remote surveyor information for a
 * given StudySpace. Never instantiated; just initialize static variables.
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

    private final String rootFolderPath;

    private final Configuration htmlTemplateConfiguration;

    public static final String initErrorHtmlFoot = "</td></tr></table></center></body></html>";

    public SurveyorApplication(String appContext, String rootFolderPath,
	    WiseProperties properties) throws IOException {
	super(properties);

	if (ApplicationName == null) {
	    SurveyorApplication.ApplicationName = appContext;
	}
	this.sharedFileUrl = WISEApplication.rootURL + "/"
		+ SurveyorApplication.ApplicationName + "/"
		+ WISEApplication.sharedFilesLink + "/";
	this.sharedImageUrl = this.sharedFileUrl + "images/";
	this.servletUrl = WISEApplication.rootURL + "/"
		+ SurveyorApplication.ApplicationName + "/" + "survey" + "/";

	this.initErrorHtmlHead = "<HTML><HEAD><TITLE>WISE survey system -- Startup error</TITLE>"
		+ "<LINK href='"
		+ this.sharedFileUrl
		+ "style.css' type=text/css rel=stylesheet>"
		+ "<body text=#000000 bgColor=#ffffcc><center><table>"
		+ "<tr><td>Sorry, the WISE Surveyor application failed to initialize. "
		+ "Please contact the system administrator with the following information.";

	if ((ApplicationName == null) || (this.servletUrl == null)
		|| (this.sharedFileUrl == null)) {
	    throw new IllegalArgumentException(
		    "They survey application is not initialized correctly");
	}

	this.rootFolderPath = rootFolderPath;
	this.htmlTemplateConfiguration = this
		.createHtmlTemplateConfiguration(rootFolderPath);
    }

    private Configuration createHtmlTemplateConfiguration(String rootFolderPath)
	    throws IOException {
	Configuration cfg = new Configuration();
	cfg.setDirectoryForTemplateLoading(new File(rootFolderPath
		+ "survey/templates"));
	cfg.setIncompatibleImprovements(new Version(1, 0, 0));
	cfg.setDefaultEncoding("UTF-8");
	cfg.setLocale(Locale.US);
	cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	return cfg;
    }

    /**
     * Checks if the static variables are initialized or not. If not the it
     * initializes then in case of error it responds with error message.
     * 
     * @param appContext
     *            Name of the application.
     * @return String Error message in case of error or null
     * @throws IOException
     */
    public static void initialize(String appContext, String rootFolderPath,
	    WiseProperties properties) throws IOException {
	if (surveyorApplication == null) {
	    surveyorApplication = new SurveyorApplication(appContext,
		    rootFolderPath, properties);
	}
    }

    public static void reload(String appContext, WiseProperties properties)
	    throws IOException {
	surveyorApplication = new SurveyorApplication(appContext,
		SurveyorApplication.getInstance().getRootFolderPath(),
		properties);
    }

    public static SurveyorApplication getInstance() {
	return surveyorApplication;
    }

    public static String getSharedfileurlref() {
	return sharedFileUrlRef;
    }

    public static String getSharedimageurlref() {
	return sharedImageUrlRef;
    }

    public String getSharedFileUrl() {
	return this.sharedFileUrl;
    }

    public String getSharedImageUrl() {
	return this.sharedImageUrl;
    }

    public String getServletUrl() {
	return this.servletUrl;
    }

    public Configuration getHtmlTemplateConfiguration() {
	return this.htmlTemplateConfiguration;
    }

    public String getRootFolderPath() {
	return this.rootFolderPath;
    }
}
