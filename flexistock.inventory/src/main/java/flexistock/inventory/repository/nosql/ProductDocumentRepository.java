package flexistock.inventory.repository.nosql;

import flexistock.inventory.entity.nosql.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductDocumentRepository extends MongoRepository<ProductDocument, UUID> {
    boolean existsBySku(String sku);
    Optional<ProductDocument> findBySku(String sku);
    List<ProductDocument> findByQuantityLessThanEqual(Integer threshold);
}
