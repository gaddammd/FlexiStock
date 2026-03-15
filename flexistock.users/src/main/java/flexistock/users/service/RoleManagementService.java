package flexistock.users.service;

import flexistock.users.dto.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RoleManagementService {
    private final UserService userService;

    public RoleManagementService(UserService userService) {
        this.userService = userService;
    }

    public User requestAdminAccess(UUID userId) {
        return userService.requestAdminAccess(userId);
    }

    public User updateUserRole(UUID targetUserId, boolean approve) {
        return userService.updateUserRole(targetUserId, approve);
    }
}
