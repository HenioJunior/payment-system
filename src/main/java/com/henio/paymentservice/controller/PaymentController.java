package com.henio.paymentservice.controller;

import com.henio.paymentservice.dto.PaymentRequest;
import com.henio.paymentservice.dto.PaymentResponse;
import com.henio.paymentservice.dto.PaymentUpdateRequest;
import com.henio.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        return paymentService.createPayment(paymentRequest);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(@PathVariable Long paymentId) {
        return paymentService.getPaymentById(paymentId);
    }

    @GetMapping
    public List<PaymentResponse> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/payer/{payerId}")
    public List<PaymentResponse> getPaymentsByPayer(@PathVariable UUID payerId) {
        return paymentService.getPaymentsByPayerId(payerId);
    }

    @PutMapping("/{paymentId}")
    public PaymentResponse updatePayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentUpdateRequest updateRequest) {
        return paymentService.updatePayment(paymentId, updateRequest);
    }
}
