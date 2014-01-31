package edu.ucla.wise.common;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import edu.ucla.wise.commons.DataBank;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.initializer.WiseProperties;
import edu.ucla.wise.studyspace.parameters.DatabaseRelatedConstants;
import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;

public class DataBankTest {

	private static final String TEST_MSG_ID = "wNkpC63u6w3mcj8JQgvGDq";

	@Test
	public void testMakeUserFromMessageId() throws SQLException{
		DataBank mockDB =  getMockDataBank();
		User user = mockDB.makeUserFromMsgID(TEST_MSG_ID);

		user.getId();
	}
	
	private DataBank getMockDataBank(){
		
		Map<String,String> parameters = new HashMap<>();
		parameters.put(DatabaseRelatedConstants.DATABASE_NAME, "wisedev");
		parameters.put(DatabaseRelatedConstants.DATABASE_USER, "wisedev");
		parameters.put(DatabaseRelatedConstants.DATABASE_PASSWORD, "password");
		
		DataBank.SetupDB(new WiseProperties("conf/dev/wise.properties","WISE"));
		
		return new DataBank(null, new MockStudySpaceParams(parameters));
	}

	class MockStudySpaceParams extends StudySpaceParameters{

		public MockStudySpaceParams(Map<String, String> parametersMap) {
			super(parametersMap);
		}
		
	}

}
