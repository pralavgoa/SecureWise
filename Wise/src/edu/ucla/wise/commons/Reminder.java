package edu.ucla.wise.commons;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * This class contains a typical reminder message sequence and its properties
 * 
 * @author Douglas Bell
 * @version 1.0
 * 
 */
public class Reminder extends Message {
    public static final Logger LOGGER = Logger.getLogger(Reminder.class);
    /** Instance Variables */
    public int triggerDays, maxCount;

    /**
     * the constructor for reminder
     * 
     * @param n
     *            XML Node from which the details of the trigger days and max
     *            count are obtained.
     */
    public Reminder(Node n) {
	super(n);
	try {
	    // System.out.println("reminder id=" + id);
	    this.triggerDays = Integer.parseInt(n.getAttributes()
		    .getNamedItem("Trigger_Days").getNodeValue());
	    this.maxCount = Integer.parseInt(n.getAttributes()
		    .getNamedItem("Max_Count").getNodeValue());
	    // System.out.println("max count=" + max_count);
	} catch (NumberFormatException e) {
	    LOGGER.error("WISE EMAIL - REMINDER: ID = " + this.id
		    + "; Subject = " + this.subject + " --> " + e.toString(),
		    null);
	    return;
	} catch (DOMException e) {
	    LOGGER.error("WISE EMAIL - REMINDER: ID = " + this.id
		    + "; Subject = " + this.subject + " --> " + e.toString(),
		    null);
	    return;
	}
    }
}
