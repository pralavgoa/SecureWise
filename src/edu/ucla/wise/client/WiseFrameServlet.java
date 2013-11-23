package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * WiseFrameServlet produces the inner survey frameset 
 * -- with appropriately-localized servlet refs
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class WiseFrameServlet extends HttpServlet {
    static final long serialVersionUID = 1000;
    static final String html = "<html><head><title>Web-based Interactive Survey Environment (WISE)</title></head>"
	    + "<frameset cols='140,*' frameborder='NO' border='0' framespacing='0' rows='*'>"
	    +
	    // "	<frame name='instruct' noresize src='progress'>" +
	    "	<frame name='form' src='setup_survey'>"
	    + "</frameset>"
	    + "<noframes><body bgcolor='#FFFFFF' text='#000000'>"
	    + "WISE requires frames. Please use a browser that can support frames.</body></noframes>"
	    + "</html>";

    /**
     * Produces the wise inner survey framesets.
     *  
     * @param 	req	 HTTP Request.
     * @param 	res	 HTTP Response.
     * @throws 	ServletException and IOException. 
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res)
    		throws ServletException, IOException {
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		out.println(html);
		out.close();
    }

}
