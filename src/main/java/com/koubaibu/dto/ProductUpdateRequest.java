package com.koubaibu.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 商品更新リクエストDTO
 */
public class ProductUpdateRequest {

    @NotBlank(message = "商品名は必須です")
    @Size(min = 1, max = 255, message = "商品名は1〜255文字で入力してください")
    private String name;

    @NotNull(message = "価格は必須です")
    @DecimalMin(value = "0.00", message = "価格は0以上で入力してください")
    @Digits(integer = 10, fraction = 2, message = "価格は小数第2位までで入力してください")
    private BigDecimal price;

    @NotBlank(message = "操作担当者名は必須です")
    @Size(min = 1, max = 64, message = "操作担当者名は1〜64文字で入力してください")
    private String operatorName;

    public ProductUpdateRequest() {
    }

    public ProductUpdateRequest(String name, BigDecimal price, String operatorName) {
        this.name = name;
        this.price = price;
        this.operatorName = operatorName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
}
