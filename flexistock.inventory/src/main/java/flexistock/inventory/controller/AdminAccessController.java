package flexistock.inventory.controller;

import flexistock.inventory.service.AdminAccessRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AdminAccessController {
    private final AdminAccessRequestService adminAccessRequestService;

    public AdminAccessController(AdminAccessRequestService adminAccessRequestService) {
        this.adminAccessRequestService = adminAccessRequestService;
    }

    @PostMapping({"/request-admin-access", "/api/request-admin-access"})
    public ResponseEntity<String> requestAdminAccess(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody(required = false) Map<String, Object> payload
    ) {
        return adminAccessRequestService.requestAdminAccess(token, payload);
    }
}
