package com.olivejua.payservice.domain;

import com.olivejua.payservice.domain.type.PaybackStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class Payback {
    private final Long id;
    private final PaybackPolicy policy;
    private final Payment payment;
    private final Long amount;
    private final PaybackStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime canceledAt;

    @Builder
    public Payback(Long id, PaybackPolicy policy, Payment payment, Long amount, PaybackStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime canceledAt) {
        this.id = id;
        this.policy = policy;
        this.payment = payment;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.canceledAt = canceledAt;
    }

    public static Payback from(PaybackPolicy policy, Payment payment) {
        LocalDateTime createdDateTime = LocalDateTime.now();
        return Payback.builder()
                .policy(policy)
                .payment(payment)
                .amount(BigDecimal.valueOf(payment.getAmount()).multiply(policy.getRate()).longValue())
                .createdAt(createdDateTime)
                .updatedAt(createdDateTime)
                .build();
    }

    public boolean hasStatusOf(PaybackStatus status) {
        return this.status == status;
    }

    public Payback cancel() {
        LocalDateTime now = LocalDateTime.now();

        return Payback.builder()
                .id(id)
                .policy(policy)
                .payment(payment)
                .amount(amount)
                .status(PaybackStatus.CANCELED)
                .createdAt(createdAt)
                .updatedAt(now)
                .canceledAt(now)
                .build();
    }
}
