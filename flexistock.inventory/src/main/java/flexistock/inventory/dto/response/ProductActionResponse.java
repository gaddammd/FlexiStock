package flexistock.inventory.dto.response;

import flexistock.inventory.dto.ProductResponseDto;

public record ProductActionResponse(
        boolean success,
        String message,
        ProductResponseDto product
) {
}
