package edu.ucla.wise.common;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Test;

import edu.ucla.wise.commons.MessageSender;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.email.EmailMessage;
import edu.ucla.wise.email.EmailProperties;
import edu.ucla.wise.initializer.WiseProperties;

/**
 * Test the message sender. TODO: this is incomplete
 * 
 * @author pdessai
 * 
 */
public class MessageSenderTest {

    @Test
    public void testMessageSender() throws Exception {
        Session session = WISEApplication.getMailSession(null, this.getWiseProperties());
        MimeMessage mimeMessage = new MimeMessage(session);

        InternetAddress fromAddr = new InternetAddress("pdessai@mednet.ucla.edu");

        mimeMessage.setFrom(fromAddr);
        java.util.Date today = new java.util.Date();
        mimeMessage.setSentDate(today);
        mimeMessage.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress("pralavgoa@gmail.com"));
        mimeMessage.setSubject("Test");
        mimeMessage.setText("Test Message");

        EmailProperties emailProperties = new EmailProperties(this.getWiseProperties());
        EmailMessage emailMessage = new EmailMessage("pralavgoa@gmail.com", "Mr", "Dessai");
        MessageSender.mailingProcess(mimeMessage, session, "pdessai@mednet.ucla.edu", "pdessai@mednet.ucla.edu",
                emailMessage, emailProperties);
    }

    private WiseProperties getWiseProperties() {
        return new WiseProperties("conf/dev/wise.properties", "WISE");
    }
}
