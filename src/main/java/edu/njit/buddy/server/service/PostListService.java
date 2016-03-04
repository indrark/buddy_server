package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * @author toyknight 3/3/2016.
 */
public class PostListService extends Service {

    public PostListService(Context context) {
        super(context, true);
    }

    @Override
    public void service(JSONObject request, Response response) throws SQLException, JSONException {
        int page = request.getInt("page");
        int category = request.getInt("category");
        int attention = request.getInt("attention");
        if (attention == 1) {
            JSONObject response_content = getContext().getDBManager().listAttention(getUID(), page);
            onSuccess(response, response_content);
        } else {
            JSONObject response_content = category >= 0 ?
                    getContext().getDBManager().listPost(getUID(), page, category) :
                    getContext().getDBManager().listPost(getUID(), page);
            onSuccess(response, response_content);
        }
    }

}
