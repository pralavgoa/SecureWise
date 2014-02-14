<%@page import="org.apache.log4j.Logger"%>
<%@page import="edu.ucla.wise.admin.AdminUserSession"%>
<%@page import="edu.ucla.wise.client.interview.InterviewManager"%>
<%@ page contentType="text/html;charset=windows-1252"%><%@ page
	language="java"%><%@ page
	import="edu.ucla.wise.commons.*,java.sql.*,java.util.Date,java.util.*,java.net.*,java.io.*,org.xml.sax.*,org.w3c.dom.*,javax.xml.parsers.*,java.lang.*,javax.xml.transform.*,javax.xml.transform.dom.*,javax.servlet.jsp.JspWriter,javax.xml.transform.stream.*,com.oreilly.servlet.MultipartRequest"%><html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=windows-1252">
<%
    final Logger LOGGER = Logger.getLogger(this.getClass());
	//get the path
	String path = request.getContextPath();
%>
<link rel="stylesheet" href="<%=path%>/style.css" type="text/css">
<title>WISE Administration Tools - Saving the WATI assignments</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">

<%
	session = request.getSession(true);
	if (session.isNew()) {
		response.sendRedirect(path + "/index.html");
		return;
	}

	//get the admin info obj
	AdminUserSession adminUserSession = (AdminUserSession) session
	.getAttribute("ADMIN_USER_SESSION");
	if (adminUserSession == null) {
		response.sendRedirect(path + "/error.htm");
		return;
	}

	String survey_id = request.getParameter("survey");
	String interviewer_id = request.getParameter("interviewer");
	if (interviewer_id == null || interviewer_id.equals("")) {
%>
<p>Error: You must select one interviewer</p>
<%
	return;
	}

	//create interviewer obj
	// Interviewer inv = new Interviewer(adminUserSession);
	// if(!inv.get_interviewer(interviewer_id))
	// {
	//     out.println("Save WATI Error: Can not get the interviewer with id = "+interviewer_id);
	//     return;
	// }

	session.setAttribute("SURVEY_ID", survey_id);
	session.setAttribute("INTERVIEWER_ID", interviewer_id);

	String url = null;//adminUserSession.study_server+ "file_test/interview/Show_Assignment.jsp?SID="+adminUserSession.study_id+"&InterviewerID="+interviewer_id; 

	String whereStr = request.getParameter("whereclause");
	if (whereStr == null || whereStr.equals("")) {
		String allUser = request.getParameter("alluser");
		if (allUser == null || allUser.equals("")) {
	String nonResp = request.getParameter("nonresp");
	if (nonResp == null || nonResp.equals("")) {
		String user[] = request.getParameterValues("user");
		whereStr = "id in (";
		for (int i = 0; i < user.length; i++)
			whereStr += user[i] + ",";
		whereStr = whereStr.substring(0,
				whereStr.lastIndexOf(','))
				+ ")";
	} else {
		whereStr = "id not in (select distinct invitee from survey_subject)";
	}
		} else {
	whereStr = "id in (select distinct id from invitee)";
		}
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
		<font color="#CC6666" face="Times New Roman" size="4"><b>Saving
		WATI Assignments</b></font></td>
		<td width="160" align=center><a href='javascript: history.go(-1)'><img
			src="admin_images/back.gif" border="0"></a></td>
	</tr>
</table>
</center>
<center>
<form method='post' action='reassign_wati.jsp'>

<table class=tth width=600 border=1 cellpadding="2" cellspacing="2"
	bgcolor=#FFFFE1>
	<tr bgcolor=#003366>
		<td align=center><font color=white>ASSIGN&nbsp;&nbsp;WATI</font></td>
	</tr>
	<tr bgcolor=#996600>
		<td align=center><font color=white>Survey ID:<%=survey_id%></font></td>
	</tr>
	<tr>
		<td align=center>
		<%
			boolean new_assign = false;
			boolean pend_assign = false;
			try {
				Connection conn = adminUserSession.getDBConnection();
				Statement stmt = conn.createStatement();
				Statement stmta = conn.createStatement();
				Statement stmtb = conn.createStatement();
				String sql = "SELECT id, firstname, lastname FROM invitee where "
						+ whereStr;
				boolean dbtype = stmt.execute(sql);
				ResultSet rs = stmt.getResultSet();
				while (rs.next()) {
					String invitee_id = rs.getString("id");
					String invitee_firstname = rs.getString("firstname");
					String invitee_lastname = rs.getString("lastname");
					//check the previous assignment
					String sqla = "select assign_date, pending from interview_assignment where interviewer='"
							+ interviewer_id
							+ "' and invitee='"
							+ invitee_id
							+ "' and survey='" + survey_id + "'";
					//Study_Util.email_alert("sqla:"+sqla);
					boolean dbtypea = stmta.execute(sqla);
					ResultSet rsa = stmta.getResultSet();
					//if the new assignment had been done before, ignore it
					if (rsa.next()) {
						String pend_val = rsa.getString("pending");
						//print out the already assigned info - a duplicate
						out.println("<table class=tth width=400 border=1 cellpadding=1 cellspacing=1 bgcolor=#FFFFE1>");
						out.println("<tr bgcolor=#CC6666><td align=center colspan=3><font color=white>Redundant Assignment</font></td></tr>");
						out.println("<tr><td class=sfon align=center>ID</td>");
						out.println("<td align=center>" + interviewer_id
								+ "</td>");
						out.println("<td rowspan=5>");

						if (pend_val.equalsIgnoreCase("1")) {
							out.println("This interviewer had already been assigned to the invitee <b>"
									+ invitee_firstname
									+ " "
									+ invitee_lastname
									+ "</b>. New assignment is <b>ignored</b>.</td></tr>");
						}
						/*
						//the option has already been checked in tool.jsp
						else if(pend_val.equalsIgnoreCase("0"))
						{
						  out.println("This interviewer had been assigned to the invitee in the past <b>"
						 +invitee_firstname+" "+invitee_lastname+"</b> and already finished this interview. New assignment is <b>ignored</b>.</td></tr>");
						}
						 */
						else if (pend_val.equalsIgnoreCase("-1")) {
							pend_assign = true;
							out.println("This interviewer had been assigned to the invitee <b>"
									+ invitee_firstname
									+ " "
									+ invitee_lastname
									+ "</b> in the past and put in pending status now.");
							out.println("Do you want to continue(activiate the pending)? <br>");
							out.println("<input type='radio' name='openpend_"
									+ invitee_id
									+ "' value='yes' checked> YES ");
							out.println("<input type='radio' name='openpend_"
									+ invitee_id + "' value='no'> NO<br>");
							out.println("<input type='hidden' name='inviteepend' value='"
									+ invitee_id + "'></td></tr>");
						}

						out.println("<tr><td class=sfon align=center>Interviewer</td>");
						// out.println("<td align=center>"+inv.first_name+" "+inv.last_name+"</td></tr>");
						out.println("<tr><td class=sfon align=center>Invitee</td>");
						out.println("<td align=center>" + invitee_firstname
								+ " " + invitee_lastname + "</td></tr>");
						out.println("<tr><td class=sfon align=center>Assigned Date</td>");
						out.println("<td align=center>"
								+ rsa.getString("assign_date") + "</td></tr>");
						out.println("</table>");
						out.println("&nbsp;&nbsp;&nbsp;&nbsp;");
					} else {
						out.println("<table class=tth width=400 border=1 cellpadding=1 cellspacing=1 bgcolor=#FFFFE1>");

						//list those interviewers with the same assignment(invitee&survey) before for resignment
						String sqlb = "select id, interviewer, assign_date from interview_assignment where "
								+ "invitee='"
								+ invitee_id
								+ "' and survey='"
								+ survey_id
								+ "' and pending <> -1 and interviewer <>"
								+ interviewer_id;
						boolean dbtypeb = stmta.execute(sqlb);
						ResultSet rsb = stmta.getResultSet();
						Interviewer[] pre_inv = new Interviewer[100];
						String[] pre_id = new String[100];
						String[] pre_date = new String[100];
						int i = 0;

						while (rsb.next()) {
							new_assign = true;
							//    pre_inv[i] = new Interviewer(adminUserSession);
							InterviewManager.getInstance().getInterviewer(
									adminUserSession.getMyStudySpace(),
									rsb.getString("interviewer"));
							//pre_inv[i].get_interviewer(rsb.getString("interviewer"));
							pre_id[i] = rsb.getString("id");
							pre_date[i] = rsb.getString("assign_date");
							i++;
						}

						if (new_assign) {
							out.println("<tr bgcolor=#3399CC>");
							out.println("<td align=center colspan=4><font color=white>Duplicate Assignment</font></td>");
							out.println("</tr><tr>");
							out.println("<td align=left colspan=4>");
							out.println("The assignment are also assigned to the following other interviewers.");
							out.println("In order to continue the new assignment, they have to be set to be reassigned (set to be pending status).");
							out.println("Click the reassign button to cotinue OR click the back button to cancle.");
							out.println("<input type='hidden' name='inviteereassign' value='"
									+ invitee_id + "'></td>");
							out.println("</tr><tr>");
							//out.println("<td class=sfon align=center>&nbsp;</td>");
							out.println("<td class=sfon align=center>ID</td>");
							out.println("<td class=sfon align=center>Interviewer</td>");
							out.println("<td class=sfon align=center>Invitee</td>");
							out.println("<td class=sfon align=center>Assigned Date</td></tr>");

							for (int k = 0; k < i; k++) {
		%>
		
	<tr>
		<td align=center><input type="hidden"
			name="reassignment_<%=invitee_id%>" value="<%=pre_id[k]%>"> <%=pre_inv[k].id%></td>
		<td align=center><%=pre_inv[k].firstName%> <%=pre_inv[k].lastName%></td>
		<td align=center><%=invitee_firstname%> <%=invitee_lastname%></td>
		<td align=center><%=pre_date[k]%></td>
	</tr>
	<%
		} //end for

				} //end of if 2
				else {
					//no duplication, no assigned peers, then make the assignment
					String sqlc = "insert into interview_assignment(interviewer, invitee, survey, assign_date, pending) values('"
							+ interviewer_id
							+ "','"
							+ invitee_id
							+ "','" + survey_id + "', now(), 1)";
					boolean dbtypec = stmta.execute(sqlc);
					ResultSet rsc = stmta.getResultSet();
	%>
	<tr bgcolor=#996600>
		<td align=center colspan=4><font color=white>Interviewer <b><!-- inv.first_name%> -->
		<!-- inv.last_name%></b> has been assigned invitee <b><%=invitee_firstname%> -->
		<%=invitee_lastname%></b>.</font></td>
	</tr>
	<%
		} //end of else

			out.println("</table>&nbsp;&nbsp;&nbsp;&nbsp;");
		} //end of else
			} //end of while
		} catch (Exception e) {
			out.println("error message:" + e.toString());
			LOGGER.error("SAVE WATI - SAVE ASSIGNMENTS:"
			+ e.toString(), e);
		}

		if (new_assign || pend_assign) {
	%>
	</td>
	</tr>
	<tr>
		<td align=center><input type="image" alt="submit"
			src="admin_images/reassign.gif"> <%
 	}
 %>
		</td>
	</tr>
	<tr>
		<td align=center>Now you can go to the WATI page of the current
		interviewer: <a href='<%=url%>'><img
			src="admin_images/go_view.gif" border="0"></a> OR go back to admin
		page by clicking the back button above.</td>
	</tr>
</table>

</form>
</center>
</body>
</html>
