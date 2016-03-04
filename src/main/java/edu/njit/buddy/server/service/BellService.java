package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * @author toyknight 3/3/2016.
 */
public class BellService extends Service {

    public BellService(Context context) {
        super(context, true);
    }

    @Override
    public void service(JSONObject request, Response response) throws SQLException, JSONException {
        int pid = request.getInt("pid");
        getContext().getDBManager().bell(getUID(), pid);
        onSuccess(response);
    }

}
