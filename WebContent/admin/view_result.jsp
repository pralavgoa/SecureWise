<%@ page contentType="text/html;charset=windows-1252"%><%@ page
	language="java"%><%@ page
	import="edu.ucla.wise.commons.*,java.sql.*,java.util.Date,java.util.*,java.net.*,java.io.*,org.xml.sax.*,org.w3c.dom.*,javax.xml.parsers.*,java.lang.*,javax.xml.transform.*,javax.xml.transform.dom.*,javax.servlet.jsp.JspWriter,javax.xml.transform.stream.*,com.oreilly.servlet.MultipartRequest"%><html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=windows-1252">
<script>
	//remove the check of the option for all invitees
	function remove_check_allusers() {
		if (document.form1.alluser.checked)
			document.form1.alluser.checked = false;
	}

	//remove the check of the option for all the single invitees 
	function remove_check_oneuser() {
		for (i = 0; i < document.form1.length; i++) {
			if (document.form1.elements[i].type == "checkbox"
					&& document.form1.elements[i].name != "alluser")
				document.form1.elements[i].checked = false;
		}
	}
</script>
<%
	//get the server path
	String path = request.getContextPath();
%>
<link rel="stylesheet" href="<%=path%>/admin/style.css" type="text/css">
<title>WISE Administration Tools - View Results</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<%
	session = request.getSession(true);
	//if the session is expired, go back to the logon page
	if (session.isNew()) {
		response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP
		+ "/index.html");
		return;
	}
	//get the admin info object from the session
	AdminApplication admin_info = (AdminApplication) session
	.getAttribute("ADMIN_INFO");
	//get the survey ID
	String survey_id = request.getParameter("s");
	//security feature changes
	if(SanityCheck.sanityCheck(survey_id)) {
		response.sendRedirect(path + "/admin/sanity_error.html");
		return;
	}
	survey_id=SanityCheck.onlyAlphaNumeric(survey_id);
	//if the session is invalid, display the error
	if (admin_info == null || survey_id == null) {
		response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP
		+ "/error.htm");
		return;
	}
%>
<center>
<table cellpadding=2 cellspacing="0" border=0>
	<tr>
		<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"><br>
		<br>
		<font color="#CC6666" face="Times New Roman" size="4"><b>VIEW
		RESULTS</b></font></td>
		<td width="160" align=center><a href="tool.jsp"><img
			src="admin_images/back.gif" border="0"></a></td>
	</tr>
</table>
<p>
<p>
<form name="form1" method='post' action='<%=path%>/admin/survey_result'><input
	type='hidden' name='s' value='<%=survey_id%>'> <br>

<table class=tth border=1 cellpadding="2" cellspacing="0"
	bgcolor=#FFFFF5>
	<tr>
		<td colspan=4>Enter WHERE clause for invitees in the data table:
		<input type='text' name='whereclause' width=60></td>
	</tr>
	<tr>
		<td colspan=4>OR <input type='checkbox' name='alluser'
			value='ALL' onClick='javascript: remove_check_oneuser()'>
		Select ALL recipients</td>
	</tr>
	<tr>
		<td colspan=4>OR select recipients for the message:</td>
	</tr>

	<%
		try {
		//connect to the database
		Connection conn = admin_info.getDBConnection();
		Statement stmt = conn.createStatement();
		//get the survey responders' info
		String sql = "SELECT d.invitee, i.firstname, i.lastname, i.salutation, AES_DECRYPT(i.email,'"
		+ admin_info.myStudySpace.db.emailEncryptionKey
		+ "') FROM invitee as i, ";
		sql += survey_id
		+ "_data as d where d.invitee=i.id order by i.id";
		boolean dbtype = stmt.execute(sql);
		ResultSet rs = stmt.getResultSet();

		out.println("<tr>");
		out.println("<td class=sfon>&nbsp;</td>");
		out.println("<td class=sfon align=center>User ID</td>");
		out.println("<td class=sfon align=center>User Name</td>");
		out.println("<td class=sfon align=center>User's Email Address</td></tr>");

		while (rs.next()) {
			out.println("<tr>");
			out.println("<td align=center><input type='checkbox' name='user' value='"
			+ rs.getString(1)
			+ "' onClick='javascript: remove_check_allusers()'></td>");
			out.println("<td align=center>" + rs.getString(1) + "</td>");
			out.println("<td align=center>" + rs.getString(4) + " "
			+ rs.getString(2) + " " + rs.getString(3) + "</td>");
			out.println("<td align=center>" + rs.getString(5) + "</td>");
			out.println("</tr>");
		}
		rs.close();
		stmt.close();
		conn.close();
			} catch (Exception e) {
		AdminApplication.logError("WISE ADMIN - VIEW RESULT:"
		+ e.toString(), e);
			}
	%>
</table>
<br>
<center><input type="image" alt="submit"
	src="admin_images/viewresults.gif"><br>
</form>
<hr>
</center>
</body>
</html>
