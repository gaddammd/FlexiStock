package flexistock.inventory.exception;

import flexistock.inventory.dto.response.ApiMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiMessageResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Invalid request");
        logger.warn("Inventory validation failed: {}", message);
        return ResponseEntity.badRequest().body(new ApiMessageResponse(false, message));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiMessageResponse> handleDuplicateKey(DuplicateKeyException ex) {
        logger.warn("Inventory duplicate key error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiMessageResponse(false, "Product SKU already exists"));
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ApiMessageResponse> handleRemoteServiceError(RestClientResponseException ex) {
        logger.error("Inventory remote service error status={} message={}", ex.getStatusCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ApiMessageResponse(false, ex.getResponseBodyAsString().isBlank() ? "User service request failed" : ex.getResponseBodyAsString()));
    }
}
