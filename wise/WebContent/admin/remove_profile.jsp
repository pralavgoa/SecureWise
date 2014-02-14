<%@page import="org.apache.log4j.Logger"%>
<%@page import="edu.ucla.wise.admin.AdminUserSession"%>
<%@ page contentType="text/html;charset=windows-1252"%><%@ page
	language="java"%><%@ page
	import="edu.ucla.wise.commons.*, java.sql.*, java.util.Date, java.util.*, java.net.*, java.io.*,
org.xml.sax.*, org.w3c.dom.*, javax.xml.parsers.*,  java.lang.*,
javax.xml.transform.*, javax.xml.transform.dom.*, 
javax.xml.transform.stream.*, com.oreilly.servlet.MultipartRequest"%><html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=windows-1252">
<%
        final Logger LOGGER = Logger.getLogger(this.getClass());
        //get the path
        String path=request.getContextPath();
%>
<link rel="stylesheet" href="<%=path%>/style.css" type="text/css">
<title>Remove Interviewers</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<center>
<table cellpadding=2 cellpadding="0" cellspacing="0" border=0>
	<tr>
		<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"><br>
		<br>
		<font color="#CC6666" face="Times New Roman" size="4"><b>Saving
		WATI Assignments</b></font></td>
		<td width="160" align=center>&nbsp;</td>
	</tr>
</table>
</center>
<br>
<center>
<table class=tth width=500 border=1 cellpadding="2" cellspacing="2"
	bgcolor=#FFFFE1>
	<tr bgcolor=#003366>
		<td align=center><font color=white>REMOVE&nbsp;&nbsp;RESULT</font></td>
	</tr>
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
				        AdminUserSession adminUserSession = (AdminUserSession) session.getAttribute("ADMIN_USER_SESSION");
				        if(adminUserSession == null)
				        {
				            response.sendRedirect(path + "/error.htm");
				            return;
				        }

				        
				        String []  interviewer = (String[]) session.getAttribute("INVLIST");

				        if(interviewer!=null)
				        {

				          try
				          {
				              Connection conn = adminUserSession.getDBConnection();
				              Statement statement = conn.createStatement();
				              String sql = "delete from interviewer where id in (";
				              for (int i = 0; i < interviewer.length; i++)
				              {
				                 sql += interviewer[i];
				                 if(i<interviewer.length-1)
				                  sql+=", ";
				                 else
				                  sql+=")";
				              }
				              boolean results = statement.execute(sql);
				              out.println("The removing of interviewer(s) is done.");
				          }
				          catch (Exception e)
				          {
				              LOGGER.error("REMOVE INTERVIEWERS:"+e.toString(), e);
				              out.println("Error of removing:" + e.toString());
				          }

				        }
				        else
				        {
				          out.println("Can not get the interviewer list that is ready to remove");
				        }
		%>
		</td>
	</tr>
</table>
<p>
<p>
<p>
<p><a href="list_interviewer.jsp"><img
	src="admin_images/back.gif" border="0"></a>
</center>
