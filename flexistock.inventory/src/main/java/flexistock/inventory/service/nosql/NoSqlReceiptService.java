package flexistock.inventory.service.nosql;

import flexistock.inventory.dto.ReceiptResponseDto;
import flexistock.inventory.entity.nosql.ReceiptDocument;
import flexistock.inventory.repository.nosql.ReceiptDocumentRepository;
import flexistock.inventory.service.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class NoSqlReceiptService {
    private static final Logger logger = LoggerFactory.getLogger(NoSqlReceiptService.class);

    private final ReceiptDocumentRepository receiptRepository;

    public NoSqlReceiptService(ReceiptDocumentRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    public ReceiptResponseDto createReceipt(
            AuthenticatedUser user,
            String fileName,
            String contentType,
            long fileSize,
            byte[] fileData,
            String storeName,
            String description,
            LocalDate receiptDate
    ) {
        ReceiptDocument document = new ReceiptDocument();
        document.setFileName(fileName);
        document.setContentType(contentType);
        document.setFileSize(fileSize);
        document.setFileData(fileData);
        document.setStoreName(storeName);
        document.setDescription(description);
        document.setReceiptDate(receiptDate);
        document.setUploadedBy(user.id());
        document.setUploadedAt(Instant.now());

        ReceiptDocument saved = receiptRepository.save(document);
        logger.info("NoSQL receipt uploaded receiptId={} storeName={} uploadedBy={}", saved.getId(), saved.getStoreName(), user.id());
        return toResponse(saved);
    }

    public List<ReceiptResponseDto> getReceipts() {
        return receiptRepository.findAllByOrderByUploadedAtDesc().stream().map(this::toResponse).toList();
    }

    public ReceiptDocument getReceipt(String id) {
        return receiptRepository.findById(id).orElseThrow(() -> {
            logger.warn("NoSQL receipt not found receiptId={}", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Receipt not found");
        });
    }

    private ReceiptResponseDto toResponse(ReceiptDocument document) {
        return new ReceiptResponseDto(
                document.getId(),
                document.getFileName(),
                "/api/v1/nosql/receipts/" + document.getId() + "/file",
                document.getFileSize(),
                document.getStoreName(),
                document.getDescription(),
                document.getReceiptDate(),
                document.getUploadedBy(),
                document.getUploadedAt()
        );
    }
}
