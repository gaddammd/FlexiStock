package flexistock.inventory.repository.sql;

import flexistock.inventory.entity.sql.InventoryLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryLogRepository extends JpaRepository<InventoryLogEntity, Long> {
    List<InventoryLogEntity> findByProductIdOrderByActionTimeDesc(UUID productId);
    List<InventoryLogEntity> findAllByOrderByActionTimeDesc();
}
