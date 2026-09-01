package com.henio.paymentservice.repository;

import com.henio.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.payerId = :payerId AND " +
            "p.createdAt >= :start AND p.createdAt < :end")
    BigDecimal sumPaymentsByPayerIdAndDate(@Param("payerId") UUID payerId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    List<Payment> findAllByPayerId(UUID payerId);
}
