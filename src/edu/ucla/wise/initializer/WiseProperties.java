package edu.ucla.wise.initializer;
public class WiseProperties extends AbstractWiseProperties{

	private static final long serialVersionUID = 1L;

    public static String EMAIL_FROM = "wise.email.from";
    public static String ALERT_EMAIL = "alert.email";
    public static String EMAIL_HOST= "email.host";
    public static String EMAIL_USERNAME= "SMTP_AUTH_USER";
    public static String EMAIL_PASSWORD= "SMTP_AUTH_PASSWORD";
    public static String ADMIN_SERVER= "admin.server";
    public static String IMAGES_PATH= "shared_image.path";
    public static String STYLES_PATH= "shared_style.path";

    public static String SSL_EMAIL= "email.ssl";
	
    public static String XML_LOC = "xml_root.path";
    
	public WiseProperties(String fileName, String applicationName) {
		super(fileName, applicationName);
	}
	
	public String getStylesPath(){
		return getStringProperty(STYLES_PATH);
	}
	
	public String getImagesPath(){
		return getStringProperty(IMAGES_PATH);
	}
	
	public String getAdminServer(){
		return getStringProperty(ADMIN_SERVER);
	}
	
	public String getEmailUsername(){
		return getStringProperty(EMAIL_USERNAME);
	}
	public String getEmailPassword(){
		return getStringProperty(EMAIL_PASSWORD);
	}
	public String getEmailFrom(){
		return getStringProperty(EMAIL_FROM);
	}
	
	public String getAlertEmail(){
		return getStringProperty(ALERT_EMAIL);
	}
	
	public String getEmailHost(){
		return getStringProperty(EMAIL_HOST);
	}
	
	public boolean useSslEmail(){
		return "true".equalsIgnoreCase(getStringProperty(SSL_EMAIL));
	}
	
	public String getXmlRootPath(){
		return getStringProperty(XML_LOC);
	}

}
