package edu.ucla.wise.initializer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.ucla.wise.client.web.WebRequester;
import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;

/**
 * Class contains configuration when the code is run in production. Provides
 * separation from DevelopmentConfiguration
 * 
 * @author pdessai
 * 
 */
public class ProductionConfiguration extends WiseConfiguration {

    public ProductionConfiguration(WiseProperties wiseProperties) {
	super(wiseProperties);
    }

    /**
     * Logger for this class
     */
    private static final Logger LOGGER = Logger
	    .getLogger(ProductionConfiguration.class);

    @Override
    public final Map<String, StudySpaceParameters> getStudySpaceParameters() {
	try {
	    WebRequester webRequester = new WebRequester(this
		    .getWiseProperties().getStudySpaceWizardParametersUrl());
	    return webRequester.getStudySpaceParameters(this
		    .getWiseProperties().getStudySpaceWizardPassword());
	} catch (MalformedURLException e) {
	    LOGGER.error(e);
	} catch (IOException e) {
	    LOGGER.error(e);
	}
	return null;
    }

    @Override
    public void reload() {
	// TODO Auto-generated method stub

    }

    @Override
    public final CONFIG_TYPE getConfigType() {
	return WiseConfiguration.CONFIG_TYPE.PRODUCTION;
    }

}
