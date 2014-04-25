package edu.ucla.wise.shared.properties;

import org.apache.log4j.Logger;

public class WiseSharedProperties extends AbstractWiseProperties {
    public static final String DATABASE_DRIVER_NAME = "com.mysql.jdbc.Driver";
    public static final String DATABASE_URL = "jdbc:mysql://localhost/";

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(WiseSharedProperties.class);

    public WiseSharedProperties(String fileName, String applicationName) {
        super(fileName, applicationName);
    }

    public String getDatabaseUsername() {
        String databaseUsername = this.getStringProperty("database.root.username");
        LOGGER.info("Wise shared will use database.root.username=" + databaseUsername);
        return databaseUsername;
    }

    public String getDatabasePassword() {
        return this.getStringProperty("database.root.password");
    }
}
