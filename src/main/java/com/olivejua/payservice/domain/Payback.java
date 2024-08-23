package com.olivejua.payservice.domain;

import com.olivejua.payservice.domain.type.PaybackStatus;
import lombok.Builder;

import java.time.LocalDateTime;

public class Payback {
    private final Long id;
    private final Payment payment;
    private final Long amount;
    private final PaybackStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime canceledAt;

    @Builder
    public Payback(Long id, Payment payment, Long amount, PaybackStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime canceledAt) {
        this.id = id;
        this.payment = payment;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.canceledAt = canceledAt;
    }
}
