package flexistock.inventory.controller;

import flexistock.inventory.dto.request.TestEmailRequest;
import flexistock.inventory.dto.response.ApiMessageResponse;
import flexistock.inventory.service.TestEmailService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestEmailController {
    private final TestEmailService testEmailService;

    public TestEmailController(TestEmailService testEmailService) {
        this.testEmailService = testEmailService;
    }

    @PostMapping("/api/test-email")
    public ApiMessageResponse sendTestEmail(@Valid @RequestBody TestEmailRequest request) {
        testEmailService.send(request.to(), request.subject(), request.message());
        return new ApiMessageResponse(true, "Test email sent");
    }
}
