package com.henio.paymentservice.validator;

import com.henio.paymentservice.exceptions.PaymentLimitException;

import java.math.BigDecimal;

public final class PaymentLimitValidator {

    private static final BigDecimal MAX_LIMIT = new BigDecimal("2000.00");

    public static boolean isWithinLimit(BigDecimal amount) {
        if (amount == null) return false;

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentLimitException("Amount must be greater than zero");
        }

        return amount.compareTo(MAX_LIMIT) <= 0;
    }
}
