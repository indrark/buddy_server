package edu.njit.buddy.server.service.web;

import edu.njit.buddy.server.Context;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.StaticHttpHandlerBase;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @author toyknight 10/25/2016.
 */
public abstract class WebService extends StaticHttpHandlerBase {

    protected static final String admin_dir = "html_admin";

    private final Context context;

    private final boolean check_administrator;

    private Method method;

    public WebService(Context context, boolean check_administrator) {
        this.context = context;
        this.check_administrator = check_administrator;
    }

    public Context getContext() {
        return context;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    protected boolean handle(final String uri, final Request request, final Response response) throws IOException {
        try {
            if (checkMethod(request)) {
                if (check_administrator) {
                    if (checkAdministrator(request.getCookies())) {
                        handle(request, response);
                    } else {
                        response.sendError(403);
                    }
                } else {
                    handle(request, response);
                }
            } else {
                response.sendError(405);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            response.sendError(500);
        }
        return true;
    }

    private boolean checkAdministrator(Cookie[] cookies) throws SQLException {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("authorization")) {
                return getContext().getDBManager().checkAdministrator(cookie.getValue());
            }
        }
        return false;
    }

    private boolean checkMethod(Request request) {
        return method == null || request.getMethod().equals(method);
    }

    protected abstract void handle(final Request request, final Response response) throws Exception;

    protected final void sendResource(Response response, File resource) throws IOException {
        pickupContentType(response, resource.getPath());

        sendFile(response, resource);
    }

}
