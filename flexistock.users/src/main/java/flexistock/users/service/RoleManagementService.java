package flexistock.users.service;

import flexistock.users.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RoleManagementService {
    private static final Logger logger = LoggerFactory.getLogger(RoleManagementService.class);

    private final UserService userService;

    public RoleManagementService(UserService userService) {
        this.userService = userService;
    }

    public User requestAdminAccess(UUID userId) {
        logger.info("Processing admin access request for userId={}", userId);
        return userService.requestAdminAccess(userId);
    }

    public User updateUserRole(UUID targetUserId, boolean approve) {
        logger.info("Processing role update for targetUserId={} approve={}", targetUserId, approve);
        return userService.updateUserRole(targetUserId, approve);
    }
}
