package com.henio.paymentservice.dto;

import com.henio.paymentservice.model.enums.PaymentSource;
import com.henio.paymentservice.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private UUID payerId;
    private PaymentSource paymentSource;
    private BigDecimal amount;
    private PaymentStatus status;
}
