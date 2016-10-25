package edu.njit.buddy.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;

/**
 * @author toyknight 10/24/2016.
 */
public class ServerConfiguration {

    private final String name;

    private final int port;

    private final String doc_root;

    private final String database_host;

    private final String database_name;

    private final String database_username;

    private final String database_password;

    private final String database_timezone;

    private final String gmail_username;

    private final String gmail_password;

    private final BotConfiguration bot_configuration;

    private final long clean_rate;

    private final long verification_validity;

    private final long recovery_validity;

    private ServerConfiguration(String name,
                                int port,
                                String doc_root,
                                String database_host,
                                String database_name,
                                String database_username,
                                String database_password,
                                String database_timezone,
                                String gmail_username,
                                String gmail_password,
                                BotConfiguration bot_configuration,
                                long clean_rate,
                                long verification_validity,
                                long recovery_validity) {
        this.name = name;
        this.port = port;
        this.doc_root = doc_root;
        this.database_host = database_host;
        this.database_name = database_name;
        this.database_username = database_username;
        this.database_password = database_password;
        this.database_timezone = database_timezone;
        this.gmail_username = gmail_username;
        this.gmail_password = gmail_password;
        this.bot_configuration = bot_configuration;
        this.clean_rate = clean_rate;
        this.verification_validity = verification_validity;
        this.recovery_validity = recovery_validity;
    }

    public final String getName() {
        return name;
    }

    public final int getPort() {
        return port;
    }

    public final String getDocumentRoot() {
        return doc_root;
    }

    public final String getDatabaseHost() {
        return database_host;
    }

    public final String getDatabaseName() {
        return database_name;
    }

    public final String getDatabaseUsername() {
        return database_username;
    }

    public final String getDatabasePassword() {
        return database_password;
    }

    public final String getDatabaseTimezone() {
        return database_timezone;
    }

    public final String getGmailUsername() {
        return gmail_username;
    }

    public final String getGmailPassword() {
        return gmail_password;
    }

    public final BotConfiguration getBotConfiguration() {
        return bot_configuration;
    }

    public final long getCleanRate() {
        return clean_rate;
    }

    public final long getVerificationValidity() {
        return verification_validity;
    }

    public final long getRecoveryValidity() {
        return recovery_validity;
    }

    public static ArrayList<ServerConfiguration> load(File configuration_file) throws IOException, JSONException {
        ArrayList<ServerConfiguration> server_configurations = new ArrayList<>();

        String raw_configuration = read(configuration_file);
        JSONObject json_configuration = new JSONObject(raw_configuration);

        JSONArray json_servers = json_configuration.getJSONArray("servers");
        for (int server_index = 0; server_index < json_servers.length(); server_index++) {
            JSONObject json_server = json_servers.getJSONObject(server_index);
            String name = json_server.getString("name");
            int port = json_server.getInt("port");
            String doc_root = json_server.getString("doc_root");
            String database_host = json_server.getString("database_host");
            String database_name = json_server.getString("database_name");
            String database_username = json_server.getString("database_username");
            String database_password = json_server.getString("database_password");
            String database_timezone = json_server.getString("database_timezone");
            String gmail_username = json_server.getString("gmail_username");
            String gmail_password = json_server.getString("gmail_password");
            BotConfiguration bot_configuration = new BotConfiguration();
            JSONArray json_bots = json_server.getJSONArray("bots");
            for (int bot_index = 0; bot_index < json_bots.length(); bot_index++) {
                JSONObject json_bot = json_bots.getJSONObject(bot_index);
                bot_configuration.addBot(
                        json_bot.getInt("uid"), json_bot.getInt("test_group"), json_bot.getLong("interval"));
            }
            long clean_rate = json_server.getLong("clean_rate");
            long verification_validity = json_server.getLong("verification_validity");
            long recovery_validity = json_server.getLong("recovery_validity");
            server_configurations.add(new ServerConfiguration(
                    name,
                    port,
                    doc_root,
                    database_host,
                    database_name,
                    database_username,
                    database_password,
                    database_timezone,
                    gmail_username,
                    gmail_password,
                    bot_configuration,
                    clean_rate,
                    verification_validity,
                    recovery_validity));
        }
        return server_configurations;
    }

    private static String read(File configuration_file) throws IOException {
        FileInputStream fis = new FileInputStream(configuration_file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public static class BotConfiguration {

        private int bot_count = 0;

        private final ArrayList<Integer> uids = new ArrayList<>();

        private final ArrayList<Integer> test_groups = new ArrayList<>();

        private final ArrayList<Long> intervals = new ArrayList<>();

        private void addBot(int uid, int test_group, long interval) {
            uids.add(uid);
            test_groups.add(test_group);
            intervals.add(interval);
            bot_count++;
        }

        public final int getBotCount() {
            return bot_count;
        }

        public final int getUID(int index) {
            return uids.get(index);
        }

        public final int getTestGroup(int index) {
            return test_groups.get(index);
        }

        public final long getInterval(int index) {
            return intervals.get(index);
        }

    }

}
