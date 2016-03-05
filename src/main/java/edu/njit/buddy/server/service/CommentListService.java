package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.ServerException;
import edu.njit.buddy.server.service.Service;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * @author toyknight 3/5/2016.
 */
public class CommentListService extends Service {

    public CommentListService(Context context) {
        super(context, true);
    }

    @Override
    public void service(JSONObject request, Response response) throws ServerException, SQLException, JSONException {
        int pid = request.getInt("pid");
        int page = request.getInt("page");
        JSONObject response_content = getContext().getDBManager().listComments(pid, page);
        onSuccess(response, response_content);
    }

}
