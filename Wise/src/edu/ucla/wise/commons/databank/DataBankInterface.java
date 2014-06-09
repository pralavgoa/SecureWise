package edu.ucla.wise.commons.databank;

import java.sql.Connection;
import java.sql.SQLException;

import edu.ucla.wise.persistence.data.WiseTables;
import edu.ucla.wise.utils.SQLTemplateUtil;

public interface DataBankInterface {

    Connection getDBConnection() throws SQLException;

    String getStudySpace();

    WiseTables getWiseTables();

    SQLTemplateUtil getSqlTemplateUtil();

}
