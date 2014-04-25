<%@page import="org.apache.log4j.Logger"%>
<%@page import="edu.ucla.wise.admin.AdminUserSession"%>
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
<script language="javascript">
  var c_n, c_t, c_d;
  //c_n - column name
  //c_t - column data type
  //c_d - column default value

  //assign the field values in the form when to update
  function put_update_value()
  {    
      document.form1.cname.value=c_n;
      document.form1.ctype.value=c_t;
      document.form1.cdefault.value=c_d;
      document.form1.cedit.value ="update";
      document.form1.coname.value = c_n;
  }

  //clean up the field values in the form when to delete
  function put_delete_value()
  {
      document.form1.cname.value="";
      document.form1.ctype.value="";
      document.form1.cdefault.value="null";
      document.form1.cedit.value ="delete";
      document.form1.coname.value = c_n;
  }

  //display the warning message before removing the column
  function check_submit()
  {
      var msg;
      if(document.form1.cedit.value == "delete")
      {
        msg = "the column "+ c_n +" will be dropped from the invitee table. Do you want to proceed?";
        if (confirm(msg))
          return true;
        else
          return false;
      }
      else
        return true;     
  }

</script>
<%
    final Logger LOGGER = Logger.getLogger(this.getClass());
	//get the server path
        String path=request.getContextPath();
        session = request.getSession(true);
        //if the session is expired, go back to the logon page
        if (session.isNew())
        {
            response.sendRedirect(path+"/index.html");
            return;
        }
        //get the admin info object from session
        AdminUserSession adminUserSession = (AdminUserSession) session.getAttribute("ADMIN_USER_SESSION");
        //if the session is invalid, display the error
        if(adminUserSession == null)
        {
            response.sendRedirect(path + "/error.htm");
            return;
        }
%>
<link rel="stylesheet" href="<%=path%>/style.css" type="text/css">
<title>WISE Administration Tools - Edit Invitee Table</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<center>
<table cellpadding=2 cellpadding="0" cellspacing="0" border=0>
	<tr>
		<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"></td>
		<td width="160" align=center>&nbsp;</td>
	</tr>
