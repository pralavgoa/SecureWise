package edu.ucla.wise.studyspacewizard.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasypt.util.text.BasicTextEncryptor;

import com.google.common.base.Strings;
import com.google.gson.Gson;

import edu.ucla.wise.studyspacewizard.Constants;
import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;

@WebServlet("/getParameters")
public class StudySpaceParametersServlet extends HttpServlet{

	//!!! Move the password to properties file!
	private static final String PASSWORD = "password";
	
	private static final long serialVersionUID = 5762548314423256943L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
		PrintWriter out =  response.getWriter();
		String studySpaceName = request.getParameter(Constants.STUDY_SPACE_NAME);
		
		if(Strings.isNullOrEmpty(studySpaceName)){
			out.write("Please provide study space name");
			return;
		}
		
		DatabaseConnector databaseConnector = new DatabaseConnector();
		
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(PASSWORD);
		
		Gson gson = new Gson();
		if(Constants.ALL.equals(studySpaceName)){

		out.write(textEncryptor.encrypt(gson.toJson(databaseConnector.getAllStudySpaceParameters())));
			
		}else{
			out.write(textEncryptor.encrypt(gson.toJson(databaseConnector.getStudySpaceParameters(studySpaceName))));
		}
	}
}
