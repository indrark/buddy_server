package edu.njit.buddy.server;

import org.json.JSONObject;

/**
 * @author toyknight on 4/3/2016.
 */
public class RequestWrapper {

    private final int uid;

    private final JSONObject body;

    public RequestWrapper(int uid, JSONObject body) {
        this.uid = uid;
        this.body = body;
    }

    public int getUID() {
        return uid;
    }

    public JSONObject getBody() {
        return body;
    }

}
