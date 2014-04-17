package edu.ucla.wise.commons;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.initializer.WiseProperties;

/**
 * Class to represent common elements for a given installation of the wise
 * surveyor or admin java application. Never instantiated; Handles most
 * interfaces to the file system
 * 
 * @author mrao
 * @author dbell
 * @author ssakdeo
 */
public class WISEApplication {

    private static Logger LOGGER = Logger.getLogger(WISEApplication.class);
    public static String rootURL;

    public static WiseProperties wiseProperties;

    public static String surveyApp;
    public static String sharedFilesLink;
    /* Commenting these out and moving them to Surveyor Class */
    // public String servlet_url, shared_file_url,shared_image_url;

    public static Hashtable<String, SurveyorApplication> AppInstanceTbl = new Hashtable<String, SurveyorApplication>();

    public static final String htmlExt = ".htm";
    public static final String mailUserNameExt = ".username";
    public static final String mailPasswdExt = ".password";

    // public static final String xml_ext = ".xml";
    private static final String WiseDefaultAcctPropID = "wise.email";

    public static Session mailSession; // Holds values for sending message;

    private static class VarAuthenticator extends Authenticator {
        String userName = null;
        String password = null;

        @SuppressWarnings("unused")
        public VarAuthenticator() {
            super();
            this.userName = wiseProperties.getEmailUsername();
            this.password = wiseProperties.getEmailPassword();
            System.out.println(this.userName + "/" + this.password);
        }

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

    public WISEApplication(WiseProperties properties) {

        wiseProperties = properties;

        /* Load server's local properties */
        String sharedPropPath;
        try {

            /* Loading Local Properties */
            rootURL = wiseProperties.getStringProperty("server.rootURL");
            sharedPropPath = wiseProperties.getStringProperty("shared.Properties.file");
            if (Strings.isNullOrEmpty(rootURL) || Strings.isNullOrEmpty(sharedPropPath)) {
                throw new Exception("Failed to read from local properties");
            }

            sharedFilesLink = wiseProperties.getStringProperty("default.sharedFiles_linkName");

            if (Strings.isNullOrEmpty(wiseProperties.getXmlRootPath())) {
                LOGGER.error("WISE Application initialization Error: Failed to read from Shared properties file "
                        + sharedPropPath + "\n");
            }

            /* set up Study_Space class -- pre-reads from sharedProps */
            StudySpace.setupStudies();

            /*
             * setup default email session for sending messages -- WISE needs
             * this to send alerts
             */
            mailSession = getMailSession(WiseDefaultAcctPropID, wiseProperties);
            if (mailSession == null) {
                LOGGER.error("WISE Application initialization Error: Failed to initialize mail session\n");
            }
        } catch (NullPointerException e) {
            LOGGER.error("WISE Application initialization Error: " + e);
        } catch (MissingResourceException e) {
            LOGGER.error("WISE Application initialization Error: " + e);
        } catch (ClassCastException e) {
            LOGGER.error("WISE Application initialization Error: " + e);
        } catch (Exception e) {
            LOGGER.error("WISE Application initialization Error: " + e);
        }
    }

    /**
     * Loads all the variables needed to run the application from the properties
     * files
     * 
     * @param errStr
     *            The error message that has to be printed.
     * @returns PrintStream PrintStream that prints the input error message.
     */
    static PrintStream initError(String errStr) {
        PrintStream ps = null;
        try {
            FileOutputStream fos = new FileOutputStream("WISE_errors.txt", true);
            ps = new PrintStream(fos, true);
            ps.print(errStr);
        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
            e.printStackTrace(System.err);
        } catch (SecurityException e) {
            System.err.println(e.toString());
            e.printStackTrace(System.err);
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace(System.err);
        }
        return ps;
    }

    /**
     * Send an email alert to someone. Older version of this application used to
     * send email alert for every error. This would flood inbox trememdously.
     * Replaced this by logging errors, an industry standard of recording events
     * having in an application.
     * 
     * @param email_to
     *            Email Id to whom mail has to be sent.
     * @param subject
     *            Subject of the Email.
     * @param body
     *            Body of the Email.
     * 
     */
    @Deprecated
    public static void sendEmail(String email_to, String subject, String body) {
        try {
            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(wiseProperties.getEmailFrom()));
            message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(email_to));
            message.setSubject(subject);
            message.setText(body);

