package flexistock.inventory.service;

import flexistock.inventory.dto.InventoryLogResponseDto;
import flexistock.inventory.dto.ProductResponseDto;
import flexistock.inventory.dto.request.CreateProductRequest;
import flexistock.inventory.dto.request.QuantityAdjustmentRequest;
import flexistock.inventory.dto.request.UpdateProductRequest;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
    ProductResponseDto createProduct(CreateProductRequest request, AuthenticatedUser user);
    List<ProductResponseDto> getProducts();
    List<ProductResponseDto> getLowStockProducts(Integer threshold);
    ProductResponseDto getProduct(UUID id);
    ProductResponseDto updateProduct(UUID id, UpdateProductRequest request, AuthenticatedUser user);
    ProductResponseDto adjustQuantity(UUID id, QuantityAdjustmentRequest request, AuthenticatedUser user);
    void deleteProduct(UUID id, AuthenticatedUser user);
    List<InventoryLogResponseDto> getInventoryLogs();
    List<InventoryLogResponseDto> getInventoryLogsForProduct(UUID productId);
}
