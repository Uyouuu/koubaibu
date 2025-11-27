package com.koubaibu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 在庫イベントエンティティ (監査ログ)
 * Inventory Event Entity (Audit Log)
 */
@Entity
@Table(name = "inventory_events")
public class InventoryEvent {

    public enum EventType {
        ADD,        // 商品追加
        UPDATE,     // 商品更新
        DELETE,     // 商品削除
        INCREASE,   // 在庫増加
        DECREASE,   // 在庫減少
        ALERT,      // アラート発生
        ALERT_CONFIG // アラート設定変更
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @NotBlank(message = "操作担当者名は必須です")
    @Size(min = 1, max = 64, message = "操作担当者名は1〜64文字で入力してください")
    @Column(name = "operator_name", nullable = false)
    private String operatorName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column
    private Integer delta;

    @NotNull
    @Column(name = "snapshot_stock", nullable = false)
    private Integer snapshotStock;

    @Size(max = 64)
    @Column(name = "request_id")
    private String requestId;

    @Column(columnDefinition = "CLOB")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public InventoryEvent() {
    }

    public InventoryEvent(Product product, String operatorName, EventType eventType, 
                          Integer delta, Integer snapshotStock, String requestId) {
        this.product = product;
        this.operatorName = operatorName;
        this.eventType = eventType;
        this.delta = delta;
        this.snapshotStock = snapshotStock;
        this.requestId = requestId;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    public Integer getSnapshotStock() {
        return snapshotStock;
    }

    public void setSnapshotStock(Integer snapshotStock) {
        this.snapshotStock = snapshotStock;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
