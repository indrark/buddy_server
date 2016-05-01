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
public class PostListService extends Service {

    public PostListService(Context context) {
        super(context, true);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws SQLException, JSONException {
        int page = request.getBody().getInt("page");
        int category = request.getBody().has("category") ? request.getBody().getInt("category") : -1;
        int attention = request.getBody().has("attention") ? request.getBody().getInt("attention") : 0;
        int target_uid = request.getBody().has("target_uid") ? request.getBody().getInt("target_uid") : 0;
        if (page >= 0) {
            JSONObject response_content = getContext().getDBManager().listPosts(
                    request.getUID(), page, category, attention, target_uid);
            onSuccess(response, response_content);
        } else {
            onFail(response, ResponseCode.NEGATIVE_PAGE_NUMBER);
        }
    }

}
