package flexistock.inventory.service.nosql;

import flexistock.inventory.dto.InventoryLogResponseDto;
import flexistock.inventory.dto.ProductResponseDto;
import flexistock.inventory.dto.request.CreateProductRequest;
import flexistock.inventory.dto.request.QuantityAdjustmentRequest;
import flexistock.inventory.dto.request.UpdateProductRequest;
import flexistock.inventory.entity.nosql.InventoryLogDocument;
import flexistock.inventory.entity.nosql.ProductDocument;
import flexistock.inventory.repository.nosql.InventoryLogDocumentRepository;
import flexistock.inventory.repository.nosql.ProductDocumentRepository;
import flexistock.inventory.service.AuthenticatedUser;
import flexistock.inventory.service.InventoryService;
import flexistock.inventory.service.LowStockNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NoSqlInventoryService implements InventoryService {
    private static final Logger logger = LoggerFactory.getLogger(NoSqlInventoryService.class);

    private static final String CREATE = "CREATE";
    private static final String UPDATE = "UPDATE";
    private static final String DELETE = "DELETE";
    private static final String INCREASE = "INCREASE";
    private static final String DECREASE = "DECREASE";

    private final ProductDocumentRepository productRepository;
    private final InventoryLogDocumentRepository inventoryLogRepository;
    private final LowStockNotificationService lowStockNotificationService;

    public NoSqlInventoryService(
            ProductDocumentRepository productRepository,
            InventoryLogDocumentRepository inventoryLogRepository,
            LowStockNotificationService lowStockNotificationService
    ) {
        this.productRepository = productRepository;
        this.inventoryLogRepository = inventoryLogRepository;
        this.lowStockNotificationService = lowStockNotificationService;
    }

    @Override
    public ProductResponseDto createProduct(CreateProductRequest request, AuthenticatedUser user) {
        if (productRepository.existsBySku(request.sku())) {
            logger.warn("Attempt to create duplicate NoSQL product sku={} by userId={}", request.sku(), user.id());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product SKU already exists");
        }

        ProductDocument product = new ProductDocument();
        product.setId(UUID.randomUUID());
        product.setSku(request.sku().trim());
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setCategory(request.category().trim());
        product.setQuantity(request.quantity());
        product.setLocation(request.location());
        product.setLowStockThreshold(request.lowStockThreshold());
        product.setAttributes(new LinkedHashMap<>(request.attributes() == null ? Map.of() : request.attributes()));
        product.setCreatedBy(user.id());
        product.setUpdatedBy(user.id());
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(product.getCreatedAt());

        ProductDocument saved = productRepository.save(product);
        saveLog(saved.getId(), CREATE, saved.getQuantity(), 0, saved.getQuantity(), user.id(), "Product created");
        logger.info("NoSQL product created productId={} sku={} by userId={}", saved.getId(), saved.getSku(), user.id());
        return toResponse(saved);
    }

    @Override
    public List<ProductResponseDto> getProducts() {
        logger.debug("Fetching all NoSQL products");
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<ProductResponseDto> getLowStockProducts(Integer threshold) {
        logger.debug("Fetching low-stock NoSQL products threshold={}", threshold);
        return productRepository.findByQuantityLessThanEqual(threshold).stream().map(this::toResponse).toList();
    }

    @Override
    public ProductResponseDto getProduct(UUID id) {
        logger.debug("Fetching NoSQL product productId={}", id);
        return toResponse(getProductDocument(id));
    }

    @Override
    public ProductResponseDto updateProduct(UUID id, UpdateProductRequest request, AuthenticatedUser user) {
        ProductDocument product = getProductDocument(id);
        int oldQuantity = product.getQuantity();

        if (request.name() != null) {
            product.setName(request.name().trim());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.category() != null) {
            product.setCategory(request.category().trim());
        }
        if (request.quantity() != null) {
            product.setQuantity(request.quantity());
        }
        if (request.location() != null) {
            product.setLocation(request.location());
        }
        if (request.lowStockThreshold() != null) {
            product.setLowStockThreshold(request.lowStockThreshold());
        }
        if (request.attributes() != null) {
            product.setAttributes(new LinkedHashMap<>(request.attributes()));
        }

        product.setUpdatedBy(user.id());
        product.setUpdatedAt(Instant.now());
        ProductDocument saved = productRepository.save(product);
        saveLog(saved.getId(), UPDATE, saved.getQuantity() - oldQuantity, oldQuantity, saved.getQuantity(), user.id(), "Product updated");
        logger.info("NoSQL product updated productId={} sku={} by userId={}", saved.getId(), saved.getSku(), user.id());
        notifyLowStockIfNeeded(user, saved.getSku(), saved.getName(), oldQuantity, saved.getQuantity(), saved.getLowStockThreshold());
        return toResponse(saved);
    }

    @Override
    public ProductResponseDto adjustQuantity(UUID id, QuantityAdjustmentRequest request, AuthenticatedUser user) {
        ProductDocument product = getProductDocument(id);
        int oldQuantity = product.getQuantity();
        int newQuantity = oldQuantity + request.quantityChange();
        if (newQuantity < 0) {
            logger.warn("Rejected NoSQL quantity adjustment productId={} userId={} oldQuantity={} change={}", id, user.id(), oldQuantity, request.quantityChange());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity cannot be negative");
        }

        product.setQuantity(newQuantity);
        product.setUpdatedBy(user.id());
        product.setUpdatedAt(Instant.now());
        ProductDocument saved = productRepository.save(product);
        saveLog(saved.getId(), request.quantityChange() >= 0 ? INCREASE : DECREASE, request.quantityChange(), oldQuantity, newQuantity, user.id(), request.notes());
        logger.info("NoSQL quantity adjusted productId={} sku={} by userId={} oldQuantity={} newQuantity={}", saved.getId(), saved.getSku(), user.id(), oldQuantity, newQuantity);
        notifyLowStockIfNeeded(user, saved.getSku(), saved.getName(), oldQuantity, newQuantity, saved.getLowStockThreshold());
        return toResponse(saved);
    }

    @Override
    public void deleteProduct(UUID id, AuthenticatedUser user) {
        ProductDocument product = getProductDocument(id);
        saveLog(product.getId(), DELETE, -product.getQuantity(), product.getQuantity(), 0, user.id(), "Product deleted");
        productRepository.delete(product);
        logger.info("NoSQL product deleted productId={} sku={} by userId={}", product.getId(), product.getSku(), user.id());
    }

    @Override
    public List<InventoryLogResponseDto> getInventoryLogs() {
        logger.debug("Fetching all NoSQL inventory logs");
        return inventoryLogRepository.findAllByOrderByActionTimeDesc().stream().map(this::toResponse).toList();
    }

    @Override
    public List<InventoryLogResponseDto> getInventoryLogsForProduct(UUID productId) {
        logger.debug("Fetching NoSQL inventory logs for productId={}", productId);
        return inventoryLogRepository.findByProductIdOrderByActionTimeDesc(productId).stream().map(this::toResponse).toList();
    }

    private ProductDocument getProductDocument(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("NoSQL product not found productId={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
                });
    }

    private void saveLog(
            UUID productId,
            String actionType,
            Integer quantityChange,
            Integer oldQuantity,
            Integer newQuantity,
            UUID performedBy,
            String notes
    ) {
        InventoryLogDocument log = new InventoryLogDocument();
        log.setProductId(productId);
        log.setActionType(actionType);
        log.setQuantityChange(quantityChange);
        log.setOldQuantity(oldQuantity);
        log.setNewQuantity(newQuantity);
        log.setPerformedBy(performedBy);
        log.setNotes(notes);
        log.setActionTime(Instant.now());
        inventoryLogRepository.save(log);
        logger.debug("NoSQL inventory log saved productId={} actionType={} quantityChange={} performedBy={}", productId, actionType, quantityChange, performedBy);
    }

    private void notifyLowStockIfNeeded(
            AuthenticatedUser user,
            String sku,
            String productName,
            int oldQuantity,
            int newQuantity,
            int lowStockThreshold
    ) {
        lowStockNotificationService.notifyIfThresholdCrossed(
                user.token(),
                sku,
                productName,
                oldQuantity,
                newQuantity,
                lowStockThreshold
        );
    }

    private ProductResponseDto toResponse(ProductDocument product) {
        return new ProductResponseDto(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getQuantity(),
                product.getLocation(),
                product.getLowStockThreshold(),
                product.getAttributes(),
                product.getCreatedBy(),
                product.getUpdatedBy(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private InventoryLogResponseDto toResponse(InventoryLogDocument entity) {
        return new InventoryLogResponseDto(
                entity.getId(),
                entity.getProductId(),
                entity.getActionType(),
                entity.getQuantityChange(),
                entity.getOldQuantity(),
                entity.getNewQuantity(),
                entity.getPerformedBy(),
                entity.getNotes(),
                entity.getActionTime()
        );
    }
}
