package edu.ucla.wise.commons;

import javax.activation.DataHandler;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

/**
 * This class encapsulates some specific methods to send messages from Message
 * Sequences.
 * 
 * @author Douglas Bell
 * @version 1.0
 */
public class MessageSender {
    public static final Logger LOGGER = Logger.getLogger(MessageSender.class);
    /** Instance Variables */
    public Session session;
    private String fromStr, replyStr;

    /**
     * Constructor : gets the email session from WISE Application.
     */
    public MessageSender() {
	this.session = WISEApplication.getMailSession(null);
    }

    /**
     * Constructor : gets the email session from WISE Application and other
     * instance variables from MessageSequence.
     * 
     * @param msgSeq
     *            Message sequence for which sender has to be created.
     */
    public MessageSender(MessageSequence msgSeq) {

	// String myFromID = msg_seq.emailID();
	/* WISEApplication knows how to look up passwords */
	this.session = WISEApplication.getMailSession(null);
	this.fromStr = msgSeq.getFromString();
	this.replyStr = msgSeq.getReplyString();
    }

    // public void set_fromString(String fromString)
    // {
    //
    // }

    /**
     * looks up, compose, and send email message
     * 
     * @param msg
     *            Message object to be sent to the User.
     * @param messageUseID
     *            The id used for generating URL which is unique to the user.
     * @param toUser
     *            User to whom the message has to be sent.
     * @param db
     *            Data bank object used from talking to DB.
     * @return String Empty string if successful.
     */
    public String sendMessage(Message msg, String messageUseID, User toUser,
	    DataBank db) {
	String salutation = toUser.getSalutation();
	String lastname = toUser.getLastName();
	String email = toUser.getEmail();
	String inviteeID = toUser.getId();

	/* these are all pretty fixed relationships */
	String ssid = toUser.getCurrentSurvey().getStudySpace().id;
	return this.sendMessage(msg, messageUseID, email, salutation, lastname,
		ssid, db, inviteeID);
    }

    /**
     * Sends an email to the User.
     * 
     * @param msg
     *            Message object to be sent to the User.
     * @param messageUseID
     *            The id used for generating URL which is unique to the user.
     * @param toEmail
     *            Email id to which message has to be sent.
     * @param salutation
     *            Salutation to address the invitee to whom email has to be
     *            sent.
     * @param lastname
     *            Last name of the invitee.
     * @param ssid
     *            Studyspace id for generation of the URL.
     * @param db
     *            Data bank object used from talking to DB.
     * @param inviteeId
     *            Id of the user to whom email has to be sent
     * @return String Empty string if successful.
     */
    public String sendMessage(Message msg, String messageUseID, String toEmail,
	    String salutation, String lastname, String ssid, DataBank db,
	    String inviteeId) {
	String outputString = "uncaught exception";
	String message = null;
	try {
	    /* create message object */
	    MimeMessage mMessage = new MimeMessage(this.session);

	    /* send message to each of the users */
	    InternetAddress tmpAddr = new InternetAddress(this.fromStr);
	    mMessage.setFrom(tmpAddr);
	    if (this.replyStr != null) {
		tmpAddr = new InternetAddress(this.replyStr);
		mMessage.setReplyTo(new InternetAddress[] { tmpAddr });
	    }
	    java.util.Date today = new java.util.Date();
	    mMessage.setSentDate(today);
	    mMessage.addRecipient(javax.mail.Message.RecipientType.TO,
		    new InternetAddress(toEmail));
	    mMessage.setSubject(msg.subject);

	    /* check if message produces an html body; null indicates no */
	    message = msg.composeHtmlBody(salutation, lastname, ssid,
		    messageUseID);
	    message = db.replacePattern(message, inviteeId);

	    /* if message is null go ahead and prepare a text body */
	    if (message == null) {
		message = msg.composeTextBody(salutation, lastname, ssid,
			messageUseID);
		message = db.replacePattern(message, inviteeId);
		mMessage.setText(message);
	    } else {

		/*
		 * create an "Alternative" Multipart message to send both html &
		 * text email
		 */
		Multipart mp = new MimeMultipart("alternative");

		/* add text body part */
		BodyPart bpText = new MimeBodyPart();
		bpText.setDataHandler(new DataHandler(msg.composeTextBody(
			salutation, lastname, ssid, messageUseID), "text/plain"));
		mp.addBodyPart(bpText);

		/* add html body part */
		BodyPart bpHtml = new MimeBodyPart();
		bpHtml.setDataHandler(new DataHandler(message, "text/html"));
		mp.addBodyPart(bpHtml);

		/* set the message body */
		mMessage.setContent(mp);
	    }

	    // System.out.println(message);
	    /* send message and return the result */
	    outputString = this.mailingProcess(mMessage);

	} catch (MessagingException e) {
	    LOGGER.error("\r\nWISE - MESSAGE SENDER on email message: "
		    + message + ".\r\n Full error: " + e.toString(), e);
	} catch (Exception e) {
	    LOGGER.error("\r\nWISE - MESSAGE SENDER on email message: "
		    + message + ".\r\n Full error: " + e.toString(), e);
	}
	return outputString;
    }

