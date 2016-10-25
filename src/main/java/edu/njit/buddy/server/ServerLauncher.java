package edu.njit.buddy.server;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * @author toyknight 10/24/2016.
 */
public class ServerLauncher {

    public static void main(String[] args) {
        try {
            File configuration_file = new File("config.json");
            ArrayList<ServerConfiguration> configurations = ServerConfiguration.load(configuration_file);
            for (ServerConfiguration configuration : configurations) {
                new Thread(new LaunchTask(configuration), "server-thread-" + configuration.getName()).start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static class LaunchTask implements Runnable {

        private final ServerConfiguration configuration;

        public LaunchTask(ServerConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void run() {
            BuddyServer server = new BuddyServer(configuration);
            try {
                server.initialize();
                server.start();
            } catch (InterruptedException ex) {
                server.getLogger().log(Level.SEVERE, "Server was interrupted.");
            } catch (ServerException | IOException ex) {
                server.getLogger().log(Level.SEVERE, "Error starting server: " + ex.toString());
            } catch (SQLException ex) {
                server.getLogger().log(Level.SEVERE, "Error connecting to database: " + ex.toString());
            }
        }
    }

}
