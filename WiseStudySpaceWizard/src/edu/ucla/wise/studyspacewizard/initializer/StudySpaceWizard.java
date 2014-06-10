package edu.ucla.wise.studyspacewizard.initializer;

import edu.ucla.wise.studyspacewizard.StudySpaceWizardProperties;
import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;

public class StudySpaceWizard {

    private static StudySpaceWizard studySpaceWizard;

    private final StudySpaceWizardProperties properties;
    private final DatabaseConnector databaseConnector;

    private StudySpaceWizard(StudySpaceWizardProperties properties) {
        this.properties = properties;
        this.databaseConnector = new DatabaseConnector(this.properties);

    }

    public static StudySpaceWizard getInstance() {
        return studySpaceWizard;
    }

    public static void initialize(StudySpaceWizardProperties properties) {

        studySpaceWizard = new StudySpaceWizard(properties);

    }

    public static void destroy() {

    }

    public DatabaseConnector getDatabaseConnector() {
        return this.databaseConnector;
    }

    public StudySpaceWizardProperties getStudySpaceWizardProperties() {
        return this.properties;
    }

}
