package edu.ucla.wise.commons.databank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import edu.ucla.wise.persistence.data.WiseTables;

public class ResultDataProviderTest {

    private static final String STUDY_NAME = "wisedev";

    @Test
    public void getDistictQuestionIdsTest() {
        String surveyName = "wisedev";
        int questionLevel = 0;
        ResultDataProvider rdp = new ResultDataProvider(new DataBankTest());
        List<String> questionIds = rdp.getDistinctQuestionIds(surveyName, questionLevel);
        System.out.println(questionIds);
        List<String> inviteeIds = rdp.getDistinctInviteeIdsFromDataTable(surveyName, questionLevel);
        System.out.println(inviteeIds);

        System.out.println(rdp.getInviteeAnswersTable(surveyName, 0));

        System.out.println(rdp.getRepeatSetData(surveyName, "repeat_set_publications"));
    }

    class DataBankTest implements DataBankInterface {
        private Connection connection = null;

        @Override
        public Connection getDBConnection() throws SQLException {
            if (this.connection == null) {
                this.connection = DriverManager.getConnection(getTestConnectionParameterString());
            }
            return this.connection;
        }

        @Override
        public String getStudySpace() {
            return STUDY_NAME;
        }

        @Override
        public WiseTables getWiseTables() {
            return new WiseTables(STUDY_NAME);
        }

    }

    private static String getTestConnectionParameterString() {
        try {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        } catch (SQLException e) {
            System.out.println("DataBank init Error");
            e.printStackTrace();
        }
        return "jdbc:mysql://localhost:3306/wisedev?user=wisedev&password=password&autoReconnect=true";
    }
}
