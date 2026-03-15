package flexistock.users.controller;

import flexistock.users.dto.User;
import flexistock.users.dto.request.LoginRequestDto;
import flexistock.users.dto.request.LogoutRequestDto;
import flexistock.users.dto.request.SignupRequestDto;
import flexistock.users.dto.response.AuthResponseDto;
import flexistock.users.dto.response.UserActionResponseDto;
import flexistock.users.service.AuthenticationService;
import flexistock.users.util.UserViewMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserActionResponseDto signup(@RequestBody SignupRequestDto request) {
        User created = authenticationService.signup(request.getName(), request.getEmail(), request.getPassword());
        return new UserActionResponseDto(true, "User registered successfully", UserViewMapper.toResponse(created));
    }

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody LoginRequestDto request) {
        String token = authenticationService.login(request.getEmail(), request.getPassword());
        return new AuthResponseDto(true, "Login successful", token);
    }

    @PostMapping("/logout")
    public AuthResponseDto logout(@RequestBody LogoutRequestDto request) {
        authenticationService.logout(request.getToken());
        return new AuthResponseDto(true, "Logout successful", null);
    }
}
