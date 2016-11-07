package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.RequestWrapper;
import edu.njit.buddy.server.ResponseCode;
import edu.njit.buddy.server.exceptions.ServerException;
import edu.njit.buddy.server.exceptions.UserNotFoundException;
import edu.njit.buddy.server.util.PasswordValidator;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;

import java.sql.SQLException;

/**
 * @author toyknight 11/7/2016.
 */
public class PasswordResetService extends Service {

    public PasswordResetService(Context context) {
        super(context, true);
        setCheckAdministrator(true);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        int uid = request.getBody().getInt("uid");
        String password = request.getBody().getString("password");
        if(PasswordValidator.isValidPassword(password)) {
            try {
                getContext().getDBManager().setPassword(uid, password);
                onSuccess(response);
            } catch (UserNotFoundException e) {
                onFail(response, ResponseCode.USER_NOT_FOUND);
            }
        } else {
            onFail(response, ResponseCode.PASSWORD_NOT_VALID);
        }
    }

}
