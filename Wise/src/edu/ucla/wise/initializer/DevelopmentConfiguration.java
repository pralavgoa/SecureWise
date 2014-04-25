package edu.ucla.wise.initializer;

import java.util.HashMap;
import java.util.Map;

import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;

public class DevelopmentConfiguration extends WiseConfiguration {

    public DevelopmentConfiguration(WiseProperties wiseProperties) {
	super(wiseProperties);
    }

    @Override
    public Map<String, StudySpaceParameters> getStudySpaceParameters() {
	Map<String, StudySpaceParameters> parameters = new HashMap<>();
	StudySpaceParameters ssp = new StudySpaceParameters("wisedev", "2",
		"http://localhost:8080", "WISE", "survey", "wisedev",
		"wisedev", "wisedev", "password",
		"Wise Developement Environment", "wisewisewisewise", "0");
	parameters.put("wisedev", ssp);
	return parameters;
    }

    @Override
    public void reload() {
	// TODO Auto-generated method stub

    }

    @Override
    public CONFIG_TYPE getConfigType() {
	return WiseConfiguration.CONFIG_TYPE.DEVELOPMENT;
    }

}
