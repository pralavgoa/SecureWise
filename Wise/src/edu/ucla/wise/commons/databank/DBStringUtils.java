package edu.ucla.wise.commons.databank;

import java.util.List;

public class DBStringUtils {
    /**
     * If there is a quote in the string, replace it with double quotes this is
     * necessary for sql to store the quote properly.
     * 
     * @param s
     *            Input string with quotes.
     * @return String Modifies string.
     */
    public static String fixquotes(String s) {
        if (s == null) {
            return "";
        }

        int len = s.length();
        String s1, s2;

        s2 = "";
        for (int i = 0; i < len; i++) {
            s1 = s.substring(i, i + 1);
            s2 = s2 + s1;
            if (s1.equalsIgnoreCase("'")) {
                s2 = s2 + "'";
            }
        }
        return s2;
    }

    public static String convertListToCSVString(List<String> list) {
        StringBuilder result = new StringBuilder();
        String comma = "";
        for (String element : list) {
            result.append(comma);
            result.append(element);
            comma = ",";
        }
        return result.toString();
    }
}
