package edu.ucla.wise.commons.databank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

public class ResultDataProviderTest {

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
