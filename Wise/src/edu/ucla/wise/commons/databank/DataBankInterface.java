package edu.ucla.wise.commons.databank;

import java.sql.Connection;
import java.sql.SQLException;

import edu.ucla.wise.persistence.data.WiseTables;

public interface DataBankInterface {

    Connection getDBConnection() throws SQLException;

    String getStudySpace();

    WiseTables getWiseTables();

}
