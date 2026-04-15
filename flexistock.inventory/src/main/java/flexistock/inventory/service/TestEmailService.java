package flexistock.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
public class TestEmailService {
    private static final Logger logger = LoggerFactory.getLogger(TestEmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String fromAddress;

    public TestEmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${inventory.notifications.from-address:${spring.mail.username:no-reply@flexistock.local}}") String fromAddress
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.fromAddress = fromAddress;
    }

    public void send(String to, String subject, String messageBody) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Mail sender is not configured");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(messageBody);

        mailSender.send(message);
        logger.info("Test email sent to={} from={}", to, fromAddress);
    }
}
