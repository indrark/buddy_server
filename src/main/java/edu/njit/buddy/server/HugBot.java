package edu.njit.buddy.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

/**
 * @author toyknight 3/4/2016.
 */
public class HugBot {

    private final Context context;

    private final int bot_uid;
    private final int male_uid;
    private final int female_uid;

    private final Timer bot_timer;
    private final Timer human_timer;

    public HugBot(Context context, int bot_uid, int male_uid, int female_uid) {
        this.context = context;
        this.bot_uid = bot_uid;
        this.male_uid = male_uid;
        this.female_uid = female_uid;
        this.bot_timer = new Timer();
        this.human_timer = new Timer();
    }

    public Context getContext() {
        return context;
    }

    public int getBotUID() {
        return bot_uid;
    }

    public int getMaleUID() {
        return male_uid;
    }

    public int getFemaleUID() {
        return female_uid;
    }

    public void start() {
        bot_timer.scheduleAtFixedRate(bot_task, 1000, 60000);
        human_timer.scheduleAtFixedRate(human_task, 2000, 600000);
    }

    private void serviceBot() throws SQLException {
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
                                "    hugged.hugged = 0",
                        getBotUID(), 0));
        while (result.next()) {
            getContext().getDBManager().hug(getBotUID(), result.getInt("pid"));
        }
    }

    private void serviceHuman() throws SQLException {
        ResultSet result = getContext().getDBConnector().executeQuery(
                String.format("SELECT\n" +
                        "\tpost.pid\n" +
                        "FROM\n" +
                        "\tpost, user,\n" +
                        "    (SELECT\n" +
                        "\t\tpost.pid, count(hug.hid) AS hugged\n" +
                        "\t FROM post LEFT OUTER JOIN hug ON post.pid = hug.pid AND (hug.uid = %d OR hug.uid = %d)\n" +
                        "\t GROUP BY post.pid) AS hugged\n" +
                        "WHERE\n" +
                        "\tpost.pid = hugged.pid AND\n" +
                        "    post.uid = user.uid AND\n" +
                        "    user.test_group = %d AND\n" +
                        "    hugged.hugged = 0", getMaleUID(), getFemaleUID(), 1));
        int select = new Random().nextInt(2);
        while (result.next()) {
            if(select == 1) {
                getContext().getDBManager().hug(getMaleUID(), result.getInt("pid"));
            } else {
                getContext().getDBManager().hug(getFemaleUID(), result.getInt("pid"));
            }
        }
    }

    private final TimerTask bot_task = new TimerTask() {
        @Override
        public void run() {
            try {
                serviceBot();
            } catch (SQLException ex) {
                getContext().getLogger().log(Level.SEVERE, "Regular bot service error: " + ex.toString());
            }
        }
    };

    private final TimerTask human_task = new TimerTask() {
        @Override
        public void run() {
            try {
                serviceHuman();
            } catch (SQLException ex) {
                getContext().getLogger().log(Level.SEVERE, "Human bot service error: " + ex.toString());
            }
        }
    };

}
