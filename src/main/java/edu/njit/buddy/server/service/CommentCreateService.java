package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.RequestWrapper;
import edu.njit.buddy.server.exceptions.ServerException;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;

import java.sql.SQLException;

/**
 * @author toyknight 3/5/2016.
 */
public class CommentCreateService extends Service {

    public CommentCreateService(Context context) {
        super(context, true);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        int pid = request.getBody().getInt("pid");
        String content = request.getBody().getString("content");
        getContext().getDBManager().comment(request.getUID(), pid, content);
        onSuccess(response);
    }

}
