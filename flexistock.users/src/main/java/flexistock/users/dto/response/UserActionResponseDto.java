package flexistock.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserActionResponseDto {
    private boolean success;
    private String message;
    private UserResponseDto user;
}
