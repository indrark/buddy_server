package edu.njit.buddy.server;

import edu.njit.buddy.server.service.*;
import org.glassfish.grizzly.http.server.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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

    public void initialize() throws IOException, SQLException, NoSuchAlgorithmException {
        Properties properties = loadConfiguration();

        int server_port = Integer.parseInt(properties.getProperty("SERVER_PORT", "80"));
        String doc_root = properties.getProperty("DOC_ROOT", "html/");
        this.server = HttpServer.createSimpleServer(doc_root, server_port);

        this.db_connector = new DBConnector();
        String database_host = properties.getProperty("DATABASE_HOST", "127.0.0.1");
        String database_name = properties.getProperty("DATABASE_NAME", "buddy");
        String database_username = properties.getProperty("DATABASE_USERNAME", "super_buddy");
        String database_password = properties.getProperty("DATABASE_PASSWORD", "buddy_password");
        getDBConnector().connect(database_host, database_name, database_username, database_password);

        this.db_manager = new DBManager(this);

        getHttpServer().getServerConfiguration().addHttpHandler(new RegisterService(this), "/register");
        getHttpServer().getServerConfiguration().addHttpHandler(new LoginService(this), "/login");
        getHttpServer().getServerConfiguration().addHttpHandler(new PostCreateService(this), "/post/create");
        getHttpServer().getServerConfiguration().addHttpHandler(new FlagService(this), "/post/flag");
        getHttpServer().getServerConfiguration().addHttpHandler(new BellService(this), "/post/bell");
        getHttpServer().getServerConfiguration().addHttpHandler(new HugService(this), "/post/hug");
        getHttpServer().getServerConfiguration().addHttpHandler(new PostListService(this), "/post/list");
    }

    private Properties loadConfiguration() throws IOException {
        File configure_file = new File("server.conf");
        Properties properties = new Properties();
        properties.load(new FileInputStream(configure_file));
        return properties;
    }

    public void start() throws IOException, InterruptedException {
        server.start();
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
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Error starting server: " + ex.toString());
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error connecting to database: " + ex.toString());
                } catch (NoSuchAlgorithmException ex) {
                    logger.log(Level.SEVERE, "MD5 encoding is not supported by local JRE.");
                }
            }
        };
        new Thread(server_runner, "server-launch-thread").start();
    }

}
