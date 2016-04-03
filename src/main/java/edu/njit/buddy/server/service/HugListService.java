package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.RequestWrapper;
import edu.njit.buddy.server.ServerException;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * @author toyknight 3/4/2016.
 */
public class HugListService extends Service {


    public HugListService(Context context) {
        super(context, true);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        int pid = request.getBody().getInt("pid");
        int page = request.getBody().getInt("page");
        JSONObject response_content = getContext().getDBManager().listHugs(pid, page);
        onSuccess(response, response_content);
    }

}
