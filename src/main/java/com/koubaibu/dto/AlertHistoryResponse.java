package com.koubaibu.dto;

import com.koubaibu.entity.AlertHistory;
import java.time.LocalDateTime;

/**
 * アラート履歴レスポンスDTO
 */
public class AlertHistoryResponse {

    private Long id;
    private Long productId;
    private String productName;
    private LocalDateTime alertTime;
    private String message;
    private String status;
    private String operatorName;
    private String requestId;

    public AlertHistoryResponse() {
    }

    public AlertHistoryResponse(AlertHistory alertHistory) {
        this.id = alertHistory.getId();
        this.productId = alertHistory.getProduct().getId();
        this.productName = alertHistory.getProduct().getName();
        this.alertTime = alertHistory.getAlertTime();
        this.message = alertHistory.getMessage();
        this.status = alertHistory.getStatus().name();
        this.operatorName = alertHistory.getOperatorName();
        this.requestId = alertHistory.getRequestId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
