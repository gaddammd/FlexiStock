package flexistock.inventory.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ProductResponseDto(
        UUID id,
        String sku,
        String name,
        String description,
        String category,
        Integer quantity,
        String location,
        Integer lowStockThreshold,
        Map<String, String> attributes,
        UUID createdBy,
        UUID updatedBy,
        Instant createdAt,
        Instant updatedAt
) {
}
