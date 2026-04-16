package flexistock.inventory.repository.nosql;

import flexistock.inventory.entity.nosql.ReceiptDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReceiptDocumentRepository extends MongoRepository<ReceiptDocument, String> {
    List<ReceiptDocument> findAllByOrderByUploadedAtDesc();
}
