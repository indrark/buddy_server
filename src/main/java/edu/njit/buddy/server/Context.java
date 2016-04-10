package edu.njit.buddy.server;

import edu.njit.buddy.server.util.Encoder;
import org.glassfish.grizzly.http.server.HttpServer;

import java.util.logging.Logger;

/**
 * @author Leiping 3/2/2016.
 */
public interface Context {

    Logger getLogger();

    HttpServer getHttpServer();

    DBManager getDBManager();

    PasswordManager getPasswordManager();

    DBConnector getDBConnector();

    int getNextTestGroup();

}
