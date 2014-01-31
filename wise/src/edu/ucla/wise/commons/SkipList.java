package edu.ucla.wise.commons;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains a skip list set and all its properties
 * 
 * @author Douglas Bell
 * @version 1.0  
 *
 */
public class SkipList {
    /** Instance Variables */
    public String[] values;
    public String[] pages;
    public ClosedQuestion question;

    /**
     * Constructor: parse a skip list node from the XML
     * 
     * @param n		XML node that has to be parsed to get the details of the skip.
     * @param cq	parent of the node that is passed.
     */
    public SkipList(Node n, ClosedQuestion cq) {
		try {
			
		    /* assign its parent node - the closed question */
		    question = cq;	
		    NodeList nodelist = n.getChildNodes();
		    values = new String[nodelist.getLength()];
		    pages = new String[nodelist.getLength()];
		    for (int i = 0; i < nodelist.getLength(); i++) {
				NamedNodeMap nnm1 = nodelist.item(i).getAttributes();
				Node n2 = nnm1.getNamedItem("Value");
				values[i] = n2.getNodeValue();
				n2 = nnm1.getNamedItem("Page");
				pages[i] = n2.getNodeValue();
		    }
		} catch (DOMException e) {
		    WISEApplication.logError(
		    		"WISE - SKIP LIST CONSTRUCTOR: " + e.toString(), null);
		    return;
		}
    }

    /**
     * when render the closed question, directly skip to a target defined in the
     * skip list
     * 
     * @param 	value	The value is the option index or its value in closed question
     * @return	String	Page to which the survey has to be skipped to or it is empty 
     * 			in case of the page is DONE	
     */
    public String renderFormElement(int value) {
		
		String v = String.valueOf(value);
		String target = "DONE";
		for (int i = 0; i < values.length; i++) {
		    
			/* if the option value is a value set in the skip list, assign the
		     * page ID then after submission, the survey will skip to that page directly
		     * by using JavaScript
		     */
		    if (values[i].equalsIgnoreCase(v)) {
				target = pages[i];
				break;
		    }
		}
		String element = "onClick=\"PageSkip('" + target + "');\"";
		if (target.equalsIgnoreCase("DONE")) {
		    element = "";
		}
		return element;
    }

   /**
    * when render the closed question, directly skip to a target defined in the
    * skip list
    * @param 	value	The value is the option index or its value in closed question
    * @return	String	Page to which the survey has to be skipped to or it is empty 
     * 			in case of the page is DONE	
    */
    public String renderFormElement(String value) {
		String v = value;
		String target = "DONE";
		for (int i = 0; i < values.length; i++) {
		    if (values[i].equalsIgnoreCase(v)) {
			target = pages[i];
			break;
		    }
		}
		String element = "onClick=\"PageSkip('" + target + "');\"";
		if (target.equalsIgnoreCase("DONE"))
		    element = "";
		return element;
    }

    /**
     * Renders the form element to skip to a target
     * @param 	value 	The value is the option index or its value in closed question.
     * @return	String	The Font of the skip list or empty if the page is already Done.
     */
    public String renderIdentifier(int value) {
		String v = String.valueOf(value);
		String target = "DONE";
		for (int i = 0; i < values.length; i++) {
		    if (values[i].equalsIgnoreCase(v)) {
				target = pages[i];
				break;
		    }
		}
		String element = "<FONT FACE='Wingdings'>&egrave;</FONT>";
		if (target.equalsIgnoreCase("DONE"))
		    element = "";
		return element;
    }

    /**
     * Renders the form element to skip to a target
     * @param 	value 	The value is the option index or its value in closed question.
     * @return	String	The Font of the skip list or empty if the page is already Done.
     */
    public String renderIdentifier(String value) {
		String v = value;
		String target = "DONE";
		for (int i = 0; i < values.length; i++) {
		    if (values[i].equalsIgnoreCase(v)) {
			target = pages[i];
			break;
		    }
		}
		String element = "<FONT FACE='Wingdings'>&egrave;</FONT>";
		if (target.equalsIgnoreCase("DONE"))
		    element = "";
		return element;
    }

    /**
     * Returns the number of targets in a skip list 
     * @return int 	Number of targets.
     */
    public int getSize() {
    	return values.length;
    }

    /** prints out a skip_list */
    /*
     * public String print() { String s = "SKIP LIST<br>"; s += "Targets: <br>";
     * for (int i = 0; i < values.length; i++) s +=
     * values[i]+":"+pages[i]+"<br>"; s += "<p>"; return s; }
     */

}
