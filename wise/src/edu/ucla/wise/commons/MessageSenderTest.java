package edu.ucla.wise.commons;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * This class is used to test email sending part.
 * 
 * @author Pralav
 * @version 1.0  
 */
public class MessageSenderTest {
    private static final String SMTP_HOST_NAME = "mail.ucla.edu";
    private static final String SMTP_AUTH_USER = "pdessai@ucla.edu";
    private static final String SMTP_AUTH_PWD = "Pr@l@v12";
    private static final String emailMsgTxt = "Online Order Confirmation Message. Also include the Tracking Number.";
    private static final String emailSubjectTxt = "Order Confirmation Subject";
    private static final String emailFromAddress = "pdessai@ucla.edu";

    /* Add List of Email address to who email needs to be sent to */
    private static final String[] emailList = { "pralavgoa@gmail.com" };

    public static void main(String args[]) throws Exception {
    	MessageSenderTest smtpMailSender = new MessageSenderTest();
    	smtpMailSender.postMail(emailList, emailSubjectTxt, emailMsgTxt,
    			emailFromAddress);
    	System.out.println("Sucessfully Sent mail to All Users");
    }

    /**
     * Sends an email to the recipients.
     * 
     * @param recipients		Email IDs to whom mails are to be sent.
     * @param subject			Subject of the email.
     * @param message			Body of the email.
     * @param from				From part of email.S
     * @throws MessagingException
     */
    public void postMail(String recipients[], String subject, String message,
    		String from) throws MessagingException {
    	boolean debug = false;

    	/* Set the host smtp address */
    	Properties props = new Properties();
    	props.put("mail.smtp.host", SMTP_HOST_NAME);
    	props.put("mail.smtp.auth", "true");
    	props.put("mail.debug", "true");
    	props.put("mail.smtp.port", "465");
    	props.put("mail.smtp.user", SMTP_AUTH_USER);
    	props.put("mail.smtp.password", SMTP_AUTH_PWD);
    	props.put("mail.smtp.starttls.enable", "true");
    	String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    	props.setProperty("mail.smtp.socketFactory.port", "465");
    	props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
    	props.setProperty("mail.smtp.socketFactory.fallback", "false");
    	Authenticator auth = new SMTPAuthenticator();

    	Session session = Session.getInstance(props, auth);

    	session.setDebug(debug);

    	/* create a message */
    	javax.mail.Message msg = new MimeMessage(session);

    	/* set the from and to address */
    	InternetAddress addressFrom = new InternetAddress(from);
    	msg.setFrom(addressFrom);

    	InternetAddress[] addressTo = new InternetAddress[recipients.length];
    	for (int i = 0; i < recipients.length; i++) {
    		addressTo[i] = new InternetAddress(recipients[i]);
    	}
    	msg.setRecipients(javax.mail.Message.RecipientType.TO, addressTo);

    	/* Setting the Subject and Content Type */
    	msg.setSubject(subject);
    	msg.setContent(message, "text/plain");
    	Transport.send(msg);
    }

    /**
     * SimpleAuthenticator is used to do simple authentication when the SMTP
     * server requires it.
     * 
     * @author  Pralav
     * @version	1.0
     */
    private class SMTPAuthenticator extends javax.mail.Authenticator {
	
		public PasswordAuthentication getPasswordAuthentication() {
		    String username = SMTP_AUTH_USER;
		    String password = SMTP_AUTH_PWD;
		    return new PasswordAuthentication(username, password);
		}
    }

}
