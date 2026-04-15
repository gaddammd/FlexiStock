package flexistock.inventory.controller;

import flexistock.inventory.service.UserValidationService;
import flexistock.inventory.service.sql.SqlInventoryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sql")
public class SqlInventoryController extends BaseInventoryController {
    public SqlInventoryController(SqlInventoryService inventoryService, UserValidationService userValidationService) {
        super(inventoryService, userValidationService);
    }
}
