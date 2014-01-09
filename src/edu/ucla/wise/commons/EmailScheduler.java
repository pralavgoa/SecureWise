/**
 * 
 */
package edu.ucla.wise.commons;


/**
 * This email thread will spawn action of sending reminders.
 * 
 * @author Pralav
 * @version 1.0  
 */
public class EmailScheduler {

    public static void main(String[] args) {
    	
		/* start the email sending procedure */
    	java.util.Date today = new java.util.Date();
		if (args.length < 1) {
		    System.out.println("Usage: EmailScheduler [application_name]");
		    return;
		}
		String appName = args[0];
		System.out.print("Launching Email Manager on " + today.toString()
				+ " for studies assigned to " + appName + " on this server.");
	
		StudySpace[] allSpaces = new StudySpace[0];
		
		allSpaces = StudySpace.getAll();
		
		if (allSpaces == null || allSpaces.length < 1) {
			System.out.print("Error while getting the  study spaces"
					+ " assigned to " + appName + " on this server.");
			return;
		}		
				
		/* iterate over all Study_Spaces that this server manages */
		for (int i = 0; i < allSpaces.length; i++) {
		    StudySpace ss = allSpaces[i];
		    DataBank db = ss.db;
		    System.out.println("\nStudy_Space " + ss.studyName
			    + " CONNECTING to database: " + db.dbdata);
		    System.out.println(db.sendReminders());
		}
    }
}
