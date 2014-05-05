package edu.ucla.wise.studyspacewizard.initializer;

import edu.ucla.wise.studyspacewizard.StudySpaceWizardProperties;
import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;

public class StudySpaceWizard {

    private static StudySpaceWizard studySpaceWizard;

    private final StudySpaceWizardProperties studySpaceWizardProperties;
    private final DatabaseConnector databaseConnector;

    private StudySpaceWizard() {
        this.studySpaceWizardProperties = new StudySpaceWizardProperties();
        this.databaseConnector = new DatabaseConnector(this.studySpaceWizardProperties);

    }

    public static StudySpaceWizard getInstance() {
        return studySpaceWizard;
    }

    public static void initialize() {

        studySpaceWizard = new StudySpaceWizard();

    }

    public static void destroy() {

    }

    public DatabaseConnector getDatabaseConnector() {
        return this.databaseConnector;
    }

    public StudySpaceWizardProperties getStudySpaceWizardProperties() {
        return this.studySpaceWizardProperties;
    }

}
