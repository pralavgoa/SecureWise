package edu.ucla.wise.initializer;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.emailscheduler.EmailScheduler;
import edu.ucla.wise.studyspace.parameters.StudySpaceDatabaseProperties;

/**
 * WiseApplicationInitializer class is used to initialize the classes 
 * needed for running the WISE Application.
 * 
 * @author Pralav
 * @version 1.0  
 */
public class WiseApplicationInitializer implements ServletContextListener {

	/**
     * Destroys the email scheduler.
     * 
     * @param 	arg0	 ServletContextEvent. 
     */
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
		EmailScheduler.destroyScheduler();
    }

    /**
     * Initializes all the needed classes and starts the email scheduler thread.
     * 
     * @param 	servletContextEvent	 ServletContextEvent.
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
		try {
			WISEApplication.logInfo("Wise Application initializing");
			StudySpaceParametersProvider.initialize(new StudySpaceDatabaseProperties(){

				@Override
				public String getDatabaseRootUsername() {
					return "root";
				}

				@Override
				public String getDatabaseRootPassword() {
				   return "";
				}

				@Override
				public String getDatabaseServerHost() {
					return "localhost";
				}
				
			});	
		    String contextPath = servletContextEvent.getServletContext()
		    		.getContextPath();
	
		    String adminInitializeError = AdminApplication.checkInit(contextPath);
	
		    if (adminInitializeError != null) {
				WISEApplication.logInfo("Admin app not initialized");
				throw new IllegalStateException("Admin app not initialized");
		    }
	
		    String surveyInitializeError = SurveyorApplication
		    		.checkInit(contextPath);
	
		    if (surveyInitializeError != null) {
				WISEApplication.logInfo("Survey app not initialized");
				throw new IllegalStateException("Survey app not initialized");
		    }
	
			WISEApplication.logInfo("Wise Application initialized");
			WISEApplication.logInfo("Staring Email Scheduler");
			
			EmailScheduler.startEmailSendingThreads();
			WISEApplication.logInfo("Email Scheduler is alive");
		} catch (IOException e) {
		    WISEApplication.logError("IO Exception while initializing", e);
		} catch (IllegalStateException e) {
		    WISEApplication
			    	.logError("The admin or the survey app was not " +
			    			"initialized, WISE application cannot start", e);
		}

    }

}
