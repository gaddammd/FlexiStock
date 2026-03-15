package flexistock.users.controller;

import flexistock.users.dto.User;
import flexistock.users.dto.response.MessageResponseDto;
import flexistock.users.dto.response.UserActionResponseDto;
import flexistock.users.dto.response.UserResponseDto;
import flexistock.users.service.AuthenticationService;
import flexistock.users.service.UserService;
import flexistock.users.util.UserViewMapper;
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
        return userService.getAllUsers().stream().map(UserViewMapper::toResponse).toList();
    }

    @GetMapping("/users/{id}")
    public UserResponseDto getUser(@PathVariable UUID id, @RequestHeader("X-Auth-Token") String token) {
        UUID requesterId = authenticationService.getUserIdFromToken(token);
        requireOwnerOrAdmin(requesterId, id);
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

        User updated = userService.updateUser(id, request.name(), request.email(), request.password());
        return new UserActionResponseDto(true, "User updated successfully", UserViewMapper.toResponse(updated));
    }

    @DeleteMapping("/users/{id}")
    public MessageResponseDto deleteUser(@PathVariable UUID id, @RequestHeader("X-Auth-Token") String token) {
        UUID requesterId = authenticationService.getUserIdFromToken(token);
        requireOwnerOrAdmin(requesterId, id);
        userService.deleteUser(id);
        return new MessageResponseDto(true, "User deleted successfully");
    }

    private void requireAdmin(UUID userId) {
        if (!userService.isAdmin(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    private void requireOwnerOrAdmin(UUID requesterId, UUID targetUserId) {
        if (!requesterId.equals(targetUserId) && !userService.isAdmin(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions");
        }
    }

    public record UpdateUserRequest(String name, String email, String password) {
    }
}
