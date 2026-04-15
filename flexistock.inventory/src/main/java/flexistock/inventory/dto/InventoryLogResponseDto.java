package flexistock.inventory.dto;

import java.time.Instant;
import java.util.UUID;

public record InventoryLogResponseDto(
        String id,
        UUID productId,
        String actionType,
        Integer quantityChange,
        Integer oldQuantity,
        Integer newQuantity,
        UUID performedBy,
        String notes,
        Instant actionTime
) {
}
