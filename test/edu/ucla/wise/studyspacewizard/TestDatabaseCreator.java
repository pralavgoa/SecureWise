package edu.ucla.wise.studyspacewizard;

import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;

public class TestDatabaseCreator {
	public static void main(String[] args){
		if(!DatabaseConnector.createDatabase("testingDatabase")){
			System.out.println("Database creation failed");;
		}
	}
}
