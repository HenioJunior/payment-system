package com.henio.paymentservice.integration;

import com.henio.paymentservice.dto.PaymentRequest;
import com.henio.paymentservice.dto.PaymentResponse;
import com.henio.paymentservice.model.Payment;
import com.henio.paymentservice.model.enums.PaymentSource;
import com.henio.paymentservice.model.enums.PaymentStatus;
import com.henio.paymentservice.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void createPayment() throws Exception {
        String payloader = """ 
                {
                "payerId": "123e4567-e89b-12d3-a456-426655440000",
                "paymentSource": "PIX",
                "amount": 100.50}"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloader))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payerId", is("123e4567-e89b-12d3-a456-426655440000")))
                .andExpect(jsonPath("$.paymentSource", is(PaymentSource.PIX.name())))
                .andExpect(jsonPath("$.amount", is(100.50)))
                .andExpect(jsonPath("$.status", is(PaymentStatus.PENDING.name())));
    }

    @Test
    void createPaymentWithMapper() throws Exception {
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .payerId(UUID.randomUUID())
                .paymentSource(PaymentSource.PIX)
                .amount(new BigDecimal("100.50"))
                .build();

        String responseInJson = mockMvc.perform(MockMvcRequestBuilders.post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PaymentResponse paymentResponse = mapper.readValue(responseInJson, PaymentResponse.class);

        Assertions.assertThat(paymentResponse.getPayerId()).isEqualTo(paymentRequest.getPayerId());
        Assertions.assertThat(paymentResponse.getPaymentSource()).isEqualTo(paymentRequest.getPaymentSource());
        Assertions.assertThat(paymentResponse.getAmount()).isEqualTo(paymentRequest.getAmount());
        Assertions.assertThat(paymentResponse.getStatus()).isEqualTo(PaymentStatus.PENDING);

    }

    @Test
    void getPaymentById() throws Exception {
        //precondicao para o teste
        Payment payment = Payment.builder()
                .payerId(UUID.randomUUID())
                .paymentSource(PaymentSource.PIX)
                .amount(new BigDecimal("100.50"))
                .status(PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        mockMvc.perform(get("/api/payments/{paymentId}", savedPayment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payerId", is(savedPayment.getPayerId().toString())))
                .andExpect(jsonPath("$.paymentSource", is(savedPayment.getPaymentSource().name())))
                .andExpect(jsonPath("$.amount", is(savedPayment.getAmount().doubleValue())))
                .andExpect(jsonPath("$.status", is(savedPayment.getStatus().name())));
    }

}
