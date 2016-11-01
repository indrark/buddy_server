package edu.njit.buddy.server.exceptions;

/**
 * @author toyknight 4/11/2016.
 */
public class TokenExpiredException extends ServerException {

    public TokenExpiredException(String message) {
        super(message);
    }

}
