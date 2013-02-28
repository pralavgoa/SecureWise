package edu.ucla.wise.studyspacewizard;

import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;

public class TestStudySpaceParameters {
	public static void main(String[] args){
		System.out.println(DatabaseConnector.getStudySpaceParamsAsJSON("test"));
	}
}
