package edu.ucla.wise.commons;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * This class contains a IRB set object in the preface.
 * 
 * @author Douglas Bell
 * @version 1.0
 */

public class IRBSet {
    public static final Logger LOGGER = Logger.getLogger(IRBSet.class);

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
     * @param n
     *            IRB XML DOM node to be parsed.
     * @param p
     *            Preface to which this IRBSet is to be linked.
     */
    public IRBSet(Node n, Preface p) {
	try {
	    this.preface = p;

	    /* assign id (required) */
	    this.id = n.getAttributes().getNamedItem("ID").getNodeValue();

	    /* assign various attributes */
	    Node node_child = n.getAttributes().getNamedItem("Name");
	    if (node_child != null) {
		this.irbName = node_child.getNodeValue();
	    } else {
		this.irbName = "";
	    }
	    node_child = n.getAttributes().getNamedItem("Expiration_Date");
	    if (node_child != null) {
		this.expirDate = node_child.getNodeValue();
	    } else {
		this.expirDate = "";
	    }
	    node_child = n.getAttributes().getNamedItem("IRB_Approval_Number");
	    if (node_child != null) {
		this.approvalNumber = node_child.getNodeValue();
	    } else {
		this.approvalNumber = "";
	    }
	    node_child = n.getAttributes().getNamedItem("Logo_File");
	    if (node_child != null) {
		this.irbLogo = node_child.getNodeValue();
	    } else {
		this.irbLogo = "";
	    }
	} catch (DOMException e) {
	    LOGGER.error("WISE - IRB SET : ID = " + this.id + "; Preface = "
		    + p.projectName + " --> " + e.toString(), null);
	    return;
	}
    }
}
