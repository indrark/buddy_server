package edu.njit.buddy.server;

/**
 * @author toyknight 4/11/2016.
 */
public class TokenExpiredException extends ServerException {

    public TokenExpiredException(String message) {
        super(message);
    }

}
