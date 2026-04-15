package flexistock.users.controller;

import flexistock.users.dto.response.LoginHistoryItemDto;
import flexistock.users.service.AuthenticationService;
import flexistock.users.service.LoginHistoryService;
import flexistock.users.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
public class LoginHistoryController {
    private static final Logger logger = LoggerFactory.getLogger(LoginHistoryController.class);

    private final LoginHistoryService loginHistoryService;
    private final AuthenticationService authenticationService;
    private final UserService userService;

    public LoginHistoryController(
            LoginHistoryService loginHistoryService,
            AuthenticationService authenticationService,
            UserService userService
    ) {
        this.loginHistoryService = loginHistoryService;
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @GetMapping("/login-history")
    public List<LoginHistoryItemDto> getLoginHistory(@RequestHeader("X-Auth-Token") String token) {
        UUID requesterId = authenticationService.getUserIdFromToken(token);
        if (!userService.isAdmin(requesterId)) {
            logger.warn("Login history access denied for userId={}", requesterId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        logger.debug("Login history requested by admin userId={}", requesterId);
        return loginHistoryService.getLoginHistory();
    }
}
