package flexistock.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserValidationResponseDto {
    private UUID id;
    private String name;
    private String email;
    private String role;
    private boolean adminAccessRequested;
}
