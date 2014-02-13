/**
 * 
 */
package edu.ucla.wise.studyspace.parameters;

import com.google.gson.Gson;

import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;

/**
 * @author Pralav
 *
 *This class will be responsible to provide parameters to various study spaces when they contact it.
 * *
 */
public class StudySpaceParametersProvider {

	public String getParametersForStudySpace(String studySpaceName){
		
		//Construct a json like string with study space parameters
		
		//Select statement for the database;
		
		DatabaseConnector databaseConnector = new DatabaseConnector();
		
		return new Gson().toJson(databaseConnector.getStudySpaceParameters(studySpaceName));	
		
	}
	
}
