package edu.njit.buddy.server.service;

import edu.njit.buddy.server.*;
import edu.njit.buddy.server.exceptions.PostNotFoundException;
import edu.njit.buddy.server.exceptions.ServerException;
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
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        int pid = request.getBody().getInt("pid");
        int page = request.getBody().getInt("page");
        try {
            if (page >= 0) {
                JSONObject response_content = getContext().getDBManager().listComments(pid, page);
                onSuccess(response, response_content);
            } else {
                onFail(response, ResponseCode.NEGATIVE_PAGE_NUMBER);
            }
        } catch (PostNotFoundException ex) {
            onFail(response, ResponseCode.POST_NOT_FOUND);
        }
    }

}
