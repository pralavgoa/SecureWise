package edu.ucla.wise.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.CommonUtils;
import edu.ucla.wise.commons.DataBank;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WiseConstants;

/**
 * AppImageRenderServlet class is used to get an image form database 
 * and in-case it fails it tries to get it from file system.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class AppImageRenderServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(AppImageRenderServlet.class);

    /**
     * Gets the image from the database or file system based on the name of 
     * the image and populates the response accordingly.
     *  
     * @param 	request	 HTTP Request.
     * @param 	response HTTP Response.
     * @throws 	IOException and ServletException.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
    		throws IOException, ServletException {
		String appName = request.getParameter("app");
		if (appName == null)
		    appName = "";
		String imageName = request.getParameter("img");
		
		/* Sanity check for the inputs */
		if (SanityCheck.sanityCheck(imageName)) {
			response.sendRedirect(WISEApplication.rootURL
				    + "/WISE/" + WiseConstants.ADMIN_APP + "/sanity_error.html");
		    return;
	    }
	
		int bufferSize = 2 << 12;// 16kb buffer
		byte[] byteBuffer = new byte[bufferSize];
		response.setContentType("image");
	
		HttpSession session = request.getSession(true);
	
		/* if session is new, then show the session expired info */
		if (session.isNew()) {
		    getFromFileSystem(response, imageName, appName, true);
		    return;
		}
		
		StudySpace studySpace = (StudySpace) session
				.getAttribute("STUDYSPACE");
		
		if (studySpace == null) {
		    
			/* retrieve image from directory [duplicated code] */
			log.info("Fetching image from file system");
		    getFromFileSystem(response, imageName, appName, true);
		    return;
		}
	
		DataBank db = studySpace.getDB();
		InputStream imageStream = null;
		try {
		    imageStream = db.getFileFromDatabase(imageName, appName);
		    if (imageStream != null) {
				response.reset();
				if (imageName.contains("gif")) {
				    response.setContentType("image/gif");
				} else {
				    response.setContentType("image/jpg");
				}
				int count = 1;// initializing to a value > 0
				while (count > 0) {
				    count = imageStream.read(byteBuffer, 0, bufferSize);
				    response.getOutputStream().write(byteBuffer, 0,
				    		bufferSize);
				}
				response.getOutputStream().flush();
		    } else {
				log.info("Fetching image from file system: " + imageName);
				getFromFileSystem(response, imageName, appName, true);
		    }
	
		    if (imageStream != null) {
		    	imageStream.close();
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		    log.error("File not found", e);
		}
    }

    /**
     * Gets the image from the file system based on the name of the image.
     *  
     * @param 	res	 		HTTP Response.
     * @param 	imageName 	Name of the image to be fetched from the file system.
     * @param 	appName 	Name of the App to which this image corresponds to.
     * @param	isImage		If the content being fetched is image or not. 
     */
    public static void getFromFileSystem(HttpServletResponse response,
    		String imageName, String appName, boolean isImage) {
		int bufferSize = 2 << 12;// 16kb buffer
		byte[] byteBuffer = new byte[bufferSize];
		InputStream imageStream = null;
		try {
		    
			/* Retrieve the image from the correct directory */
		    String pathToImages = WISEApplication.imagesPath;
	
		    String pathWithStudyName;
		    if ("".equals(appName)) {
				pathWithStudyName = pathToImages
						+ System.getProperty("file.separator") + imageName;
		    } else {
				pathWithStudyName = pathToImages
						+ System.getProperty("file.separator") + appName
						+ System.getProperty("file.separator") + imageName;
		    }
	
		    imageStream = CommonUtils.loadResource(pathWithStudyName);
		    if (imageStream == null) {
		    	/* trying to load the file, will 100% fail! */
				imageStream = new FileInputStream(pathWithStudyName);
		    }
		    response.reset();
		    response.setContentType("image/jpg");
		    int count = 1;// initializing to a value > 0
		    while (count > 0) {
				count = imageStream.read(byteBuffer, 0, bufferSize);
				response.getOutputStream().write(byteBuffer, 0, bufferSize);
		    }
		    response.getOutputStream().flush();
		} catch (IOException e) {
		    e.printStackTrace();
		    log.error("Error while reading file from file system", e);
		} catch (NullPointerException e) {
			e.printStackTrace();
		    log.error("Error while reading file from file system due to null buffer", e);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		    log.error("Error while reading file from file system due to illegal buffer lengths", e);
		} finally {
		    if (imageStream != null) {
				try {
				    imageStream.close();
				} catch (IOException e) {
				    log.error(e);
				}
		    }
		}
    }
}
