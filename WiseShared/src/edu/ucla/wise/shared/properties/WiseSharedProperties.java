package edu.ucla.wise.shared.properties;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class WiseSharedProperties {
	public static final String DATABASE_DRIVER_NAME = "com.mysql.jdbc.Driver";
	public static final String DATABASE_URL = "jdbc:mysql://localhost/";

	private static final ResourceBundle properties = ResourceBundle.getBundle(
			"wise_shared", Locale.getDefault());

	private static Logger log = Logger.getLogger(WiseSharedProperties.class);

	public static String getDatabaseUsername() {
		log.info("DB username is "
				+ properties.getString("database.root.username"));
		return properties.getString("database.root.username");
	}

	public static String getDatabasePassword() {
		return properties.getString("database.root.password");
	}
}
