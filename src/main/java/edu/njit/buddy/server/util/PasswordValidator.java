package edu.njit.buddy.server.util;

/**
 * @author toyknight 3/2/2016.
 */
public class PasswordValidator {

    public static boolean isValidPassword(String password) {
        return password.length() >= 8;
    }

}
