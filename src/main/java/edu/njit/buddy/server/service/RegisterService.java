package edu.njit.buddy.server.service;

import edu.njit.buddy.server.*;
import edu.njit.buddy.server.util.EmailValidator;
import edu.njit.buddy.server.util.PasswordValidator;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;

import java.sql.SQLException;

/**
 * @author toyknight 3/2/2016.
 */
public class RegisterService extends Service {

    public RegisterService(Context context) {
        super(context, false);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        String email = request.getBody().getString("email");
        String username = request.getBody().getString("username");
        String password = request.getBody().getString("password");
        String verification = request.getBody().getString("verification");
        try {
            if (EmailValidator.validate(email)) {
                if (PasswordValidator.isValidPassword(password)) {
                    if (getContext().getDBManager().isEmailAvailable(email)) {
                        if (getContext().getTokenManager().checkVerification(email, verification)) {
                            getContext().getDBManager().register(email, username, password);
                            onSuccess(response);
                        } else {
                            onFail(response, ResponseCode.VERIFICATION_CODE_ERROR);
                        }
                    } else {
                        onFail(response, ResponseCode.EMAIL_NOT_AVAILABLE);
                    }
                } else {
                    onFail(response, ResponseCode.PASSWORD_NOT_VALID);
                }
            } else {
                onFail(response, ResponseCode.EMAIL_NOT_VALID);
            }
        } catch (TokenExpiredException ex) {
            onFail(response, ResponseCode.VERIFICATION_CODE_EXPIRED);
        }
    }

}
