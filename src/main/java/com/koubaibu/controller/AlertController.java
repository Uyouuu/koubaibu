package com.koubaibu.controller;

import com.koubaibu.dto.*;
import com.koubaibu.entity.AlertHistory;
import com.koubaibu.entity.AlertSettings;
import com.koubaibu.service.AlertService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * アラートAPI コントローラ
 * Alert API Controller
 */
@RestController
@RequestMapping("/api")
public class AlertController {

    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);

    private final AlertService alertService;

    @Autowired
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * アラート設定を取得
     * GET /api/alert-settings
     */
    @GetMapping("/alert-settings")
    public ResponseEntity<AlertSettingsResponse> getAlertSettings(HttpServletRequest request) {
        String requestId = getOrCreateRequestId(request);
        MDC.put("requestId", requestId);
        
        try {
            logger.info("Getting alert settings");
            
            AlertSettings settings = alertService.getAlertSettings();
            return ResponseEntity.ok()
                    .header("X-Request-ID", requestId)
                    .body(new AlertSettingsResponse(settings));
        } finally {
            MDC.remove("requestId");
        }
    }

    /**
     * アラート設定を更新
     * PUT /api/alert-settings
     */
    @PutMapping("/alert-settings")
    public ResponseEntity<?> updateAlertSettings(
            @Valid @RequestBody AlertSettingsRequest settingsRequest,
            HttpServletRequest request) {
        
        String requestId = getOrCreateRequestId(request);
        MDC.put("requestId", requestId);
        
        try {
            logger.info("Updating alert settings: threshold={}, operator={}", 
                        settingsRequest.getThreshold(), settingsRequest.getOperatorName());
            
            AlertSettings settings = alertService.updateAlertSettings(settingsRequest, requestId);
            return ResponseEntity.ok()
                    .header("X-Request-ID", requestId)
                    .body(new AlertSettingsResponse(settings));
        } catch (Exception e) {
            logger.error("Failed to update alert settings: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .header("X-Request-ID", requestId)
                    .body(new ErrorResponse(e.getMessage()));
        } finally {
            MDC.remove("requestId");
        }
    }

    /**
     * アラート履歴を取得
     * GET /api/alert-history
     */
    @GetMapping("/alert-history")
    public ResponseEntity<Page<AlertHistoryResponse>> getAlertHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest request) {
        
        String requestId = getOrCreateRequestId(request);
        MDC.put("requestId", requestId);
        
        try {
            logger.info("Getting alert history: page={}, size={}", page, size);
            
            Pageable pageable = PageRequest.of(page, Math.min(size, 100));
            Page<AlertHistory> history = alertService.getAlertHistory(pageable);
            Page<AlertHistoryResponse> response = history.map(AlertHistoryResponse::new);
            
            return ResponseEntity.ok()
                    .header("X-Request-ID", requestId)
                    .body(response);
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
     * アラート設定レスポンス用内部クラス
     */
    public static class AlertSettingsResponse {
        private Long id;
        private Integer threshold;

        public AlertSettingsResponse(AlertSettings settings) {
            this.id = settings.getId();
            this.threshold = settings.getThreshold();
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getThreshold() {
            return threshold;
        }

        public void setThreshold(Integer threshold) {
            this.threshold = threshold;
        }
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
