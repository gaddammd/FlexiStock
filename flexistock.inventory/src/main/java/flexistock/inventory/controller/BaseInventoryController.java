package flexistock.inventory.controller;

import flexistock.inventory.dto.InventoryLogResponseDto;
import flexistock.inventory.dto.ProductResponseDto;
import flexistock.inventory.dto.request.CreateProductRequest;
import flexistock.inventory.dto.request.QuantityAdjustmentRequest;
import flexistock.inventory.dto.request.UpdateProductRequest;
import flexistock.inventory.dto.response.ApiMessageResponse;
import flexistock.inventory.dto.response.ProductActionResponse;
import flexistock.inventory.service.AuthenticatedUser;
import flexistock.inventory.service.InventoryService;
import flexistock.inventory.service.UserValidationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

public abstract class BaseInventoryController {
    private static final Logger logger = LoggerFactory.getLogger(BaseInventoryController.class);

    private final InventoryService inventoryService;
    private final UserValidationService userValidationService;

    protected BaseInventoryController(InventoryService inventoryService, UserValidationService userValidationService) {
        this.inventoryService = inventoryService;
        this.userValidationService = userValidationService;
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductActionResponse createProduct(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody CreateProductRequest request
    ) {
        AuthenticatedUser user = requireAdmin(token);
        logger.info("Create product request received from userId={} sku={}", user.id(), request.sku());
        ProductResponseDto product = inventoryService.createProduct(request, user);
        return new ProductActionResponse(true, "Product created successfully", product);
    }

    @GetMapping("/products")
    public List<ProductResponseDto> getProducts(
            @RequestParam(name = "lowStockOnly", defaultValue = "false") boolean lowStockOnly,
            @RequestParam(name = "threshold", required = false) Integer threshold
    ) {
        logger.debug("Get products request received lowStockOnly={} threshold={}", lowStockOnly, threshold);
        if (!lowStockOnly) {
            return inventoryService.getProducts();
        }
        return inventoryService.getLowStockProducts(threshold == null ? 10 : threshold);
    }

    @GetMapping("/products/{id}")
    public ProductResponseDto getProduct(@PathVariable UUID id) {
        logger.debug("Get product request received productId={}", id);
        return inventoryService.getProduct(id);
    }

    @PutMapping("/products/{id}")
    public ProductActionResponse updateProduct(
            @PathVariable UUID id,
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        AuthenticatedUser user = requireAdmin(token);
        logger.info("Update product request received from userId={} productId={}", user.id(), id);
        ProductResponseDto product = inventoryService.updateProduct(id, request, user);
        return new ProductActionResponse(true, "Product updated successfully", product);
    }

    @PatchMapping("/products/{id}/quantity")
    public ProductActionResponse adjustQuantity(
            @PathVariable UUID id,
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody QuantityAdjustmentRequest request
    ) {
        AuthenticatedUser user = requireAuthenticated(token);
        logger.info("Adjust quantity request received from userId={} productId={} quantityChange={}", user.id(), id, request.quantityChange());
        ProductResponseDto product = inventoryService.adjustQuantity(id, request, user);
        return new ProductActionResponse(true, "Product quantity updated successfully", product);
    }

    @DeleteMapping("/products/{id}")
    public ApiMessageResponse deleteProduct(
            @PathVariable UUID id,
            @RequestHeader("X-Auth-Token") String token
    ) {
        AuthenticatedUser user = requireAdmin(token);
        logger.info("Delete product request received from userId={} productId={}", user.id(), id);
        inventoryService.deleteProduct(id, user);
        return new ApiMessageResponse(true, "Product deleted successfully");
    }

    @GetMapping("/inventory-logs")
    public List<InventoryLogResponseDto> getInventoryLogs(@RequestHeader("X-Auth-Token") String token) {
        AuthenticatedUser user = requireAdmin(token);
        logger.debug("Get inventory logs request received from admin userId={}", user.id());
        return inventoryService.getInventoryLogs();
    }

    @GetMapping("/inventory-logs/{productId}")
    public List<InventoryLogResponseDto> getInventoryLogsForProduct(
            @PathVariable UUID productId,
            @RequestHeader("X-Auth-Token") String token
    ) {
        AuthenticatedUser user = requireAuthenticated(token);
        logger.debug("Get inventory logs for product request received from userId={} productId={}", user.id(), productId);
        return inventoryService.getInventoryLogsForProduct(productId);
    }

    protected AuthenticatedUser requireAdmin(String token) {
        AuthenticatedUser user = requireAuthenticated(token);
        if (!user.isAdmin()) {
            logger.warn("Admin access denied for userId={} role={}", user.id(), user.role());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return user;
    }

    protected AuthenticatedUser requireAuthenticated(String token) {
        return userValidationService.validate(token);
    }
}
