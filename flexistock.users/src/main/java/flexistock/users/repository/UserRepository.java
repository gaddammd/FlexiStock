package flexistock.users.repository;

import flexistock.users.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findByRoleIgnoreCase(String role);

    boolean existsByEmail(String email);
}
