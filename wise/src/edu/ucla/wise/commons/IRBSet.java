package edu.ucla.wise.commons;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * This class contains a IRB set object in the preface.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */

public class IRBSet {
    /** Instance Variables */
    public String id;
    public String irbName;
    public String expirDate;
    public String approvalNumber;
    public String irbLogo;
    public Preface preface;

    /**
     * Constructor: parse a IRB set node from Preface.xml
     * 
     * @param n		IRB XML DOM node to be parsed.
     * @param p		Preface to which this IRBSet is to be linked.
     */
    public IRBSet(Node n, Preface p) {
    	try {
    		preface = p;
    		
    		/* assign id (required) */
    		id = n.getAttributes().getNamedItem("ID").getNodeValue();
    		
    		/* assign various attributes */
    		Node node_child = n.getAttributes().getNamedItem("Name");
    		if (node_child != null) {
    			irbName = node_child.getNodeValue();
    		} else {
    			irbName = "";
    		}
    		node_child = n.getAttributes().getNamedItem("Expiration_Date");
    		if (node_child != null) {
    			expirDate = node_child.getNodeValue();
    		} else {
    			expirDate = "";
    		}
    		node_child = n.getAttributes().getNamedItem("IRB_Approval_Number");
    		if (node_child != null) {
    			approvalNumber = node_child.getNodeValue();
    		} else {
    			approvalNumber = "";
    		}
    		node_child = n.getAttributes().getNamedItem("Logo_File");
    		if (node_child != null) {
    			irbLogo = node_child.getNodeValue();
    		} else {
    			irbLogo = "";
			}
    	} catch (DOMException e) {
    		WISEApplication.logError("WISE - IRB SET : ID = " + id
    				+ "; Preface = " + p.projectName + " --> " + e.toString(),
    				null);
    		return;
    	}
    }
}
