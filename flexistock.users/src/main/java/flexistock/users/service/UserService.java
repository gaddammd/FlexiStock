package flexistock.users.service;

import flexistock.users.dto.User;
import flexistock.users.entity.UserEntity;
import flexistock.users.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

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
            logger.info("Seeded default admin user email=admin@flexistock.local");
        } else {
            logger.debug("Default admin user already exists");
        }
    }

    public User createUser(String name, String email, String password) {
        if (email == null || email.isBlank()) {
            logger.warn("User creation rejected because email was missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (password == null || password.isBlank()) {
            logger.warn("User creation rejected for email={} because password was missing", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        if (userRepository.existsByEmail(email)) {
            logger.warn("User creation rejected because email already exists email={}", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        UserEntity entity = new UserEntity();
        entity.setName(name == null ? "" : name);
        entity.setEmail(email);
        entity.setPassword(password);
        entity.setRole(ROLE_USER);
        entity.setAdminAccessRequested(false);

        User created = toDto(userRepository.save(entity));
        logger.info("User created userId={} email={}", created.getId(), created.getEmail());
        return created;
    }

    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<String> getAdminEmails() {
        logger.debug("Fetching admin email recipients");
        return userRepository.findByRoleIgnoreCase(ROLE_ADMIN).stream()
                .map(UserEntity::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .distinct()
                .toList();
    }

    public User getUser(UUID id) {
        logger.debug("Fetching user userId={}", id);
        return toDto(getUserEntity(id));
    }

    public User getUserByEmail(String email) {
        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User lookup failed for email={}", email);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });
        logger.debug("Resolved user by email={}", email);
        return toDto(entity);
    }

    public User updateUser(UUID id, String name, String email, String password) {
        UserEntity existing = getUserEntity(id);

        if (email != null && !email.isBlank() && !email.equals(existing.getEmail()) && userRepository.existsByEmail(email)) {
            logger.warn("User update rejected because email already exists targetUserId={} email={}", id, email);
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

        User updated = toDto(userRepository.save(existing));
        logger.info("User updated userId={} email={}", updated.getId(), updated.getEmail());
        return updated;
    }

    public void deleteUser(UUID id) {
        UserEntity existing = getUserEntity(id);
        userRepository.delete(existing);
        logger.info("User deleted userId={} email={}", existing.getId(), existing.getEmail());
    }

    public User requestAdminAccess(UUID id) {
        UserEntity user = getUserEntity(id);
        user.setAdminAccessRequested(true);
        User updated = toDto(userRepository.save(user));
        logger.info("Admin access requested by userId={} email={}", updated.getId(), updated.getEmail());
        return updated;
    }

    public User updateUserRole(UUID id, boolean approve) {
        UserEntity user = getUserEntity(id);
        user.setRole(approve ? ROLE_ADMIN : ROLE_USER);
        user.setAdminAccessRequested(false);
        User updated = toDto(userRepository.save(user));
        logger.info("User role updated userId={} role={}", updated.getId(), updated.getRole());
        return updated;
    }

    public boolean isAdmin(UUID id) {
        boolean admin = ROLE_ADMIN.equalsIgnoreCase(getUserEntity(id).getRole());
        logger.debug("Checked admin role userId={} isAdmin={}", id, admin);
        return admin;
    }

    private UserEntity getUserEntity(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found userId={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
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
