package flexistock.inventory.dto.request;

import java.util.ArrayList;
import java.util.List;

public record BulkCreateProductRequest(
        ArrayList<CreateProductRequest> requests
) {
}
