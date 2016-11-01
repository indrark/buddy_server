package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.RequestWrapper;
import edu.njit.buddy.server.ResponseCode;
import edu.njit.buddy.server.exceptions.AccessDeniedException;
import edu.njit.buddy.server.exceptions.PostNotFoundException;
import edu.njit.buddy.server.exceptions.ServerException;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;

import java.sql.SQLException;

/**
 * @author toyknight 10/31/2016.
 */
public class PostDeleteService extends Service {

    public PostDeleteService(Context context) {
        super(context, true);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        int pid = request.getBody().getInt("pid");
        try {
            getContext().getDBManager().deletePost(request.getUID(), pid);
        } catch (PostNotFoundException ex) {
            onFail(response, ResponseCode.POST_NOT_FOUND);
        } catch (AccessDeniedException ex) {
            onFail(response, ResponseCode.ACCESS_DENIED);
        }
    }
}
