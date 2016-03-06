package edu.njit.buddy.server.service;

import edu.njit.buddy.server.Context;
import edu.njit.buddy.server.ServerException;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * @author toyknight 3/6/2016.
 */
public class ProfileEditService extends Service {

    public ProfileEditService(Context context) {
        super(context, true);
    }

    @Override
    public void service(JSONObject request, Response response) throws ServerException, SQLException, JSONException {
        String username = request.getString("username");
        String description = request.getString("description");
        int description_open = request.getInt("description_open");
        String birthday = request.getString("birthday");
        int birthday_open = request.getInt("birthday_open");
        String gender = request.getString("gender");
        int gender_open = request.getInt("gender_open");
        String sexuality = request.getString("sexuality");
        int sexuality_open = request.getInt("sexuality_open");
        String race = request.getString("race");
        int race_open = request.getInt("race_open");
        getContext().getDBManager().profileEdit(
                getUID(),
                username,
                description, description_open,
                birthday, birthday_open,
                gender, gender_open,
                sexuality, sexuality_open,
                race, race_open);
        onSuccess(response);
    }

}
