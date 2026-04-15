package flexistock.users.controller;

import flexistock.users.dto.User;
import flexistock.users.dto.response.MessageResponseDto;
import flexistock.users.dto.response.UserActionResponseDto;
import flexistock.users.dto.response.UserResponseDto;
import flexistock.users.service.AuthenticationService;
import flexistock.users.service.UserService;
import flexistock.users.util.UserViewMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final AuthenticationService authenticationService;

    public UserController(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/users")
    public List<UserResponseDto> getAllUsers(@RequestHeader("X-Auth-Token") String token) {
        UUID requesterId = authenticationService.getUserIdFromToken(token);
        requireAdmin(requesterId);
        logger.debug("All users requested by admin userId={}", requesterId);
        return userService.getAllUsers().stream().map(UserViewMapper::toResponse).toList();
    }

    @GetMapping("/users/admin-emails")
    public List<String> getAdminEmails(@RequestHeader("X-Auth-Token") String token) {
        UUID requesterId = authenticationService.getUserIdFromToken(token);
        logger.debug("Admin email recipient list requested by userId={}", requesterId);
        return userService.getAdminEmails();
    }

    @GetMapping("/users/{id}")
    public UserResponseDto getUser(@PathVariable UUID id, @RequestHeader("X-Auth-Token") String token) {
        UUID requesterId = authenticationService.getUserIdFromToken(token);
        requireOwnerOrAdmin(requesterId, id);
        logger.debug("User details requested by requesterId={} targetUserId={}", requesterId, id);
        return UserViewMapper.toResponse(userService.getUser(id));
    }

    @PutMapping("/users/{id}")
    public UserActionResponseDto updateUser(
            @PathVariable UUID id,
            @RequestHeader("X-Auth-Token") String token,
            @RequestBody UpdateUserRequest request
    ) {
        UUID requesterId = authenticationService.getUserIdFromToken(token);
        requireOwnerOrAdmin(requesterId, id);

        logger.info("User update requested by requesterId={} targetUserId={}", requesterId, id);
        User updated = userService.updateUser(id, request.name(), request.email(), request.password());
        return new UserActionResponseDto(true, "User updated successfully", UserViewMapper.toResponse(updated));
    }

    @DeleteMapping("/users/{id}")
    public MessageResponseDto deleteUser(@PathVariable UUID id, @RequestHeader("X-Auth-Token") String token) {
        UUID requesterId = authenticationService.getUserIdFromToken(token);
        requireOwnerOrAdmin(requesterId, id);
        logger.info("User delete requested by requesterId={} targetUserId={}", requesterId, id);
        userService.deleteUser(id);
        return new MessageResponseDto(true, "User deleted successfully");
    }

    private void requireAdmin(UUID userId) {
        if (!userService.isAdmin(userId)) {
            logger.warn("Admin user listing access denied for userId={}", userId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    private void requireOwnerOrAdmin(UUID requesterId, UUID targetUserId) {
        if (!requesterId.equals(targetUserId) && !userService.isAdmin(requesterId)) {
            logger.warn("User access denied requesterId={} targetUserId={}", requesterId, targetUserId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions");
        }
    }

    public record UpdateUserRequest(String name, String email, String password) {
    }
}
