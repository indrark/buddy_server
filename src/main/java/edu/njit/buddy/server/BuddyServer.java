package edu.njit.buddy.server;

import edu.njit.buddy.server.exceptions.ServerException;
import edu.njit.buddy.server.service.*;
import edu.njit.buddy.server.service.web.AdministratorLoginService;
import edu.njit.buddy.server.service.web.DashboardService;
import org.glassfish.grizzly.http.server.HttpServer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * @author toyknight 2/29/2016.
 */
public class BuddyServer implements Context {

    private static final Logger logger = Logger.getLogger(BuddyServer.class.getName());

    private final Object SERVER_LOCK = new Object();

    private final ServerConfiguration configuration;

    private HttpServer server;

    private DBConnector db_connector;

    private DBManager db_manager;

    private MailSender mail_sender;

    private TokenManager token_manager;

    private BotManager bot_manager;

    private Timer timer;

    private long CLEAN_RATE;

    private int current_test_group;

    public BuddyServer(ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    public void initialize() throws IOException, SQLException, ServerException {
        this.server = HttpServer.createSimpleServer("html", getConfiguration().getPort());

        this.db_connector = new DBConnector();
        getDBConnector().connect(
                getConfiguration().getDatabaseHost(),
                getConfiguration().getDatabaseName(),
                getConfiguration().getDatabaseTimezone(),
                getConfiguration().getDatabaseUsername(),
                getConfiguration().getDatabasePassword());

        this.db_manager = new DBManager(this);

        this.mail_sender = new MailSender(getConfiguration().getGmailUsername(), getConfiguration().getGmailPassword());

        this.CLEAN_RATE = getConfiguration().getCleanRate();

        long VERIFICATION_VALIDITY = getConfiguration().getVerificationValidity();
        long RECOVERY_VALIDITY = getConfiguration().getRecoveryValidity();
        this.token_manager = new TokenManager(VERIFICATION_VALIDITY, RECOVERY_VALIDITY);

        this.current_test_group = getDBManager().getCurrentTestGroup();
        this.bot_manager = new BotManager(this, getConfiguration().getBotConfiguration());

        this.timer = new Timer();

        //API Services
        getHttpServer().getServerConfiguration().addHttpHandler(new RegisterService(this), "/register");
        getHttpServer().getServerConfiguration().addHttpHandler(new LoginService(this), "/login");
        getHttpServer().getServerConfiguration().addHttpHandler(new PostCreateService(this), "/post/create");
        getHttpServer().getServerConfiguration().addHttpHandler(new PostDeleteService(this), "/post/delete");
        getHttpServer().getServerConfiguration().addHttpHandler(new FlagService(this), "/post/flag");
        getHttpServer().getServerConfiguration().addHttpHandler(new BellService(this), "/post/bell");
        getHttpServer().getServerConfiguration().addHttpHandler(new HugService(this), "/post/hug");
        getHttpServer().getServerConfiguration().addHttpHandler(new HugListService(this), "/post/hug/list");
        getHttpServer().getServerConfiguration().addHttpHandler(new PostListService(this), "/post/list");
        getHttpServer().getServerConfiguration().addHttpHandler(new CommentCreateService(this), "/comment/create");
        getHttpServer().getServerConfiguration().addHttpHandler(new CommentListService(this), "/comment/list");
        getHttpServer().getServerConfiguration().addHttpHandler(new ProfileViewService(this), "/profile/view");
        getHttpServer().getServerConfiguration().addHttpHandler(new ProfileEditService(this), "/profile/edit");
        getHttpServer().getServerConfiguration().addHttpHandler(new PasswordChangeService(this), "/password/change");
        getHttpServer().getServerConfiguration().addHttpHandler(new MailVerificationService(this), "/verification");
        getHttpServer().getServerConfiguration().addHttpHandler(new MoodSubmitService(this), "/mood/submit");
        getHttpServer().getServerConfiguration().addHttpHandler(new MoodListService(this), "/mood/list");
        getHttpServer().getServerConfiguration().addHttpHandler(new RecordService(this), "/record");
        getHttpServer().getServerConfiguration().addHttpHandler(new ServerStatusService(this), "/admin/status");

        //Web Services
        getHttpServer().getServerConfiguration().addHttpHandler(new AdministratorLoginService(this), "/admin/login");
        getHttpServer().getServerConfiguration().addHttpHandler(new DashboardService(this), "/admin/dashboard");
    }

    public void start() throws IOException, InterruptedException {
        server.start();
        bot_manager.start();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getMailSender().clean();
                getTokenManager().clean();
            }
        }, CLEAN_RATE, CLEAN_RATE);
        synchronized (SERVER_LOCK) {
            SERVER_LOCK.wait();
            stop();
        }
    }

    public void stop() {
        bot_manager.stop();
        mail_sender.stop();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return configuration;
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
    public MailSender getMailSender() {
        return mail_sender;
    }

    @Override
    public TokenManager getTokenManager() {
        return token_manager;
    }

    @Override
    public DBConnector getDBConnector() {
        return db_connector;
    }

    @Override
    public int getNextTestGroup() {
        current_test_group = current_test_group == 1 ? 0 : 1;
        return current_test_group;
    }

}
