package edu.ucla.wise.studyspacewizard;

import org.apache.log4j.Logger;


public class StudySpaceWizardProperties {

	private static Logger log = Logger
			.getLogger(StudySpaceWizardProperties.class);

	/*
	 * private final ResourceBundle properties;
	 * 
	 * public StudySpaceWizardProperties() { properties =
	 * ResourceBundle.getBundle("studyspacewizard", Locale.getDefault());
	 * log.info("The username provided is " +
	 * properties.getString("database.root.username")); }
	 * 
	 * public String getDatabaseRootUsername() { return
	 * properties.getString("database.root.username"); }
	 * 
	 * public String getDatabaseRootPassword() { return
	 * properties.getString("database.root.password"); }
	 * 
	 * public String getDatabaseServerHost() { return
	 * properties.getString("database.server.host"); }
	 */

	public String getDatabaseRootUsername() {
		return "admin";
	}

	public String getDatabaseRootPassword() {
		return "eightball";
	}

	public String getDatabaseServerHost() {
		return "localhost";
	}

}
