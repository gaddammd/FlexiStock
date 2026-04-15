package flexistock.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class FlexiStockUsersApplication {
    private static final Logger logger = LoggerFactory.getLogger(FlexiStockUsersApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FlexiStockUsersApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStartup() {
        logger.info("FlexiStock Users service started successfully");
    }
}
