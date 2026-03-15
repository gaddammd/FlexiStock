package flexistock.users.service;

import flexistock.users.dto.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthenticationService {
    private  UserService userService;
    private  LoginHistoryService loginHistoryService;
    private final Map<String, UUID> userIdByToken = new ConcurrentHashMap<>();

    public AuthenticationService(UserService userService, LoginHistoryService loginHistoryService) {
        this.userService = userService;
        this.loginHistoryService = loginHistoryService;
    }

    public User signup(String name, String email, String password) {
        return userService.createUser(name, email, password);
    }

    public String login(String email, String password) {
        User user = userService.getUserByEmail(email);
        if (!user.getPassword().equals(password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = UUID.randomUUID().toString();
        userIdByToken.put(token, user.getId());
        loginHistoryService.recordLogin(user.getId(), user.getEmail(), Instant.now().toString());
        return token;
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
        }
        UUID removed = userIdByToken.remove(token);
        if (removed == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    public UUID getUserIdFromToken(String token) {
        UUID userId = userIdByToken.get(token);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        return userId;
    }
}