    /*
     * //Overloaded method for send_message public String send_message(Message
     * msg, String invitee_id, String message_seq_id, String toEmail, String
     * salutation, String lastname, String ssid, Data_Bank dataBank, String
     * msg_type, String survey_id) { String outputString = "uncaught exception";
     * String message = null;
     * 
     * try { Connection conn = dataBank.getDBConnection(); Statement msgUseQry =
     * conn.createStatement(); Statement usrSteQry = conn.createStatement(); //
     * create message object MimeMessage mMessage = new MimeMessage(session); //
     * send message to each of the users InternetAddress tmpAddr = new
     * InternetAddress(from_str); mMessage.setFrom(tmpAddr); if (reply_str !=
     * null) { tmpAddr = new InternetAddress(reply_str); mMessage.setReplyTo(new
     * InternetAddress[] { tmpAddr }); } java.util.Date today = new
     * java.util.Date(); mMessage.setSentDate(today);
     * mMessage.addRecipient(javax.mail.Message.RecipientType.TO, new
     * InternetAddress(toEmail)); mMessage.setSubject(msg.subject);
     * 
     * String message_useID = org.apache.commons.lang3.RandomStringUtils
     * .randomAlphanumeric(22);
     * 
     * 
     * // check if message produces an html body; null indicates no message =
     * msg.compose_html_body(salutation, lastname, ssid, message_useID);
     * message=dataBank.replacePattern(message, invitee_id); // if message is
     * null go ahead and prepare a text body if (message == null) { message =
     * msg.compose_text_body(salutation, lastname, ssid, message_useID);
     * message=dataBank.replacePattern(message, invitee_id);
     * mMessage.setText(message); } else { // create an "Alternative" Multipart
     * message to send both html & // text email Multipart mp = new
     * MimeMultipart("alternative"); // add text body part BodyPart bp_text =
     * new MimeBodyPart(); bp_text.setDataHandler(new
     * DataHandler(msg.compose_text_body( salutation, lastname, ssid,
     * message_useID), "text/plain")); mp.addBodyPart(bp_text); // add html body
     * part BodyPart bp_html = new MimeBodyPart(); bp_html.setDataHandler(new
     * DataHandler(message, "text/html")); mp.addBodyPart(bp_html); // set the
     * message body mMessage.setContent(mp); }
     * 
     * outputString = mailing_process(mMessage);
     * 
     * //Email sending is done, need to update the tables if
     * (outputString.equalsIgnoreCase("")){ //updating the survey_message_use
     * table with the sucess status String msgUse_sql =
     * "INSERT INTO survey_message_use (messageId,invitee, survey, message) VALUES('"
     * + message_useID + "', " + invitee_id + ", '" + survey_id + "', '" +
     * msg.id + "')"; AdminInfo.log_info(
     * "The sql query run when inserting into survey_message_use "+ msgUse_sql);
     * msgUseQry.execute(msgUse_sql); String state = "invited";
     * outputString=message_useID; //updating the survey_user_state table if
     * (msg_type.equalsIgnoreCase("invite")) { String sql_u =
     * "INSERT INTO survey_user_state (invitee, state, survey, message_sequence) "
     * + "VALUES(" + invitee_id + ", '" + state + "', '" + survey_id + "', '" +
     * message_seq_id + "') " + "ON DUPLICATE KEY UPDATE state='" + state +
     * "', state_count=1, message_sequence=VALUES(message_sequence)";
     * AdminInfo.log_info
     * ("The sql query run when inserting into survey_user_state "+ msgUse_sql);
     * // note timestamp updates automatically usrSteQry.execute(sql_u); }
     * }else{ String msgUse_sql =
     * "INSERT INTO survey_message_use (messageId,invitee, survey, message) VALUES('"
     * + message_useID + "', " + invitee_id + ", '" + survey_id + "', 'err:" +
     * outputString + "')"; AdminInfo.log_info(
     * "The sql query run when inserting into survey_message_use "+ msgUse_sql);
     * msgUseQry.execute(msgUse_sql); String state = "email_error"; //updating
     * the survey_user_state table if (msg_type.equalsIgnoreCase("invite")) {
     * String sql_u =
     * "INSERT INTO survey_user_state (invitee, state, survey, message_sequence) "
     * + "VALUES(" + invitee_id + ", '" + state + "', '" + survey_id + "', '" +
     * message_seq_id + "') " + "ON DUPLICATE KEY UPDATE state='" + state +
     * "', state_count=1, message_sequence=VALUES(message_sequence)";
     * AdminInfo.log_info
     * ("The sql query run when inserting into survey_user_state "+ msgUse_sql);
     * // note timestamp updates automatically usrSteQry.execute(sql_u); }
     * conn.close(); }
     * 
     * }catch (Exception e) { WISE_Application.log_error(
     * "\r\nWISE - MESSAGE SENDER on email message: " + message +
     * ".\r\n Full error: " + e.toString(), e); } return outputString;
     * 
     * }
     */

