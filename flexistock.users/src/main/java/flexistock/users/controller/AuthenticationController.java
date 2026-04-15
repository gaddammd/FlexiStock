package flexistock.users.controller;

import flexistock.users.dto.User;
import flexistock.users.dto.request.LoginRequestDto;
import flexistock.users.dto.request.LogoutRequestDto;
import flexistock.users.dto.request.SignupRequestDto;
import flexistock.users.dto.response.AuthResponseDto;
import flexistock.users.dto.response.UserValidationResponseDto;
import flexistock.users.dto.response.UserActionResponseDto;
import flexistock.users.service.AuthenticationService;
import flexistock.users.util.UserViewMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserActionResponseDto signup(@RequestBody SignupRequestDto request) {
        logger.info("Signup request received for email={}", request.getEmail());
        User created = authenticationService.signup(request.getName(), request.getEmail(), request.getPassword());
        return new UserActionResponseDto(true, "User registered successfully", UserViewMapper.toResponse(created));
    }

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody LoginRequestDto request) {
        logger.info("Login request received for email={}", request.getEmail());
        String token = authenticationService.login(request.getEmail(), request.getPassword());
        return new AuthResponseDto(true, "Login successful", token);
    }

    @PostMapping("/logout")
    public AuthResponseDto logout(@RequestBody LogoutRequestDto request) {
        logger.info("Logout request received");
        authenticationService.logout(request.getToken());
        return new AuthResponseDto(true, "Logout successful", null);
    }

    @GetMapping("/auth/validate")
    public UserValidationResponseDto validate(@RequestHeader("X-Auth-Token") String token) {
        logger.debug("Auth validation request received");
        User authenticated = authenticationService.getAuthenticatedUser(token);
        return new UserValidationResponseDto(
                authenticated.getId(),
                authenticated.getName(),
                authenticated.getEmail(),
                authenticated.getRole(),
                authenticated.isAdminAccessRequested()
        );
    }
}
