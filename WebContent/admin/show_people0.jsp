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
        //get the server path
        String path=request.getContextPath();
%>
<link rel="stylesheet" href="<%=path%>/style.css" type="text/css">
<title>WISE Administration Tools - Group Invitees</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<center>
<table cellpadding=2 cellpadding="0" cellspacing="0" border=0>
	<tr>
		<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"></td>
		<td width="160" align=center><a href="javascript: history.go(-1)"><img
			src="admin_images/back.gif" border="0"></a></td>
	</tr>
</table>
<p>
<p>
<p>
<%
	session = request.getSession(true);
        //if the session is expired, go back to the logon page
        if (session.isNew())
        {
            response.sendRedirect(path+"/index.html");
            return;
        }

        //get the admin info object from the session
        AdminApplication admin_info = (AdminApplication) session.getAttribute("ADMIN_INFO");
        String survey_id = request.getParameter("s");
        
        //if the session is invalid, display the error
        if(admin_info == null || survey_id == null)
        {
            response.sendRedirect(path + "/error.htm");
            return;
        }

        //print out the user groups identified by their state
%>

<table cellpadding=2 cellspacing="0" border=1 bgcolor=#FFFFF5>
	<tr>
		<td height=30 bgcolor="#6666CC" align=center colspan=7><font
			color=white><b>Potential users who have not been invited</b></font></td>
	</tr>
	<%=admin_info.printUserState("not_invited", survey_id)%>
</table>
<p>
<table cellpadding=2 cellspacing="0" border=1 bgcolor=#FFFFF5>
	<tr>
		<td height=30 bgcolor="#996600" align=center colspan=7><font
			color=white><b>Invitees who have explicitly declined</b></font></td>
	</tr>
	<%=admin_info.printUserState("declined", survey_id)%>
</table>
<p>
<table cellpadding=2 cellspacing="0" border=1 bgcolor=#FFFFF5>
	<tr>
		<td height=30 bgcolor="#339999" align=center colspan=7><font
			color=white><b>Invitees who have not yet responded</b></font></td>
	</tr>
	<%=admin_info.printUserState("invited", survey_id)%>
	<td colspan=7></td>
	<%=admin_info.printUserState("start_reminder", survey_id)%>
</table>
<p>
<table cellpadding=2 cellspacing="0" border=1 bgcolor=#FFFFF5>
	<tr>
		<td height=30 bgcolor="#6666CC" align=center colspan=7><font
			color=white><b>Invitees who never responded despite all
		reminders</b></font></td>
	</tr>
	<%=admin_info.printUserState("non_responder", survey_id)%>
</table>
<p>
<table cellpadding=2 cellspacing="0" border=1 bgcolor=#FFFFF5>
	<tr>
		<td height=30 bgcolor="#996600" align=center colspan=7><font
			color=white><b>Invitees who are taking the survey now</b></font></td>
	</tr>
	<%=admin_info.printUserState("started", survey_id)%>
</table>
<p>
<table cellpadding=2 cellspacing="0" border=1 bgcolor=#FFFFF5>
	<tr>
		<td height=30 bgcolor="#339999" align=center colspan=7><font
			color=white><b>Invitees who quit the survey before
		completion</b></font></td>
	</tr>
	<%=admin_info.printUserState("interrupted", survey_id)%>
	<td colspan=7></td>
	<%=admin_info.printUserState("completion_reminder", survey_id)%>
</table>
<p>
<table cellpadding=2 cellspacing="0" border=1 bgcolor=#FFFFF5>
	<tr>
		<td height=30 bgcolor="#6666CC" align=center colspan=7><font
			color=white><b>Invitees who failed to complete the survey
		despite completion reminders </b></font></td>
	</tr>
	<%=admin_info.printUserState("incompleter", survey_id)%>
</table>
<p>
</table>
<table cellpadding=2 cellspacing="0" border=1 bgcolor=#FFFFF5>
	<tr>
		<td height=30 bgcolor="#996600" align=center colspan=7><font
			color=white><b>Invitees who completed the survey</b></font></td>
	</tr>
	<%=admin_info.printUserState("completed", survey_id)%>
</table>
<p>
</center>
</body>
</html>
