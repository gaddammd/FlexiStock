package flexistock.inventory.dto.response;

import java.util.UUID;

public record UserValidationResponse(
        UUID id,
        String name,
        String email,
        String role,
        boolean adminAccessRequested
) {
}
