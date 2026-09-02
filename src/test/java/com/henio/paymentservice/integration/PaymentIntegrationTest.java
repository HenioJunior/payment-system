package com.henio.paymentservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henio.paymentservice.dto.PaymentRequest;
import com.henio.paymentservice.dto.PaymentResponse;
import com.henio.paymentservice.dto.PaymentUpdateRequest;
import com.henio.paymentservice.model.Payment;
import com.henio.paymentservice.model.enums.PaymentSource;
import com.henio.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.henio.paymentservice.model.enums.PaymentStatus.PAID;
import static com.henio.paymentservice.model.enums.PaymentStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("mysql")
public class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void cleanDatabase() {
        paymentRepository.deleteAll();
    }

//    @Test
//    @DisplayName("Should create a new payment and return 201 Created")
//    void createPayment() throws Exception {
//        String payloader = """
//                {
//                "payerId": "123e4567-e89b-12d3-a456-426655440000",
//                "paymentSource": "PIX",
//                "amount": 100.50}"
//                }
//                """;
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/api/payments")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(payloader))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.payerId", is("123e4567-e89b-12d3-a456-426655440000")))
//                .andExpect(jsonPath("$.paymentSource", is(PaymentSource.PIX.name())))
//                .andExpect(jsonPath("$.amount", is(100.50)))
//                .andExpect(jsonPath("$.status", is(PaymentStatus.PENDING.name())));
//    }

    @Test
    @DisplayName("Should create a new payment and return 201 Created")
    void createPayment() throws Exception {
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .payerId(UUID.randomUUID())
                .paymentSource(PaymentSource.PIX)
                .amount(new BigDecimal("100.50"))
                .build();

        String responseInJson = mockMvc.perform(MockMvcRequestBuilders.post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PaymentResponse paymentResponse = mapper.readValue(responseInJson, PaymentResponse.class);

        assertThat(paymentResponse.getPayerId()).isEqualTo(paymentRequest.getPayerId());
        assertThat(paymentResponse.getPaymentSource()).isEqualTo(paymentRequest.getPaymentSource());
        assertThat(paymentResponse.getAmount()).isEqualTo(paymentRequest.getAmount());
        assertThat(paymentResponse.getStatus()).isEqualTo(PENDING);

    }

    @Test
    @DisplayName("Should find a payment by ID and return 200 OK")
    void getPayment() throws Exception {
        var payment = Payment.builder().payerId(UUID.randomUUID())
                .paymentSource(PaymentSource.CREDIT_CARD)
                .amount(BigDecimal.valueOf(200.75)).status(PENDING).build();

        var savedPayment = paymentRepository.save(payment);

        var responseInJson = mockMvc.perform(get("/api/payments/{paymentId}",
                        savedPayment.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var paymentResponse = mapper.readValue(responseInJson, PaymentResponse.class);

        assertThat(paymentResponse.getId()).isEqualTo(savedPayment.getId());
        assertThat(paymentResponse.getPayerId()).isEqualTo(savedPayment.getPayerId());
        assertThat(paymentResponse.getPaymentSource()).isEqualTo(savedPayment.getPaymentSource());
        assertThat(paymentResponse.getAmount()).isEqualTo(savedPayment.getAmount());
        assertThat(paymentResponse.getStatus()).isEqualTo(savedPayment.getStatus());
    }

    @Test
    @DisplayName("Should find all payments and return 200 OK")
    void getAllPayments() throws Exception {
        UUID payerId = UUID.randomUUID();

        var firstPayment = Payment.builder().payerId(payerId).paymentSource(PaymentSource.CREDIT_CARD)
                .amount(BigDecimal.valueOf(100.00)).status(PENDING).build();

        var secondPayment = Payment.builder().payerId(payerId).paymentSource(PaymentSource.CREDIT_CARD)
                .amount(BigDecimal.valueOf(200.00)).status(PENDING).build();

        paymentRepository.saveAll(List.of(firstPayment, secondPayment));

        var responseInJson = mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<PaymentResponse> listOfPaymentResponse = mapper.readValue(responseInJson,
                mapper.getTypeFactory().constructCollectionType(List.class, PaymentResponse.class)
        );

        assertThat(listOfPaymentResponse).hasSize(2);
        assertThat(listOfPaymentResponse.get(0).getId()).isNotNull();
        assertThat(listOfPaymentResponse.get(0).getPayerId()).isEqualTo(payerId);
        assertThat(listOfPaymentResponse.get(0).getPaymentSource()).isEqualTo(PaymentSource.CREDIT_CARD);
        assertThat(listOfPaymentResponse.get(0).getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(listOfPaymentResponse.get(0).getStatus()).isEqualTo(PENDING);
        assertThat(listOfPaymentResponse.get(1).getId()).isNotNull();
        assertThat(listOfPaymentResponse.get(1).getPayerId()).isEqualTo(payerId);
        assertThat(listOfPaymentResponse.get(1).getPaymentSource()).isEqualTo(PaymentSource.CREDIT_CARD);
        assertThat(listOfPaymentResponse.get(1).getAmount()).isEqualTo(new BigDecimal("200.00"));
        assertThat(listOfPaymentResponse.get(1).getStatus()).isEqualTo(PENDING);
    }

    @Test
    @DisplayName("Should find all payments by payerId and return 200 OK")
    void getPaymentsByPayerId() throws Exception {
        UUID payerId = UUID.randomUUID();

        var firstPayment = Payment.builder().payerId(payerId).paymentSource(PaymentSource.CREDIT_CARD)
                .amount(BigDecimal.valueOf(100.00)).status(PENDING).build();

        var secondPayment = Payment.builder().payerId(payerId).paymentSource(PaymentSource.PIX)
                .amount(BigDecimal.valueOf(200.00)).status(PENDING).build();

        paymentRepository.saveAll(List.of(firstPayment, secondPayment));

        var responseInJson = mockMvc.perform(get("/api/payments/payer/{payerId}", payerId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<PaymentResponse> listOfPaymentResponse = mapper.readValue(responseInJson,
                mapper.getTypeFactory().constructCollectionType(List.class, PaymentResponse.class)
        );

        assertThat(listOfPaymentResponse).hasSize(2);
        assertThat(listOfPaymentResponse.get(0).getId()).isNotNull();
        assertThat(listOfPaymentResponse.get(0).getPayerId()).isEqualTo(payerId);
        assertThat(listOfPaymentResponse.get(0).getPaymentSource()).isEqualTo(PaymentSource.CREDIT_CARD);
        assertThat(listOfPaymentResponse.get(0).getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(listOfPaymentResponse.get(0).getStatus()).isEqualTo(PENDING);
        assertThat(listOfPaymentResponse.get(1).getId()).isNotNull();
        assertThat(listOfPaymentResponse.get(1).getPayerId()).isEqualTo(payerId);
        assertThat(listOfPaymentResponse.get(1).getPaymentSource()).isEqualTo(PaymentSource.PIX);
        assertThat(listOfPaymentResponse.get(1).getAmount()).isEqualTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Should update a payment and return 200 OK")
    void updatePayment() throws Exception {
        var paymentUpdateRequest = PaymentUpdateRequest.builder().status(PAID).build();

        var payment = Payment.builder().payerId(UUID.randomUUID()).paymentSource(PaymentSource.CREDIT_CARD)
                .amount(BigDecimal.valueOf(300.00)).status(PENDING).build();

        var savedPayment = paymentRepository.save(payment);

        String responseInJson = mockMvc.perform(put("/api/payments/{paymentId}", savedPayment.getId())
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(paymentUpdateRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PaymentResponse updatedPaymentResponse = mapper.readValue(responseInJson, PaymentResponse.class);

        assertThat(updatedPaymentResponse.getId()).isEqualTo(savedPayment.getId());
        assertThat(updatedPaymentResponse.getStatus()).isEqualTo(PAID);
        assertThat(updatedPaymentResponse.getPayerId()).isEqualTo(savedPayment.getPayerId());
        assertThat(updatedPaymentResponse.getPaymentSource()).isEqualTo(savedPayment.getPaymentSource());
        assertThat(updatedPaymentResponse.getAmount()).isEqualByComparingTo(savedPayment.getAmount());
    }

    @Test
    @DisplayName("Should return 404 when payment not found for update")
    void updatePayment_ShouldReturn404WhenPaymentNotFound() throws Exception {
        var paymentUpdateRequest = PaymentUpdateRequest.builder().status(PAID).build();

        var nonExistentPaymentId = UUID.randomUUID();

        mockMvc.perform(put("/api/payments/{paymentId}/payer/{payerId}",
                        nonExistentPaymentId, "123456789")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(paymentUpdateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when payment not found")
    void getPayment_ShouldReturn404WhenNotFound() throws Exception {
        var nonExistentId = new Random().nextLong();

        mockMvc.perform(get("/api/payments/{paymentId}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return empty list when no payments exist")
    void getAllPayments_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
