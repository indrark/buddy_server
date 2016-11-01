package edu.njit.buddy.server;

import edu.njit.buddy.server.exceptions.ServerException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

/**
 * @author toyknight 3/4/2016.
 */
public class BotManager {

    private final Context context;

    private final ArrayList<Bot> bots;

    public BotManager(Context context, ServerConfiguration.BotConfiguration configuration) {
        this.context = context;
        this.bots = new ArrayList<>();
        for (int index = 0; index < configuration.getBotCount(); index++) {
            bots.add(new Bot(
                    configuration.getUID(index), configuration.getTestGroup(index), configuration.getInterval(index)));
        }
    }

    public Context getContext() {
        return context;
    }

    public void start() {
        for (Bot bot : bots) {
            bot.start();
        }
    }

    public void stop() {
        for (Bot bot : bots) {
            bot.stop();
        }
    }

    private void service(int uid, int test_group) throws SQLException, ServerException {
        ResultSet result = getContext().getDBConnector().executeQuery(
                String.format("SELECT\n" +
                        "\tpost.pid\n" +
                        "FROM\n" +
                        "\tpost, user,\n" +
                        "    (SELECT\n" +
                        "\t\tpost.pid, count(hug.hid) AS hugged\n" +
                        "\t FROM post LEFT OUTER JOIN hug ON post.pid = hug.pid AND hug.uid = %d\n" +
                        "\t GROUP BY post.pid) AS hugged\n" +
                        "WHERE\n" +
                        "\tpost.pid = hugged.pid AND\n" +
                        "    post.uid = user.uid AND\n" +
                        "    user.test_group = %d AND\n" +
                        "    hugged.hugged = 0", uid, test_group));
        while (result.next()) {
            getContext().getDBManager().hug(uid, result.getInt("pid"));
        }
    }

    private class Bot {

        private final int uid;

        private final int test_group;

        private final long interval;

        private Timer timer;

        public Bot(int uid, int test_group, long interval) {
            this.uid = uid;
            this.test_group = test_group;
            this.interval = interval;
            this.timer = new Timer();
        }

        public void start() {
            timer.scheduleAtFixedRate(bot_task, interval, interval);
        }

        public void stop() {
            timer.cancel();
        }

        private final TimerTask bot_task = new TimerTask() {
            @Override
            public void run() {
                try {
                    service(uid, test_group);
                } catch (SQLException | ServerException ex) {
                    getContext().getLogger().log(Level.SEVERE, "Regular bot service error: " + ex.toString());
                }
            }
        };

    }

}
