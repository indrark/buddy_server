package edu.njit.buddy.server;

import edu.njit.buddy.server.util.Encoder;
import org.json.JSONArray;
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

    public int getUID(String authorization) throws SQLException, NotAuthorizedException {
        ResultSet result = getContext().getDBConnector().executeQuery(
                String.format("SELECT uid FROM user WHERE authorization = '%s'", authorization));
        if (result.next()) {
            return result.getInt("uid");
        } else {
            throw new NotAuthorizedException();
        }
    }

    public void register(String email, String username, String password) throws SQLException {
        String sql = String.format(
                "INSERT INTO user (email, username, password) VALUES ('%s', '%s', '%s')", email, username, password);
        getContext().getDBConnector().executeUpdate(sql);
    }

    public JSONObject login(String email, String password) throws ServerException, SQLException {
        JSONObject response = new JSONObject();
        ResultSet result = getContext().getDBConnector().executeQuery(
                String.format("SELECT uid FROM user WHERE email = '%s' AND password = '%s'", email, password));
        if (result.next()) {
            int uid = result.getInt("uid");
            String authorization = Encoder.encode(email + System.currentTimeMillis());
            getContext().getDBConnector().executeUpdate(
                    String.format("UPDATE user SET authorization = '%s' WHERE uid = '%d'", authorization, uid));
            response.put("uid", uid);
            response.put("authorization", authorization);
            return response;
        } else {
            return response;
        }
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

    public JSONObject listPost(int uid, int page) throws SQLException {
        String sql = String.format(
                "SELECT \n" +
                        "\tpost.pid, \n" +
                        "    post.uid, \n" +
                        "    user.username, \n" +
                        "    post.timestamp, \n" +
                        "    post.category, \n" +
                        "    post.content, \n" +
                        "    hugs.hugs, \n" +
                        "    hugged.hugged, \n" +
                        "    belled.belled, \n" +
                        "    flagged.flagged\n" +
                        "FROM\n" +
                        "\tuser, post,\n" +
                        "\t(SELECT \n" +
                        "\t\tpost.pid, count(hug.hid) AS hugs \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN hug ON post.pid = hug.pid \n" +
                        "\tGROUP BY post.pid) AS hugs,\n" +
                        "    (SELECT \n" +
                        "\t\tpost.pid, count(hug.uid) AS hugged \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN hug ON post.pid = hug.pid AND hug.uid = %d\n" +
                        "\tGROUP BY post.pid) AS hugged,\n" +
                        "    (SELECT \n" +
                        "\t\tpost.pid, count(bell.uid) AS belled \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN bell ON post.pid = bell.pid AND bell.uid = %d\n" +
                        "\tGROUP BY post.pid) AS belled,\n" +
                        "    (SELECT \n" +
                        "\t\tpost.pid, count(flag.uid) AS flagged \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN flag ON post.pid = flag.pid AND flag.uid = %d\n" +
                        "\tGROUP BY post.pid) AS flagged\n" +
                        "WHERE\n" +
                        "\tpost.uid = user.uid \n" +
                        "    AND post.pid = hugs.pid \n" +
                        "    AND post.pid = hugged.pid \n" +
                        "    AND post.pid = belled.pid \n" +
                        "    AND post.pid = flagged.pid\n" +
                        "ORDER BY post.pid DESC\n" +
                        "LIMIT %d, %d",
                uid, uid, uid, page * 10, 10);
        ResultSet result = getContext().getDBConnector().executeQuery(sql);
        JSONObject response = new JSONObject();
        JSONArray posts = createPostList(result);
        response.put("posts", posts);
        return response;
    }

    public JSONObject listPost(int uid, int page, int category) throws SQLException {
        String sql = String.format(
                "SELECT \n" +
                        "\tpost.pid, \n" +
                        "    post.uid, \n" +
                        "    user.username, \n" +
                        "    post.timestamp, \n" +
                        "    post.category, \n" +
                        "    post.content, \n" +
                        "    hugs.hugs, \n" +
                        "    hugged.hugged, \n" +
                        "    belled.belled, \n" +
                        "    flagged.flagged\n" +
                        "FROM\n" +
                        "\tuser, post,\n" +
                        "\t(SELECT \n" +
                        "\t\tpost.pid, count(hug.hid) AS hugs \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN hug ON post.pid = hug.pid \n" +
                        "\tGROUP BY post.pid) AS hugs,\n" +
                        "    (SELECT \n" +
                        "\t\tpost.pid, count(hug.uid) AS hugged \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN hug ON post.pid = hug.pid AND hug.uid = %d\n" +
                        "\tGROUP BY post.pid) AS hugged,\n" +
                        "    (SELECT \n" +
                        "\t\tpost.pid, count(bell.uid) AS belled \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN bell ON post.pid = bell.pid AND bell.uid = %d\n" +
                        "\tGROUP BY post.pid) AS belled,\n" +
                        "    (SELECT \n" +
                        "\t\tpost.pid, count(flag.uid) AS flagged \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN flag ON post.pid = flag.pid AND flag.uid = %d\n" +
                        "\tGROUP BY post.pid) AS flagged\n" +
                        "WHERE\n" +
                        "\tpost.uid = user.uid \n" +
                        "    AND post.pid = hugs.pid \n" +
                        "    AND post.pid = hugged.pid \n" +
                        "    AND post.pid = belled.pid \n" +
                        "    AND post.pid = flagged.pid\n" +
                        "    AND post.category = %d\n" +
                        "ORDER BY post.pid DESC\n" +
                        "LIMIT %d, %d",
                uid, uid, uid, category, page * 10, 10);
        ResultSet result = getContext().getDBConnector().executeQuery(sql);
        JSONObject response = new JSONObject();
        JSONArray posts = createPostList(result);
        response.put("posts", posts);
        return response;
    }

    public JSONObject listAttention(int uid, int page) throws SQLException {
        String sql = String.format(
                "SELECT \n" +
                        "\tpost.pid, \n" +
                        "    post.uid, \n" +
                        "    user.username, \n" +
                        "    post.timestamp, \n" +
                        "    post.category, \n" +
                        "    post.content, \n" +
                        "    hugs.hugs, \n" +
                        "    hugged.hugged, \n" +
                        "    belled.belled, \n" +
                        "    flagged.flagged\n" +
                        "FROM\n" +
                        "\tuser, post,\n" +
                        "\t(SELECT \n" +
                        "\t\tpost.pid, count(hug.hid) AS hugs \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN hug ON post.pid = hug.pid \n" +
                        "\tGROUP BY post.pid) AS hugs,\n" +
                        "    (SELECT \n" +
                        "\t\tpost.pid, count(bell.bid) AS bells \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN bell ON post.pid = bell.pid \n" +
                        "\tGROUP BY post.pid) AS bells,\n" +
                        "    (SELECT \n" +
                        "\t\tpost.pid, count(hug.uid) AS hugged \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN hug ON post.pid = hug.pid AND hug.uid = %d\n" +
                        "\tGROUP BY post.pid) AS hugged,\n" +
                        "    (SELECT \n" +
                        "\t\tpost.pid, count(bell.uid) AS belled \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN bell ON post.pid = bell.pid AND bell.uid = %d\n" +
                        "\tGROUP BY post.pid) AS belled,\n" +
                        "    (SELECT \n" +
                        "\t\tpost.pid, count(flag.uid) AS flagged \n" +
                        "\tFROM \n" +
                        "\t\tpost LEFT OUTER JOIN flag ON post.pid = flag.pid AND flag.uid = %d\n" +
                        "\tGROUP BY post.pid) AS flagged\n" +
                        "WHERE\n" +
                        "\tpost.uid = user.uid \n" +
                        "    AND post.pid = hugs.pid \n" +
                        "    AND post.pid = bells.pid\n" +
                        "    AND post.pid = hugged.pid \n" +
                        "    AND post.pid = belled.pid \n" +
                        "    AND post.pid = flagged.pid\n" +
                        "    AND bells.bells >= 2\n" +
                        "ORDER BY post.pid DESC\n" +
                        "LIMIT %d, %d",
                uid, uid, uid, page * 10, 10);
        ResultSet result = getContext().getDBConnector().executeQuery(sql);
        JSONObject response = new JSONObject();
        JSONArray posts = createPostList(result);
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
            post.put("hugged", result.getInt("hugged"));
            post.put("belled", result.getInt("belled"));
            post.put("flagged", result.getInt("flagged"));
            posts.put(post);
        }
        return posts;
    }

}
