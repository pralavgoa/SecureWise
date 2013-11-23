package edu.ucla.wise.commons;

import java.io.StringWriter;
import java.util.Hashtable;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is a subclass of Page_Item and represents a directive object on
 * the page.
 * @author Douglas Bell
 * @version 1.0  
 */

public class Directive extends PageItem {
    /** Instance Variables */
    public String text;
    public boolean hasPrecondition = false;
    public Condition cond;


    /**
     * constructor: parse a directive node from XML
     * 
     * @param n	XML node from which directive object information is obtained.
     * 
     */
    public Directive(Node n) {
    	
    	/* parse the page item properties */
    	super(n);
    	try {
    		
    		/* convert to the translated question stem */
    		if (this.translationId != null) {
    			text = questionTranslated.text;
    		} else {
    			Node node = n;
    			NodeList childNodes = node.getChildNodes();

    			for (int i = 0; i < childNodes.getLength(); i++) {

    				Node childNode = childNodes.item(i);
    				if (childNode.getNodeName()
    						.equalsIgnoreCase("Precondition")) {
    					node.removeChild(childNode);
    				}
    			}

    			Transformer transformer = TransformerFactory.newInstance()
    					.newTransformer();
    			StringWriter sw = new StringWriter();
    			transformer
    			.transform(new DOMSource(node), new StreamResult(sw));
    			text = sw.toString();
    		}
    		
    		/* parse the precondition */
    		NodeList nodelist = n.getChildNodes();
    		for (int i = 0; i < nodelist.getLength(); i++) {
    			if (nodelist.item(i).getNodeName()
    					.equalsIgnoreCase("Precondition")) {
    				hasPrecondition = true;
    				
    				/* create the condition object */
    				cond = new Condition(nodelist.item(i));
    			}
    		}
    	} catch (DOMException e) {
    		WISEApplication.logError("WISE - DIRECTIVE: " + e.toString(),
    				null);
    		return;
    	} catch ( TransformerConfigurationException e) {
    		WISEApplication.logError("WISE - DIRECTIVE: " + e.toString(),
    				null);
    		return;
    	} catch ( TransformerException e) {
    		WISEApplication.logError("WISE - DIRECTIVE: " + e.toString(),
    				null);
    		return;
    	} 
    	
    }

    @Override
    public int countFields() {
	return 0;
    }

    @Override
    public void knitRefs(Survey mySurvey) {
	html = makeHtml();
    }

    @Override
    public String[] listFieldNames() {
	return new String[0];
    }

    /**
     * Renders form for directive item.
     * 
     * @return	String	HTML format.
     */
    public String makeHtml() {
    	String s = "";
    	s += "<table cellspacing='0' cellpadding='0' width=100%' border='0'>";
    	s += "<tr>";
    	s += "<td><font face='Verdana, Arial, Helvetica, sans-serif' size='-1'>"
    			+ text + "</font></td>";
    	s += "</tr>";
    	s += "</table>";
    	return s;
    }

    /** print survey for directive item - used for admin tool: print survey 
     * 
     * @return String	HTML format.
     */
    @Override
    public String printSurvey() {
	String s = "<table cellspacing='0' cellpadding='0' width=100%' border='0'>";
	s += "<tr>";
	s += "<td>" + text + "</td>";
	s += "</tr>";
	s += "</table>";
	return s;
    }
	
    /**
     * Render results for directive item.
     * 
     * @param 	data			Hashtable which contains results.
     * @param 	whereclause		whereclause to restrict the invitee selection.
     * @return 	String 			HTML format of the results is returned.
     */
    @SuppressWarnings("rawtypes")
    public String renderResults(Hashtable data, String whereclause) {
    	String s = "<table cellspacing='0' cellpadding='0' width=100%' border='0'>";
    	s += "<tr>";
    	s += "<td><i>" + text + "</i></td>";
    	s += "</tr>";
    	s += "</table>";
    	return s;
    }

    /** print information about a directive item */
    /*
     * public String print() { String s = "DIRECTIVE<br>"; s += super.print(); s
     * += "Text: "+text+"<br>"; s += "<p>"; return s; }
     */
}
