package edu.ucla.wise.email;

import edu.ucla.wise.initializer.WiseProperties;

public class EmailProperties {

    private final String username;
    private final String password;
    private final String emailHost;
    private final boolean useSSL;

    public EmailProperties(WiseProperties properties) {
        this.username = properties.getEmailUsername();
        this.password = properties.getEmailPassword();
        this.emailHost = properties.getEmailHost();
        this.useSSL = properties.useSslEmail();
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getEmailHost() {
        return this.emailHost;
    }

    public boolean isUseSSL() {
        return this.useSSL;
    }
}
