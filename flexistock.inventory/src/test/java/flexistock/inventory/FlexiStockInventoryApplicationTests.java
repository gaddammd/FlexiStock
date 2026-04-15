package flexistock.inventory;

import flexistock.inventory.repository.nosql.InventoryLogDocumentRepository;
import flexistock.inventory.repository.nosql.ProductDocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class FlexiStockInventoryApplicationTests {
    @MockitoBean
    private ProductDocumentRepository productDocumentRepository;

    @MockitoBean
    private InventoryLogDocumentRepository inventoryLogDocumentRepository;

    @Test
    void contextLoads() {
    }
}
