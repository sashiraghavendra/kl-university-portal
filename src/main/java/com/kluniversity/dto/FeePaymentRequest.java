package com.kluniversity.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeePaymentRequest {
    @NotBlank
    private String regNo;
    @NotNull
    private Integer semester;
    @DecimalMin("1.00")
    private BigDecimal amount;
    @NotBlank
    private String paymentMethod;
}
