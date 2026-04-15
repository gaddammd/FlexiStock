package flexistock.inventory.repository.nosql;

import flexistock.inventory.entity.nosql.InventoryLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryLogDocumentRepository extends MongoRepository<InventoryLogDocument, String> {
    List<InventoryLogDocument> findByProductIdOrderByActionTimeDesc(UUID productId);
    List<InventoryLogDocument> findAllByOrderByActionTimeDesc();
}
