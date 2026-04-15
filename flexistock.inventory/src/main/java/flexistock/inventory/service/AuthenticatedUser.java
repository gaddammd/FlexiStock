package flexistock.inventory.service;

import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String name,
        String email,
        String role,
        String token
) {
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
