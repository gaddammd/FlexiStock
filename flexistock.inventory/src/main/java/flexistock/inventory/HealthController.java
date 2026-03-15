package flexistock.inventory;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/")
    public String index() {
        return "FlexiStock Inventory service is running";
    }
}
