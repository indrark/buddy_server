package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.RequestWrapper;
import edu.njit.buddy.server.ResponseCode;
import edu.njit.buddy.server.exceptions.ServerException;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;

import java.sql.SQLException;

/**
 * @author toyknight 7/1/2016.
 */
public class MoodSubmitService extends Service {

    public MoodSubmitService(Context context) {
        super(context, true);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        int uid = request.getUID();
        int mood = request.getBody().getInt("mood");
        if (getContext().getDBManager().submitMood(uid, mood)) {
            onSuccess(response);
        } else {
            onFail(response, ResponseCode.MOOD_TODAY_EXISTS);
        }
    }

}
