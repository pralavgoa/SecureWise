package edu.ucla.wise.admin.console;

import java.io.IOException;
import java.io.PrintWriter;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.admin.AdminUserSession;
import edu.ucla.wise.admin.web.AdminSessionServlet;
import edu.ucla.wise.commons.WISEApplication;
import freemarker.template.TemplateException;

@WebServlet("/admin/healthChecker")
public class HealthChecker extends AdminSessionServlet {

    private static final Logger LOGGER = Logger.getLogger(HealthChecker.class);
    private static final long serialVersionUID = 1L;

    @Override
    public void getMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException, TemplateException {
        PrintWriter out = response.getWriter();
        StringBuilder output = new StringBuilder();
        try {
            String message = request.getParameter("message");
            String subject = request.getParameter("subject");
            String email = request.getParameter("email");
            if (Strings.isNullOrEmpty(email)) {
                output.append("Please provide and email address.");

            } else {
                String fromAddress = email;
                String[] recipients = { email };

                Session session = WISEApplication.getInstance().getEmailer().getMailSession();

                /* create a message */
                javax.mail.Message msg = new MimeMessage(session);

                /* set the from and to address */
                InternetAddress addressFrom = new InternetAddress(fromAddress);
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
        } catch (MessagingException e) {
            LOGGER.error("Mail system failure", e);
            output.append("Error is: ").append(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error("Health checker failure", e);
            output.append("Error is: ").append(e.getMessage());
        }
        out.write(output.toString());

    }

    @Override
    public void postMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException {

    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

}
