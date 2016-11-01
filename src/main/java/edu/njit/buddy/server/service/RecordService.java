package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.RequestWrapper;
import edu.njit.buddy.server.exceptions.ServerException;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;

import java.sql.SQLException;

/**
 * @author toyknight 3/6/2016.
 */
public class RecordService extends Service {


    public RecordService(Context context) {
        super(context, true);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        getContext().getDBManager().record(request.getUID());
        onSuccess(response);
    }

}
