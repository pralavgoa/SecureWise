package edu.ucla.wise.selenium;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.Test;

import com.google.gson.Gson;

import edu.ucla.wise.client.web.WebRequester;
import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;

public class WebRequestTest {
	@Test
	public void webRequestTest() throws IOException{
		// HTTP GET request


		String url = "http://localhost:8080/WiseStudySpaceWizard/getParameters?studySpaceName=all";

		WebRequester wr= new WebRequester(url);
		System.out.println(wr.getStudySpaceParameters("password"));
	}
}
