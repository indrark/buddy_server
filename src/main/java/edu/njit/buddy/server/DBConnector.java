package edu.njit.buddy.server;

import java.sql.*;

/**
 * @author toyknight 3/2/2016.
 */
public class DBConnector {

    private Connection connection;

    public void connect(String host, String name, String username, String password) throws SQLException {
        connection = DriverManager.getConnection(
                String.format("jdbc:mysql://%s/%s?user=%s&password=%s", host, name, username, password));
    }

    public Connection getConnection() {
        return connection;
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        Statement statement = getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        return statement.executeQuery(sql);
    }

    public int executeUpdate(String sql) throws SQLException {
        Statement statement = getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        return statement.executeUpdate(sql);
    }

}
