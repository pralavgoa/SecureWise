package edu.ucla.wise.admin.healthmon;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WiseConstants;

/**
 * HealthMonitoringManager class is used to monitor the status of email/database/survey server.
 * It is a thread that runs continuously to monitor the status
 * 
 * @author ssakdeo
 * @version 1.0  
 */
public class HealthMonitoringManager implements Runnable {

    Logger log = Logger.getLogger(HealthMonitoringManager.class);
    AdminApplication adminInfo;
    private static HealthMonitoringManager hMon = null;

    private HealthMonitoringManager(AdminApplication adminInfo) {
    	this.adminInfo = adminInfo;
    }

    /**
     * This function will start monitoring for database and smtp if it has
     * already been not started. If the monitoring has already started then this
     * function will just return;
     * 
     * @param survey
     */
    public static synchronized void monitor(AdminApplication adminInfo) {
		if (hMon == null) {
		    hMon = new HealthMonitoringManager(adminInfo);
		    Thread t = new Thread(hMon);
		    t.start();
		}
    }

    @Override
    public void run() {
		while (true) {
		    checkDbHealth();
		    checkSmtpHealth();
		    try {
		    	Thread.sleep(WiseConstants.dbSmtpCheckInterval);
		    } catch (InterruptedException e) {
		    	log.error("Could not get session variable! Please retry!", e);
		    }
		}
    }
    
    /**
     * This function will check smtp server status by connecting to the email client
     * and will update the status in the Wise admin page.
     */
    private void checkSmtpHealth() {
		HealthStatus hStatus = HealthStatus.getInstance();
		Session session = WISEApplication.getMailSession(null);
		if (session == null) {
		    log.error("Could not get session variable! Please retry!");
		    hStatus.updateSmtp(false, Calendar.getInstance().getTime());
		    return;
		}
		Transport tr = null;
		try {
		    tr = session.getTransport("smtp");
		} catch (NoSuchProviderException e) {
		    log.error(e);
		    hStatus.updateSmtp(false, Calendar.getInstance().getTime());
		    return;
		}
		if (tr == null) {
		    log.error("Could not get transport object");
		    hStatus.updateSmtp(false, Calendar.getInstance().getTime());
		    return;
		}
		String MailHost = null;
		String user = null;
		String pass = null;
	
		pass = WISEApplication.wiseProperties.getStringProperty("SMTP_AUTH_PASSWORD");
		user = WISEApplication.wiseProperties.getStringProperty("SMTP_AUTH_USER");//
		MailHost = WISEApplication.wiseProperties.getStringProperty("email.host");
		try {
		    tr.connect(MailHost, user, pass);
		} catch (MessagingException e) {
		    log.error("Could not connect!");
		    hStatus.updateSmtp(false, Calendar.getInstance().getTime());
		    return;
		}
		try {
		    tr.close();
		} catch (MessagingException e) {
		    log.info("Transport connected successully " +
		    		"however closing failed but thats fine", e);
		}
		hStatus.updateSmtp(true, Calendar.getInstance().getTime());
    }

    /**
     * This function will check the database status by getting the connection
     * and will update the status in the Wise admin page.
     */
    private void checkDbHealth() {
		HealthStatus hStatus = HealthStatus.getInstance();
		Connection dbConnection = null;
		try {
		    dbConnection = adminInfo.getDBConnection();
		} catch (SQLException e) {
		    log.error(e);
		    hStatus.updateDb(false, Calendar.getInstance().getTime());
		    return;
		} finally {
		    if (dbConnection != null) {
			try {
			    dbConnection.close();
			} catch (SQLException e) {
			}
		    }
		}
		hStatus.updateDb(true, Calendar.getInstance().getTime());
	}
}