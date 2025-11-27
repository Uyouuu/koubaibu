package com.koubaibu.service;

import com.koubaibu.dto.*;
import com.koubaibu.entity.InventoryEvent;
import com.koubaibu.entity.Product;
import com.koubaibu.repository.ProductRepository;
import com.koubaibu.repository.InventoryEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 商品サービス
 * Product Service - Handles product CRUD and inventory operations
 */
@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final InventoryEventRepository inventoryEventRepository;
    private final AlertService alertService;

    @Autowired
    public ProductService(ProductRepository productRepository,
                          InventoryEventRepository inventoryEventRepository,
                          AlertService alertService) {
        this.productRepository = productRepository;
        this.inventoryEventRepository = inventoryEventRepository;
        this.alertService = alertService;
    }

    /**
     * すべての商品を取得
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * ページング付きで商品を取得
     */
    @Transactional(readOnly = true)
    public Page<Product> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    /**
     * キーワードで商品を検索
     */
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.findByNameContaining(keyword.trim(), pageable);
    }

    /**
     * IDで商品を取得
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * 新しい商品を作成
     */
    public Product createProduct(ProductCreateRequest request, String requestId) {
        logger.info("Creating product: name={}, operator={}, requestId={}", 
                    request.getName(), request.getOperatorName(), requestId);

        // 商品名の重複チェック
        if (productRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("同じ名前の商品が既に存在します: " + request.getName());
        }

        Product product = new Product(request.getName(), request.getPrice(), request.getInitialStock());
        product = productRepository.save(product);

        // 監査ログを記録
        InventoryEvent event = new InventoryEvent(
                product,
                request.getOperatorName(),
                InventoryEvent.EventType.ADD,
                request.getInitialStock(),
                product.getStockQuantity(),
                requestId
        );
        inventoryEventRepository.save(event);

        // 閾値チェック
        alertService.checkAndAlert(product, request.getOperatorName(), requestId);

        logger.info("Product created successfully: id={}, name={}", product.getId(), product.getName());
        return product;
    }

    /**
     * 商品を更新
     */
    public Product updateProduct(Long id, ProductUpdateRequest request, String requestId) {
        logger.info("Updating product: id={}, operator={}, requestId={}", 
                    id, request.getOperatorName(), requestId);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません: ID=" + id));

        // 商品名の重複チェック（自身を除く）
        Optional<Product> existingProduct = productRepository.findByName(request.getName());
        if (existingProduct.isPresent() && !existingProduct.get().getId().equals(id)) {
            throw new IllegalArgumentException("同じ名前の商品が既に存在します: " + request.getName());
        }

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product = productRepository.save(product);

        // 監査ログを記録
        InventoryEvent event = new InventoryEvent(
                product,
                request.getOperatorName(),
                InventoryEvent.EventType.UPDATE,
                null,
                product.getStockQuantity(),
                requestId
        );
        inventoryEventRepository.save(event);

        logger.info("Product updated successfully: id={}, name={}", product.getId(), product.getName());
        return product;
    }

    /**
     * 商品を削除
     */
    public void deleteProduct(Long id, String operatorName, String requestId) {
        logger.info("Deleting product: id={}, operator={}, requestId={}", 
                    id, operatorName, requestId);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません: ID=" + id));

        // 在庫が0でない場合は削除不可
        if (product.getStockQuantity() > 0) {
            throw new IllegalStateException("在庫が残っている商品は削除できません。現在の在庫数: " + product.getStockQuantity());
        }

        // 監査ログを記録（削除前）
        InventoryEvent event = new InventoryEvent(
                null, // 削除後は商品参照なし
                operatorName,
                InventoryEvent.EventType.DELETE,
                null,
                0,
                requestId
        );
        event.setMetadata("{\"deletedProductId\":" + id + ",\"deletedProductName\":\"" + product.getName() + "\"}");
        inventoryEventRepository.save(event);

        productRepository.delete(product);
        logger.info("Product deleted successfully: id={}", id);
    }

    /**
     * 在庫を増加
     */
    public Product increaseStock(Long id, StockChangeRequest request, String requestId) {
        logger.info("Increasing stock: id={}, delta={}, operator={}, requestId={}", 
                    id, request.getDelta(), request.getOperatorName(), requestId);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません: ID=" + id));

        int updatedRows = productRepository.increaseStock(id, request.getDelta(), product.getVersion());
        if (updatedRows == 0) {
            throw new IllegalStateException("在庫更新に失敗しました。他のユーザーが同時に更新した可能性があります。");
        }

        // 更新後の商品を再取得
        product = productRepository.findById(id).orElseThrow();

        // 監査ログを記録
        InventoryEvent event = new InventoryEvent(
                product,
                request.getOperatorName(),
                InventoryEvent.EventType.INCREASE,
                request.getDelta(),
                product.getStockQuantity(),
                requestId
        );
        inventoryEventRepository.save(event);

        logger.info("Stock increased successfully: id={}, newStock={}", id, product.getStockQuantity());
        return product;
    }

    /**
     * 在庫を減少
     */
    public Product decreaseStock(Long id, StockChangeRequest request, String requestId) {
        logger.info("Decreasing stock: id={}, delta={}, operator={}, requestId={}", 
                    id, request.getDelta(), request.getOperatorName(), requestId);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません: ID=" + id));

        if (product.getStockQuantity() < request.getDelta()) {
            throw new IllegalStateException("在庫が不足しています。現在の在庫数: " + product.getStockQuantity() + ", 要求: " + request.getDelta());
        }

        int updatedRows = productRepository.decreaseStock(id, request.getDelta(), product.getVersion());
        if (updatedRows == 0) {
            throw new IllegalStateException("在庫更新に失敗しました。他のユーザーが同時に更新したか、在庫が不足しています。");
        }

        // 更新後の商品を再取得
        product = productRepository.findById(id).orElseThrow();

        // 監査ログを記録
        InventoryEvent event = new InventoryEvent(
                product,
                request.getOperatorName(),
                InventoryEvent.EventType.DECREASE,
                request.getDelta(),
                product.getStockQuantity(),
                requestId
        );
        inventoryEventRepository.save(event);

        // アラートチェック
        alertService.checkAndAlert(product, request.getOperatorName(), requestId);

        logger.info("Stock decreased successfully: id={}, newStock={}", id, product.getStockQuantity());
        return product;
    }
}
