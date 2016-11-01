package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.RequestWrapper;
import edu.njit.buddy.server.ResponseCode;
import edu.njit.buddy.server.exceptions.ServerException;
import edu.njit.buddy.server.exceptions.UserNotFoundException;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * @author toyknight 3/5/2016.
 */
public class ProfileViewService extends Service {

    public ProfileViewService(Context context) {
        super(context, true);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        int target_uid = request.getBody().getInt("uid");
        try {
            JSONObject response_content = getContext().getDBManager().profileView(request.getUID(), target_uid);
            onSuccess(response, response_content);
        } catch (UserNotFoundException ex) {
            onFail(response, ResponseCode.USER_NOT_FOUND);
        }
    }

}