    /**
     * This method tests sending messages.
     * 
     * @param msgText
     *            Message test that has to be sent.
     * @return String If the mail sending is successful or not.
     */
    public String sendTest(String msgText) {
	String outputString = "";
	try {

	    /* create message object */
	    MimeMessage message = new MimeMessage(this.session);

	    /* send message to each of the users */
	    message.setFrom(new InternetAddress("merg@mednet.ucla.edu"));
	    java.util.Date today = new java.util.Date();
	    message.setSentDate(today);
	    message.addRecipient(javax.mail.Message.RecipientType.TO,
		    new InternetAddress("dbell@mednet.ucla.edu"));
	    message.setSubject("This is a test");
	    message.setText(msgText);

	    /* send message and analyze the mailing failure */
	    String msgResult = this.mailingProcess(message);
	    if (msgResult.equalsIgnoreCase("")) {
		outputString = "D";
	    } else {
		outputString = msgResult;
	    }

	} catch (MessagingException e) {
	    LOGGER.error(
		    "WISE EMAIL - MESSAGE SENDER - SEND REMINDER: "
			    + e.toString(), null);
	} catch (Exception e) {
	    LOGGER.error(
		    "WISE EMAIL - MESSAGE SENDER - SEND REMINDER: "
			    + e.toString(), null);
	}

	return outputString;
    }

