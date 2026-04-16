package flexistock.inventory.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    private static final Logger logger = LoggerFactory.getLogger(MailConfig.class);

    @Bean
    JavaMailSender javaMailSender(
            @Value("${spring.mail.host:smtp.gmail.com}") String host,
            @Value("${spring.mail.port:587}") int port,
            @Value("${spring.mail.username:}") String username,
            @Value("${spring.mail.password:}") String password,
            @Value("${spring.mail.properties.mail.smtp.auth:true}") String smtpAuth,
            @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}") String startTlsEnable,
            @Value("${spring.mail.properties.mail.smtp.starttls.required:true}") String startTlsRequired,
            @Value("${spring.mail.properties.mail.smtp.connectiontimeout:5000}") String connectionTimeout,
            @Value("${spring.mail.properties.mail.smtp.timeout:5000}") String timeout,
            @Value("${spring.mail.properties.mail.smtp.writetimeout:5000}") String writeTimeout
    ) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        String normalizedHost = normalize(host);
        sender.setHost(normalizedHost);
        sender.setPort(port);
        sender.setUsername(normalize(username));
        sender.setPassword(normalizePassword(password, normalizedHost));

        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", smtpAuth);
        properties.setProperty("mail.smtp.starttls.enable", startTlsEnable);
        properties.setProperty("mail.smtp.starttls.required", startTlsRequired);
        properties.setProperty("mail.smtp.connectiontimeout", connectionTimeout);
        properties.setProperty("mail.smtp.timeout", timeout);
        properties.setProperty("mail.smtp.writetimeout", writeTimeout);
        sender.setJavaMailProperties(properties);

        return sender;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizePassword(String password, String host) {
        if (password == null) {
            return null;
        }

        String trimmed = password.trim();
        if (host != null && host.toLowerCase().contains("gmail.com")) {
            String normalized = trimmed.replaceAll("\\s+", "");
            if (!normalized.equals(trimmed)) {
                logger.warn("Whitespace was removed from Gmail SMTP password during normalization");
            }
            return normalized;
        }

        return trimmed;
    }
}
