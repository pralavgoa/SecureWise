package edu.ucla.wise.commons;

import java.util.ArrayList;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains a message sequence and its properties.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class MessageSequence {
    /** Instance Variables */
    public Preface myPref;
    public String id, surveyId, irbId;
    public String fromString = "";
    private String fromEmail = null;
    private String replyString = null; 
    
    /* this holds both address and name components, unlike "from" */

    // public Hashtable message_hash;
    public Message inviteMsg;
    public Message interruptMsg;
    public Message doneMsg;
    public Message reviewMsg;
    ArrayList<Message> startReminders, completionReminders, otherMsgs;

    /**
     * Constructor for Message Sequences
     * 
     * @param sourceNode	XML DOM  message sequence node.
     * @param prefaceParam	Preface object to which this sequence is related to.
     */	
    public MessageSequence(Node sourceNode, Preface prefaceParam) {
    	try {
    		String errTemp = "";
    		startReminders = new ArrayList<Message>();
    		completionReminders = new ArrayList<Message>();
    		otherMsgs = new ArrayList<Message>();
    		myPref = prefaceParam;
    		
    		/* parse out the message sequence attributes: ID, survey ID and IRB ID if has */
    		id = "";
    		surveyId = "";
    		irbId = "";
    		
    		/* ID & survey ID are required -- don't need to check for nulls */
    		id = sourceNode.getAttributes().getNamedItem("ID").getNodeValue();
    		surveyId = sourceNode.getAttributes().getNamedItem("Survey_ID")
    				.getNodeValue();
    		
    		/* IRB ID is optional */
    		Node attrNode = sourceNode.getAttributes().getNamedItem("IRB_ID");
    		if (attrNode != null) {
    			irbId = attrNode.getNodeValue();
    		}
    		
    		/* From String is optional */
    		attrNode = sourceNode.getAttributes().getNamedItem("From_String");
    		if (attrNode != null) {
    			fromString = attrNode.getNodeValue();
    			fromString = fromString.replaceAll(",", "");
    		}
    		
    		attrNode = sourceNode.getAttributes().getNamedItem("From_Email");
    		if (attrNode != null) {
    			fromEmail = attrNode.getNodeValue(); // TODO: validate presence of @
    		}
    		if (fromEmail == null) {
    			fromEmail = WISEApplication.emailFrom; // always assign default email here
    		}
    		attrNode = sourceNode.getAttributes().getNamedItem("Reply_Email");
    		if (attrNode != null) {
    			replyString = attrNode.getNodeValue(); // TODO: validate presence of @
    			attrNode = sourceNode.getAttributes().getNamedItem(
    					"Reply_String");
    			if (attrNode != null) {
    				replyString = attrNode.getNodeValue().replaceAll(",", "")
    						+ " <" + replyString + ">";
    			}
    		}

    		NodeList msgNodeList = sourceNode.getChildNodes();
    		for (int i = 0; i < msgNodeList.getLength(); i++) {
    			
    			/* create the messages for each stage in the message sequence */
    			Node childNode = msgNodeList.item(i);
    			String nodeName = childNode.getNodeName();
    			Message newMsg = null;
    			try {
    				if (nodeName.equalsIgnoreCase("Initial_Invitation")) {
    					newMsg = new Message(childNode);
    					inviteMsg = newMsg;
    				} else if (nodeName.equalsIgnoreCase("Interrupt")) {
    					newMsg = new Message(childNode);
    					interruptMsg = newMsg;
    				} else if (nodeName.equalsIgnoreCase("Done")) {
    					newMsg = new Message(childNode);
    					doneMsg = newMsg;
    				} else if (nodeName.equalsIgnoreCase("Review")) {
    					newMsg = new Message(childNode);
    					reviewMsg = newMsg;
    				} else if (nodeName.equalsIgnoreCase("Start_Reminder")) {
    					
    					/* create the reminder class */
    					newMsg = new Reminder(childNode);
    					startReminders.add(newMsg);
    				} else if (nodeName.equalsIgnoreCase("Completion_Reminder")) {
    					newMsg = new Reminder(childNode);
    					completionReminders.add(newMsg);
    				} else if (nodeName.equalsIgnoreCase("Message")) {
    					newMsg = new Message(childNode);
    					otherMsgs.add(newMsg);
    				}
    			} catch (RuntimeException e) {
    				WISEApplication.logError(
    						"Msg SEQ Choke at Parsing message" + nodeName
    						+ ". After:" + i + "\n" + errTemp
    						+ e.toString(), e);
    			}
    			
    			/* save the message here and in preface's master index */
    			try {
    				if (newMsg != null)
    					myPref.addMessage(newMsg, this);
    			} catch (RuntimeException e) {
    				WISEApplication.logError("Msg SEQ Choke at Adding "
    						+ nodeName + ". After:\n" + errTemp + e.toString(),
    						e);
    			}
    		}
    	} catch (DOMException e) {
    		WISEApplication.logError("WISE - MESSAGE SEQUENCE: ID = " + id
    				+ "; Survey ID = " + surveyId + " --> " + e.toString(),
    				null);
    		return;
    	}
    }

    /**
     * Returns the from email address.
     * 
     * @return	String  Return from email id if the instance variables
     * 					(fromString/fromEmail) are null default from address is sent.
     */
    public String getFromString() {
    	
    	/* should actually be initialized to this; just checking */ 
    	if (fromEmail == null) {
    		return WISEApplication.emailFrom;
    	}
    	if (fromString == null) {
    		return fromEmail;
    	}
    	
    	/* brackets already added for now */
    	return fromString + " <" + fromEmail + ">"; 
    }

    /**
     * Returns the body of the message.
     * 
     * @return	String	Body of the message.
     */
    public String getReplyString() {
    	
    	/* returns null if not specified */
    	return replyString;
    }

    /**
     * Returns just the id part of the email for example if id is xxxx@yyyy.zzz
     * it returns xxxx.
     * 
     * @return	String	The id part of the email.
     */
    public String emailID() {
    	if (fromEmail == null) {
    		return null;
    	}
    	int atindx = fromEmail.indexOf('@');
    	if (atindx > 0) {
    		return fromEmail.substring(0, atindx);
    	} else {
    		return fromEmail;
    	}
    }

   /**
     * Returns the requested message type from the sequence.
     * 
     * @param 	messageType		The type of the message that is to be returned from 
     * 							the message sequence. Types can be invite/interrupt/done/review
     * 	
     * @return	Message			The message of the requested type is returned.
     */
    public Message getTypeMessage(String messageType) {
    	
    	/* use integer to get one of the other messages */
    	if (messageType.equalsIgnoreCase("invite")) {
    		return inviteMsg;
    	} else if (messageType.equalsIgnoreCase("interrupt")) {
    		return interruptMsg;
    	} else if (messageType.equalsIgnoreCase("done")) {
    		return doneMsg;
    	} else if (messageType.equalsIgnoreCase("review")) {
    		return reviewMsg;
    	} else {
    		int index = Integer.parseInt(messageType);
    		return (Message) otherMsgs.get(index);
    	}
    }

    /**
     * Returns the indexed start reminder message.
     * 
     * @param 	index		Index of the message in the message sequence.
     * @return	Reminder	returns the required reminder.
     */
    public Reminder getStartReminder(int index) {
    	return (Reminder) startReminders.get(index);
    }

    /**
     * Returns the indexed Completion reminder message.
     * 
     * @param 	index		Index of the message in the message sequence.
     * @return	Reminder	returns the required reminder.
     */
    public Reminder getCompletionReminder(int index) {
    	return (Reminder) completionReminders.get(index);
    }

    /**
     * Returns the total number of start reminder messages.
     * 
     * @return	int	total number of start reminders.
     */
    public int totalStartReminders() {
    	return startReminders.size();
    }

    /**
     * Returns the total number of completion reminder messages.
     * 
     * @return	int	total number of completion reminders.
     */
    public int totalCompletionReminders() {
    	return completionReminders.size();
    }

    /**
     * Returns the total number of other messages.
     * 
     * @return	int	total number of other messages.
     */
    public int totalOtherMessages() {
    	return otherMsgs.size();
    }

    /**
     * Returns the information about this message sequence in form of string.
     * 
     * @return	String	String format of the message sequence.
     */
    public String toString() {
    	String resp = "<b>Message Sequence: " + id + "</b> for survey ID(s): "
    			+ surveyId + "<br>Messages<br>";
    	resp += inviteMsg.toString();
    	resp += interruptMsg.toString();
    	resp += doneMsg.toString();
    	resp += "Start reminders n=" + startReminders.size()
    			+ "; Completion reminders n=" + completionReminders.size()
    			+ "<br>";
    	return resp;
    }
}
