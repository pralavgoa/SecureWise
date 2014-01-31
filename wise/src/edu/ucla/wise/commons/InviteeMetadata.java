/**
 * 
 */
package edu.ucla.wise.commons;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucla.wise.commons.User.INVITEE_FIELDS;

/**
 * This class represents the current set of fields as driven by the survey file.
 * 
 * @author 	ssakdeo
 * @version	1.0
 * 
 */
public class InviteeMetadata {

	/**
	 * Each invitee field is associated with label and optional possible set of
     * values.
	 * @author 	ssakdeo
	 * @version	1.0
	 */
    public class Values {

	public String label;
	public Map<String, String> values;
	public boolean userNode;
	public String type;
    }

    public Map<String, Values> fieldMap = new HashMap<String, Values>();
 

    /**
     * Parses the Invitee_Fields from the survey xml.
     * 
     * @param rootNode	XML DOM Node to be parsed to get the details.
     * @param survey	The survey to which this metadata has to be linked.
     */
    public InviteeMetadata(Node rootNode, Survey survey) {

    	NodeList nodelist = rootNode.getChildNodes();
    	for (int i = 0; i < nodelist.getLength(); i++) {

    		String nodeName = null;
    		String nodeLabel = null;
    		Map<String, String> nodeValues = new HashMap<String, String>();
    		boolean userNode = false;
    		String nodeType = null;
    		Node currentNode = nodelist.item(i);
    		
    		/* If a optional field */
    		 if (currentNode.getNodeName().equals(
                     INVITEE_FIELDS.codedField.name())
                     || currentNode.getNodeName().equals(
                             INVITEE_FIELDS.textField.name())) {
    			Node attribNode = currentNode.getAttributes().getNamedItem(
    					INVITEE_FIELDS.field.getAttributeName());
    			if (attribNode == null)
    				continue;
    			nodeName = attribNode.getNodeValue();
    			userNode = true;
    		} else {
    			nodeName = nodelist.item(i).getNodeName();
    		}
    		nodeType = INVITEE_FIELDS.codedField.name().equals(
    				currentNode.getNodeName()) ? DataBank.intFieldDDL
    						: DataBank.textFieldDDL;
    		
    		/* Expecting only one child node */
    		NodeList childNodes = currentNode.getChildNodes();
    		for (int j = 0; j < childNodes.getLength(); j++) {
    			Node currentChildNode = childNodes.item(j);
    			if (currentChildNode.getNodeName().equals("label")) {
    				nodeLabel = currentChildNode.getFirstChild().getNodeValue();
    			} else if (currentChildNode.getNodeName().equals("values")) {
    				NodeList valueNodeList = currentChildNode.getChildNodes();
    				for (int k = 0; k < valueNodeList.getLength(); k++) {
    					Node valueNode = valueNodeList.item(k);
    					NamedNodeMap attributes = valueNode.getAttributes();
    					Node descNode = null;
    					if (attributes != null) {
    						descNode = attributes.getNamedItem("desc");
    					}
    					if (!valueNode.getNodeName().equals("value")) {
    						continue;
    					}
    					nodeValues.put(
    							valueNode.getFirstChild().getNodeValue(),
    							descNode == null ? null : descNode
    									.getFirstChild().getNodeValue());
    				}
    			}
    		}
    		if (nodeLabel == null) {
    			continue;
    		}
    		Values val = new Values();
    		val.label = nodeLabel;
    		val.values = nodeValues;
    		val.userNode = userNode;
    		val.type = nodeType;

    		fieldMap.put(nodeName, val);
    	}
    }
}