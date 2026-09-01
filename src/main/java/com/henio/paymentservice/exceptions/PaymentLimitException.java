package com.henio.paymentservice.exceptions;

public class PaymentLimitException extends RuntimeException {

    public PaymentLimitException(String message) {
        super(message);
    }
}