    /**
     * Sends the actual email to the user.
     * 
     * @param msg
     *            Mime message which contains the details for sending email.
     * @return String Empty if successful.
     * @throws MessagingException
     * @throws Exception
     */
    public String mailingProcess(MimeMessage msg) throws MessagingException,
	    Exception {
	String mailingResult = "";
	if (msg == null) {
	    return "msg is null";
	}
	try {
	    if (this.session == null) {
		LOGGER.info("Session is null!!");
	    }
	    Transport tr = this.session.getTransport("smtp");

	    if (tr == null) {
		LOGGER.info("tr is null!!");
	    }

	    String MailHost = null;
	    String user = null;
	    String pass = null;
	    boolean sslEmail = true;

	    pass = WISEApplication.wiseProperties
		    .getStringProperty("SMTP_AUTH_PASSWORD");
	    user = WISEApplication.wiseProperties
		    .getStringProperty("SMTP_AUTH_USER");//
	    MailHost = WISEApplication.wiseProperties
		    .getStringProperty("email.host");
	    sslEmail = "true".equalsIgnoreCase(WISEApplication.wiseProperties
		    .getStringProperty("email.ssl"));
	    // WISE_Application.log_info("@@@@@the email setting are user is "+
	    // user +" the pass is "+ pass+ " and the email host is "+MailHost);

	    if ((MailHost == null) || (user == null) || (pass == null)) {
		LOGGER.info("MailHost or user or pass is null");
	    }
	    msg.saveChanges(); // don't forget this
	    if (msg.getAllRecipients() == null) {
		LOGGER.info("Get All Recepients is null");
	    }
	    if (sslEmail) {
		tr.connect(MailHost, user, pass);
		Transport.send(msg, msg.getAllRecipients());
	    } else {
		Transport.send(msg, msg.getAllRecipients());
	    }
	    tr.close();

	    // Transport.send(msg);
	} catch (AuthenticationFailedException e) {
	    LOGGER.error(
		    "Message_Sender - Authentication failed. From string: "
			    + this.fromStr + "; Reply: " + this.replyStr
			    + ". \n" + e.toString(), e);
	    mailingResult = "Authentication process failed";
	    return mailingResult;
	} catch (SendFailedException e) {
	    LOGGER.error(
		    "Message_Sender - Invalid email address. " + e.toString(),
		    e);
	    mailingResult = "Email address is invalid.";
	    return mailingResult;
	} catch (MethodNotSupportedException e) {
	    LOGGER.error(
		    "Message_Sender - Unsupported message type. "
			    + e.toString(), e);
	    mailingResult = "Message is not supported.";
	    return mailingResult;
	} catch (Exception e) {
	    LOGGER.info(
		    "Message_Sender - mailing_process failure: " + e.toString(),
		    e);
	    mailingResult = "Email failed (null pointer error): "
		    + e.toString();
	    throw e;
	    // mailing_result = "abcdefg";
	}
	return mailingResult;
    }
    /***************************************************************************
     * Old Send_message() String mailing_result=""; try { Transport tr =
     * session.getTransport("smtp"); String MailHost =
     * WISE_Application.email_host; String user =
     * WISE_Application.mail_username; String pass =
     * WISE_Application.mail_password; tr.connect(MailHost, user, pass);
     * msg.saveChanges(); // don't forget this tr.sendMessage(msg,
     * msg.getAllRecipients()); tr.close(); //Transport.send(msg); }
     * 
     * catch (AuthenticationFailedException e) { WISE_Application.email_alert(
     * "Message_Sender - Authentication failed. From string: " + from_str +
     * "; Reply: "+ reply_str +". \n" + e.toString(), e);
     * mailing_result="Authentication process failed"; return mailing_result; }
     * catch (SendFailedException e) {
     * WISE_Application.email_alert("Message_Sender - Invalid email address. "
     * +e.toString(), e); mailing_result="Email address is invalid."; return
     * mailing_result; }
     * 
     * catch (MethodNotSupportedException e) { WISE_Application.email_alert(
     * "Message_Sender - Unsupported message type. "+e.toString(), e);
     * mailing_result="Message is not supported."; return mailing_result; }
     * 
     * catch (Exception e) { WISE_Application.email_alert(
     * "Message_Sender - mailing_process failure: "+e.toString(), e);
     * mailing_result = "Email failed (unknown error): " + e.toString(); }
     * return mailing_result; }
     *********************************************************************/

}
