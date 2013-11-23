package edu.ucla.wise.commons;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains a subject set and all its possible answers
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class SubjectSet {
    /** Instance Variables */
    public String id;
    private int[] subjectIDs;
    public String[] subjectLabels;
    public Survey survey;
    public int subjectCount;

    /**
     * Constructor: parse a subject set node from XML
     * @param n	Node from the XML that has to be parsed to get details.
     * @param p Preface object to which this SubjectSet is linked with.
     */
    public SubjectSet(Node n, Survey s) {
		try {
		    survey = s;
		    
		    /* assign various attributes */
		    id = n.getAttributes().getNamedItem("ID").getNodeValue();	
		    NodeList nlist = n.getChildNodes();
		    subjectCount = nlist.getLength();
		    subjectIDs = new int[subjectCount];
		    subjectLabels = new String[subjectCount];
	
		    /* get each subject name and its value in the subject set */
		    for (int j = 0; j < subjectCount; j++) {
				int idNum = 1;
				Node subj = nlist.item(j);
				if (subj.getNodeName().equalsIgnoreCase("Subject")) {
				    
					/* get the subject value */
				    Node sIDnode = subj.getAttributes().getNamedItem("IDnum");
				    if (sIDnode == null) {
						
				    	/* ID value is not specified in XML, assign the currentindex as its value */
						subjectIDs[j] = idNum++;
				    } else {
						subjectIDs[j] = Integer.parseInt(sIDnode
								.getNodeValue());
						idNum = Math.max(idNum, subjectIDs[j]);
						idNum++;
				    }
				    
				    /* record the subject name */
				    subjectLabels[j] = subj.getFirstChild().getNodeValue();
				}
		    }
		} catch (DOMException  e) {
		    WISEApplication.logError("WISE - SUBJECT SET : ID = " + id
		    		+ "; Survey = " + id + "; Study = " + s.studySpace.id
		    		+ " --> " + e.toString(), null);
		    return;
		}
    }

    /**
     * Returns the id of the subject for the given Index. It is used by the question
     * block class to get the Id of the subject which meets the precondition
     * 
     * @param 	index	The Index whose subject ID is needed.
     * @return	String	The subject ID.	
     */
    public String getfieldNamesuffix(int index) {
		if (index < subjectCount) {
		    return "_" + Integer.toString(subjectIDs[index]);
		} else {
		    return ""; // not entirely safe, but should never be out of bounds
		}
    }

    /**
     * Converts the subject ids and labels to and string and returns it.
     * 
     * @return	String	The string format of the subject Ids and labels. 
     */
    public String toString() {
	String s = "<p><b>SubjectSet</b><br>";
	s += "ID: " + id + "<br>";

	for (int i = 0; i < subjectCount; i++)
	    s += "   " + subjectIDs[i] + ": " + subjectLabels[i];
	return s;
    }

}
