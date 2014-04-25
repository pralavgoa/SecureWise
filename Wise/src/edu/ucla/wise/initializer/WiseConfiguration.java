package edu.ucla.wise.initializer;

import java.util.Map;

import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;

public abstract class WiseConfiguration {

    static enum CONFIG_TYPE {
	PRODUCTION, DEVELOPMENT
    };

    private final WiseProperties wiseProperties;

    public WiseConfiguration(WiseProperties wiseProperties) {
	this.wiseProperties = wiseProperties;
    }

    public abstract Map<String, StudySpaceParameters> getStudySpaceParameters();

    public abstract void reload();

    public abstract CONFIG_TYPE getConfigType();

    public WiseProperties getWiseProperties() {
	return wiseProperties;
    }
}
