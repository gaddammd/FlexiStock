package flexistock.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String description,
        @NotBlank String category,
        @NotNull @Min(0) Integer quantity,
        String location,
        @NotNull @Min(0) Integer lowStockThreshold,
        Map<String, String> attributes
) {
}
