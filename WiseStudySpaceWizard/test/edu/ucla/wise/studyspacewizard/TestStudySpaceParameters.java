package edu.ucla.wise.studyspacewizard;

import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;
import edu.ucla.wise.studyspacewizard.initializer.StudySpaceWizard;

public class TestStudySpaceParameters {
    public static void main(String[] args) {
        StudySpaceWizard.initialize();
        DatabaseConnector databaseConnector = StudySpaceWizard.getInstance().getDatabaseConnector();
        System.out.println(databaseConnector.getStudySpaceParameters("test"));
    }
}
