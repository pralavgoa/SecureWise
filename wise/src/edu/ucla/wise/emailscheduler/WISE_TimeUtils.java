package edu.ucla.wise.emailscheduler;

public class WISE_TimeUtils {

    public static final long MILLISECONDS_IN_A_DAY = 86400000;
    public static final long MILLISECONDS_IN_AN_HOUR = 3600000;
    public static final long MILLISECONDS_IN_A_MINUTE = 60000;

    public static String convertMillisToHumanReadableForm(long milliseconds) {

	if(milliseconds < 0){
	    return "CANNOT_CONVERT";
	}
	
	if (milliseconds > 86400000) {
	    return getNumberOfDays(milliseconds);
	} else {
	    return getNumberOfHours(milliseconds);
	}

    }

    public static String getNumberOfDays(long milliseconds) {

	long days = milliseconds / MILLISECONDS_IN_A_DAY;

	long remainder = milliseconds % MILLISECONDS_IN_A_DAY;

	return days + " days, " + getNumberOfHours(remainder);
    }

    public static String getNumberOfHours(long milliseconds) {
	long hours = milliseconds / MILLISECONDS_IN_AN_HOUR;

	long remainder = milliseconds % MILLISECONDS_IN_AN_HOUR;

	return hours + " hours, " + getNumberOfMinutes(remainder);
    }

    public static String getNumberOfMinutes(long milliseconds) {
	long minutes = milliseconds / MILLISECONDS_IN_A_MINUTE;

	long remainder = milliseconds % MILLISECONDS_IN_A_MINUTE;

	return minutes + " minutes, " + remainder / 1000 + " seconds";
    }


}
