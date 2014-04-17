package edu.ucla.wise.emailscheduler.web;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucla.wise.emailscheduler.EmailScheduler;

@WebServlet("/emailSchedulerStatus")
public class EmailSchedulerStatus extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.getWriter().println(EmailScheduler.getInstance().getEmailSchedulerStatusMap());

    }

}
