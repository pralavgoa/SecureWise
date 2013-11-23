<%@page import="edu.ucla.wise.commons.WiseConstants"%>
<%@page import="edu.ucla.wise.commons.WiseConstants.SURVEY_STATUS"%>
<%@page import="org.apache.catalina.authenticator.Constants"%>
<%@page import="edu.ucla.wise.admin.healthmon.HealthMonitoringManager"%>
<%@page import="edu.ucla.wise.admin.healthmon.HealthStatus"%>
<%@ page contentType="text/html;charset=windows-1252"%><%@ page
	language="java"%>
<%@ page
	import="edu.ucla.wise.commons.*,edu.ucla.wise.commons.WISEApplication,java.sql.*,java.util.Date,java.util.*,java.net.*,java.io.*,org.xml.sax.*,org.w3c.dom.*,javax.xml.parsers.*,java.lang.*,java.text.*,javax.xml.transform.*,javax.xml.transform.dom.*,javax.xml.transform.stream.*,com.oreilly.servlet.MultipartRequest"%>
<html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=windows-1252">
<%
	//get the server path
	String path = request.getContextPath();
	path = path + "/";
	AdminApplication admin_info;
	Date today1 = new Date();
	DateFormat f = new SimpleDateFormat("E");
	String wkday = f.format(today1);
%>
<script>
	var sid, jid, jstatus;
	//sid - survey's internal ID
	//jid - survey ID
	//jstatus - D: clean up the survey data (developing mode)
	//          R: remove the survey data table (developing mode)
	//          P: archive the survey data (production mode)

	//manipulate the survey data according to the situation selected (jstatus)
	function remove_confirm() {
		var msg;
		if (jstatus.toUpperCase() == "R") {
			msg = "\nThis operation will remove the survey and permanently delete all data collected."
					+ "\n(Note this operation is not available for surveys in Production mode.) \nAre you sure you want to continue?\n";
		} else if (jstatus.toUpperCase() == "P") {
			msg = "\nThis operation will remove the survey from the available list and will archive any data collected.\n"
					+ "Are you sure you want to continue?\n";
		} else {
			msg = "\nThis operation will clear all submitted data and associated tracking data for this survey."
					+ "\n(Note this operation is not available for surveys in Production mode.)\nAre you sure you want to continue?\n";
		}
		var url = "drop_survey.jsp?s=" + jid + "&t=" + jstatus;
		if (confirm(msg))
			location.replace(url);
		else
			return;
	}

	//change the survey mode from developing to production
	function change_mode() {
		var msg = "\nYou are about to change the survey mode from development to production.\n"
				+ "Are you sure to continue this operation?\n";
		var url = "dev2prod?s=" + sid;
		if (confirm(msg))
			location.replace(url);
		else
			return;
	}
</script>

