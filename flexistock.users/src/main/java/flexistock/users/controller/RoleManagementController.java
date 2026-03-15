package flexistock.users.controller;

import flexistock.users.dto.User;
import flexistock.users.dto.response.UserActionResponseDto;
import flexistock.users.service.AuthenticationService;
import flexistock.users.service.RoleManagementService;
import flexistock.users.service.UserService;
import flexistock.users.util.UserViewMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
public class RoleManagementController {
    private final RoleManagementService roleManagementService;
    private final AuthenticationService authenticationService;
    private final UserService userService;

    public RoleManagementController(
            RoleManagementService roleManagementService,
            AuthenticationService authenticationService,
            UserService userService
    ) {
        this.roleManagementService = roleManagementService;
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @PostMapping("/request-admin-access")
    public UserActionResponseDto requestAdminAccess(@RequestHeader("X-Auth-Token") String token) {
        UUID requesterId = authenticationService.getUserIdFromToken(token);
        User updated = roleManagementService.requestAdminAccess(requesterId);
        return new UserActionResponseDto(true, "Admin access request submitted", UserViewMapper.toResponse(updated));
    }

    @PutMapping("/update-user-role/{id}")
    public UserActionResponseDto updateUserRole(
            @PathVariable UUID id,
            @RequestHeader("X-Auth-Token") String token,
            @RequestBody UpdateRoleRequest request
    ) {
        UUID requesterId = authenticationService.getUserIdFromToken(token);
        requireAdmin(requesterId);

        User updated = roleManagementService.updateUserRole(id, request.approve());
        return new UserActionResponseDto(
                true,
                request.approve() ? "Admin access approved" : "Admin access rejected",
                UserViewMapper.toResponse(updated)
        );
    }

    private void requireAdmin(UUID userId) {
        if (!userService.isAdmin(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    public record UpdateRoleRequest(boolean approve) {
    }
}
