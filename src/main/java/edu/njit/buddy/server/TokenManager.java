package edu.njit.buddy.server;

import edu.njit.buddy.server.exceptions.TokenExpiredException;

import java.util.*;

/**
 * @author toyknight 4/10/2016.
 */
public class TokenManager {

    private final long VERIFICATION_VALIDITY;
    private final long RECOVERY_VALIDITY;

    private final HashMap<String, Token> verification_tokens;
    private final Object VERIFICATION_LOCK = new Object();

    private final Random random;

    public TokenManager(long VERIFICATION_VALIDITY, long RECOVERY_VALIDITY) {
        this.VERIFICATION_VALIDITY = VERIFICATION_VALIDITY;
        this.RECOVERY_VALIDITY = RECOVERY_VALIDITY;
        this.verification_tokens = new HashMap<>();
        this.random = new Random();
    }

    public void clean() {
        synchronized (VERIFICATION_LOCK) {
            HashSet<String> identifiers = new HashSet<>();
            identifiers.addAll(verification_tokens.keySet());
            for (String identifier : identifiers) {
                Token token = verification_tokens.get(identifier);
                if (isTokenExpired(token, VERIFICATION_VALIDITY)) {
                    verification_tokens.remove(identifier);
                }
            }
        }
    }

    public String createVerification(String email) {
        synchronized (VERIFICATION_LOCK) {
            String token = String.format(
                    "%d%d%d%d", random.nextInt(10), random.nextInt(10), random.nextInt(10), random.nextInt(10));
            verification_tokens.put(email, new Token(token));
            return token;
        }
    }

    public boolean checkVerification(String email, String token) throws TokenExpiredException {
        synchronized (VERIFICATION_LOCK) {
            if (verification_tokens.containsKey(email)) {
                Token stored_token = verification_tokens.get(email);
                if (isTokenExpired(stored_token, VERIFICATION_VALIDITY)) {
                    throw new TokenExpiredException(String.format("Verification token expired for email [%s]", email));
                } else {
                    return token.equals(stored_token.getValue());
                }
            } else {
                return false;
            }
        }
    }

    private boolean isTokenExpired(Token token, long validity) {
        return System.currentTimeMillis() - token.getCreatingTime() > validity;
    }

    private class Token {

        private final String value;
        private final long creating_time;

        public Token(String content) {
            this.value = content;
            this.creating_time = System.currentTimeMillis();
        }

        public String getValue() {
            return value;
        }

        public long getCreatingTime() {
            return creating_time;
        }

    }

}
