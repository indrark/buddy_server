package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.RequestWrapper;
import edu.njit.buddy.server.ServerException;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;

import java.sql.SQLException;

/**
 * @author toyknight 3/6/2016.
 */
public class ProfileEditService extends Service {

    public ProfileEditService(Context context) {
        super(context, true);
    }

    @Override
    public void service(RequestWrapper request, Response response) throws ServerException, SQLException, JSONException {
        String username = request.getBody().getString("username");
        String description = request.getBody().getString("description");
        int description_open = request.getBody().getInt("description_open");
        String birthday = request.getBody().getString("birthday");
        int birthday_open = request.getBody().getInt("birthday_open");
        String gender = request.getBody().getString("gender");
        int gender_open = request.getBody().getInt("gender_open");
        String sexuality = request.getBody().getString("sexuality");
        int sexuality_open = request.getBody().getInt("sexuality_open");
        String race = request.getBody().getString("race");
        int race_open = request.getBody().getInt("race_open");
        getContext().getDBManager().profileEdit(
                request.getUID(),
                username,
                description, description_open,
                birthday, birthday_open,
                gender, gender_open,
                sexuality, sexuality_open,
                race, race_open);
        onSuccess(response);
    }

}
