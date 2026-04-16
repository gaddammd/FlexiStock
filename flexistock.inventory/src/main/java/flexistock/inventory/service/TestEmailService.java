package flexistock.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
public class TestEmailService {
    private static final Logger logger = LoggerFactory.getLogger(TestEmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String fromAddress;
    private final String mailUsername;
    private final String mailPassword;

    public TestEmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${inventory.notifications.from-address:${spring.mail.username:no-reply@flexistock.local}}") String fromAddress,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.fromAddress = fromAddress;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
    }

    public void send(String to, String subject, String messageBody) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Mail sender is not configured");
        }
        if (mailUsername == null || mailUsername.isBlank() || mailPassword == null || mailPassword.isBlank()) {
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE,
                    "Mail credentials are missing. Set SPRING_MAIL_USERNAME and SPRING_MAIL_PASSWORD."
            );
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(messageBody);

        try {
            mailSender.send(message);
            logger.info("Test email sent to={} from={}", to, fromAddress);
        } catch (MailException ex) {
            logger.error("Test email failed to send to={} from={} reason={}", to, fromAddress, ex.getMessage());
            throw new ResponseStatusException(
                    BAD_GATEWAY,
                    "Email send failed. Check SPRING_MAIL_HOST, SPRING_MAIL_PORT, SPRING_MAIL_USERNAME, and SPRING_MAIL_PASSWORD.",
                    ex
            );
        }
    }
}
