package edu.ucla.wise.commons;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains a welcome page object set in the preface.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class WelcomePage {
    /** Instance Variables */
    public String id;
    public String title;
    public String banner;
    public String logo;
    public String surveyId;
    public String irbId;
    public String pageContents;

    public Preface preface;

    /**
     * Constructor: parse a response set node from XML
     * 
     * @param n	Node from the XML that has to be parsed to get the information about welcome page.
     * @param p Preface object to which this WelcomePage is linked with.
     */
    public WelcomePage(Node n, Preface p) {
		try {
		    preface = p;
		    
		    /* assign id & survey id (required) */
		    id = n.getAttributes().getNamedItem("ID").getNodeValue();
		    surveyId = n.getAttributes().getNamedItem("Survey_ID")
		    		.getNodeValue();
	
		    /* assign various attributes */
		    Node nodeChild = n.getAttributes().getNamedItem("Title");
		    if (nodeChild != null) {
		    	title = nodeChild.getNodeValue();
		    } else {
		    	title = "";
		    }
		    
		    nodeChild = n.getAttributes().getNamedItem("BannerFileName");
		    if (nodeChild != null) {
		    	banner = nodeChild.getNodeValue();
		    } else {
		    	banner = "title.gif";
		    }
		    
		    nodeChild = n.getAttributes().getNamedItem("LogoFileName");
		    if (nodeChild != null) {
		    	logo = nodeChild.getNodeValue();
		 	} else {
		    	logo = "proj_logo.gif";
		    }
		    
		    nodeChild = n.getAttributes().getNamedItem("IRB_ID");
		    if (nodeChild != null) {
		    	irbId = nodeChild.getNodeValue();
		    } else {
		    	irbId = "";
		    }
		    
		    NodeList nodeP = n.getChildNodes();
		    pageContents = "";
		    for (int j = 0; j < nodeP.getLength(); j++) {
				if (nodeP.item(j).getNodeName().equalsIgnoreCase("p")) {
				    pageContents += "<p>"
				    		+ nodeP.item(j).getFirstChild().getNodeValue()
				    		+ "</p>";
				}
				if (nodeP.item(j).getNodeName()
						.equalsIgnoreCase("html_content")) {
				    NodeList nodeN = nodeP.item(j).getChildNodes();
				    for (int k = 0; k < nodeN.getLength(); k++) {
						if (nodeN.item(k).getNodeName()
								.equalsIgnoreCase("#cdata-section")) {
						    pageContents += nodeN.item(k).getNodeValue();
						}
				    }
				}
		    }
		} catch (DOMException  e) {
		    WISEApplication.logError("WISE - WELCOME PAGE : ID = " + id
		    		+ "; Preface Project name = " + p.projectName + "; --> "
		    		+ e.toString(), null);
		    return;
		} 
    }
}
