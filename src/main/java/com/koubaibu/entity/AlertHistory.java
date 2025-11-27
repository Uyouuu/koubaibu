package com.koubaibu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * アラート履歴エンティティ
 * Alert History Entity
 */
@Entity
@Table(name = "alert_history")
public class AlertHistory {

    public enum Status {
        SENT,
        FAILED,
        SUPPRESSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "alert_time", nullable = false)
    private LocalDateTime alertTime;

    @NotBlank(message = "メッセージは必須です")
    @Size(max = 255, message = "メッセージは255文字以内で入力してください")
    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.SENT;

    @Size(max = 64)
    @Column(name = "operator_name")
    private String operatorName;

    @Size(max = 64)
    @Column(name = "request_id")
    private String requestId;

    public AlertHistory() {
    }

    public AlertHistory(Product product, String message, String operatorName, String requestId) {
        this.product = product;
        this.message = message;
        this.alertTime = LocalDateTime.now();
        this.operatorName = operatorName;
        this.requestId = requestId;
        this.status = Status.SENT;
    }

    @PrePersist
    protected void onCreate() {
        if (alertTime == null) {
            alertTime = LocalDateTime.now();
        }
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

    public LocalDateTime getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(LocalDateTime alertTime) {
        this.alertTime = alertTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
