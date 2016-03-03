package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.NotAuthorizedException;
import edu.njit.buddy.server.ResponseCode;
import edu.njit.buddy.server.ServerException;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * @author toyknight 3/2/2016.
 */
public abstract class Service extends HttpHandler {

    private final Context context;

    private final boolean need_authorization;

    private int uid;

    public Service(Context context, boolean need_authorization) {
        super();
        this.context = context;
        this.need_authorization = need_authorization;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void service(Request request, Response response) {
        try {
            if (request.getMethod().getMethodString().equals("POST")) {
                if (need_authorization) {
                    String authorization = request.getAuthorization();
                    uid = getContext().getDBManager().getUID(authorization);
                }
                InputStreamReader ir = new InputStreamReader(request.getInputStream(), "UTF-8");
                BufferedReader br = new BufferedReader(ir);
                String line;
                StringBuilder content = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    content.append(line).append("\n");
                }
                br.close();
                service(new JSONObject(content.toString()), response);
            } else {
                response.getWriter().write("ERROR: Only [POST] method is allowed.");
            }
        } catch (JSONException ex) {
            onFail(response, ResponseCode.BAD_REQUEST);
        } catch (ServerException | SQLException | IOException ex) {
            onFail(response, ResponseCode.SERVER_ERROR);
        } catch (NotAuthorizedException e) {
            onFail(response, ResponseCode.LOGIN_REQUIRED);
        }
    }

    abstract public void service(JSONObject request, Response response)
            throws ServerException, SQLException, JSONException;

    protected int getUID() {
        return uid;
    }

    protected void onSuccess(Response response) {
        onSuccess(response, new JSONObject());
    }

    protected void onSuccess(Response response, JSONObject content) {
        try {
            content.put("response_code", ResponseCode.BUDDY_OK);
            response.setCharacterEncoding("UTF8");
            response.setContentType("application/json");
            response.getWriter().write(content.toString());
        } catch (IOException ex) {
            getContext().getLogger().log(Level.SEVERE, "An error occurred while responding: " + ex.toString());
        }
    }

    protected void onFail(Response response, int response_code) {
        try {
            response.setCharacterEncoding("UTF8");
            response.setContentType("application/json");
            response.getWriter().write(String.format("{\"response_code\":%d}", response_code));
        } catch (IOException ex) {
            getContext().getLogger().log(Level.SEVERE, "An error occurred while responding: " + ex.toString());
        }
    }

}
