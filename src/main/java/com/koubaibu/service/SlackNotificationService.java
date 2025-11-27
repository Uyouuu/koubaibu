package com.koubaibu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Slack通知サービス
 * Slack Notification Service - Handles Slack webhook notifications
 */
@Service
public class SlackNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(SlackNotificationService.class);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${slack.enabled:false}")
    private boolean slackEnabled;

    @Value("${slack.webhook-url:}")
    private String webhookUrl;

    @Value("${slack.channel:#lab-inventory-alert}")
    private String channel;

    @Value("${slack.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${slack.retry.initial-interval:1000}")
    private long initialRetryInterval;

    @Value("${slack.retry.multiplier:2.0}")
    private double retryMultiplier;

    private final RestTemplate restTemplate;

    public SlackNotificationService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 在庫アラートをSlackに送信
     */
    public boolean sendStockAlert(String productName, int currentStock, int threshold, 
                                   String operatorName, String requestId) {
        if (!slackEnabled || webhookUrl == null || webhookUrl.isEmpty()) {
            logger.info("Slack notification disabled or webhook URL not configured. Skipping alert.");
            return true; // 設定されていない場合は成功として扱う
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("channel", channel);
        payload.put("username", "在庫管理システム");
        payload.put("icon_emoji", ":warning:");
        
        String text = String.format(
            ":warning: *在庫アラート*\n" +
            "• 商品名: %s\n" +
            "• 現在庫: %d 個\n" +
            "• 閾値: %d 個\n" +
            "• 操作者: %s\n" +
            "• 発生日時: %s\n" +
            "• RequestID: %s",
            productName, currentStock, threshold, operatorName,
            LocalDateTime.now().format(DATETIME_FORMATTER), requestId
        );
        payload.put("text", text);

        return sendWithRetry(payload);
    }

    /**
     * 設定変更通知をSlackに送信
     */
    public boolean sendSettingsChangeNotification(int oldThreshold, int newThreshold, 
                                                   String operatorName, String requestId) {
        if (!slackEnabled || webhookUrl == null || webhookUrl.isEmpty()) {
            logger.info("Slack notification disabled or webhook URL not configured. Skipping notification.");
            return true;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("channel", channel);
        payload.put("username", "在庫管理システム");
        payload.put("icon_emoji", ":gear:");
        
        String text = String.format(
            ":gear: *アラート設定変更*\n" +
            "• 旧閾値: %d 個\n" +
            "• 新閾値: %d 個\n" +
            "• 操作者: %s\n" +
            "• 変更日時: %s\n" +
            "• RequestID: %s",
            oldThreshold, newThreshold, operatorName,
            LocalDateTime.now().format(DATETIME_FORMATTER), requestId
        );
        payload.put("text", text);

        return sendWithRetry(payload);
    }

    /**
     * 指数バックオフでリトライしてSlackに送信
     * Uses asynchronous delay to avoid blocking the main thread
     */
    private boolean sendWithRetry(Map<String, Object> payload) {
        long retryInterval = initialRetryInterval;

        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("Slack notification sent successfully on attempt {}", attempt);
                    return true;
                }

                if (response.getStatusCode().is5xxServerError()) {
                    logger.warn("Slack API returned 5xx error on attempt {}: {}", attempt, response.getStatusCode());
                } else {
                    logger.error("Slack API returned error: {}", response.getStatusCode());
                    return false; // 5xx以外のエラーはリトライしない
                }

            } catch (RestClientException e) {
                logger.warn("Failed to send Slack notification on attempt {}: {}", attempt, e.getMessage());
            }

            if (attempt < maxRetryAttempts) {
                try {
                    // Use TimeUnit for clearer code
                    TimeUnit.MILLISECONDS.sleep(retryInterval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.warn("Slack notification retry interrupted");
                    return false;
                }
                retryInterval = (long) (retryInterval * retryMultiplier);
            }
        }

        logger.error("All {} attempts to send Slack notification failed", maxRetryAttempts);
        return false;
    }

    /**
     * 非同期でSlack通知を送信
     * Sends Slack notification asynchronously to avoid blocking the caller
     */
    @Async
    public CompletableFuture<Boolean> sendStockAlertAsync(String productName, int currentStock, int threshold,
                                                           String operatorName, String requestId) {
        boolean result = sendStockAlert(productName, currentStock, threshold, operatorName, requestId);
        return CompletableFuture.completedFuture(result);
    }
}
