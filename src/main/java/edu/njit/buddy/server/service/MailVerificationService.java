package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.RequestWrapper;
import edu.njit.buddy.server.ResponseCode;
import edu.njit.buddy.server.exceptions.ServerException;
import edu.njit.buddy.server.util.MailUtil;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;

import java.sql.SQLException;

/**
 * @author toyknight 4/11/2016.
 */
public class MailVerificationService extends Service {

    public MailVerificationService(Context context) {
        super(context, false);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        String recipient = request.getBody().getString("email");
        if (getContext().getMailSender().canSend(recipient)) {
            if (getContext().getDBManager().isEmailAvailable(recipient)) {
                String token = getContext().getTokenManager().createVerification(recipient);
                String mail_content = MailUtil.createVerificationContent(token);
                getContext().getMailSender().sendMail(recipient, "Buddy Verification Code", mail_content);
                onSuccess(response);
            } else {
                onFail(response, ResponseCode.EMAIL_NOT_AVAILABLE);
            }
        } else {
            onFail(response, ResponseCode.MAIL_SENDING_TOO_FREQUENT);
        }
    }

}
