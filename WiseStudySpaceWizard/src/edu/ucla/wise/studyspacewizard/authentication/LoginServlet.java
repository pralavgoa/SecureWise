package edu.ucla.wise.studyspacewizard.authentication;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            response.getWriter().println("Authentication failed");
            return;
        }

        if ("admin".equals(username) && "password".equals(password)) {
            request.getSession().setAttribute("authenticated", "true");
            response.sendRedirect("LoginPage.jsp");
        }

    }

}
