package com.koubaibu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koubaibu.dto.ProductCreateRequest;
import com.koubaibu.dto.StockChangeRequest;
import com.koubaibu.entity.Product;
import com.koubaibu.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Create a test product
        testProduct = new Product("APIテスト商品", BigDecimal.valueOf(150), 20);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void testGetAllProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetProductById() throws Exception {
        mockMvc.perform(get("/api/products/" + testProduct.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("APIテスト商品"))
            .andExpect(jsonPath("$.stockQuantity").value(20));
    }

    @Test
    void testGetProductByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/products/99999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testCreateProduct() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest(
            "新規API商品",
            BigDecimal.valueOf(200),
            15,
            "APIテスト担当者"
        );

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("新規API商品"))
            .andExpect(jsonPath("$.stockQuantity").value(15));
    }

    @Test
    void testCreateProductValidationError() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest(
            "",  // Empty name - should fail validation
            BigDecimal.valueOf(200),
            15,
            "APIテスト担当者"
        );

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testIncreaseStock() throws Exception {
        StockChangeRequest request = new StockChangeRequest(5, "APIテスト担当者");

        mockMvc.perform(post("/api/products/" + testProduct.getId() + "/increase")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockQuantity").value(25));
    }

    @Test
    void testDecreaseStock() throws Exception {
        StockChangeRequest request = new StockChangeRequest(5, "APIテスト担当者");

        mockMvc.perform(post("/api/products/" + testProduct.getId() + "/decrease")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockQuantity").value(15));
    }

    @Test
    void testDecreaseStockConflict() throws Exception {
        StockChangeRequest request = new StockChangeRequest(25, "APIテスト担当者");

        mockMvc.perform(post("/api/products/" + testProduct.getId() + "/decrease")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    void testDeleteProduct() throws Exception {
        // First set stock to 0
        testProduct.setStockQuantity(0);
        productRepository.save(testProduct);

        mockMvc.perform(delete("/api/products/" + testProduct.getId())
                .with(csrf())
                .param("operatorName", "APIテスト担当者"))
            .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteProductWithStockConflict() throws Exception {
        mockMvc.perform(delete("/api/products/" + testProduct.getId())
                .with(csrf())
                .param("operatorName", "APIテスト担当者"))
            .andExpect(status().isConflict());
    }
}
