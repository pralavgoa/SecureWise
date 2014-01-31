package edu.ucla.wise.commons;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains a consent form object set in the preface.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */

public class ConsentForm {
    /** Instance Variables */
    public String id;
    public String irbId;
    public String title;
    public String subTitle;
    public String surveyId;
    public String consentHeaderHtml = "", consentP = "", consentUl = "",
	    consentS = "";

    public Preface preface;

    /**
     * Constructor: parse a Consent form node from XML
     * 
     * @param n		XML DOM node for consent form.
     * @param p		Preface object to which this object has to be linked to.
     */
    public ConsentForm(Node n, Preface p) {
    	try {
    		preface = p;
    		
    		/* assign id, irb id & survey id (required) */
    		id = n.getAttributes().getNamedItem("ID").getNodeValue();
    		irbId = n.getAttributes().getNamedItem("IRB_ID").getNodeValue();
    		surveyId = n.getAttributes().getNamedItem("Survey_ID")
    				.getNodeValue();

    		/* assign various attributes */
    		Node nodeChild = n.getAttributes().getNamedItem("Title");
    		if (nodeChild != null) {
    			title = nodeChild.getNodeValue();
    		} else {
    			title = "";
    		}
    		nodeChild = n.getAttributes().getNamedItem("Sub_Title");
    		if (nodeChild != null) {
    			subTitle = nodeChild.getNodeValue();
    		} else {
    			subTitle = "";
    		}
    		NodeList nodeP = n.getChildNodes();
    		for (int j = 0; j < nodeP.getLength(); j++) {
    			if (nodeP.item(j).getNodeName().equalsIgnoreCase("p")) {
    				consentP += "<p>"
    						+ nodeP.item(j).getFirstChild().getNodeValue()
    						+ "</p>";
    			}
    			if (nodeP.item(j).getNodeName().equalsIgnoreCase("s")) {
    				consentS += "<p>"
    						+ nodeP.item(j).getFirstChild().getNodeValue()
    						+ "</p>";
    			}
    			if (nodeP.item(j).getNodeName()
    					.equalsIgnoreCase("html_header")) {
    				NodeList nodeN = nodeP.item(j).getChildNodes();
    				for (int k = 0; k < nodeN.getLength(); k++) {
    					if (nodeN.item(k).getNodeName()
    							.equalsIgnoreCase("#cdata-section")) {
    						consentHeaderHtml += nodeN.item(k)
    						.getNodeValue();
    					}
    				}
    			}
    			if (nodeP.item(j).getNodeName().equalsIgnoreCase("bullets")) {
    				consentUl += "<ul>";
    				NodeList nodeB = nodeP.item(j).getChildNodes();
    				for (int k = 0; k < nodeB.getLength(); k++) {
    					if (nodeB.item(k).getNodeName()
    							.equalsIgnoreCase("bullet_item")) {
    						NodeList nodeC = nodeB.item(k).getChildNodes();
    						consentUl += "<li>";
    						for (int t = 0; t < nodeC.getLength(); t++) {
    							if (nodeC.item(t).getNodeName()
    									.equalsIgnoreCase("item_subject")) {
    								consentUl += "<b>"
    										+ nodeC.item(t).getFirstChild()
    										.getNodeValue()
    										+ "</b><br>";
    							}
    							if (nodeC.item(t).getNodeName()
    									.equalsIgnoreCase("item_content")) {
    								consentUl += nodeC.item(t)
    										.getFirstChild().getNodeValue();
    								NodeList nodeU = nodeC.item(t)
    										.getChildNodes();
    								for (int tt = 0; tt < nodeU.getLength(); tt++) {
    									if (nodeU.item(tt).getNodeName()
    											.equalsIgnoreCase(
    													"#cdata-section")) {
    										consentUl += nodeU.item(tt)
    										.getNodeValue();
    									}
    								}
    								consentUl += "<br>";
    							}
    						}
    						consentUl += "<br>";
    					}
    				}
    				consentUl += "</ul><br>";
    			}
    		}
    	} catch (DOMException e) {
    		WISEApplication.logError("WISE - CONSENT FORM : ID = " + id
    				+ "; Preface = " + p.projectName + " --> " + e.toString(),
    				null);
    		return;
    	}
    }
}
