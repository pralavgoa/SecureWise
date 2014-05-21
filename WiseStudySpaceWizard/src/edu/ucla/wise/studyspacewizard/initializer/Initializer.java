package edu.ucla.wise.studyspacewizard.initializer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class Initializer implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(Initializer.class);

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // do nothing
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        LOGGER.info("Initializing WISEStudySpaceWizard.");
        StudySpaceWizard.initialize();
        LOGGER.info("WISEStudySpaceWizard initialized.");

    }
}
