package com.henio.paymentservice.dto;

import com.henio.paymentservice.model.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentUpdateRequest {

    @NotNull(message = "Status is required")
    private PaymentStatus status;
}
