package edu.njit.buddy.server;

import edu.njit.buddy.server.service.NotAuthorizedException;
import edu.njit.buddy.server.service.UserNotFoundException;
import edu.njit.buddy.server.util.Encoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author toyknight 3/2/2016.
 */
public class DBManager {

    private final Context context;

    public DBManager(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public boolean isEmailAvailable(String email) throws SQLException {
        ResultSet result = getContext().getDBConnector().executeQuery(
                String.format("SELECT count(email) FROM user WHERE email = '%s'", email));
        result.next();
        return result.getInt(1) == 0;
    }

    public int getCurrentTestGroup() throws SQLException {
        ResultSet result = getContext().getDBConnector().executeQuery(
                "SELECT\n" +
                        "\ttest_group\n" +
                        "FROM\n" +
                        "\tuser,\n" +
                        "    (SELECT \n" +
                        "\t\tmax(uid) AS uid\n" +
                        "\t FROM \n" +
                        "\t\tuser) AS latest_user\n" +
                        "WHERE\n" +
                        "\tuser.uid = latest_user.uid");
        if (result.next()) {
            return result.getInt("test_group");
        } else {
            return 0;
        }
    }

    public int getUID(String authorization) throws SQLException, NotAuthorizedException {
        ResultSet result = getContext().getDBConnector().executeQuery(
                String.format("SELECT uid FROM user WHERE authorization = '%s'", authorization));
        if (result.next()) {
            return result.getInt("uid");
        } else {
            throw new NotAuthorizedException();
        }
    }

    public void register(String email, String username, String password) throws ServerException, SQLException {
        String sql = String.format(
                "INSERT INTO user (email, username, password, test_group) VALUES ('%s', '%s', '%s', %d)",
                email, username, Encoder.encode(password), getContext().getNextTestGroup());
        getContext().getDBConnector().executeUpdate(sql);
    }

    public JSONObject login(String email, String password) throws ServerException, SQLException {
        JSONObject response = new JSONObject();
        ResultSet result = getContext().getDBConnector().executeQuery(String.format(
                "SELECT uid FROM user WHERE email = '%s' AND password = '%s'", email, Encoder.encode(password)));
        if (result.next()) {
            int uid = result.getInt("uid");
            String authorization = Encoder.encode(email + password + System.currentTimeMillis());
            getContext().getDBConnector().executeUpdate(String.format(
                    "UPDATE user SET authorization = '%s' WHERE uid = '%d'", authorization, uid));
            response.put("uid", uid);
            response.put("authorization", authorization);
            return response;
        } else {
            throw new PasswordMismatchException(String.format("Password mismatch for email [%s]", email));
        }
    }

    public void changePassword(int uid, String old_password, String new_password)
            throws ServerException, SQLException {
        ResultSet result = getContext().getDBConnector().executeQuery(String.format(
                "SELECT uid FROM user WHERE uid = '%d' AND password = '%s'", uid, Encoder.encode(old_password)));
        if (result.next()) {
            getContext().getDBConnector().executeUpdate(String.format(
                    "UPDATE user SET password = '%s' WHERE uid = '%d'", Encoder.encode(new_password), uid));
        } else {
            throw new PasswordMismatchException(String.format("Password mismatch for uid [%d]", uid));
        }
    }

    public void setPassword(int uid, String password) throws ServerException, SQLException {
        getContext().getDBConnector().executeUpdate(String.format(
                "UPDATE user SET password = '%s' WHERE uid = '%d'", Encoder.encode(password), uid));
    }

    public void post(int uid, int category, String content) throws SQLException {
        String sql = String.format(
                "INSERT INTO post (uid, category, content) VALUES (%d, %d, '%s')", uid, category, content);
        getContext().getDBConnector().executeUpdate(sql);
    }

    public void flag(int uid, int pid) throws SQLException {
        ResultSet result = getContext().getDBConnector().executeQuery(
                String.format("SELECT fid FROM flag WHERE uid = %d AND pid = %d", uid, pid));
        if (result.next()) {
            int fid = result.getInt("fid");
            getContext().getDBConnector().executeUpdate(
                    String.format("DELETE FROM flag WHERE fid = %d", fid));
        } else {
            getContext().getDBConnector().executeUpdate(
                    String.format("INSERT INTO flag (uid, pid) VALUES (%d, %d)", uid, pid));
        }
    }

    public void bell(int uid, int pid) throws SQLException {
        ResultSet result = getContext().getDBConnector().executeQuery(
                String.format("SELECT bid FROM bell WHERE uid = %d AND pid = %d", uid, pid));
        if (result.next()) {
            int bid = result.getInt("bid");
            getContext().getDBConnector().executeUpdate(
                    String.format("DELETE FROM bell WHERE bid = %d", bid));
        } else {
            getContext().getDBConnector().executeUpdate(
                    String.format("INSERT INTO bell (uid, pid) VALUES (%d, %d)", uid, pid));
        }
    }

    public void hug(int uid, int pid) throws SQLException {
        ResultSet result = getContext().getDBConnector().executeQuery(
                String.format("SELECT hid FROM hug WHERE uid = %d AND pid = %d", uid, pid));
        if (result.next()) {
            int hid = result.getInt("hid");
            getContext().getDBConnector().executeUpdate(
                    String.format("DELETE FROM hug WHERE hid = %d", hid));
        } else {
            getContext().getDBConnector().executeUpdate(
                    String.format("INSERT INTO hug (uid, pid) VALUES (%d, %d)", uid, pid));
        }
    }

    public void comment(int uid, int pid, String content) throws SQLException {
        String sql = String.format(
                "INSERT INTO comment (uid, pid, content, timestamp) VALUES (%d, %d, '%s', now())", uid, pid, content);
        getContext().getDBConnector().executeUpdate(sql);
    }

    public JSONObject listPosts(int uid, int page, int category, int attention, int target_uid) throws SQLException {
        String category_replacer = category >= 0 ? "AND post.category = " + category : "";
        String attention_replacer = attention == 1 ? "AND bells.bells >=2" : "";
        String target_replacer = target_uid > 0 ? "AND post.uid = " + target_uid : "";
        String sql = String.format(
                "SELECT \n" +
                        "\tpost.pid, \n" +
                        "\tpost.uid, \n" +
                        "\tuser.username, \n" +
                        "\tpost.timestamp, \n" +
                        "\tpost.category, \n" +
                        "\tpost.content, \n" +
                        "\thugs.hugs, \n" +
                        "\tcomments.comments, \n" +
                        "\thugged.hugged, \n" +
                        "\tbelled.belled, \n" +
                        "\tflagged.flagged\n" +
                        "FROM\n" +
                        "\tuser, post,\n" +
                        "\t(SELECT \n" +
                        "\t\tpost.pid, count(hug.hid) AS hugs \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN hug ON post.pid = hug.pid \n" +
                        "\tGROUP BY post.pid) AS hugs,\n" +
                        "\t(SELECT \n" +
                        "\t\tpost.pid, count(bell.bid) AS bells \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN bell ON post.pid = bell.pid \n" +
                        "\tGROUP BY post.pid) AS bells,\n" +
                        "\t(SELECT \n" +
                        "\t\tpost.pid, count(comment.cid) AS comments \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN comment ON post.pid = comment.pid \n" +
                        "\tGROUP BY post.pid) AS comments,\n" +
                        "\t(SELECT \n" +
                        "\t\tpost.pid, count(hug.uid) AS hugged \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN hug ON post.pid = hug.pid AND hug.uid = %d\n" +
                        "\tGROUP BY post.pid) AS hugged,\n" +
                        "\t(SELECT \n" +
                        "\t\tpost.pid, count(bell.uid) AS belled \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN bell ON post.pid = bell.pid AND bell.uid = %d\n" +
                        "\tGROUP BY post.pid) AS belled,\n" +
                        "\t(SELECT \n" +
                        "\t\tpost.pid, count(flag.uid) AS flagged \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN flag ON post.pid = flag.pid AND flag.uid = %d\n" +
                        "\tGROUP BY post.pid) AS flagged\n" +
                        "WHERE\n" +
                        "\tpost.uid = user.uid \n" +
                        "\tAND post.pid = hugs.pid \n" +
                        "\tAND post.pid = bells.pid\n" +
                        "\tAND post.pid = comments.pid\n" +
                        "\tAND post.pid = hugged.pid \n" +
                        "\tAND post.pid = belled.pid \n" +
                        "\tAND post.pid = flagged.pid\n" +
                        "\t%s\n" +
                        "\t%s\n" +
                        "\t%s\n" +
                        "ORDER BY post.pid DESC\n" +
                        "LIMIT %d, %d",
                uid, uid, uid, attention_replacer, category_replacer, target_replacer, page * 10, 10);
        ResultSet result = getContext().getDBConnector().executeQuery(sql);
        JSONArray posts = createPostList(result);
        JSONObject response = new JSONObject();
        response.put("posts", posts);
        return response;
    }

    private JSONArray createPostList(ResultSet result) throws SQLException {
        JSONArray posts = new JSONArray();
        while (result.next()) {
            JSONObject post = new JSONObject();
            post.put("pid", result.getInt("pid"));
            post.put("uid", result.getInt("uid"));
            post.put("username", result.getString("username"));
            post.put("timestamp", result.getTimestamp("timestamp").getTime());
            post.put("category", result.getInt("category"));
            post.put("content", result.getString("content"));
            post.put("hugs", result.getInt("hugs"));
            post.put("comments", result.getInt("comments"));
            post.put("hugged", result.getInt("hugged"));
            post.put("belled", result.getInt("belled"));
            post.put("flagged", result.getInt("flagged"));
            posts.put(post);
        }
        return posts;
    }

    public JSONObject listHugs(int pid, int page) throws SQLException, JSONException {
        String sql = String.format(
                "SELECT\n" +
                        "\tuser.uid, user.username\n" +
                        "FROM\n" +
                        "\tuser, hug\n" +
                        "WHERE\n" +
                        "\tuser.uid = hug.uid AND hug.pid = %d\n" +
                        "ORDER BY timestamp DESC\n" +
                        "LIMIT %d, %d",
                pid, page * 10, 10);
        ResultSet result = getContext().getDBConnector().executeQuery(sql);
        JSONArray hugs = createHugList(result);
        JSONObject response = new JSONObject();
        response.put("hugs", hugs);
        return response;
    }

    public JSONArray createHugList(ResultSet result) throws SQLException, JSONException {
        JSONArray hugs = new JSONArray();
        while (result.next()) {
            JSONObject hug = new JSONObject();
            hug.put("uid", result.getInt("uid"));
            hug.put("username", result.getString("username"));
            hugs.put(hug);
        }
        return hugs;
    }

    public JSONObject listComments(int pid, int page) throws SQLException, JSONException {
        ResultSet result = getContext().getDBConnector().executeQuery(
                String.format(
                        "SELECT\n" +
                                "\tuser.uid,\n" +
                                "\tusername,\n" +
                                "\ttimestamp,\n" +
                                "\tcontent\n" +
                                "FROM\n" +
                                "\tuser, comment\n" +
                                "WHERE\n" +
                                "\tuser.uid = comment.uid AND comment.pid = %d\n" +
                                "ORDER BY timestamp DESC\n" +
                                "LIMIT %d, %d", pid, page * 10, 10));
        JSONArray comments = createCommentList(result);
        JSONObject response = new JSONObject();
        response.put("comments", comments);
        return response;
    }

    private JSONArray createCommentList(ResultSet result) throws SQLException, JSONException {
        JSONArray comments = new JSONArray();
        while (result.next()) {
            JSONObject comment = new JSONObject();
            comment.put("uid", result.getInt("uid"));
            comment.put("username", result.getString("username"));
            comment.put("timestamp", result.getTimestamp("timestamp").getTime());
            comment.put("content", result.getString("content"));
            comments.put(comment);
        }
        return comments;
    }

    public JSONObject profileView(int request_uid, int target_uid)
            throws SQLException, JSONException, UserNotFoundException {
        ResultSet result = getContext().getDBConnector().executeQuery(
                String.format(
                        "SELECT\n" +
                                "\tusername,\n" +
                                "    description,\n" +
                                "    description_open,\n" +
                                "    birthday,\n" +
                                "    birthday_open,\n" +
                                "    gender,\n" +
                                "    gender_open,\n" +
                                "    sexuality,\n" +
                                "    sexuality_open,\n" +
                                "    race,\n" +
                                "    race_open\n" +
                                "FROM user\n" +
                                "WHERE uid = %d", target_uid));

        if (result.next()) {
            JSONObject response = new JSONObject();
            response.put("username", result.getString("username"));
            if (request_uid == target_uid || result.getInt("description_open") == 1) {
                response.put("description", result.getString("description"));
            }
            response.put("description_open", result.getInt("description_open"));
            if (request_uid == target_uid || result.getInt("birthday_open") == 1) {
                response.put("birthday", result.getString("birthday"));
            }
            response.put("birthday_open", result.getInt("birthday_open"));
            if (request_uid == target_uid || result.getInt("gender_open") == 1) {
                response.put("gender", result.getString("gender"));
            }
            response.put("gender_open", result.getInt("gender_open"));
            if (request_uid == target_uid || result.getInt("sexuality_open") == 1) {
                response.put("sexuality", result.getString("sexuality"));
            }
            response.put("sexuality_open", result.getInt("sexuality_open"));
            if (request_uid == target_uid || result.getInt("race_open") == 1) {
                response.put("race", result.getString("race"));
            }
            response.put("race_open", result.getInt("race_open"));
            return response;
        } else {
            throw new UserNotFoundException();
        }
    }

    public void profileEdit(
            int uid,
            String username,
            String description, int description_open,
            String birthday, int birthday_open,
            String gender, int gender_open,
            String sexuality, int sexuality_open,
            String race, int race_open) throws SQLException {
        String sql = String.format(
                "UPDATE\n" +
                        "\tuser\n" +
                        "SET\n" +
                        "\tusername = '%s',\n" +
                        "    description = '%s',\n" +
                        "    description_open = %d,\n" +
                        "    birthday = '%s',\n" +
                        "    birthday_open = %d,\n" +
                        "    gender = '%s',\n" +
                        "    gender_open = %d,\n" +
                        "    sexuality = '%s',\n" +
                        "    sexuality_open = %d,\n" +
                        "    race = '%s',\n" +
                        "    race_open = %d\n" +
                        "WHERE\n" +
                        "\tuid = %d",
                username,
                description, description_open,
                birthday, birthday_open,
                gender, gender_open,
                sexuality, sexuality_open,
                race, race_open,
                uid);
        getContext().getDBConnector().executeUpdate(sql);
    }

    public void record(int uid) throws SQLException {
        getContext().getDBConnector().executeUpdate(
                String.format("UPDATE user SET using_times = using_times + 1 WHERE uid = %d", uid));
    }

}