</table>
<p>
<%
	//declare variables
        String sqlm=null;
        String edit_type, col_oname, col_name, col_value, col_def;
        String column_name, column_type, column_default, column_key;

        try
        {
            //connect to the database
            Connection conn = adminUserSession.getDBConnection();
            Statement stmtm = conn.createStatement();
            Statement stmt = conn.createStatement();
            //get the edit type - add, delete, update etc from the request
            edit_type = request.getParameter("cedit");
            //Security features Changes
            if(SanityCheck.sanityCheck(edit_type)){
            	response.sendRedirect(path + "/admin/sanity_error.html");
		return;
            }
            edit_type=SanityCheck.onlyAlphaNumeric(edit_type);
            //get the new column name, data type, default value and old column name
            if(edit_type!=null)
            {
              col_name = request.getParameter("cname");
              col_value = request.getParameter("ctype");
              col_def = request.getParameter("cdefault");
              col_oname = request.getParameter("coname");
              if(SanityCheck.sanityCheck(col_name) || SanityCheck.sanityCheck(col_value) || 
            		  SanityCheck.sanityCheck(col_def) || SanityCheck.sanityCheck(col_oname) ){
              	response.sendRedirect(path + "/admin/sanity_error.html");
  				return;
              }
              col_name=SanityCheck.onlyAlphaNumericandSpecial(col_name);
              col_value=SanityCheck.onlyAlphaNumericandSpecial(col_value);
              col_def=SanityCheck.onlyAlphaNumericandSpecial(col_def);
              col_oname=SanityCheck.onlyAlphaNumericandSpecial(col_oname);
              //end of security features changes
              
              //if it is to add a new column, the default value should be entered in single quotes if it is  only string
              if(edit_type.equalsIgnoreCase("add"))
              {
                  if(col_name!=null && !col_name.equalsIgnoreCase("") && col_value !=null && !col_value.equalsIgnoreCase(""))
                  {
                      sqlm = "alter table invitee add "+col_name+" "+col_value;
                      if(col_def!=null && col_def.equalsIgnoreCase("null"))
                        sqlm += " default NULL";
                      else if (col_def!=null)
                        sqlm += " NOT NULL default "+col_def;
                  }
                  else
                  {
                      out.println("<p>Please note that you have to fill up the column name/value to add it</p>");
                  }
              }
              //if it is to delete a column - display the warning message before proceed
              else if(edit_type.equalsIgnoreCase("delete"))
              {
                  sqlm = "alter table invitee drop "+ col_oname;
              }
              //if it is to update the column
              else if(edit_type.equalsIgnoreCase("update"))
              {
                  if(col_name!=null && !col_name.equalsIgnoreCase("") && col_value !=null && !col_value.equalsIgnoreCase(""))
                  {
                    sqlm = "alter table invitee change "+col_oname+" "+col_name+" "+col_value;
                    if(col_def!=null && col_def.equalsIgnoreCase("null"))
                      sqlm += " default NULL";
                    else if (col_def!=null)
                      sqlm += " NOT NULL default "+col_def;
                  }
                  else
                  {
                    out.println("<p>You have to fill up the column name/value to update it</p>");
                  }            
              }

              //run the query to update/add/delete the column
              boolean dbtypem;
              if(sqlm!=null)
              {
                  dbtypem = stmtm.execute(sqlm);
                  out.println("<p>The invitee table has been successfully modified</p>");
              }
            }
%>

<table cellpadding=2 cellpadding="0" cellspacing="0" border=0>
	<tr>
		<td align=center>

		<form name="form1" method="post" onsubmit="return check_submit()"
			action="change_invitee.jsp">
		<table class=tth border=1 cellpadding="4" cellspacing="0"
			bgcolor=#FFFFF5>
			<tr>
				<td bgcolor="#CC6666" align=center colspan=3><font color=white><b>Edit
				Invitee Tables</b></font></td>
			</tr>
			<tr>
				<td width="150" align=center><b>Name</b></td>
				<td width="150" align=center><b>Type</b></td>
				<td width="50" align=center><b>Default</b></td>
				<%
					//out.println("<td width=50 align=center><b>Key</b></td>");
				              //out.println("<td width=50 align=center bgcolor=#DE9E9E><font color=white><b>Update</b></font></td>");
				              //out.println("<td width=50 align=center bgcolor=#DE9E9E><font color=white><b>Delete</b></font></td>");
				%>
			</tr>
			<%
				//get the column names in the invitee table
			            String sql = "describe invitee";
			            boolean dbtype = stmt.execute(sql);
			            ResultSet rs = stmt.getResultSet();
			            
			            while (rs.next())
			            {
			              column_name = rs.getString("Field");
			              column_type = rs.getString("Type");
			              column_default = rs.getString("Default");
			              column_key = rs.getString("Key");

			              out.println("<tr><td>"+column_name+"</td>");
			              out.println("<td>"+column_type+"</td>");
			              out.println("<td align=center>"+column_default+"</td>");
			              /*
			              //to update/delete the current column
			              if(column_key!=null && !column_key.equalsIgnoreCase(""))
			              {
			                out.println("<td align=center>Key</td>");
			                out.println("<td>&nbsp;</td>");
			                out.println("<td>&nbsp;</td>");
			              }
			              else
			              {
			                out.println("<td>&nbsp;</td>");
			                out.println("<td align=center><input type='radio' name='modify' value='update' onClick='javascript: c_n=\""+column_name+"\"; c_t=\""+column_type+"\"; c_d=\""+column_default+"\"; put_update_value();'></td>");
			                out.println("<td align=center><input type='radio' name='modify' value='delete' onClick='javascript: c_n=\""+column_name+"\"; c_t=\""+column_type+"\"; c_d=\""+column_default+"\"; put_delete_value();'></td></tr>");
			              }
			              */
			            }

			            //to add a new column
			%>
			<tr>
				<td align=center bgcolor="#CC6666" colspan=3><font color=white><b>Add
				New Column</b></font></td>
			</tr>
			<tr>
				<td align=center>Column Name:</td>
				<td colspan=2><input type="text" name="cname" maxlength="30"
					size="20" value=""></td>
			</tr>
			<tr>
				<td align=center>Column Type:</td>
				<td colspan=2><input type="text" name="ctype" maxlength="30"
					size="20" value=""></td>
			<tr>
				<td align=center>Default Value:</td>
				<td colspan=2><input type="text" name="cdefault" maxlength="30"
					size="20" value="null"> <input type="hidden" name="cedit"
					value="add"> <input type="hidden" name="coname" value="">
				</td>
			</tr>
			<tr>
				<td colspan=3 align=center><input type="image" alt="submit"
					src="admin_images/submit.gif">&nbsp;&nbsp; <img alt="reset"
					src="admin_images/reset.gif" onClick="document.form1.reset()"></td>
				</td>
			</tr>
		</table>
		</form>
		<%
			stmtm.close();
				            stmt.close();
				            conn.close();
				        }
				        catch (Exception e)
				        {
				            LOGGER.error("WISE ADMIN - CHANGE INVITEE: "+e.toString(), e);
				            //security features changes
				            //out.println("<p>Error: "+e.toString()+"</p>");
				            //end of security features changes
				        }
		%>
		</td>
	<tr>
		<td align=center>
		<p><a href="load_invitee.jsp">Return to Manage Invitees</a>
		</td>
	</tr>

</table>
</center>
</body>
</html>
