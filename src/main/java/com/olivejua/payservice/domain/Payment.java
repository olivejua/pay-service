package com.olivejua.payservice.domain;

import com.olivejua.payservice.domain.type.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Payment {
    private final Long id;
    private final User user;
    private final Long amount;
    private final PaymentStatus status;
    private final String transactionId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime approvedAt;
    private final LocalDateTime canceledAt;

    @Builder
    public Payment(Long id, User user, Long amount, PaymentStatus status, String transactionId, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime approvedAt, LocalDateTime canceledAt) {
        this.id = id;
        this.user = user;
        this.amount = amount;
        this.status = status;
        this.transactionId = transactionId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.approvedAt = approvedAt;
        this.canceledAt = canceledAt;
    }

    public Payment cancel(LocalDateTime canceledAt) {
        return Payment.builder()
                .id(id)
                .user(user)
                .amount(amount)
                .status(PaymentStatus.CANCELED)
                .transactionId(transactionId)
                .createdAt(createdAt)
                .updatedAt(LocalDateTime.now())
                .approvedAt(approvedAt)
                .canceledAt(canceledAt)
                .build();
    }

    public boolean hasDifferentPayerFrom(User user) {
        return Objects.equals(this.user.getId(), user.getId());
    }

    public boolean hasStatusOf(PaymentStatus status) {
        return this.status == status;
    }
}
