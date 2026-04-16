package flexistock.inventory.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReceiptResponseDto(
        String id,
        String fileName,
        String fileUrl,
        long fileSize,
        String storeName,
        String description,
        LocalDate receiptDate,
        UUID uploadedBy,
        Instant uploadedAt
) {
}
