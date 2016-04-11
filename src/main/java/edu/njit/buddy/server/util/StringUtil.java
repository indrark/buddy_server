package edu.njit.buddy.server.util;

/**
 * @author toyknight 4/10/2016.
 */
public class StringUtil {

    public static String escape(String str) {
        return str.replace("'", "''").replace("\\", "\\\\");
    }

}
