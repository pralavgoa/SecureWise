package edu.ucla.wise.initializer;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.emailscheduler.EmailScheduler;

/**
 * WiseApplicationInitializer class is used to initialize the classes needed for
 * running the WISE Application.
 * 
 * Things to consider before running WISE on server:
 * 
 * 1. Check if the properties file is correct 2. Check if the configuration is
 * for development or for production
 * 
 * @author Pralav
 * @version 1.0
 */
public class WiseApplicationInitializer implements ServletContextListener {

    public static final Logger LOGGER = Logger.getLogger(WiseApplicationInitializer.class);

    /**
     * Destroys the email scheduler.
     * 
     * @param arg0
     *            ServletContextEvent.
     */
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        EmailScheduler.destroyScheduler();
    }

    /**
     * Initializes all the needed classes and starts the email scheduler thread.
     * 
     * @param servletContextEvent
     *            ServletContextEvent.
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            LOGGER.info("Wise Application initializing");

            String rootFolderPath = servletContextEvent.getServletContext().getRealPath("/");
            WiseProperties properties = new WiseProperties(rootFolderPath + "wise.properties", "WISE");
            String contextPath = servletContextEvent.getServletContext().getContextPath();

            WiseConfiguration configuration = new DevelopmentConfiguration(properties);

            // All initializing statements below
            this.initializeStudySpaceParametersProvider(configuration);
            this.initializeAdminApplication(contextPath, properties);
            this.initializeSurveyApplication(contextPath, rootFolderPath, properties);
            this.startEmailSendingThreads(properties, configuration);
            // end of initializing statements

            LOGGER.info("Wise Application initialized");
        } catch (IOException e) {
            LOGGER.error("IO Exception while initializing", e);
        } catch (IllegalStateException e) {
            LOGGER.error("The admin or the survey app was not " + "initialized, WISE application cannot start", e);
        }

    }

    private void initializeStudySpaceParametersProvider(WiseConfiguration config) {
        StudySpaceParametersProvider.initialize(config.getStudySpaceParameters());
    }

    private void initializeAdminApplication(String contextPath, WiseProperties properties) throws IOException {
        AdminApplication.initialize(contextPath, properties);
    }

    private void initializeSurveyApplication(String contextPath, String rootFolderPath, WiseProperties properties)
            throws IOException {
        SurveyorApplication.initialize(contextPath, rootFolderPath, properties);
    }

    private void startEmailSendingThreads(WiseProperties properties, WiseConfiguration configuration) {
        if (configuration.getConfigType() == WiseConfiguration.CONFIG_TYPE.PRODUCTION) {
            LOGGER.info("Staring Email Scheduler");
            EmailScheduler.intialize(properties);
            EmailScheduler.getInstance().startEmailSendingThreads();
            LOGGER.info("Email Scheduler is alive");
        } else {
            LOGGER.info("Skipping email scheduler in dev mode");
        }
    }
}