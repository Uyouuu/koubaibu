package com.koubaibu.dto;

import jakarta.validation.constraints.*;

/**
 * 在庫増減リクエストDTO
 */
public class StockChangeRequest {

    @NotNull(message = "変動量は必須です")
    @Min(value = 1, message = "変動量は1以上で入力してください")
    @Max(value = 1000, message = "変動量は1000以下で入力してください")
    private Integer delta;

    @NotBlank(message = "操作担当者名は必須です")
    @Size(min = 1, max = 64, message = "操作担当者名は1〜64文字で入力してください")
    private String operatorName;

    public StockChangeRequest() {
    }

    public StockChangeRequest(Integer delta, String operatorName) {
        this.delta = delta;
        this.operatorName = operatorName;
    }

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
}
