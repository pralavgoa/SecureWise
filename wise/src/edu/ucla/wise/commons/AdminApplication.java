package edu.ucla.wise.commons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.initializer.StudySpaceParametersProvider;
import edu.ucla.wise.initializer.WiseProperties;
import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;

/*
 Admin information set -- 
 The class represents that Admin application
 Instances represent administrator user sessions
 TODO (med): untangle Frank's survey uploading spaghetti in load_data.jsp
 */
/**
 * This class represents the Admin information when running the Admin
 * application. Instance represent administrator user session.
 * 
 * @author mrao
 * @author dbell
 * @author ssakdeo
 */
public class AdminApplication extends WISEApplication {

	private static AdminApplication adminApplication;

	private final Logger log = Logger.getLogger(AdminApplication.class);

	private final String dbBackupPath;
	private final String styleRootPath;
	private final String imageRootPath;


	// 08-Nov-2009
	// Pushed down into the children classes like Surveyor_Application
	// and AdminInfo as static in the children
	// Make Surveyor_Application a singleton class per JBOSS
	public static String ApplicationName = null;

	public static String sharedFileUrl;
	public static String sharedImageUrl;
	public static String servletUrl;

	public AdminApplication(String appContext, WiseProperties properties){
		super(properties);
		AdminApplication.initStaticFields(appContext);
		imageRootPath = WISEApplication.wiseProperties.getStringProperty("shared_image.path");
		styleRootPath = WISEApplication.wiseProperties.getStringProperty("shared_style.path");
		dbBackupPath = WISEApplication.wiseProperties.getStringProperty("db_backup.path")
				+ System.getProperty("file.separator");

	}

	/**
	 * Initializes the static instance fields of the class.
	 * 
	 * @param appContext	Name of the application context.
	 */
	public static void initStaticFields(String appContext) {
		if (ApplicationName == null) {
			ApplicationName = appContext;
		}
		sharedFileUrl = WISEApplication.rootURL + "/" + ApplicationName
				+ "/" + WISEApplication.sharedFilesLink + "/";
		sharedImageUrl = sharedFileUrl + "images/";
		servletUrl = WISEApplication.rootURL + ApplicationName + "/";
	}

	public static String forceInit(String appContext, WiseProperties properties) throws IOException {
		String initErr = null;
		initialize(appContext, properties);
		if (ApplicationName == null) // *still* null means uninitialized
			initErr = "Wise Admin Application -- uncaught initialization error";
		return initErr;
	}

	public static void initialize(String appContext, WiseProperties properties) throws IOException {
		if(adminApplication == null){
			adminApplication = new AdminApplication(appContext,properties);
		}
	}
	
	public static AdminApplication getInstance(){
		if(adminApplication == null){
			throw new IllegalStateException("The admin application is not initialized");
		}
		return adminApplication;
	}
	/**
	 * Logs the message and the exception stack trace.
	 * 
	 * @param message	The message to be printed.
	 * @param e			Exception to be logged.
	 */
	public static void logError(String message, Exception e) {
		WISEApplication.logError(message, e);
	}



	/**
	 * Decodes a string that has been encoded by encode function.
	 * 
	 * @param 	charId	String to decode
	 * @return	String	Decoded string.	
	 */
	@Deprecated
	public static String decode(String charId) {
		String result = new String();
		int sum = 0;
		for (int i = charId.length() - 1; i >= 0; i--) {
			char c = charId.charAt(i);
			int remainder = c - 65;
			sum = sum * 26 + remainder;
		}

		sum = sum - 97654;
		int remain = sum % 31;
		if (remain == 0) {
			sum = sum / 31;
			result = Integer.toString(sum);
		} else {
			result = "invalid";
		}
		return result;
	}

	/**
	 * Encodes a given string.
	 * 
	 * @param 	userId	String to encode.
	 * @return	String	Encoded string.
	 */
	@Deprecated
	public static String encode(String userId) {
		int baseNumb = Integer.parseInt(userId) * 31 + 97654;
		String s1 = Integer.toString(baseNumb);
		String s2 = Integer.toString(26);
		BigInteger b1 = new BigInteger(s1);
		BigInteger b2 = new BigInteger(s2);

		int counter = 0;
		String charId = new String();
		while (counter < 5) {
			BigInteger[] bs = b1.divideAndRemainder(b2);
			b1 = bs[0];
			int encodeValue = bs[1].intValue() + 65;
			charId = charId + (new Character((char) encodeValue).toString());
			counter++;
		}
		return charId;
	}

	public String getDbBackupPath() {
		return dbBackupPath;
	}

	public String getStyleRootPath() {
		return styleRootPath;
	}

	public String getImageRootPath() {
		return imageRootPath;
	}
}