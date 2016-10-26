package edu.njit.buddy.server.service.web;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.PasswordMismatchException;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

/**
 * @author toyknight 10/25/2016.
 */
public class AdministratorLoginService extends WebService {

    public AdministratorLoginService(Context context) {
        super(context, false);
        setMethod(Method.POST);
    }

    @Override
    protected void handle(Request request, Response response) throws Exception {
        try {
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String authorization = getContext().getDBManager().login(email, password).getString("authorization");
            response.addCookie(new Cookie("authorization", authorization));
            response.sendRedirect("/admin/dashboard");
        } catch (PasswordMismatchException ex) {
            response.sendRedirect("/");
        }
    }

}
