package edu.ucla.wise.studyspacewizard;

import org.apache.log4j.Logger;

import edu.ucla.wise.shared.properties.AbstractWiseProperties;
import edu.ucla.wise.studyspace.parameters.StudySpaceDatabaseProperties;

public class StudySpaceWizardProperties extends AbstractWiseProperties implements StudySpaceDatabaseProperties {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(StudySpaceWizardProperties.class);
    private static final String WISE_STUDY_SPACE_WIZARD_HOME = "WISE_SSW_HOME";
    private static final String WISE_STUDY_SPACE_WIZARD_HOME_PATH = System.getenv(WISE_STUDY_SPACE_WIZARD_HOME);

    public StudySpaceWizardProperties() {
        super(WISE_STUDY_SPACE_WIZARD_HOME_PATH + "studyspacewizard.properties", "StudySpaceWizard");
        log.info("The username provided is " + this.getStringProperty("database.root.username"));
    }

    @Override
    public String getDatabaseRootUsername() {
        return this.getStringProperty("database.root.username");
    }

    @Override
    public String getDatabaseRootPassword() {
        return this.getStringProperty("database.root.password");
    }

    public String getDatabaseServerHost() {
        return this.getStringProperty("database.server.host");
    }
}
