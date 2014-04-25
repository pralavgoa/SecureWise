package edu.ucla.wise.client.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.admin.AdminUserSession;
import edu.ucla.wise.commons.SanityCheck;

/**
 * Use this class to get parameters from HTTP requests.
 * 
 * @author pdessai
 * 
 */
public class WiseHttpRequestParameters {

    /**
     * HttpRequest to be wrapped by this method.
     */
    private final HttpServletRequest request;

    /**
     * Constructor to create a wrapper around request.
     * 
     * @param request
     */
    public WiseHttpRequestParameters(HttpServletRequest request) {
	this.request = request;
    }

    public String getAlphaNumericParameterValue(String parameter) {
	String value = SanityCheck.onlyAlphaNumeric(this.request
		.getParameter(parameter));
	return value;
    }

    public String getNonSanitizedStringParameter(String parameter) {
	return this.request.getParameter(parameter);
    }

    public String getEncodedStudySpaceId() {
	return this.getAlphaNumericParameterValue("t");
    }

    public String getEncodedMessageId() {
	return this.getAlphaNumericParameterValue("msg");
    }

    public String getEncodedSurveyId() {
	return this.getAlphaNumericParameterValue("s");
    }

    public AdminUserSession getAdminUserSessionFromHttpSession() {
	return (AdminUserSession) this.getSession(true).getAttribute(
		"ADMIN_USER_SESSION");
    }

    public HttpSession getSession(boolean createNew) {
	return this.request.getSession(createNew);
    }
}
