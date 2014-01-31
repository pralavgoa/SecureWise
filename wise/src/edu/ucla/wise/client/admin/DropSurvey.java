package edu.ucla.wise.client.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.admin.AdminUserSession;
import edu.ucla.wise.client.web.WiseHttpRequestParameters;
import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.SanityCheck;

@WebServlet("/admin/dropSurvey")
public class DropSurvey extends HttpServlet{
	private static final long serialVersionUID = 1L;


	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		
		WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(request);
		
		PrintWriter out = response.getWriter();

		//get the path
		String path=request.getContextPath();

		HttpSession session = request.getSession(true);
		if (session.isNew())
		{
			response.sendRedirect(path+"/index.html");
			return;
		}
		//get the admin info obj
		AdminUserSession adminUserSession = parameters.getAdminUserSessionFromHttpSession();
		if(adminUserSession == null)
		{
			response.sendRedirect(path + "/error.htm");
			return;
		}

		String survey_id = request.getParameter("s");
		String survey_status = request.getParameter("t");

		//Security feature changes

		if(SanityCheck.sanityCheck(survey_id) || SanityCheck.sanityCheck(survey_status)) {
			response.sendRedirect(path + "/admin/sanity_error.html");
			return;
		}

		survey_id = SanityCheck.onlyAlphaNumeric(survey_id);
		survey_status= SanityCheck.onlyAlphaNumeric(survey_status);

		//End changes

		if(survey_id == null || survey_id.isEmpty() || survey_status==null || survey_status.isEmpty()){
			response.sendRedirect(path + "/admin/parameters_error.html");
			return;
		}

		//==> run the updates on the database
		String resultStr = adminUserSession.clearSurvey(survey_id, survey_status);
		out.println( resultStr );

		//==> send URL request to update survey in remote server
		if (resultStr.indexOf("ERROR") == -1)
		{
			URL url = new URL(adminUserSession.getStudyServerPath()+"admin/admin_survey_update?SID="+adminUserSession.getStudyId()+"&SurveyID="+survey_id+"&SurveyStatus="+survey_status);
			// ==>

			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			String upload_result="";
			while ((line = in.readLine()) != null)
			{
				upload_result += line;
			}

			//AdminInfo.email_alert("SURVEY UPDATE RESULT IS " + upload_result);
			in.close();
			out.println(upload_result);
		}
		
		request.getRequestDispatcher("/admin/drop_survey.jsp").forward(request, response);
	}
}
