package edu.njit.buddy.server.util;

/**
 * @author toyknight 4/11/2016.
 */
public class MailUtil {

    public static String createVerificationContent(String token) {
        return String.format("Verification Code: %s", token);
    }

}
