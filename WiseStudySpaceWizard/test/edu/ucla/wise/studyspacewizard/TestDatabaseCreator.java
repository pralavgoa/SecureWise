package edu.ucla.wise.studyspacewizard;

import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;
import edu.ucla.wise.studyspacewizard.initializer.StudySpaceWizard;

public class TestDatabaseCreator {
    public static void main(String[] args) {
        StudySpaceWizard.initialize();
        DatabaseConnector databaseConnector = StudySpaceWizard.getInstance().getDatabaseConnector();
        if (!databaseConnector.createDatabase("testingDatabase")) {
            System.out.println("Database creation failed");
            ;
        }
    }
}
