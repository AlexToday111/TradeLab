package com.example.back.papertrading.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "paper_fills")
public class PaperFillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaperOrderSide side;

    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal fee;

    @Column(name = "fee_currency", nullable = false)
    private String feeCurrency;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;
}
