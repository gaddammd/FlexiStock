package flexistock.inventory.dto.request;

import jakarta.validation.constraints.NotNull;

public record QuantityAdjustmentRequest(
        @NotNull Integer quantityChange,
        String notes
) {
}
