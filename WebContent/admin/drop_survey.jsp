<%@ page contentType="text/html;charset=windows-1252"%><%@ page
	language="java"%><%@ page
	import="edu.ucla.wise.commons.*, 
java.sql.*, java.util.Date, java.util.*, java.net.*, java.io.*,
org.xml.sax.*, org.w3c.dom.*, javax.xml.parsers.*,  java.lang.*,
javax.xml.transform.*, javax.xml.transform.dom.*, 
javax.xml.transform.stream.*, com.oreilly.servlet.MultipartRequest"%><html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=windows-1252">
<%
        //get the path
        String path=request.getContextPath();
%>
<link rel="stylesheet" href="<%=path%>/style.css" type="text/css">
<title>WISE DROP SURVEY DATA</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<center>
<table cellpadding=2 cellspacing="0" border=0>
	<tr>
		<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"></td>
	</tr>
</table>
<table cellpadding=2 cellspacing="0" border=0>
	<tr>
		<td align=center>
		<%
			session = request.getSession(true);
				        if (session.isNew())
				        {
				            response.sendRedirect(path+"/index.html");
				            return;
				        }
				        //get the admin info obj
				        AdminApplication admin_info = (AdminApplication) session.getAttribute("ADMIN_INFO");
				        if(admin_info == null)
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
				String resultStr = admin_info.clearSurvey(survey_id, survey_status);
				out.println( resultStr );
				        
				        //==> send URL request to update survey in remote server
				if (resultStr.indexOf("ERROR") == -1)
				{
				        URL url = new URL(admin_info.getStudyServerPath()+"admin/admin_survey_update?SID="+admin_info.studyId+"&SurveyID="+survey_id+"&SurveyStatus="+survey_status);
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
		%>
		<p><a href="tool.jsp">Return to Administration Tools</a>
		</td>
	</tr>
</table>
</center>
</body>
</html>
