package edu.ucla.wise.client;


import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.commons.DataBank;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.WISEApplication;

/**
 * AppStyleRenderServlet class is used to get an Style sheets from database.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class AppStyleRenderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    Logger log = Logger.getLogger(AppStyleRenderServlet.class);

    /**
     * Gets the css from the database.
     *  
     * @param 	request	 HTTP Request.
     * @param 	response HTTP Response.
     * @throws 	IOException and ServletException.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    		throws ServletException, IOException {
		String cssName = request.getParameter("css");
		String appName = request.getParameter("app");
	
		if (Strings.isNullOrEmpty(appName)) {
		    response.sendRedirect(WISEApplication.rootURL
			    + "/WiseShared/style?style=" + cssName);
		    return;
		}
	
		HttpSession session = request.getSession(true);
		
		/* if session is new, then show the session expired info */
		if (session.isNew()) {
		    return;
		}
	
		StudySpace studySpace = (StudySpace) session
				.getAttribute("STUDYSPACE");
	
		if (studySpace == null) {
		    log.info("Study space name is null");
		    return;
		}
	
		DataBank db = studySpace.getDB();
		InputStream cssStream = null;
		int bufferSize = 2 << 12;// 16kb buffer
		byte[] byteBuffer;
	
		try {
		    cssStream = db.getFileFromDatabase(cssName, appName);
		    if (cssStream != null) {
				response.reset();
				response.setContentType("text/css;charset=UTF-8");
				int count = 1;// initializing to a value > 0
				while (count > 0) {
				    byteBuffer = new byte[bufferSize];
				    count = cssStream.read(byteBuffer, 0, bufferSize);
				    response.getOutputStream().write(byteBuffer, 0,
				    		bufferSize);
				}
				response.getOutputStream().flush();
		    } else {
		    	return;
		    }
		} catch (IOException e) {
		    log.error("File not found", e);
		} catch (NullPointerException e) {
			log.error("Error while reading file from file system due to null buffer", e);
		} catch (IndexOutOfBoundsException e) {
			log.error("Error while reading file from file system due to illegal buffer lengths", e);
		} finally {
		    if (cssStream != null) {
				try {
					cssStream.close();
				} catch (IOException e) {
				    log.error(e);
				}
		    }
		}
	}
}
