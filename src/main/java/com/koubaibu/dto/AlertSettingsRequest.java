package com.koubaibu.dto;

import jakarta.validation.constraints.*;

/**
 * アラート設定更新リクエストDTO
 */
public class AlertSettingsRequest {

    @NotNull(message = "閾値は必須です")
    @Min(value = 0, message = "閾値は0以上で入力してください")
    @Max(value = 999, message = "閾値は999以下で入力してください")
    private Integer threshold;

    @NotBlank(message = "操作担当者名は必須です")
    @Size(min = 1, max = 64, message = "操作担当者名は1〜64文字で入力してください")
    private String operatorName;

    public AlertSettingsRequest() {
    }

    public AlertSettingsRequest(Integer threshold, String operatorName) {
        this.threshold = threshold;
        this.operatorName = operatorName;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
}
