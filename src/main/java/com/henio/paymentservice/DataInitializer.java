package com.henio.paymentservice;

import com.henio.paymentservice.model.Payment;
import com.henio.paymentservice.model.enums.PaymentSource;
import com.henio.paymentservice.repository.PaymentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

import static com.henio.paymentservice.model.enums.PaymentStatus.PENDING;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PaymentRepository paymentRepository;

    public DataInitializer(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void run(String... args) {
        paymentRepository.save(Payment.builder().payerId(UUID.randomUUID()).paymentSource(PaymentSource.PIX)
                .amount(BigDecimal.valueOf(100.50)).status(PENDING).build());
    }
}
