package com.olivejua.payservice.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PaybackPolicy {
    private final Long id;
    private final String name;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final boolean active;
    //TODO BigDecimal 사용한 이유
    private final BigDecimal rate;
    private final Long maxPaybackAmount;
    private final Long minPaymentAmount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public PaybackPolicy(Long id, String name, LocalDateTime startDate, LocalDateTime endDate, Boolean isActive, BigDecimal rate, Long maxPaybackAmount, Long minPaymentAmount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = isActive != null && isActive;
        this.rate = rate;
        this.maxPaybackAmount = maxPaybackAmount;
        this.minPaymentAmount = minPaymentAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
