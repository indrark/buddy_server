package edu.njit.buddy.server.service;

import edu.njit.buddy.server.*;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;

import java.sql.SQLException;

/**
 * @author toyknight 3/3/2016.
 */
public class BellService extends Service {

    public BellService(Context context) {
        super(context, true);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        int pid = request.getBody().getInt("pid");
        try {
            getContext().getDBManager().bell(request.getUID(), pid);
            onSuccess(response);
        } catch (PostNotFoundException ex) {
            onFail(response, ResponseCode.POST_NOT_FOUND);
        }
    }

}
