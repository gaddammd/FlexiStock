package flexistock.inventory.repository.sql;

import flexistock.inventory.entity.sql.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    boolean existsBySku(String sku);
    Optional<ProductEntity> findBySku(String sku);
    List<ProductEntity> findByQuantityLessThanEqual(Integer threshold);
}
