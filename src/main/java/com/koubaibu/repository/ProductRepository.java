package com.koubaibu.repository;

import com.koubaibu.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商品リポジトリ
 * Product Repository
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 商品名で検索
     */
    Optional<Product> findByName(String name);

    /**
     * 商品名が存在するか確認
     */
    boolean existsByName(String name);

    /**
     * キーワードで商品を検索（商品名部分一致）
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> findByNameContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 在庫数が閾値以下の商品を検索
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold")
    List<Product> findByStockQuantityLessThanOrEqual(@Param("threshold") int threshold);

    /**
     * 在庫数を増加（楽観ロックで保護）
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :delta, p.version = p.version + 1, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id AND p.version = :version")
    int increaseStock(@Param("id") Long id, @Param("delta") int delta, @Param("version") Long version);

    /**
     * 在庫数を減少（楽観ロックで保護、0未満にならないよう制約）
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :delta, p.version = p.version + 1, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id AND p.version = :version AND p.stockQuantity >= :delta")
    int decreaseStock(@Param("id") Long id, @Param("delta") int delta, @Param("version") Long version);
}
