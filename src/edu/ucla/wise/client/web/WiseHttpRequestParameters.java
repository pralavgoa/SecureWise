package edu.ucla.wise.client.web;

import javax.servlet.http.HttpServletRequest;

import edu.ucla.wise.commons.SanityCheck;

public class WiseHttpRequestParameters {

	private final HttpServletRequest request;
	
	public WiseHttpRequestParameters(HttpServletRequest request){
		this.request = request;
	}
	
	public String getAlphaNumericParameterValue(String parameter){
		String value = SanityCheck.onlyAlphaNumeric(request.getParameter(parameter));
		return value;
	}
	
	public String getNonSanitizedStringParameter(String parameter){
		return request.getParameter(parameter);
	}
	
	public String getEncodedStudySpaceId(){
		return getAlphaNumericParameterValue("t");
	}
	
	public String getEncodedMessageId(){
		return  getAlphaNumericParameterValue("msg");
	}
	
	public String getEncodedSurveyId(){
		return  getAlphaNumericParameterValue("s");
	}
}