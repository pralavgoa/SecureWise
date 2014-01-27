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
<title>Ressign WATI</title>
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
		<td align=center><font color=white>REASSIGNMENT RESULT</font></td>
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

				        String interviewer_id = (String) session.getAttribute("INTERVIEWER_ID"); 
				        String survey_id = (String) session.getAttribute("SURVEY_ID");
				        session.removeAttribute("INTERVIEWER_ID");
				        session.removeAttribute("SURVEY_ID");

				        String invitee_reassign[] = request.getParameterValues("inviteereassign");
				        String invitee_pend[]= request.getParameterValues("inviteepend");        
				        
				        try
				        {
				          Connection conn = adminUserSession.getDBConnection();
				          Statement stmt = conn.createStatement();
				          Statement stmta = conn.createStatement();
				          if(invitee_pend!=null)
				          {
				              //update the pending status
				              for (int i = 0; i < invitee_pend.length; i++)
				              {
				                String pend_attr = "openpend_"+invitee_pend[i];
				                String open_pend = request.getParameter(pend_attr);
				                if(open_pend.equalsIgnoreCase("yes"))
				                {
				                  String sql = "update interview_assignment set pending=1 where invitee="+invitee_pend[i];
				                  sql +=" and interviewer="+interviewer_id+" and survey='"+survey_id+"'";
				                  boolean dbtype = stmt.execute(sql);
				                  ResultSet rs = stmt.getResultSet();
				                  out.println("The new assignment has activiated the pending status");
				                }
				              }
				          }
				          if(invitee_reassign!=null)
				          {
				              //insert the new assignment
				              for(int j=0; j < invitee_reassign.length; j++)
				              {
				                String invitee_id = invitee_reassign[j];
				                String sql = "insert into interview_assignment(interviewer, invitee, survey, assign_date, pending) values('"
				                +interviewer_id+"','"+invitee_id+"','"+survey_id+"', now(), 1)";
				                boolean dbtype = stmt.execute(sql);
				                ResultSet rs = stmt.getResultSet();
				              
				                out.println("The new reassignment has been created.<br>");
				                String reassign_attr = "reassignment_"+invitee_id;
				                String reassign_id[] = request.getParameterValues(reassign_attr);

				                if(reassign_id!=null)
				                {
				                  //make the reassignment for the current invitee
				                  sql = "update interview_assignment set pending=-1 where id in (";
				                  //String sql = "delete from interview_assignment where id in(";
				                  for (int i = 0; i < reassign_id.length; i++)
				                  {
				                        sql += reassign_id[i];
				                        if(i<reassign_id.length-1)
				                          sql += ", ";
				                  }
				                  sql += ")";
				                  dbtype = stmt.execute(sql);
				                  rs = stmt.getResultSet();
				                  out.println("And the reassignments have been updated.");
				                } //end if
				              } //end for
				          } //end if
				          stmt.close();
				          conn.close();
				        }
				        catch (Exception e)
				        {
				            out.println("error message:"+e.toString());
				            AdminApplication.logError("REASSIGN WATI:"+e.toString(), e);
				        }
		%>
		</td>
	</tr>
</table>
<p>
<p>
<p>
<p><a href="javascript: history.go(-2)"><img
	src="admin_images/back.gif" border="0"></a>
</center>

</body>
</html>
