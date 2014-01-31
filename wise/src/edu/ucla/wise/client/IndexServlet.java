package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.CommonUtils;

/**
 * TODO: This functionality is not possible in our current design because until
 * we have study space ID we cannot decide which is the right study space the
 * first page is related to. If URL is used in this format /WISE/survey land it
 * to default landing page of the study space.
 * 
 * @author ssakdeo
 * @version 1.0
 * 
 */
@WebServlet("/survey/index")
public class IndexServlet extends HttpServlet {
    static final long serialVersionUID = 1000;
    Logger log = Logger.getLogger(this.getClass());

    public void service(HttpServletRequest req, HttpServletResponse res)
    		throws ServletException, IOException {
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		try {
		    TransformerFactory tFactory = TransformerFactory.newInstance();
	
		    Transformer transformer = tFactory
			    .newTransformer(new javax.xml.transform.stream.StreamSource(
				    CommonUtils.loadResource("xml/ctsibip/index.xslt")));
		    StringWriter stringwriter = new StringWriter();
		    transformer.transform(new javax.xml.transform.stream.StreamSource(
			    CommonUtils.loadResource("xml/ctsibip/index.xml")),
			    new javax.xml.transform.stream.StreamResult(stringwriter));
		    out.print(stringwriter.toString());
		} catch (TransformerFactoryConfigurationError e) {
			log.error("Error in IndexServlet", e);
		} catch (TransformerException  e) {
			log.error("Error in IndexServlet", e);
		}
    }
}
