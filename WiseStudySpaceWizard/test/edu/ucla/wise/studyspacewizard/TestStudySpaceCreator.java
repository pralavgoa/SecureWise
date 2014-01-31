package edu.ucla.wise.studyspacewizard;

public class TestStudySpaceCreator {

	public static void main(String[] args){
		
		
		if (!StudySpaceCreator.createStudySpace("testingSSCreator", "test",
				"SqlScripts\\studydb_template.sql")) {
			System.out.println("Study space creator failed");;
		}
		
		
	}
	
}
