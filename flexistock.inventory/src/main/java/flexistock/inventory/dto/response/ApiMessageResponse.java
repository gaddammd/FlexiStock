package flexistock.inventory.dto.response;

public record ApiMessageResponse(
        boolean success,
        String message
) {
}
