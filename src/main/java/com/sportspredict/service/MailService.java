package com.sportspredict.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Deliberately simple mail option. If app.mail.enabled=false (default) or no SMTP host is
 * configured, messages are written to the log instead of being sent.
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final ObjectProvider<JavaMailSender> sender;
    private final boolean enabled;
    private final String from;

    public MailService(ObjectProvider<JavaMailSender> sender,
                       @Value("${app.mail.enabled:false}") boolean enabled,
                       @Value("${app.mail.from:noreply@sports-predict.local}") String from) {
        this.sender = sender;
        this.enabled = enabled;
        this.from = from;
    }

    public boolean isLive() {
        return enabled && sender.getIfAvailable() != null;
    }

    /** Sends to each recipient separately. Returns how many were sent (or logged). */
    public int send(Collection<String> recipients, String subject, String body) {
        JavaMailSender s = sender.getIfAvailable();
        int count = 0;
        for (String to : recipients) {
            if (enabled && s != null) {
                try {
                    SimpleMailMessage msg = new SimpleMailMessage();
                    msg.setFrom(from);
                    msg.setTo(to);
                    msg.setSubject(subject);
                    msg.setText(body);
                    s.send(msg);
                    count++;
                } catch (Exception ex) {
                    log.warn("Could not send mail to {}: {}", to, ex.getMessage());
                }
            } else {
                log.info("[MAIL DISABLED] to={} subject='{}' body='{}'", to, subject, body);
                count++;
            }
        }
        return count;
    }
}
