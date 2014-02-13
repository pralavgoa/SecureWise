package edu.ucla.wise.studyspacewizard;

import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;

public class TestStudySpaceParameters {
	public static void main(String[] args){
		DatabaseConnector databaseConnector = new DatabaseConnector();
		System.out.println(databaseConnector.getStudySpaceParameters("test"));
	}
}
