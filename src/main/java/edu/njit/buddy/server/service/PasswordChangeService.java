package edu.njit.buddy.server.service;

import edu.njit.buddy.server.*;
import edu.njit.buddy.server.util.PasswordValidator;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;

import java.sql.SQLException;

/**
 * @author toyknight 4/10/2016.
 */
public class PasswordChangeService extends Service {


    public PasswordChangeService(Context context) {
        super(context, true);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        String old_password = request.getBody().getString("old_password");
        String new_password = request.getBody().getString("new_password");
        if (PasswordValidator.isValidPassword(new_password)) {
            try {
                getContext().getDBManager().changePassword(request.getUID(), old_password, new_password);
                onSuccess(response);
            } catch (PasswordMismatchException ex) {
                onFail(response, ResponseCode.PASSWORD_MISMATCH);
                //TODO: record this password change failure.
            }
        } else {
            onFail(response, ResponseCode.PASSWORD_NOT_VALID);
        }
    }
}
