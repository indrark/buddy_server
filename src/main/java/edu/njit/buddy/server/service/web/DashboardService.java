package edu.njit.buddy.server.service.web;

import edu.njit.buddy.server.Context;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import java.io.File;

/**
 * @author toyknight 10/25/2016.
 */
public class DashboardService extends WebService {

    public DashboardService(Context context) {
        super(context, true);
        setMethod(Method.GET);
    }

    @Override
    protected void handle(Request request, Response response) throws Exception {
        File resource = new File(admin_dir, "/dashboard.html");
        sendResource(response, resource);
    }

}
