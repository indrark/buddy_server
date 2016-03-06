package edu.njit.buddy.server;

import edu.njit.buddy.server.service.*;
import org.glassfish.grizzly.http.server.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author toyknight 2/29/2016.
 */
public class BuddyServer implements Context {

    private static final Logger logger = Logger.getLogger(BuddyServer.class.getName());

    private final Object SERVER_LOCK = new Object();

    private HttpServer server;

    private DBConnector db_connector;

    private DBManager db_manager;

    private HugBot hug_bot;

    private int current_test_group;

    public void initialize() throws IOException, SQLException, ServerException {
        Properties properties = loadConfiguration();

        int server_port = Integer.parseInt(properties.getProperty("SERVER_PORT", "80"));
        String doc_root = properties.getProperty("DOC_ROOT", "html/");
        this.server = HttpServer.createSimpleServer(doc_root, server_port);

        this.db_connector = new DBConnector();
        String database_host = properties.getProperty("DATABASE_HOST", "127.0.0.1");
        String database_name = properties.getProperty("DATABASE_NAME", "buddy");
        String database_timezone = properties.getProperty("DATABASE_TIMEZONE", "-5:00");
        String database_username = properties.getProperty("DATABASE_USERNAME", "super_buddy");
        String database_password = properties.getProperty("DATABASE_PASSWORD", "buddy_password");
        getDBConnector().connect(database_host, database_name, database_timezone, database_username, database_password);

        this.db_manager = new DBManager(this);
        this.current_test_group = getDBManager().getCurrentTestGroup();

        try {
            boolean bot_enabled = Boolean.parseBoolean(properties.getProperty("HUGBOT_ENABLED", "false"));

            int bot_uid = Integer.parseInt(properties.getProperty("HUGBOT_BOT_UID", "NULL"));
            int male_uid = Integer.parseInt(properties.getProperty("HUGBOT_MALE_UID", "NULL"));
            int female_uid = Integer.parseInt(properties.getProperty("HUGBOT_FEMALE_UID", "NULL"));
            this.hug_bot = new HugBot(this, bot_enabled, bot_uid, male_uid, female_uid);
        } catch (NumberFormatException ex) {
            throw new ServerException("Hug bot uid is not specified correctly", ex);
        }

        getHttpServer().getServerConfiguration().addHttpHandler(new RegisterService(this), "/register");
        getHttpServer().getServerConfiguration().addHttpHandler(new LoginService(this), "/login");
        getHttpServer().getServerConfiguration().addHttpHandler(new PostCreateService(this), "/post/create");
        getHttpServer().getServerConfiguration().addHttpHandler(new FlagService(this), "/post/flag");
        getHttpServer().getServerConfiguration().addHttpHandler(new BellService(this), "/post/bell");
        getHttpServer().getServerConfiguration().addHttpHandler(new HugService(this), "/post/hug");
        getHttpServer().getServerConfiguration().addHttpHandler(new HugListService(this), "/post/hug/list");
        getHttpServer().getServerConfiguration().addHttpHandler(new PostListService(this), "/post/list");
        getHttpServer().getServerConfiguration().addHttpHandler(new CommentCreateService(this), "/comment/create");
        getHttpServer().getServerConfiguration().addHttpHandler(new CommentListService(this), "/comment/list");
        getHttpServer().getServerConfiguration().addHttpHandler(new ProfileViewService(this), "/profile/view");
    }

    private Properties loadConfiguration() throws IOException {
        File configure_file = new File("server.conf");
        Properties properties = new Properties();
        properties.load(new FileInputStream(configure_file));
        return properties;
    }

    public void start() throws IOException, InterruptedException {
        server.start();
        hug_bot.start();
        synchronized (SERVER_LOCK) {
            SERVER_LOCK.wait();
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public HttpServer getHttpServer() {
        return server;
    }

    @Override
    public DBManager getDBManager() {
        return db_manager;
    }

    @Override
    public DBConnector getDBConnector() {
        return db_connector;
    }

    @Override
    public int getNextTestGroup() {
        return current_test_group < 2 ? current_test_group + 1 : 0;
    }

    public static void main(String[] args) {
        Runnable server_runner = new Runnable() {
            @Override
            public void run() {
                try {
                    BuddyServer server = new BuddyServer();
                    server.initialize();
                    server.start();
                } catch (InterruptedException ex) {
                    logger.log(Level.SEVERE, "Server was interrupted.");
                } catch (ServerException | IOException ex) {
                    logger.log(Level.SEVERE, "Error starting server: " + ex.toString());
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error connecting to database: " + ex.toString());
                }
            }
        };
        new Thread(server_runner, "server-launch-thread").start();
    }

}
