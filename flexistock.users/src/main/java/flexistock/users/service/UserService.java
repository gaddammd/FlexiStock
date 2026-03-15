package flexistock.users.service;

import flexistock.users.dto.User;
import flexistock.users.entity.UserEntity;
import flexistock.users.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void seedAdminUser() {
        if (!userRepository.existsByEmail("admin@flexistock.local")) {
            UserEntity admin = new UserEntity();
            admin.setName("System Admin");
            admin.setEmail("admin@flexistock.local");
            admin.setPassword("admin123");
            admin.setRole(ROLE_ADMIN);
            admin.setAdminAccessRequested(false);
            userRepository.save(admin);
        }
    }

    public User createUser(String name, String email, String password) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        UserEntity entity = new UserEntity();
        entity.setName(name == null ? "" : name);
        entity.setEmail(email);
        entity.setPassword(password);
        entity.setRole(ROLE_USER);
        entity.setAdminAccessRequested(false);

        return toDto(userRepository.save(entity));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    public User getUser(UUID id) {
        return toDto(getUserEntity(id));
    }

    public User getUserByEmail(String email) {
        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return toDto(entity);
    }

    public User updateUser(UUID id, String name, String email, String password) {
        UserEntity existing = getUserEntity(id);

        if (email != null && !email.isBlank() && !email.equals(existing.getEmail()) && userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        if (name != null) {
            existing.setName(name);
        }
        if (email != null && !email.isBlank()) {
            existing.setEmail(email);
        }
        if (password != null && !password.isBlank()) {
            existing.setPassword(password);
        }

        return toDto(userRepository.save(existing));
    }

    public void deleteUser(UUID id) {
        UserEntity existing = getUserEntity(id);
        userRepository.delete(existing);
    }

    public User requestAdminAccess(UUID id) {
        UserEntity user = getUserEntity(id);
        user.setAdminAccessRequested(true);
        return toDto(userRepository.save(user));
    }

    public User updateUserRole(UUID id, boolean approve) {
        UserEntity user = getUserEntity(id);
        user.setRole(approve ? ROLE_ADMIN : ROLE_USER);
        user.setAdminAccessRequested(false);
        return toDto(userRepository.save(user));
    }

    public boolean isAdmin(UUID id) {
        return ROLE_ADMIN.equalsIgnoreCase(getUserEntity(id).getRole());
    }

    private UserEntity getUserEntity(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private User toDto(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getRole(),
                entity.isAdminAccessRequested()
        );
    }
}
