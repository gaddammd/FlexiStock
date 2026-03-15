package flexistock.users.service;

import flexistock.users.dto.response.LoginHistoryItemDto;
import flexistock.users.entity.LoginHistoryEntity;
import flexistock.users.repository.LoginHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class LoginHistoryService {
    private final LoginHistoryRepository loginHistoryRepository;

    public LoginHistoryService(LoginHistoryRepository loginHistoryRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
    }

    public void recordLogin(UUID userId, String email, String loggedInAt) {
        LoginHistoryEntity entry = new LoginHistoryEntity();
        entry.setUserId(userId);
        entry.setEmail(email);
        entry.setLoggedInAt(Instant.parse(loggedInAt));
        loginHistoryRepository.save(entry);
    }

    public List<LoginHistoryItemDto> getLoginHistory() {
        return loginHistoryRepository.findAll().stream()
                .map(entry -> new LoginHistoryItemDto(
                        entry.getUserId(),
                        entry.getEmail(),
                        entry.getLoggedInAt().toString()
                ))
                .toList();
    }
}
