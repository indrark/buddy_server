package edu.njit.buddy.server.util;

import edu.njit.buddy.server.exceptions.ServerException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author toyknight on 3/2/2016.
 */
public class Encoder {

    private final static String[] HEX_DIGITS =
            {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public static String encode(String message) throws ServerException {
        try {
            MessageDigest message_digest = MessageDigest.getInstance("MD5");
            byte[] origin = message.getBytes();
            byte[] encrypted = message_digest.digest(origin);
            return byteArrayToHexString(encrypted);
        } catch (NoSuchAlgorithmException ex) {
            throw new ServerException("Failed encoding message", ex);
        }
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder();
        for (byte aB : b) {
            resultSb.append(byteToHexString(aB));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return HEX_DIGITS[d1] + HEX_DIGITS[d2];
    }

}
