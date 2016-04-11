package edu.njit.buddy.server;

import com.sun.mail.smtp.SMTPTransport;
import com.sun.net.ssl.internal.ssl.Provider;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.Security;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author toyknight 4/11/2016.
 */
public class MailSender {

    private final String GMAIL_USERNAME;
    private final String GMAIL_PASSWORD;

    private final ExecutorService executor;

    private final HashMap<String, Long> sending_time;

    private final Object SENDING_LOCK = new Object();

    public MailSender(String GMAIL_USERNAME, String GMAIL_PASSWORD) {
        this.GMAIL_USERNAME = GMAIL_USERNAME;
        this.GMAIL_PASSWORD = GMAIL_PASSWORD;
        this.executor = Executors.newSingleThreadExecutor();
        this.sending_time = new HashMap<>();

        Provider provider = new Provider();
        Security.addProvider(provider);

        Properties props = System.getProperties();
        props.setProperty("mail.smtps.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        props.setProperty("mail.smtps.auth", "true");
        props.setProperty("mail.smtps.quitwait", "false");
    }

    public boolean canSend(String recipient) {
        Long time = sending_time.get(recipient);
        return time == null || System.currentTimeMillis() - time > 60000;
    }

    public void sendMail(String recipient, String subject, String content) {
        synchronized (SENDING_LOCK) {
            if (canSend(recipient)) {
                SendTask task = new SendTask(recipient, subject, content);
                sending_time.put(recipient, System.currentTimeMillis());
                executor.submit(task);
            }
        }
    }

    public void clean() {
        synchronized (SENDING_LOCK) {
            HashSet<String> recipients = new HashSet<>();
            recipients.addAll(sending_time.keySet());
            for (String recipient : recipients) {
                if (System.currentTimeMillis() - sending_time.get(recipient) > 60000) {
                    sending_time.remove(recipient);
                }
            }
        }
    }

    public void stop() {
        executor.shutdown();
    }

    private class SendTask implements Runnable {

        private final String recipient;
        private final String subject;
        private final String content;

        public SendTask(String recipient, String subject, String content) {
            this.recipient = recipient;
            this.subject = subject;
            this.content = content;
        }

        @Override
        public void run() {
            try {
                Session session = Session.getInstance(System.getProperties(), null);
                final MimeMessage message = new MimeMessage(session);

                message.setFrom(new InternetAddress(String.format("%s@gmail.com", GMAIL_USERNAME)));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, false));

                message.setSubject(subject);
                message.setText(content, "UTF-8");
                message.setSentDate(new Date());

                SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");

                transport.connect("smtp.gmail.com", GMAIL_USERNAME, GMAIL_PASSWORD);
                transport.sendMessage(message, message.getAllRecipients());
                transport.close();
            } catch (MessagingException ex) {
                Logger.getLogger(MailSender.class.toString()).log(Level.SEVERE, ex.toString());
            }
        }

    }

}
