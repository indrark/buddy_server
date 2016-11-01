package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.RequestWrapper;
import edu.njit.buddy.server.ResponseCode;
import edu.njit.buddy.server.exceptions.ServerException;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * @author toyknight 7/1/2016.
 */
public class MoodListService extends Service {

    public MoodListService(Context context) {
        super(context, true);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        int uid = request.getUID();
        int page = request.getBody().getInt("page");
        if (page >= 0) {
            JSONObject response_content = getContext().getDBManager().listMoods(uid, page);
            onSuccess(response, response_content);
        } else {
            onFail(response, ResponseCode.NEGATIVE_PAGE_NUMBER);
        }
    }

}
