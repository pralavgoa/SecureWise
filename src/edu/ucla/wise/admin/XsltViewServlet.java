package edu.ucla.wise.admin;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.ucla.wise.commons.AdminApplication;

/**
 * XsltViewServlet is a class used when user tries to print
 * an overview list of the pages in a survey
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class XsltViewServlet extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Generates an xslt version of the survey.
     *  
     * @param 	req	 HTTP Request.
     * @param 	res	 HTTP Response.
     * @throws 	ServletException and IOException. 
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
    		throws ServletException, IOException {

		response.setContentType("text/html");
		
		/* get information from java session */
		String path = request.getContextPath();
		HttpSession session = request.getSession(true);
		if (session.isNew()) {
		    response.sendRedirect(path + "/index.htm");
		    return;
		}
	
		String surveyName = request.getParameter("FileName");
		
		/* check if the session is still valid */
		AdminApplication adminInfo = (AdminApplication) session.getAttribute("ADMIN_INFO");	
		if (surveyName == null || adminInfo == null) {
		    AdminApplication.logError(
		    		"Wise Admin - XSLT View Error: can't get the admin info", null);
		    return;
		}
	
		/* get the file xml and processor xslt */	
		String fXml = adminInfo.studyXmlPath + surveyName;
	
		String fXslt = "/CME/WISE_pages/style/survey_all_pg.xslt";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
		try {
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    Document document = builder.parse(fXml);
	
		    /* use a Transformer for output */
		    TransformerFactory tFactory = TransformerFactory.newInstance();
		    Transformer transformer = tFactory.newTransformer(new StreamSource(fXslt));
	
		    /* reserve the DOCTYPE setting in XML file */
		    if (document.getDoctype() != null) {
			String systemValue = (new File(document.getDoctype()
					.getSystemId())).getName();
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
					systemValue);
		    }
	
		    DOMSource source = new DOMSource(document);
		    StreamResult result = new StreamResult(response.getOutputStream());
		    transformer.transform(source, result);
		} catch (ParserConfigurationException e) {
		    System.out.println("  " + e.getMessage());
		    AdminApplication.logError(
		    		"Wise Admin - XSLT View Error: " + e.getMessage(), e);
		} catch (SAXException e) {
			System.out.println("  " + e.getMessage());
		    AdminApplication.logError(
		    		"Wise Admin - XSLT View Error: " + e.getMessage(), e);
		} catch (TransformerConfigurationException e) {
			System.out.println("  " + e.getMessage());
		    AdminApplication.logError(
		    		"Wise Admin - XSLT View Error: " + e.getMessage(), e);			
		} catch (TransformerException e) {
			System.out.println("  " + e.getMessage());
		    AdminApplication.logError(
		    		"Wise Admin - XSLT View Error: " + e.getMessage(), e);
		}
    }
}
