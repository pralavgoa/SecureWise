package edu.ucla.wise.email;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public class Emailer {

    private final Session mailSession;

    public Emailer(EmailProperties properties) {
        this.mailSession = this.getMailSession(properties);
    }

    /* return the default session if null */
    public Session getMailSession(EmailProperties properties) {
        String smtpHost = properties.getEmailHost();
        String smtpAuthUser = properties.getEmailUsername();
        String smtpAuthPassword = properties.getEmailPassword();
        String smtpAuthPort = properties.getEmailAuthenticationPort();
        boolean tempsslEmail = properties.isUseSSL();

        /* Set the host smtp address */
        if (tempsslEmail) {
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpAuthPort);
            props.put("mail.smtp.user", smtpAuthUser);
            props.put("mail.smtp.password", smtpAuthPassword);
            props.setProperty("mail.smtp.socketFactory.port", smtpAuthPort);

            // defaults
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
            props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
            props.setProperty("mail.smtp.socketFactory.fallback", "false");
            props.setProperty("mail.smtp.connectiontimeout", "10000");
            props.setProperty("mail.smtp.timeout", "10000");
            Authenticator auth = new VarAuthenticator(smtpAuthUser, smtpAuthPassword);

            /* create the message session */
            return Session.getInstance(props, auth);
        } else {
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.setProperty("mail.smtp.connectiontimeout", "10000");
            props.setProperty("mail.smtp.timeout", "10000");
            return Session.getInstance(props);
        }

    }

    public Session getMailSession() {
        return this.mailSession;
    }

    private static class VarAuthenticator extends Authenticator {
        String userName = null;
        String password = null;

        public VarAuthenticator(String uName, String pword) {
            super();
            this.userName = uName;
            this.password = pword;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(this.userName, this.password);
        }
    }
}
