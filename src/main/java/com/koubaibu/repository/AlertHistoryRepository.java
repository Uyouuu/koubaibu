package com.koubaibu.repository;

import com.koubaibu.entity.AlertHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * アラート履歴リポジトリ
 * Alert History Repository
 */
@Repository
public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Long> {

    /**
     * 商品IDでアラート履歴を検索
     */
    List<AlertHistory> findByProductIdOrderByAlertTimeDesc(Long productId);

    /**
     * 最新のアラート履歴を取得
     */
    Page<AlertHistory> findAllByOrderByAlertTimeDesc(Pageable pageable);

    /**
     * 指定期間のアラート履歴を検索
     */
    @Query("SELECT a FROM AlertHistory a WHERE a.alertTime BETWEEN :start AND :end ORDER BY a.alertTime DESC")
    List<AlertHistory> findByAlertTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 商品IDと期間で重複アラートをチェック（5分以内の重複通知抑制用）
     */
    @Query("SELECT COUNT(a) > 0 FROM AlertHistory a WHERE a.product.id = :productId AND a.alertTime > :since AND a.status = 'SENT'")
    boolean existsRecentAlert(@Param("productId") Long productId, @Param("since") LocalDateTime since);

    /**
     * 失敗したアラートを検索
     */
    List<AlertHistory> findByStatusOrderByAlertTimeDesc(AlertHistory.Status status);
}
