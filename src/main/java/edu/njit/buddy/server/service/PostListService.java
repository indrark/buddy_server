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
        int category = request.has("category") ? request.getInt("category") : -1;
        int attention = request.has("attention") ? request.getInt("attention") : 0;
        int target_uid = request.has("target_uid") ? request.getInt("target_uid") : 0;
        JSONObject response_content = getContext().getDBManager().listPosts(
                getUID(), page, category, attention, target_uid);
        onSuccess(response, response_content);
    }

}
