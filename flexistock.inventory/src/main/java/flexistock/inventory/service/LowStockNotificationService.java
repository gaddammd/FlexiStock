package flexistock.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LowStockNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(LowStockNotificationService.class);

    private final AdminDirectoryService adminDirectoryService;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean enabled;
    private final String fromAddress;

    public LowStockNotificationService(
            AdminDirectoryService adminDirectoryService,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${inventory.notifications.low-stock.enabled:true}") boolean enabled,
            @Value("${inventory.notifications.from-address:${spring.mail.username:no-reply@flexistock.local}}") String fromAddress
    ) {
        this.adminDirectoryService = adminDirectoryService;
        this.mailSenderProvider = mailSenderProvider;
        this.enabled = enabled;
        this.fromAddress = fromAddress;
    }

    public void notifyIfThresholdCrossed(
            String token,
            String sku,
            String productName,
            int oldQuantity,
            int newQuantity,
            int lowStockThreshold
    ) {
        if (!enabled || oldQuantity <= lowStockThreshold || newQuantity > lowStockThreshold) {
            logger.debug(
                    "Low-stock notification not triggered for sku={} oldQuantity={} newQuantity={} threshold={} enabled={}",
                    sku,
                    oldQuantity,
                    newQuantity,
                    lowStockThreshold,
                    enabled
            );
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            logger.warn("Low-stock notification skipped for sku={} because no mail sender is configured", sku);
            return;
        }

        List<String> adminEmails = adminDirectoryService.getAdminEmails(token);
        if (adminEmails.isEmpty()) {
            logger.warn("Low-stock notification skipped for sku={} because no admin recipients were found", sku);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(adminEmails.toArray(String[]::new));
        message.setSubject("Low stock alert: " + productName + " (" + sku + ")");
        message.setText("""
                Product quantity has dropped below its configured low-stock threshold.

                Product: %s
                SKU: %s
                Previous quantity: %d
                Current quantity: %d
                Low-stock threshold: %d
                """.formatted(productName, sku, oldQuantity, newQuantity, lowStockThreshold));

        mailSender.send(message);
        logger.info(
                "Low-stock notification sent for sku={} productName={} recipients={} oldQuantity={} newQuantity={} threshold={}",
                sku,
                productName,
                adminEmails.size(),
                oldQuantity,
                newQuantity,
                lowStockThreshold
        );
    }
}
