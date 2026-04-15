package flexistock.inventory.controller;

import flexistock.inventory.service.UserValidationService;
import flexistock.inventory.service.nosql.NoSqlInventoryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nosql")
public class NoSqlInventoryController extends BaseInventoryController {
    public NoSqlInventoryController(NoSqlInventoryService inventoryService, UserValidationService userValidationService) {
        super(inventoryService, userValidationService);
    }
}
