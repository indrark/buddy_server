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
 * @author toyknight 11/7/2016.
 */
public class UserInformationService extends Service {

    public UserInformationService(Context context) {
        super(context, true);
        setCheckAdministrator(true);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        int uid = request.getBody().getInt("uid");
        String email = request.getBody().getString("email").trim();
        String username = request.getBody().getString("username").trim();
        try {
            JSONObject data = getContext().getDBManager().getUserData(uid, email, username);
            onSuccess(response, data);
        } catch (UserNotFoundException e) {
            onFail(response, ResponseCode.USER_NOT_FOUND);
        }
    }
}
