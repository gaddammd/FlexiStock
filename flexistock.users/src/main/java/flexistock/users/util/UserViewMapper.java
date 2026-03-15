package flexistock.users.util;

import flexistock.users.dto.User;
import flexistock.users.dto.response.UserResponseDto;

public final class UserViewMapper {
    private UserViewMapper() {
    }

    public static UserResponseDto toResponse(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName() == null ? "" : user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isAdminAccessRequested()
        );
    }
}
