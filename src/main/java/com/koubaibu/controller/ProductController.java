package com.koubaibu.controller;

import com.koubaibu.dto.*;
import com.koubaibu.entity.Product;
import com.koubaibu.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 商品API コントローラ
 * Product API Controller
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * すべての商品を取得
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String keywords,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size,
            HttpServletRequest request) {
        
        String requestId = getOrCreateRequestId(request);
        MDC.put("requestId", requestId);
        
        try {
            logger.info("Getting products: keywords={}, page={}, size={}", keywords, page, size);
            
            Pageable pageable = PageRequest.of(page, Math.min(size, 500));
            Page<Product> products;
            
            if (keywords != null && !keywords.trim().isEmpty()) {
                products = productService.searchProducts(keywords, pageable);
            } else {
                products = productService.getProducts(pageable);
            }
            
            Page<ProductResponse> response = products.map(ProductResponse::new);
            return ResponseEntity.ok()
                    .header("X-Request-ID", requestId)
                    .body(response);
        } finally {
            MDC.remove("requestId");
        }
    }

    /**
     * 商品をIDで取得
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        String requestId = getOrCreateRequestId(request);
        MDC.put("requestId", requestId);
        
        try {
            logger.info("Getting product by id: {}", id);
            
            return productService.getProductById(id)
                    .map(product -> ResponseEntity.ok()
                            .header("X-Request-ID", requestId)
                            .body(new ProductResponse(product)))
                    .orElse(ResponseEntity.notFound()
                            .header("X-Request-ID", requestId)
                            .build());
        } finally {
            MDC.remove("requestId");
        }
    }

    /**
     * 新しい商品を作成
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            HttpServletRequest httpRequest) {
        
        String requestId = getOrCreateRequestId(httpRequest);
        MDC.put("requestId", requestId);
        
        try {
            logger.info("Creating product: name={}, operator={}", 
                        request.getName(), request.getOperatorName());
            
            Product product = productService.createProduct(request, requestId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("X-Request-ID", requestId)
                    .body(new ProductResponse(product));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Product creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .header("X-Request-ID", requestId)
                    .body(new ErrorResponse(e.getMessage()));
        } finally {
            MDC.remove("requestId");
        }
    }

    /**
     * 商品を更新
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request,
            HttpServletRequest httpRequest) {
        
        String requestId = getOrCreateRequestId(httpRequest);
        MDC.put("requestId", requestId);
        
        try {
            logger.info("Updating product: id={}, operator={}", id, request.getOperatorName());
            
            Product product = productService.updateProduct(id, request, requestId);
            return ResponseEntity.ok()
                    .header("X-Request-ID", requestId)
                    .body(new ProductResponse(product));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Product update failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .header("X-Request-ID", requestId)
                    .body(new ErrorResponse(e.getMessage()));
        } finally {
            MDC.remove("requestId");
        }
    }

    /**
     * 商品を削除
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable Long id,
            @RequestParam String operatorName,
            HttpServletRequest request) {
        
        String requestId = getOrCreateRequestId(request);
        MDC.put("requestId", requestId);
        
        try {
            logger.info("Deleting product: id={}, operator={}", id, operatorName);
            
            productService.deleteProduct(id, operatorName, requestId);
            return ResponseEntity.noContent()
                    .header("X-Request-ID", requestId)
                    .build();
            
        } catch (IllegalArgumentException e) {
            logger.warn("Product deletion failed: {}", e.getMessage());
            return ResponseEntity.notFound()
                    .header("X-Request-ID", requestId)
                    .build();
        } catch (IllegalStateException e) {
            logger.warn("Product deletion not allowed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("X-Request-ID", requestId)
                    .body(new ErrorResponse(e.getMessage()));
        } finally {
            MDC.remove("requestId");
        }
    }

    /**
     * 在庫を増加
     * POST /api/products/{id}/increase
     */
    @PostMapping("/{id}/increase")
    public ResponseEntity<?> increaseStock(
            @PathVariable Long id,
            @Valid @RequestBody StockChangeRequest request,
            HttpServletRequest httpRequest) {
        
        String requestId = getOrCreateRequestId(httpRequest);
        MDC.put("requestId", requestId);
        
        try {
            logger.info("Increasing stock: id={}, delta={}, operator={}", 
                        id, request.getDelta(), request.getOperatorName());
            
            Product product = productService.increaseStock(id, request, requestId);
            return ResponseEntity.ok()
                    .header("X-Request-ID", requestId)
                    .body(new ProductResponse(product));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Stock increase failed: {}", e.getMessage());
            return ResponseEntity.notFound()
                    .header("X-Request-ID", requestId)
                    .build();
        } catch (IllegalStateException e) {
            logger.warn("Stock increase conflict: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("X-Request-ID", requestId)
                    .body(new ErrorResponse(e.getMessage()));
        } finally {
            MDC.remove("requestId");
        }
    }

    /**
     * 在庫を減少
     * POST /api/products/{id}/decrease
     */
    @PostMapping("/{id}/decrease")
    public ResponseEntity<?> decreaseStock(
            @PathVariable Long id,
            @Valid @RequestBody StockChangeRequest request,
            HttpServletRequest httpRequest) {
        
        String requestId = getOrCreateRequestId(httpRequest);
        MDC.put("requestId", requestId);
        
        try {
            logger.info("Decreasing stock: id={}, delta={}, operator={}", 
                        id, request.getDelta(), request.getOperatorName());
            
            Product product = productService.decreaseStock(id, request, requestId);
            return ResponseEntity.ok()
                    .header("X-Request-ID", requestId)
                    .body(new ProductResponse(product));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Stock decrease failed: {}", e.getMessage());
            return ResponseEntity.notFound()
                    .header("X-Request-ID", requestId)
                    .build();
        } catch (IllegalStateException e) {
            logger.warn("Stock decrease conflict: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("X-Request-ID", requestId)
                    .body(new ErrorResponse(e.getMessage()));
        } finally {
            MDC.remove("requestId");
        }
    }

    /**
     * リクエストIDを取得または生成
     */
    private String getOrCreateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        return requestId;
    }

    /**
     * エラーレスポンス用内部クラス
     */
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