<link rel="stylesheet" href="<%=path%>admin/style.css" type="text/css">
<title>WISE Administration Tools</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
	<%
		try {
		session = request.getSession(true);
		//if the session is expired, go back to the logon page
		if (session.isNew()) {
			response.sendRedirect(path + WiseConstants.ADMIN_APP
			+ "/index.html");
			return;
		}
		//get the admin info object from session
		admin_info = (AdminApplication) session.getAttribute("ADMIN_INFO");
		if (admin_info == null) {
			response.sendRedirect(path + WiseConstants.ADMIN_APP
			+ "/error.htm");
			return;
		}
		admin_info.loadRemote(WiseConstants.SURVEY_HEALTH_LOADER,
		admin_info.studyName);
		//get the weekday format of today to name the data backup file
			} catch (Exception e) {
		//WISE_Application.log_error("WISE ADMIN - TOOL init: ", e); 
		
		PrintWriter out2 =response.getWriter();
		out2.print("******There has Been and exception********");
		return;
			}
	%>
	<center>
		<table cellpadding=2 cellspacing=0 border=0>
			<tr>
				<td width="160" align=center><img
					src="admin_images/somlogo.gif" border="0"></td>
				<td width="400" align="center"><img
					src="admin_images/title.jpg" border="0"><br> <br> <font
					color="#CC6666" face="Times New Roman" size="4"><b><%=admin_info.studyTitle%></b></font>
				</td>
				<td width="160" align=center><a
					href="<%=path + WiseConstants.ADMIN_APP%>/logout"><img
						src="admin_images/logout_b.gif" border="0"></a></td>
			</tr>
		</table>
	</center>
	<p>
	<p>
	<center>
		<table border=0>
			<tr>
				<td align=left valign=middle>
					<table class=tth border=1 cellpadding="2" cellspacing="0"
						bgcolor=#FFFFF5>
						<tr>
							<td height=30 width=400 bgcolor="#FF9900" align=center><font
								color=white><b>File Upload</b></font></td>
						</tr>
						<tr>
							<td align="center" valign="middle" width="400">
								<FORM action="load_data" method="post"
									encType="multipart/form-data">
									Select a survey(xml), message(xml), preface(xml), invitee(csv),
									consent form(xml), style sheet(css) or image(jpg/gif) to
									upload:<br> &nbsp;<br> <INPUT type=file name=file>
									&nbsp; <input type="image" alt="submit"
										src="admin_images/upload.gif">
								</FORM>
							</td>
						</tr>
					</table>
					<table class=tth border=1 cellpadding="2" cellspacing="0"
						bgcolor=#FFFFF5>
						<tr>
							<td height=30 width=400 bgcolor="#FF9900" align=center><font
								color=white><b>Health</b></font></td>
						</tr>
						<tr>
							<%
								String dbCellColor = HealthStatus.getInstance().isDbIsAlive()
															? "#008000"
															: "#FF0000";
													String dbStatus = HealthStatus.getInstance().isDbIsAlive()
															? "OK"
															: "Fail";
													String smtpCellColor = HealthStatus.getInstance().isSmtpIsAlive()
															? "#008000"
															: "#FF0000";
													String smtpStatus = HealthStatus.getInstance().isSmtpIsAlive()
															? "OK"
															: "Fail";
													SURVEY_STATUS studyServerStatus = HealthStatus.getInstance()
															.isSurveyAlive(admin_info.studyName,
																	admin_info.myStudySpace.db);
													String surveyCellColor = null, surveyStatus = null;
													switch (studyServerStatus) {
														case OK :
															surveyCellColor = "#008000";
															surveyStatus = "OK";
															break;
														case FAIL :
															surveyCellColor = "#FF0000";
															surveyStatus = "Fail";
															break;
														case NOT_AVAIL :
															surveyCellColor = "#FF6F00";
															surveyStatus = "Not Available";
															break;
													}
							%>

							<td><b><i>Database</i> <font color="<%=dbCellColor%>">
										<%=dbStatus%>
								</font></b>&nbsp; <b><i>Mail System</i> <font
									color="<%=smtpCellColor%>"> <%=smtpStatus%>
								</font></b>&nbsp; <b>Survey Server <i><%=admin_info.studyName%></i> <font
									color="<%=surveyCellColor%>"> <%=surveyStatus%>
								</font></b></td>
						</tr>

					</table>

				</td>
				<td align=right valign=middle>
					<table class=tth border=1 cellpadding="2" cellspacing="2"
						bgcolor=#FFFFF5>
						<tr>
							<td height=30 width=250 bgcolor="#339999" align=center><font
								color=white><b>All-Survey Functions</b></font></td>
						</tr>
						<tr>
							<td align=left width=250><font size=-1><a
									href="load_invitee.jsp">Manage</a> invitees<br> &nbsp;<br>
									<a href="list_interviewer.jsp">Manage</a> interviewers<br>
									&nbsp;<br> <a
									href="download_file.jsp?fileName=preface.xml">Download</a>
									preface file<br> &nbsp;<br> <a
									href="download_file.jsp?fileName=style.css">Download</a> online
									style sheet<br> &nbsp;<br> <a
									href="download_file.jsp?fileName=print.css">Download</a>
									printing style sheet<br> &nbsp;<br> <a
									href="download_file.jsp?fileName=<%=admin_info.studyName%>_<%=wkday%>.sql">Download</a>
									MySQL dump file </font></td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</center>
	<p>
		<%
			try {
				//connect to the database
				Connection conn = admin_info.getDBConnection();
				Statement stmt = conn.createStatement();
				Statement stmt2 = conn.createStatement();

				String id, internal_id, filename, status, title, uploaded;
		%>
	
	<center>
		<table class=tth border=1 cellpadding="2" cellspacing="0"
			bgcolor=#FFFFF5>
			<tr bgcolor=#CC6666>
				<th align=center colspan=4><font color=white>CURRENT
						ACTIVE SURVEYS</font></th>
			</tr>
			<tr>
				<th class=sfon>Survey ID, <b>Title</b>, Uploaded Date & (<i>Status</i>)
				</th>
				<th class=sfon>User State</th>
				<th class=sfon>User Counts</th>
				<th class=sfon width=40%>Actions</th>
			</tr>
			<%
				//get the survey information from the database
					String sql2 = "select internal_id, id, filename, title, status, uploaded from surveys where status in ('P', 'D') and internal_id in"
							+ "(select max(internal_id) from surveys group by id) order by uploaded DESC";
					boolean dbtype = stmt2.execute(sql2);
					ResultSet rs2 = stmt2.getResultSet();
					while (rs2.next()) {
						internal_id = rs2.getString(1);
						id = rs2.getString(2);
						filename = rs2.getString(3);
						title = rs2.getString(4);
						status = rs2.getString(5);
						uploaded = rs2.getString(6);
						//assign the survey mode
						String status_exp = new String();
						if (status.equalsIgnoreCase("D"))
							status_exp = "Development";
						if (status.equalsIgnoreCase("P"))
							status_exp = "Production";
			%>
			<tr>
				<td align="center"><%=id%><br> <br> <b><%=title%></b><br>
					<br><%=uploaded%><br> <br> (<i><%=status_exp%>
						Mode</i>)<br>Copy-Paste link for anonymous survey users<br>
				<a
					href='<%=Message.buildInviteUrl(
							admin_info.myStudySpace.appUrlRoot, null,
							admin_info.myStudySpace.id, id)%>'><%=Message.buildInviteUrl(
							admin_info.myStudySpace.appUrlRoot, null,
							admin_info.myStudySpace.id, id)%></a><br></td>
				<td align="center" colspan=2><%=admin_info.getUserCountsInStates(id)%>
				</td>
				<td align="center">
					<table width=100% border=0 cellpadding=2>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="initial_invite.jsp?s=<%=id%>">Send Initial Invitation</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="initial_invite.jsp?s=<%=id%>&reminder=true">Resend
										Invitation</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="other_invite.jsp?s=<%=id%>">Send Other Messages</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="<%=path + WiseConstants.ADMIN_APP%>/view_survey?s=<%=id%>">View
										Survey</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="view_result.jsp?s=<%=id%>">View Results</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="<%=path + WiseConstants.ADMIN_APP%>/print_survey?a=FIRSTPAGE&s=<%=id%>">Print
										Survey</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="download_file.jsp?fileName=<%=filename%>">Download
										Current Survey File </a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="<%=path + WiseConstants.ADMIN_APP%>/download_file?fileName=<%=id%>_data.csv">Download
										Main Survey Data Table(CSV)</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="<%=path + WiseConstants.ADMIN_APP%>/download_file?fileName=repeat_set_project.csv">Download
										Repeating Item Set Data (CSV)</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="show_auditlogs.jsp">Show Audit Logs</a></font></td>
						</tr>
						<%
							//if the survey is in the developing mode
									if (status.equalsIgnoreCase("D")) {
						%>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href='javascript: jid="<%=id%>"; jstatus="<%=status%>"; remove_confirm();'>Clear
										Survey Data</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href='javascript: jid="<%=id%>"; jstatus="R"; remove_confirm();'>Delete
										Survey</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href='javascript: sid="<%=internal_id%>"; change_mode();'>Change
										to Production Mode</a></font></td>
						</tr>
						<%
							}
									//if the survey is in the production mode
									if (status.equalsIgnoreCase("P")) {
						%>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="assign_wati.jsp?s=<%=id%>">Assign Interviewers</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href='javascript: jid="<%=id%>"; jstatus="<%=status%>"; remove_confirm();'>Close
										& Archive Survey</a></font></td>
						</tr>
						<%
							}
						%>
					</table>
				</td>
			</tr>
			<%
				} //end of while
			%>
		</table>
		<%
			stmt.close();
				stmt2.close();
				conn.close();
			} catch (Exception e) {
				// WISE_Application.log_error(
				//	"WISE ADMIN - TOOL: " + e.toString(), e);
				return;
			}
		%>
		<p>
		<p>
		<p>
	</center>
</body>
</html>


