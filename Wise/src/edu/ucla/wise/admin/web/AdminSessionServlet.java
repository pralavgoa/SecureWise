package edu.ucla.wise.admin.web;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.admin.AdminUserSession;
import freemarker.template.TemplateException;

/**
 * Checks if there is an active and validated session before proceeding.
 */
public abstract class AdminSessionServlet extends HttpServlet {

    public enum FunctionType {

        GET, POST

    }

    /**
     * Serial version id.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public final void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.validateSession(request, response, FunctionType.GET);
        } catch (TemplateException e) {
            this.getLogger().error(e);
        } catch (IOException e) {
            this.getLogger().error(e);
        }

    }

    @Override
    public final void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            this.validateSession(request, response, FunctionType.POST);
        } catch (TemplateException e) {
            this.getLogger().error(e);
        } catch (IOException e) {
            this.getLogger().error(e);
        }

    }

    private void validateSession(HttpServletRequest request, HttpServletResponse response, FunctionType fType)
            throws IOException, TemplateException {
        String path = request.getContextPath();
        HttpSession session = request.getSession(true);
        // if the session is expired, go back to the logon page
        if (session.isNew()) {
            response.sendRedirect(path + "/index.html");
        }
        // get the admin info object from session
        AdminUserSession adminUserSession = (AdminUserSession) session.getAttribute("ADMIN_USER_SESSION");
        // if the session is invalid, display the error
        if (adminUserSession == null) {
            response.sendRedirect(path + "/error.htm");
        }

        switch (fType) {
        case GET:
            this.getMethod(request, response, adminUserSession);
            break;
        case POST:
            this.postMethod(request, response, adminUserSession);
            break;
        }
    }

    public abstract void getMethod(HttpServletRequest request, HttpServletResponse response,
            AdminUserSession adminUserSession) throws IOException, TemplateException;

    public abstract void postMethod(HttpServletRequest request, HttpServletResponse response,
            AdminUserSession adminUserSession) throws IOException;

    public abstract Logger getLogger();

}
