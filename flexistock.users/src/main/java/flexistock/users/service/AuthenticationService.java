package flexistock.users.service;

import flexistock.users.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private static final String UC_MAIL_DOMAIN = "mail.uc.edu";
    private static final String UC_STUDENT_MAIL_DOMAIN = "ucmail.uc.edu";

    private final UserService userService;
    private final LoginHistoryService loginHistoryService;
    private final Map<String, UUID> userIdByToken = new ConcurrentHashMap<>();

    public AuthenticationService(UserService userService, LoginHistoryService loginHistoryService) {
        this.userService = userService;
        this.loginHistoryService = loginHistoryService;
    }

    public User signup(String name, String email, String password) {
        validateSignupEmailDomain(email);
        logger.debug("Creating user account for email={}", email);
        return userService.createUser(name, email, password);
    }

    public String login(String email, String password) {
        User user = userService.getUserByEmail(email);
        if (!user.getPassword().equals(password)) {
            logger.warn("Login failed due to invalid password for email={}", email);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = UUID.randomUUID().toString();
        userIdByToken.put(token, user.getId());
        loginHistoryService.recordLogin(user.getId(), user.getEmail(), Instant.now().toString());
        logger.info("User logged in successfully userId={} email={}", user.getId(), user.getEmail());
        return token;
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) {
            logger.warn("Logout failed because token was missing or blank");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
        }
        UUID removed = userIdByToken.remove(token);
        if (removed == null) {
            logger.warn("Logout failed because token was invalid or expired");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        logger.info("User logged out successfully userId={}", removed);
    }

    public UUID getUserIdFromToken(String token) {
        UUID userId = userIdByToken.get(token);
        if (userId == null) {
            logger.warn("Token validation failed because token was invalid or expired");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        logger.debug("Resolved userId={} from token", userId);
        return userId;
    }

    public User getAuthenticatedUser(String token) {
        UUID userId = getUserIdFromToken(token);
        logger.debug("Fetching authenticated user details for userId={}", userId);
        return userService.getUser(userId);
    }

    private void validateSignupEmailDomain(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        boolean allowedDomain = normalizedEmail.endsWith("@" + UC_MAIL_DOMAIN)
                || normalizedEmail.endsWith("@" + UC_STUDENT_MAIL_DOMAIN);

        if (!allowedDomain) {
            logger.warn("Signup rejected because email domain is not allowed email={}", email);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email must end with @mail.uc.edu or @ucmail.uc.edu"
            );
        }
    }
}
