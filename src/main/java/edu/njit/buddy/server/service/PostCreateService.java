package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
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
    public void service(JSONObject request, Response response) throws SQLException, JSONException {
        int category = request.getInt("category");
        String content = request.getString("content");
        getContext().getDBManager().post(getUID(), category, content);

        onSuccess(response);
    }

}
