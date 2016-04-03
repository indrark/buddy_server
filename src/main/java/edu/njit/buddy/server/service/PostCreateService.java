package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.RequestWrapper;
import edu.njit.buddy.server.ResponseCode;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * @author toyknight 3/3/2016.
 */
public class PostCreateService extends Service {

    public PostCreateService(Context context) {
        super(context, true);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws SQLException, JSONException {
        int category = request.getBody().getInt("category");
        String content = request.getBody().getString("content");
        if (content.length() <= 128) {
            getContext().getDBManager().post(request.getUID(), category, content);
            onSuccess(response);
        } else {
            onFail(response, ResponseCode.CONTENT_TOO_LONG);
        }
    }

}
