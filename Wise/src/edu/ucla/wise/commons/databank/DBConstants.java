package edu.ucla.wise.commons.databank;

public class DBConstants {
    public static final String SURVEY_COMPLETED_STATUS = "survey_completed_status";

    public static String mysqlServer;
    public static final String dbDriver = "jdbc:mysql://";
    public static final String MainTableExtension = "_data";

    public static final char intValueTypeFlag = 'n';
    public static final char textValueTypeFlag = 'a';
    public static final char decimalValueTypeFlag = 'd';

    public static final String intFieldDDL = " int(6),";
    public static final String textFieldDDL = " text,";
    public static final String decimalFieldDDL = " decimal(11,3),";
}
