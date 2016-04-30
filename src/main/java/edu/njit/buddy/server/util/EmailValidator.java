package edu.njit.buddy.server.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author toyknight 3/2/2016.
 */
public class EmailValidator {

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@njit.edu";

    public static boolean validate(String email) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
