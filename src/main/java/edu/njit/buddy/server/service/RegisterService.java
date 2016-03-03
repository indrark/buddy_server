package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.ResponseCode;
import edu.njit.buddy.server.ServerException;
import edu.njit.buddy.server.util.EmailValidator;
import edu.njit.buddy.server.util.Encoder;
import edu.njit.buddy.server.util.PasswordValidator;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * @author toyknight 3/2/2016.
 */
public class RegisterService extends Service {

    public RegisterService(Context context) {
        super(context, false);
    }

    @Override
    public void service(JSONObject request, Response response) throws ServerException, SQLException, JSONException {
        String email = request.getString("email");
        String username = request.getString("username");
        String password = request.getString("password");
        if (EmailValidator.isValidEmail(email)) {
            if (PasswordValidator.isValidPassword(password)) {
                if (getContext().getDBManager().isEmailAvailable(email)) {
                    String encoded_password = Encoder.encode(password);
                    getContext().getDBManager().register(email, username, encoded_password);

                    onSuccess(response);
                } else {
                    onFail(response, ResponseCode.EMAIL_NOT_AVAILABLE);
                }
            } else {
                onFail(response, ResponseCode.PASSWORD_NOT_VALID);
            }
        } else {
            onFail(response, ResponseCode.EMAIL_NOT_VALID);
        }
    }

}
