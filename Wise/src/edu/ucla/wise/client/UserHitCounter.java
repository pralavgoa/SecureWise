package edu.ucla.wise.client;

/**
 * UserHitCounter class is used to keep track of number of users
 * that are currently logged into the survey system.
 * 
 * @author Pralav
 * @version 1.0  
 */
public class UserHitCounter {

    private static UserHitCounter userHitCounter;
    private long numberOfUserAccesses;

    /**
     * private constructor.
     */
    private UserHitCounter() {
    	numberOfUserAccesses = 0;
    }

    /**
     * Initializes new userHitCounter if already not done.
     * @return UserHitCounter.
     */
    public static UserHitCounter getInstance() {
		if (userHitCounter == null) {
		    initialize();
		}
		return userHitCounter;
    }

    /**
     * Initializes new userHitCounter object.
     */
    public static void initialize() {
    	userHitCounter = new UserHitCounter();
    }

    /**
     * @return the numberOfUserAccesses
     */
    public long getNumberOfUserAccesses() {
    	return numberOfUserAccesses;
    }

    /**
     * Increments the count of the users accessing the system.
     */
    public void incrementNumberOfUserAccesses() {
    	numberOfUserAccesses++;
    }
}
