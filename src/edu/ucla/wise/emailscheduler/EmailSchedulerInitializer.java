package edu.ucla.wise.emailscheduler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class EmailSchedulerInitializer implements ServletContextListener{

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

		EmailScheduler.destroyScheduler();
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {

		System.out.println("The context path is: "
				+ servletContextEvent.getServletContext().getContextPath());

		EmailScheduler.startEmailSendingThreads();

	}




}
