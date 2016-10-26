package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.RequestWrapper;
import edu.njit.buddy.server.ServerException;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * @author toyknight 10/25/2016.
 */
public class ServerStatusService extends Service {

    public ServerStatusService(Context context) {
        super(context, true);
        setCheckAdministrator(true);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        JSONObject status = getContext().getDBManager().getServerStatus();
        onSuccess(response, status);
    }

}
