package flexistock.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EntityScan(basePackages = "flexistock.inventory.entity.sql")
@EnableJpaRepositories(basePackages = "flexistock.inventory.repository.sql")
@EnableMongoRepositories(basePackages = "flexistock.inventory.repository.nosql")
public class FlexiStockInventoryApplication {
    private static final Logger logger = LoggerFactory.getLogger(FlexiStockInventoryApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FlexiStockInventoryApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStartup() {
        logger.info("FlexiStock Inventory service started successfully");
    }
}