            /* Send message */
            Transport.send(message);
        } catch (AddressException e) {
            LOGGER.error("WISE_Application - SEND_EMAIL error: " + "\n" + body, e);
        } catch (MessagingException e) {
            LOGGER.error("WISE_Application - SEND_EMAIL error: " + "\n" + body, e);
        }
    }

    /**
     * decoding - convert character-formatted ID to be the digit-formatted *
     * TOGGLE NAME OF THIS FUNCTION to move to production mode
     * 
     * @param charId
     *            The String to decode.
     * @return String The decoded input.
     */
    public static String decode(String charId) {
        String result = new String();
        int sum = 0;
        for (int i = charId.length() - 1; i >= 0; i--) {
            char c = charId.charAt(i);
            int remainder = c - 65;
            sum = (sum * 26) + remainder;
        }

        sum = sum - 97654;
        int remain = sum % 31;
        if (remain == 0) {
            sum = sum / 31;
            result = Integer.toString(sum);
        } else {
            result = "invalid";
        }
        return result;
    }

    // public static String decodeTest(String charId) {
    // return charId;
    // }

    /**
     * encoding - convert digit-formatted ID to be the character-formatted
     * TOGGLE NAME OF THIS FUNCTION to move to production mode
     * 
     * @param charId
     *            The String to encode.
     * @return String The encoded input.
     */
    public static String encode(String userId) {
        int baseNumb = (Integer.parseInt(userId) * 31) + 97654;
        String s1 = Integer.toString(baseNumb);
        String s2 = Integer.toString(26);
        BigInteger b1 = new BigInteger(s1);
        BigInteger b2 = new BigInteger(s2);

        int counter = 0;
        String charId = new String();
        while (counter < 5) {
            BigInteger[] bs = b1.divideAndRemainder(b2);
            b1 = bs[0];
            int encodeValue = bs[1].intValue() + 65;
            charId = charId + (new Character((char) encodeValue).toString());
            counter++;
        }
        return charId;
    }

    // public static String encode_test(String user_id) {
    // return user_id;
    // }

    // TODO not used
    // public static String read_localProp(String prop_name) {
    // File prop_file = new File(localPropPath);
    // try {
    // FileInputStream in = new FileInputStream(prop_file);
    // localProps.load(in);
    // in.close();
    // } catch (Exception e) {
    // email_alert("WISE Application Error reading local property: ", e);
    // }
    // return localProps.getString(prop_name);
    // }

    /* return the default session if null */
    public static Session getMailSession(String fromAcct, WiseProperties properties) {
        if (fromAcct == null) {
            fromAcct = WiseDefaultAcctPropID;
        }
        String uname = properties.getStringProperty(fromAcct + mailUserNameExt);
        String pwd = properties.getStringProperty(fromAcct + mailPasswdExt);

        String smtpAuthUser = properties.getStringProperty("SMTP_AUTH_USER");
        String smtpAuthPassword = properties.getStringProperty("SMTP_AUTH_PASSWORD");
        String smtpAuthPort = properties.getStringProperty("SMTP_AUTH_PORT");
        boolean tempsslEmail = "true".equalsIgnoreCase(properties.getStringProperty("email.ssl"));

        /* Set the host smtp address */
        if (tempsslEmail) {
            Properties props = System.getProperties();
            props.put("mail.smtp.host", properties.getEmailHost());
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.port", smtpAuthPort);
            props.put("mail.smtp.user", smtpAuthUser);
            props.put("mail.smtp.password", smtpAuthPassword);
            String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
            props.setProperty("mail.smtp.socketFactory.port", smtpAuthPort);
            props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
            props.setProperty("mail.smtp.socketFactory.fallback", "false");
            props.setProperty("mail.smtp.connectiontimeout", "10000");
            props.setProperty("mail.smtp.timeout", "10000");
            Authenticator auth = new VarAuthenticator(uname, pwd);

            /* create the message session */
            return Session.getInstance(props, auth);
        } else {
            Properties props = System.getProperties();
            props.put("mail.smtp.host", properties.getEmailHost());
            props.setProperty("mail.smtp.connectiontimeout", "10000");
            props.setProperty("mail.smtp.timeout", "10000");
            return Session.getInstance(props);
        }
    }
}
