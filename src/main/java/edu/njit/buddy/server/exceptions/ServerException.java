package edu.njit.buddy.server.exceptions;

/**
 * @author toyknight 3/3/2016.
 */
public class ServerException extends Exception {

    public ServerException(String message) {
        super(message);
    }

    public ServerException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
