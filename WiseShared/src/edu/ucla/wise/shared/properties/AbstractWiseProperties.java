package edu.ucla.wise.shared.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AbstractWiseProperties extends Properties {
    private static final long serialVersionUID = 1L;

    private final String fileName;
    private final String applicationName;

    public AbstractWiseProperties(String fileName, String applicationName) {
        super();
        this.fileName = fileName;
        this.applicationName = applicationName;
        this.loadFile(fileName);
    }

    public AbstractWiseProperties(InputStream stream, String fileName, String applicationName) {
        super();
        this.fileName = fileName;
        this.applicationName = applicationName;

        try {
            this.populateDefaultValues();
            this.load(stream);
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            this.logException(e, "Reading stream");
        }
    }

    private final void loadFile(String fileName) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(fileName);
            this.populateDefaultValues();
            this.load(inputStream);
        } catch (IOException ioe) {
            this.logException(ioe, "Reading file: '" + fileName + "'");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    this.logException(e, "Closing file: '" + fileName + "'");
                }
            }
        }
    }

    /**
     * redefine to add default properties
     */
    protected void populateDefaultValues() {

    }

    protected void logException(Exception exception, String message) {
        System.err.println(message);
        exception.printStackTrace();
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    @Override
    @Deprecated
    public String getProperty(String property) {
        return super.getProperty(property);
    }

    public String getStringProperty(String property) {
        return super.getProperty(property);
    }
}
