package edu.njit.buddy.server;

/**
 * @author toyknight 3/4/2016.
 */
public class HugBot {

    private final Context context;

    private final int bot_uid;
    private final int male_uid;
    private final int female_uid;

    public HugBot(Context context, int bot_uid, int male_uid, int female_uid) {
        this.context = context;
        this.bot_uid = bot_uid;
        this.male_uid = male_uid;
        this.female_uid = female_uid;
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

    }

}
