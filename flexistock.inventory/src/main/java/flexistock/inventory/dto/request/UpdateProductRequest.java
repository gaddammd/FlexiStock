package flexistock.inventory.dto.request;

import jakarta.validation.constraints.Min;

import java.util.Map;

public record UpdateProductRequest(
        String name,
        String description,
        String category,
        @Min(0) Integer quantity,
        String location,
        @Min(0) Integer lowStockThreshold,
        Map<String, String> attributes
) {
}
