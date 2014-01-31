<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<%@page import="edu.ucla.wise.shared.images.DatabaseConnector,java.util.*"%>
<title>Hello</title>
</head>
<body>
<h3>List of all the common images in the shared database</h3>
<!-- List all images in the database -->
<table style='width:800px'>
<tr><td><h4>Image Name</h4></td><td style='text-align: right'><h4>Image</h4></td></tr>
<%
for(String imageName : DatabaseConnector.getNamesOfImagesInDatabase()){
	out.write("<tr style='margin:20px'><td>"+imageName+"</td>");
	out.write("<td><img style='float:right' alt='Not found' src='image?img=");
	out.write(imageName);
	out.write("'></td>");
	out.write("</tr>");
}
%>
</table>
</body>
</html>