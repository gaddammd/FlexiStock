package flexistock.inventory.service.sql;

import flexistock.inventory.dto.InventoryLogResponseDto;
import flexistock.inventory.dto.ProductResponseDto;
import flexistock.inventory.dto.request.CreateProductRequest;
import flexistock.inventory.dto.request.QuantityAdjustmentRequest;
import flexistock.inventory.dto.request.UpdateProductRequest;
import flexistock.inventory.entity.sql.InventoryLogEntity;
import flexistock.inventory.entity.sql.ProductAttributeEntity;
import flexistock.inventory.entity.sql.ProductEntity;
import flexistock.inventory.repository.sql.InventoryLogRepository;
import flexistock.inventory.repository.sql.ProductRepository;
import flexistock.inventory.service.AuthenticatedUser;
import flexistock.inventory.service.InventoryService;
import flexistock.inventory.service.LowStockNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SqlInventoryService implements InventoryService {
    private static final Logger logger = LoggerFactory.getLogger(SqlInventoryService.class);

    private static final String CREATE = "CREATE";
    private static final String UPDATE = "UPDATE";
    private static final String DELETE = "DELETE";
    private static final String INCREASE = "INCREASE";
    private static final String DECREASE = "DECREASE";

    private final ProductRepository productRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final LowStockNotificationService lowStockNotificationService;

    public SqlInventoryService(
            ProductRepository productRepository,
            InventoryLogRepository inventoryLogRepository,
            LowStockNotificationService lowStockNotificationService
    ) {
        this.productRepository = productRepository;
        this.inventoryLogRepository = inventoryLogRepository;
        this.lowStockNotificationService = lowStockNotificationService;
    }

    @Override
    @Transactional
    public ProductResponseDto createProduct(CreateProductRequest request, AuthenticatedUser user) {
        if (productRepository.existsBySku(request.sku())) {
            logger.warn("Attempt to create duplicate SQL product sku={} by userId={}", request.sku(), user.id());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product SKU already exists");
        }

        ProductEntity entity = new ProductEntity();
        entity.setId(UUID.randomUUID());
        entity.setSku(request.sku().trim());
        entity.setName(request.name().trim());
        entity.setDescription(request.description());
        entity.setCategory(request.category().trim());
        entity.setQuantity(request.quantity());
        entity.setLocation(request.location());
        entity.setLowStockThreshold(request.lowStockThreshold());
        entity.setCreatedBy(user.id());
        entity.setUpdatedBy(user.id());
        replaceAttributes(entity, request.attributes());

        ProductEntity saved = productRepository.save(entity);
        saveLog(saved.getId(), CREATE, saved.getQuantity(), 0, saved.getQuantity(), user.id(), "Product created");
        logger.info("SQL product created productId={} sku={} by userId={}", saved.getId(), saved.getSku(), user.id());
        return toResponse(saved);
    }

    @Override
    public List<ProductResponseDto> getProducts() {
        logger.debug("Fetching all SQL products");
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<ProductResponseDto> getLowStockProducts(Integer threshold) {
        logger.debug("Fetching low-stock SQL products threshold={}", threshold);
        return productRepository.findByQuantityLessThanEqual(threshold).stream().map(this::toResponse).toList();
    }

    @Override
    public ProductResponseDto getProduct(UUID id) {
        logger.debug("Fetching SQL product productId={}", id);
        return toResponse(getProductEntity(id));
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(UUID id, UpdateProductRequest request, AuthenticatedUser user) {
        ProductEntity entity = getProductEntity(id);
        int oldQuantity = entity.getQuantity();

        if (request.name() != null) {
            entity.setName(request.name().trim());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.category() != null) {
            entity.setCategory(request.category().trim());
        }
        if (request.quantity() != null) {
            entity.setQuantity(request.quantity());
        }
        if (request.location() != null) {
            entity.setLocation(request.location());
        }
        if (request.lowStockThreshold() != null) {
            entity.setLowStockThreshold(request.lowStockThreshold());
        }
        if (request.attributes() != null) {
            replaceAttributes(entity, request.attributes());
        }

        entity.setUpdatedBy(user.id());
        ProductEntity saved = productRepository.save(entity);
        saveLog(saved.getId(), UPDATE, saved.getQuantity() - oldQuantity, oldQuantity, saved.getQuantity(), user.id(), "Product updated");
        logger.info("SQL product updated productId={} sku={} by userId={}", saved.getId(), saved.getSku(), user.id());
        notifyLowStockIfNeeded(user, saved.getSku(), saved.getName(), oldQuantity, saved.getQuantity(), saved.getLowStockThreshold());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponseDto adjustQuantity(UUID id, QuantityAdjustmentRequest request, AuthenticatedUser user) {
        ProductEntity entity = getProductEntity(id);
        int oldQuantity = entity.getQuantity();
        int newQuantity = oldQuantity + request.quantityChange();
        if (newQuantity < 0) {
            logger.warn("Rejected SQL quantity adjustment productId={} userId={} oldQuantity={} change={}", id, user.id(), oldQuantity, request.quantityChange());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity cannot be negative");
        }

        entity.setQuantity(newQuantity);
        entity.setUpdatedBy(user.id());
        ProductEntity saved = productRepository.save(entity);
        saveLog(saved.getId(), request.quantityChange() >= 0 ? INCREASE : DECREASE, request.quantityChange(), oldQuantity, newQuantity, user.id(), request.notes());
        logger.info("SQL quantity adjusted productId={} sku={} by userId={} oldQuantity={} newQuantity={}", saved.getId(), saved.getSku(), user.id(), oldQuantity, newQuantity);
        notifyLowStockIfNeeded(user, saved.getSku(), saved.getName(), oldQuantity, newQuantity, saved.getLowStockThreshold());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id, AuthenticatedUser user) {
        ProductEntity entity = getProductEntity(id);
        saveLog(entity.getId(), DELETE, -entity.getQuantity(), entity.getQuantity(), 0, user.id(), "Product deleted");
        productRepository.delete(entity);
        logger.info("SQL product deleted productId={} sku={} by userId={}", entity.getId(), entity.getSku(), user.id());
    }

    @Override
    public List<InventoryLogResponseDto> getInventoryLogs() {
        logger.debug("Fetching all SQL inventory logs");
        return inventoryLogRepository.findAllByOrderByActionTimeDesc().stream().map(this::toResponse).toList();
    }

    @Override
    public List<InventoryLogResponseDto> getInventoryLogsForProduct(UUID productId) {
        logger.debug("Fetching SQL inventory logs for productId={}", productId);
        return inventoryLogRepository.findByProductIdOrderByActionTimeDesc(productId).stream().map(this::toResponse).toList();
    }

    private ProductEntity getProductEntity(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("SQL product not found productId={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
                });
    }

    private void replaceAttributes(ProductEntity entity, Map<String, String> attributes) {
        entity.getAttributes().clear();
        Map<String, String> safeAttributes = attributes == null ? Map.of() : attributes;
        List<ProductAttributeEntity> updatedAttributes = new ArrayList<>();
        safeAttributes.forEach((key, value) -> {
            ProductAttributeEntity attribute = new ProductAttributeEntity();
            attribute.setProduct(entity);
            attribute.setAttributeKey(key);
            attribute.setAttributeValue(value);
            updatedAttributes.add(attribute);
        });
        entity.getAttributes().addAll(updatedAttributes);
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
        InventoryLogEntity log = new InventoryLogEntity();
        log.setProductId(productId);
        log.setActionType(actionType);
        log.setQuantityChange(quantityChange);
        log.setOldQuantity(oldQuantity);
        log.setNewQuantity(newQuantity);
        log.setPerformedBy(performedBy);
        log.setNotes(notes);
        inventoryLogRepository.save(log);
        logger.debug("SQL inventory log saved productId={} actionType={} quantityChange={} performedBy={}", productId, actionType, quantityChange, performedBy);
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

    private ProductResponseDto toResponse(ProductEntity entity) {
        Map<String, String> attributes = new LinkedHashMap<>();
        entity.getAttributes().forEach(attribute -> attributes.put(attribute.getAttributeKey(), attribute.getAttributeValue()));
        return new ProductResponseDto(
                entity.getId(),
                entity.getSku(),
                entity.getName(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getQuantity(),
                entity.getLocation(),
                entity.getLowStockThreshold(),
                attributes,
                entity.getCreatedBy(),
                entity.getUpdatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private InventoryLogResponseDto toResponse(InventoryLogEntity entity) {
        return new InventoryLogResponseDto(
                entity.getId().toString(),
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
