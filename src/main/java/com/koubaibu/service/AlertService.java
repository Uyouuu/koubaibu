package com.koubaibu.service;

import com.koubaibu.dto.AlertSettingsRequest;
import com.koubaibu.entity.*;
import com.koubaibu.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * アラートサービス
 * Alert Service - Handles alert threshold checking and notifications
 */
@Service
@Transactional
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    private final AlertSettingsRepository alertSettingsRepository;
    private final AlertHistoryRepository alertHistoryRepository;
    private final InventoryEventRepository inventoryEventRepository;
    private final SlackNotificationService slackNotificationService;

    @Value("${slack.duplicate-suppression-minutes:5}")
    private int duplicateSuppressionMinutes;

    @Autowired
    public AlertService(AlertSettingsRepository alertSettingsRepository,
                        AlertHistoryRepository alertHistoryRepository,
                        InventoryEventRepository inventoryEventRepository,
                        SlackNotificationService slackNotificationService) {
        this.alertSettingsRepository = alertSettingsRepository;
        this.alertHistoryRepository = alertHistoryRepository;
        this.inventoryEventRepository = inventoryEventRepository;
        this.slackNotificationService = slackNotificationService;
    }

    /**
     * 現在のアラート設定を取得
     */
    @Transactional(readOnly = true)
    public AlertSettings getAlertSettings() {
        return alertSettingsRepository.getDefaultSettings();
    }

    /**
     * アラート閾値を更新
     */
    public AlertSettings updateAlertSettings(AlertSettingsRequest request, String requestId) {
        logger.info("Updating alert settings: threshold={}, operator={}, requestId={}",
                    request.getThreshold(), request.getOperatorName(), requestId);

        AlertSettings settings = alertSettingsRepository.getDefaultSettings();
        int oldThreshold = settings.getThreshold();
        settings.setThreshold(request.getThreshold());
        settings = alertSettingsRepository.save(settings);

        // 監査ログを記録
        InventoryEvent event = new InventoryEvent(
                null,
                request.getOperatorName(),
                InventoryEvent.EventType.ALERT_CONFIG,
                null,
                request.getThreshold(),
                requestId
        );
        event.setMetadata("{\"oldThreshold\":" + oldThreshold + ",\"newThreshold\":" + request.getThreshold() + "}");
        inventoryEventRepository.save(event);

        // Slack通知（設定変更）
        slackNotificationService.sendSettingsChangeNotification(oldThreshold, request.getThreshold(), 
                                                                 request.getOperatorName(), requestId);

        logger.info("Alert settings updated successfully: oldThreshold={}, newThreshold={}",
                    oldThreshold, request.getThreshold());
        return settings;
    }

    /**
     * アラート履歴を取得
     */
    @Transactional(readOnly = true)
    public Page<AlertHistory> getAlertHistory(Pageable pageable) {
        return alertHistoryRepository.findAllByOrderByAlertTimeDesc(pageable);
    }

    /**
     * 商品の在庫が閾値以下かチェックし、必要ならアラートを送信
     */
    public void checkAndAlert(Product product, String operatorName, String requestId) {
        AlertSettings settings = alertSettingsRepository.getDefaultSettings();
        int threshold = settings.getThreshold();

        if (product.getStockQuantity() <= threshold) {
            logger.info("Stock below threshold: product={}, stock={}, threshold={}",
                        product.getName(), product.getStockQuantity(), threshold);

            // 重複アラート抑制チェック
            LocalDateTime since = LocalDateTime.now().minusMinutes(duplicateSuppressionMinutes);
            if (alertHistoryRepository.existsRecentAlert(product.getId(), since)) {
                logger.info("Duplicate alert suppressed for product: {}", product.getName());
                
                // 抑制されたアラートも履歴に記録
                AlertHistory suppressedHistory = new AlertHistory(
                        product,
                        createAlertMessage(product, threshold),
                        operatorName,
                        requestId
                );
                suppressedHistory.setStatus(AlertHistory.Status.SUPPRESSED);
                alertHistoryRepository.save(suppressedHistory);
                return;
            }

            // アラートを送信
            sendAlert(product, threshold, operatorName, requestId);
        }
    }

    /**
     * アラートを送信
     */
    private void sendAlert(Product product, int threshold, String operatorName, String requestId) {
        String message = createAlertMessage(product, threshold);
        
        AlertHistory alertHistory = new AlertHistory(product, message, operatorName, requestId);
        
        boolean success = slackNotificationService.sendStockAlert(
                product.getName(),
                product.getStockQuantity(),
                threshold,
                operatorName,
                requestId
        );

        if (success) {
            alertHistory.setStatus(AlertHistory.Status.SENT);
            logger.info("Alert sent successfully for product: {}", product.getName());
        } else {
            alertHistory.setStatus(AlertHistory.Status.FAILED);
            logger.error("Failed to send alert for product: {}", product.getName());
        }

        alertHistoryRepository.save(alertHistory);

        // 監査ログを記録
        InventoryEvent event = new InventoryEvent(
                product,
                operatorName,
                InventoryEvent.EventType.ALERT,
                null,
                product.getStockQuantity(),
                requestId
        );
        event.setMetadata("{\"alertStatus\":\"" + alertHistory.getStatus() + "\",\"threshold\":" + threshold + "}");
        inventoryEventRepository.save(event);
    }

    /**
     * アラートメッセージを作成
     */
    private String createAlertMessage(Product product, int threshold) {
        return String.format("在庫アラート: %s の在庫が %d 個になりました（閾値: %d）",
                product.getName(), product.getStockQuantity(), threshold);
    }

    /**
     * 失敗したアラートを取得
     */
    @Transactional(readOnly = true)
    public List<AlertHistory> getFailedAlerts() {
        return alertHistoryRepository.findByStatusOrderByAlertTimeDesc(AlertHistory.Status.FAILED);
    }
}
