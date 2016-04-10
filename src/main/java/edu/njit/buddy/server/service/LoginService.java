package edu.njit.buddy.server.service;

import edu.njit.buddy.server.*;
import edu.njit.buddy.server.util.Encoder;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * @author toyknight 3/2/2016.
 */
public class LoginService extends Service {

    public LoginService(Context context) {
        super(context, false);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        String email = request.getBody().getString("email");
        String password = request.getBody().getString("password");
        try {
            JSONObject response_content = getContext().getDBManager().login(email, password);
            onSuccess(response, response_content);
        } catch (PasswordMismatchException ex) {
            onFail(response, ResponseCode.PASSWORD_OR_EMAIL_MISS_MATCH);
            //TODO: record this login failure.
        }
    }

}
