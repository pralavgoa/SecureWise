package edu.ucla.wise.initializer;

import java.util.Map;

import org.apache.log4j.Logger;

import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;

/**
 * StudySpaceParametersProvider class is used to talk to WiseStudySpaceWizard
 * and get all the parameters related to study paces in the system.
 * 
 * @author Pralav
 * @version 1.0
 */
public class StudySpaceParametersProvider {

    public static final Logger LOGGER = Logger
	    .getLogger(StudySpaceParametersProvider.class);

    private static StudySpaceParametersProvider studySpaceParametersProvider;

    private final Map<String, StudySpaceParameters> studySpaceParameters;

    /**
     * Singleton constructor to ensure only one object of
     * StudySpaceParametersProvider is created.
     */
    private StudySpaceParametersProvider(
	    Map<String, StudySpaceParameters> studySpaceParameters) {

	this.studySpaceParameters = studySpaceParameters;

	LOGGER.info("Found " + studySpaceParameters.size() + " Study Spaces");
	LOGGER.info("Spaces are " + studySpaceParameters.toString());
    }

    /**
     * Checks if the studySpaceParametersProvider object is already created or
     * not. If not it creates a new instance of studySpaceParametersProvider.
     * 
     * @return true
     */
    public static boolean initialize(
	    Map<String, StudySpaceParameters> studySpaceParameters) {
	if (studySpaceParametersProvider == null) {
	    studySpaceParametersProvider = new StudySpaceParametersProvider(
		    studySpaceParameters);
	} else {
	    LOGGER.info("studySpaceParametersProvider already initialized");
	}
	return true;
    }

    /**
     * Destroys studySpaceParametersProvider and returns true.
     * 
     * @return true
     */
    public static boolean destroy() {
	studySpaceParametersProvider = null;
	return true;
    }

    /**
     * Checks if StudySpaceParametersProvider is null, if it is new
     * StudySpaceParametersProvider is created.
     * 
     * @return StudySpaceParametersProvider returns the initialized parameter.
     */
    public static StudySpaceParametersProvider getInstance() {
	return studySpaceParametersProvider;
    }

    /**
     * Returns all the parameters related to given studySpace.
     * 
     * @param studySpaceName
     *            Name of the studySpace whose parameters are needed.
     * @return StudySpaceParameters returns the studySpace parameters.
     */
    public StudySpaceParameters getStudySpaceParameters(String studySpaceName) {
	LOGGER.info("Requesting parameters for " + studySpaceName);
	if (this.studySpaceParameters.get(studySpaceName) == null) {
	    LOGGER.info("Study space parameters not found");
	    LOGGER.info("Current study space parameters are "
		    + this.getStudySpaceParametersMap().toString());
	}
	LOGGER.info("The desired Study Space is :"
		+ this.studySpaceParameters.get(studySpaceName).toString());
	return this.studySpaceParameters.get(studySpaceName);
    }

    /**
     * Returns all the parameters related to all studySpaces in System.
     * 
     * 
     * @return StudySpaceParameters returns the studySpace parameters map which
     *         contains parameters related to all studySpaces.
     */
    public Map<String, StudySpaceParameters> getStudySpaceParametersMap() {
	return this.studySpaceParameters;
    }

}
