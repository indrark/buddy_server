package edu.njit.buddy.server;

/**
 * @author toyknight 4/10/2016.
 */
public class PasswordMismatchException extends ServerException {

    public PasswordMismatchException(String message) {
        super(message);
    }

}
