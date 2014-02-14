package edu.ucla.wise.commons;

import org.apache.log4j.Logger;
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
    private static final Logger LOGGER = Logger.getLogger(WelcomePage.class);
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
     * @param n
     *            Node from the XML that has to be parsed to get the information
     *            about welcome page.
     * @param p
     *            Preface object to which this WelcomePage is linked with.
     */
    public WelcomePage(Node n, Preface p) {
	try {
	    this.preface = p;

	    /* assign id & survey id (required) */
	    this.id = n.getAttributes().getNamedItem("ID").getNodeValue();
	    this.surveyId = n.getAttributes().getNamedItem("Survey_ID")
		    .getNodeValue();

	    /* assign various attributes */
	    Node nodeChild = n.getAttributes().getNamedItem("Title");
	    if (nodeChild != null) {
		this.title = nodeChild.getNodeValue();
	    } else {
		this.title = "";
	    }

	    nodeChild = n.getAttributes().getNamedItem("BannerFileName");
	    if (nodeChild != null) {
		this.banner = nodeChild.getNodeValue();
	    } else {
		this.banner = "title.gif";
	    }

	    nodeChild = n.getAttributes().getNamedItem("LogoFileName");
	    if (nodeChild != null) {
		this.logo = nodeChild.getNodeValue();
	    } else {
		this.logo = "proj_logo.gif";
	    }

	    nodeChild = n.getAttributes().getNamedItem("IRB_ID");
	    if (nodeChild != null) {
		this.irbId = nodeChild.getNodeValue();
	    } else {
		this.irbId = "";
	    }

	    NodeList nodeP = n.getChildNodes();
	    this.pageContents = "";
	    for (int j = 0; j < nodeP.getLength(); j++) {
		if (nodeP.item(j).getNodeName().equalsIgnoreCase("p")) {
		    this.pageContents += "<p>"
			    + nodeP.item(j).getFirstChild().getNodeValue()
			    + "</p>";
		}
		if (nodeP.item(j).getNodeName()
			.equalsIgnoreCase("html_content")) {
		    NodeList nodeN = nodeP.item(j).getChildNodes();
		    for (int k = 0; k < nodeN.getLength(); k++) {
			if (nodeN.item(k).getNodeName()
				.equalsIgnoreCase("#cdata-section")) {
			    this.pageContents += nodeN.item(k).getNodeValue();
			}
		    }
		}
	    }
	} catch (DOMException e) {
	    LOGGER.error("WISE - WELCOME PAGE : ID = " + this.id
		    + "; Preface Project name = " + p.projectName + "; --> "
		    + e.toString(), null);
	    return;
	}
    }
}
