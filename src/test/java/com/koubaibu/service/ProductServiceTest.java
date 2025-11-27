package com.koubaibu.service;

import com.koubaibu.dto.ProductCreateRequest;
import com.koubaibu.dto.StockChangeRequest;
import com.koubaibu.entity.Product;
import com.koubaibu.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private String requestId;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID().toString();
    }

    @Test
    void testCreateProduct() {
        ProductCreateRequest request = new ProductCreateRequest(
            "テスト商品",
            BigDecimal.valueOf(100),
            10,
            "テスト担当者"
        );

        Product product = productService.createProduct(request, requestId);

        assertNotNull(product.getId());
        assertEquals("テスト商品", product.getName());
        assertEquals(BigDecimal.valueOf(100).setScale(2), product.getPrice().setScale(2));
        assertEquals(10, product.getStockQuantity());
    }

    @Test
    void testCreateProductDuplicateName() {
        ProductCreateRequest request1 = new ProductCreateRequest(
            "重複テスト",
            BigDecimal.valueOf(100),
            10,
            "テスト担当者"
        );
        productService.createProduct(request1, requestId);

        ProductCreateRequest request2 = new ProductCreateRequest(
            "重複テスト",
            BigDecimal.valueOf(200),
            20,
            "テスト担当者"
        );

        assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(request2, requestId);
        });
    }

    @Test
    void testIncreaseStock() {
        // Create a product first
        ProductCreateRequest createRequest = new ProductCreateRequest(
            "在庫増加テスト",
            BigDecimal.valueOf(100),
            5,
            "テスト担当者"
        );
        Product product = productService.createProduct(createRequest, requestId);

        // Increase stock
        StockChangeRequest changeRequest = new StockChangeRequest(3, "テスト担当者");
        Product updated = productService.increaseStock(product.getId(), changeRequest, requestId);

        assertEquals(8, updated.getStockQuantity());
    }

    @Test
    void testDecreaseStock() {
        // Create a product first
        ProductCreateRequest createRequest = new ProductCreateRequest(
            "在庫減少テスト",
            BigDecimal.valueOf(100),
            10,
            "テスト担当者"
        );
        Product product = productService.createProduct(createRequest, requestId);

        // Decrease stock
        StockChangeRequest changeRequest = new StockChangeRequest(4, "テスト担当者");
        Product updated = productService.decreaseStock(product.getId(), changeRequest, requestId);

        assertEquals(6, updated.getStockQuantity());
    }

    @Test
    void testDecreaseStockInsufficientStock() {
        // Create a product with low stock
        ProductCreateRequest createRequest = new ProductCreateRequest(
            "在庫不足テスト",
            BigDecimal.valueOf(100),
            3,
            "テスト担当者"
        );
        Product product = productService.createProduct(createRequest, requestId);

        // Try to decrease more than available
        StockChangeRequest changeRequest = new StockChangeRequest(5, "テスト担当者");

        assertThrows(IllegalStateException.class, () -> {
            productService.decreaseStock(product.getId(), changeRequest, requestId);
        });
    }

    @Test
    void testDeleteProductWithZeroStock() {
        // Create a product with zero stock
        ProductCreateRequest createRequest = new ProductCreateRequest(
            "削除テスト",
            BigDecimal.valueOf(100),
            0,
            "テスト担当者"
        );
        Product product = productService.createProduct(createRequest, requestId);

        // Delete should succeed
        assertDoesNotThrow(() -> {
            productService.deleteProduct(product.getId(), "テスト担当者", requestId);
        });

        // Verify deletion
        assertTrue(productService.getProductById(product.getId()).isEmpty());
    }

    @Test
    void testDeleteProductWithStock() {
        // Create a product with stock
        ProductCreateRequest createRequest = new ProductCreateRequest(
            "削除不可テスト",
            BigDecimal.valueOf(100),
            5,
            "テスト担当者"
        );
        Product product = productService.createProduct(createRequest, requestId);

        // Delete should fail
        assertThrows(IllegalStateException.class, () -> {
            productService.deleteProduct(product.getId(), "テスト担当者", requestId);
        });
    }
}
