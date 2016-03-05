package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.ServerException;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * @author toyknight 3/5/2016.
 */
public class CommentCreateService extends Service {

    public CommentCreateService(Context context) {
        super(context, true);
    }

    @Override
    public void service(JSONObject request, Response response) throws ServerException, SQLException, JSONException {
        int pid = request.getInt("pid");
        String content = request.getString("content");
        getContext().getDBManager().comment(getUID(), pid, content);
        onSuccess(response);
    }

}
