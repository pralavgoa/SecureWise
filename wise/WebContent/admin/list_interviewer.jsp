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
        //get the path
        String path=request.getContextPath();
%>
<link rel="stylesheet" href="<%=path%>/style.css" type="text/css">
<title>List All the Interviewers</title>
<STYLE>
.tw {
	FILTER: progid : DXImageTransform.Microsoft.dropShadow (   Color =
		#999999, offX = 4, offY = 4, positive = true );
	BORDER-LEFT-COLOR: #333;
	BORDER-BOTTOM-COLOR: #333;
	BORDER-TOP-COLOR: #333;
	BORDER-RIGHT-COLOR: #333;
	BORDER-COLLAPSE: collapse;
	Border-spacing: 1
}
</STYLE>
<script language="javascript">
function remove_profile()
{
 document.form1.edit_type.value = "1";
 document.form1.submit();
}
</script>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<p>
<p>
<p>
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
%>

<center>
<table cellpadding=2 cellpadding="0" cellspacing="0" border=0>
	<tr>
		<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"><br>
		<br>
		<font color="#CC6666" face="Times New Roman" size="4"><b>EDIT
		INTERVIEWER</b></font></td>
		<td width="160" align=center><a href="javascript:window.close()">
		<img src="admin_images/finish.gif" border="0"></a></td>
	</tr>
</table>
<form name="form1" method="post" action="edit_profile.jsp">
<table width=400 border=1 cellpadding="2" cellspacing="0">
	<tr bgcolor=#6699CC>
		<td align=center colspan=2><font color=white>Edition Tools</font></td>
	</tr>
	<tr>
		<td align=center><a href="edit_profile.jsp" target="_self"> <img
			src="admin_images/add.gif" border="0"></a></td>
		<td>Add a new interviewer</td>
	</tr>
	<tr>
		<td align=center><input type="image" alt="submit"
			src="admin_images/edit.gif"></td>
		<td>Select and update the existing interviewers</td>
	</tr>
	<tr>
		<td align=center><a href="javascript: remove_profile()"
			target="_self"> <img src="admin_images/remove.gif" border="0"></a>
		</td>
		<td>Select and remove the existing interviewers</td>
	</tr>
	<tr>
		<td colspan=2>
		<p><input type="hidden" name="edit_type" value="0">
		</td>
	</tr>
</table>
<p>
<p>
<table width=600 class=tw border=1 cellpadding="2" cellspacing="2"
	bgcolor=#FFFFE1>
	<tr>
		<TD width="1" height="98%" bgColor=#6699CC rowSpan=100></TD>
		<TD width="98%" height="1" bgColor=#6699CC colspan=7></TD>
		<TD width="1" height="98%" bgColor=#6699CC rowSpan=100></TD>
	</tr>
	<tr bgcolor=#6699CC>
		<td align=center colspan=7><font color=white>Current
		Interviewer List</font></td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td align=center class=spt>&nbsp;User Name</td>
		<td align=center class=spt>&nbsp;Salutation</td>
		<td align=center class=spt>&nbsp;First Name</td>
		<td align=center class=spt>&nbsp;Last Name</td>
		<td align=center class=spt>&nbsp;Email</td>
		<td align=center class=spt>&nbsp;Go to WATI</td>
	</tr>

	<%
		try
	      {
	          // open database connection
	          Connection conn = adminUserSession.getDBConnection();
	          Statement statement = conn.createStatement();

	          String sql = "select id, username, firstname, lastname, salutation, email from interviewer";
	          
	          boolean results = statement.execute(sql);
	          ResultSet rs = statement.getResultSet();

	          String id, user_name, first_name, last_name, salutation, email;

	          while (rs.next())
	          {
	              id = rs.getString("id");
	              user_name = rs.getString("username");
	              first_name = rs.getString("firstname");
	              last_name = rs.getString("lastname");
	              salutation = rs.getString("salutation");
	              email = rs.getString("email");
	%>
	<tr>
		<td align=center><input type="checkbox" name="interviewer"
			value="<%=id%>"></td>
		<td align=center><%=user_name%></td>
		<td align=center><%=salutation%></td>
		<td align=center><%=first_name%></td>
		<td align=center><%=last_name%></td>
		<td><%=email%></td>
		<td align=center><a href="goto_wati.jsp?interview_id=<%=id%>"><img
			src="admin_images/go_view.gif" border="0"></a></td>
	</tr>
	<%
		}
		          rs.close();
		          statement.close();
		          conn.close();
		      }
		      catch (Exception e)
		      {
		          AdminApplication.logError("LIST INTERVIEWER:"+e.toString(), e);
		      }
	%>

	<tr>
		<TD width="98%" height=1 colspan=7 bgColor=#6699CC></TD>
	</tr>
</table>
<p>
<p>
</form>
</center>
</body>
</html>
