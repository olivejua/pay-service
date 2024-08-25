package com.olivejua.payservice.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PaybackPolicy {
    private final Long id;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final boolean active;
    private final BigDecimal rate;
    private final BigDecimal maxPaybackAmount;
    private final BigDecimal minPurchaseAmount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public PaybackPolicy(Long id, LocalDateTime startDate, LocalDateTime endDate, Boolean isActive, BigDecimal rate, BigDecimal maxPaybackAmount, BigDecimal minPurchaseAmount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = isActive != null && isActive;
        this.rate = rate;
        this.maxPaybackAmount = maxPaybackAmount;
        this.minPurchaseAmount = minPurchaseAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
