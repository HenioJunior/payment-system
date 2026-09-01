package com.henio.paymentservice.dto;

import com.henio.paymentservice.model.enums.PaymentSource;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
public class PaymentRequest {

    @NotNull(message = "Payer ID is required")
    private UUID payerId;

    @NotNull(message = "Payment source is required")
    private PaymentSource paymentSource;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
}
