package flexistock.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginHistoryItemDto {
    private UUID userId;
    private String email;
    private String loggedInAt;
}
