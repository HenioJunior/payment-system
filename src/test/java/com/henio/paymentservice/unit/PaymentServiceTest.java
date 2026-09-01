package com.henio.paymentservice.unit;

import com.henio.paymentservice.dto.PaymentRequest;
import com.henio.paymentservice.dto.PaymentResponse;
import com.henio.paymentservice.exceptions.PaymentLimitException;
import com.henio.paymentservice.model.enums.PaymentSource;
import com.henio.paymentservice.model.enums.PaymentStatus;
import com.henio.paymentservice.repository.PaymentRepository;
import com.henio.paymentservice.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;


    @Test
    @DisplayName("Should save payment when limit is not exceeded")
    void shouldSavePaymentWhenLimitIsNotExceeded() {
        when(paymentRepository.sumPaymentsByPayerIdAndDate(any(), any(), any()))
                .thenReturn(new BigDecimal("200.00"));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .payerId(UUID.randomUUID())
                .paymentSource(PaymentSource.PIX)
                .amount(new BigDecimal("100.50"))
                .build();

        PaymentResponse createdPayment = paymentService.createPayment(paymentRequest);

        assertThat(createdPayment.getPayerId()).isEqualTo(paymentRequest.getPayerId());
        assertThat(createdPayment.getAmount()).isEqualByComparingTo(paymentRequest.getAmount());
        assertThat(createdPayment.getPaymentSource()).isEqualTo(paymentRequest.getPaymentSource());
        assertThat(createdPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        verify(paymentRepository).save(any());
    }

    @Test
    @DisplayName("Should throw PaymentLimitException when daily limit exceeded")
    void shouldThrowPaymentLimitExceptionWhenDailyLimitExceeded() {
        when(paymentRepository.sumPaymentsByPayerIdAndDate(any(), any(), any()))
                .thenReturn(new BigDecimal("8000.00"));

        var paymentRequest = PaymentRequest.builder()
                .payerId(UUID.randomUUID())
                .paymentSource(PaymentSource.CREDIT_CARD)
                .amount(BigDecimal.valueOf(2000.01))
                .build();

        assertThatThrownBy(() -> paymentService.createPayment(paymentRequest))
                .isInstanceOf(PaymentLimitException.class)
                .hasMessageContaining("Daily payment limit exceeded");
    }

    @Test
    @DisplayName("Should thrown an exception the amount is zero")
    void shouldThrowAnExceptionWhenTheAmountIsZero() {
        when(paymentRepository.sumPaymentsByPayerIdAndDate(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        var paymentRequest = PaymentRequest.builder()
                .payerId(UUID.randomUUID())
                .paymentSource(PaymentSource.CREDIT_CARD)
                .amount(BigDecimal.ZERO)
                .build();

        assertThatThrownBy(() -> paymentService.createPayment(paymentRequest))
                .isInstanceOf(PaymentLimitException.class)
                .hasMessageContaining("Amount must be greater than zero");
    }

    @Test
    @DisplayName("Should thrown an exception the amount is negative")
    void shouldThrownExceptionWhenAmountIsNegative() {
        when(paymentRepository.sumPaymentsByPayerIdAndDate(any(), any(), any()))
                .thenReturn(new BigDecimal("-100.00"));

        var paymentRequest = PaymentRequest.builder().payerId(UUID.randomUUID()).paymentSource(PaymentSource.CREDIT_CARD)
                .amount(new BigDecimal("-100.00")).build();

        assertThatThrownBy(() -> paymentService.createPayment(paymentRequest))
                .isInstanceOf(PaymentLimitException.class)
                .hasMessage("Amount must be greater than zero");
    }
}