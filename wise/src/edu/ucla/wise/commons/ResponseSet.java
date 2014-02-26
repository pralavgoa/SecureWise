package edu.ucla.wise.commons;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains an answer set called response set and all its possible
 * answers The closed question & question block contain this response set.
 * 
 * @author Douglas Bell
 * @version 1.0
 */
public class ResponseSet {
    public static final Logger LOGGER = Logger.getLogger(ResponseSet.class);
    /** Instance Variables */
    public String id;
    public String levels;
    public String startvalue;

    public ArrayList<String> responses;
    public ArrayList<String> values;

    public Survey survey;

    /**
     * Constructor: parse a response set node from XML
     * 
     * @param n
     *            Node from the XML that has to be parsed to get the information
     *            about response set.
     * @param s
     *            Survey object to which this ResponseSet is linked to.
     */
    public ResponseSet(Node n, Survey s) {
	try {
	    this.survey = s;

	    /* assign various attributes */
	    this.id = n.getAttributes().getNamedItem("ID").getNodeValue();

	    /* assign the number of levels to classify */
	    Node node1 = n.getAttributes().getNamedItem("Levels");
	    if (node1 != null) {
		this.levels = node1.getNodeValue();
	    } else {
		this.levels = "0";
	    }

	    /* assign the start value of the 1st level */
	    node1 = n.getAttributes().getNamedItem("StartValue");
	    if (node1 != null) {
		this.startvalue = node1.getNodeValue();
	    } else {
		this.startvalue = "1";
	    }
	    NodeList nodelist = n.getChildNodes();
	    this.responses = new ArrayList<String>();
	    this.values = new ArrayList<String>();

	    /* assign answer option & its value */
	    for (int i = 0; i < nodelist.getLength(); i++) {
		if (nodelist.item(i).getNodeName()
			.equalsIgnoreCase("Response_Option")) {
		    String str = nodelist.item(i).getFirstChild()
			    .getNodeValue();
		    this.responses.add(str);
		    Node node2 = nodelist.item(i).getAttributes()
			    .getNamedItem("value");
		    if (node2 != null) {
			this.values.add(node2.getNodeValue());
		    } else {
			this.values.add("-1");
		    }
		}
	    }
	} catch (DOMException e) {
	    LOGGER.error(
		    "WISE - RESPONSE SET : ID = " + this.id + "; Survey = "
			    + s.getId() + "; Study = " + s.getStudySpace().id
			    + " --> " + e.toString(), null);
	    return;
	}
    }

    /**
     * Returns the number of responses in the set
     * 
     * @return int Number of responses.
     */
    public int getSize() {
	return this.responses.size();
    }

    /** prints out a response set - used for admin tool: print survey */
    /*
     * public String print() { String s = "RESPONSE SET<br>"; s +=
     * "ID: "+id+"<br>"; s += "Levels: "+levels+"<br>"; s +=
     * "StartValue: "+startvalue+"<br>"; s += "Responses: <br>"; for (int i = 0;
     * i < responses.size(); i++) s +=
     * values.get(i)+":"+responses.get(i)+"<br>"; s += "<p>"; return s; }
     */
}
