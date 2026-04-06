package com.example.back.candles.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "candles")
public class CandleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String exchange;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(name = "\"interval\"", nullable = false, length = 16)
    private String interval;

    @Column(name = "open_time", nullable = false)
    private Instant openTime;

    @Column(name = "close_time", nullable = false)
    private Instant closeTime;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal open;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal high;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal low;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal close;

    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal volume;
}
