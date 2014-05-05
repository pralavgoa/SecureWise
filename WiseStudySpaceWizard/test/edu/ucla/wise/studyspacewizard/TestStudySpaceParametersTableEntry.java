package edu.ucla.wise.studyspacewizard;

import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;
import edu.ucla.wise.studyspacewizard.initializer.StudySpaceWizard;

public class TestStudySpaceParametersTableEntry {

    public static void main(String[] args) {
        StudySpaceWizard.initialize();
        DatabaseConnector databaseConnector = StudySpaceWizard.getInstance().getDatabaseConnector();
        if (!databaseConnector.writeStudySpaceParams("studySpaceName", "serverURL", "serverAppName",
                "serverSharedLinkName", "directoryName", "dbUsername", "dbName", "dbPassword", "projectTitle",
                "databaseEncryptionKey")) {
            System.out.println("Failed to write parameters to the database");
            ;
        } else {
            System.out.println("Wrote parameters to the database successfully");
        }
    }
}
