package edu.ucla.wise.commons.databank;

import java.sql.Connection;
import java.sql.SQLException;

public interface DataBankInterface {

    Connection getDBConnection() throws SQLException;

}
