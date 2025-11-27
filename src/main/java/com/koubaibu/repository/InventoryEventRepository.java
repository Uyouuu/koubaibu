package com.koubaibu.repository;

import com.koubaibu.entity.InventoryEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 在庫イベントリポジトリ (監査ログ)
 * Inventory Event Repository (Audit Log)
 */
@Repository
public interface InventoryEventRepository extends JpaRepository<InventoryEvent, Long> {

    /**
     * 商品IDでイベントを検索
     */
    List<InventoryEvent> findByProductIdOrderByCreatedAtDesc(Long productId);

    /**
     * 操作担当者名でイベントを検索
     */
    Page<InventoryEvent> findByOperatorNameOrderByCreatedAtDesc(String operatorName, Pageable pageable);

    /**
     * イベントタイプで検索
     */
    Page<InventoryEvent> findByEventTypeOrderByCreatedAtDesc(InventoryEvent.EventType eventType, Pageable pageable);

    /**
     * 最新のイベントを取得
     */
    Page<InventoryEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 指定期間のイベントを検索
     */
    @Query("SELECT e FROM InventoryEvent e WHERE e.createdAt BETWEEN :start AND :end ORDER BY e.createdAt DESC")
    List<InventoryEvent> findByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * リクエストIDでイベントを検索
     */
    List<InventoryEvent> findByRequestId(String requestId);
}
