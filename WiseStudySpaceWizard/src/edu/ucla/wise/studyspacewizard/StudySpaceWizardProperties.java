package edu.ucla.wise.studyspacewizard;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import edu.ucla.wise.studyspace.parameters.StudySpaceDatabaseProperties;


public class StudySpaceWizardProperties implements StudySpaceDatabaseProperties{

	private static Logger log = Logger
			.getLogger(StudySpaceWizardProperties.class);


	private final ResourceBundle properties;
	
	public StudySpaceWizardProperties() { 
		properties =
			ResourceBundle.getBundle("studyspacewizard", Locale.getDefault());
	log.info("The username provided is " +
			properties.getString("database.root.username")); 
	}

	public String getDatabaseRootUsername() { return
			properties.getString("database.root.username"); }

	public String getDatabaseRootPassword() { return
			properties.getString("database.root.password"); }

	public String getDatabaseServerHost() { return
			properties.getString("database.server.host"); }
}
