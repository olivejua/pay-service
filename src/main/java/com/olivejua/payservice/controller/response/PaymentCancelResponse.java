package com.olivejua.payservice.controller.response;

import com.olivejua.payservice.domain.Payment;
import com.olivejua.payservice.domain.type.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentCancelResponse(
        Long id,
        Long userId,
        Long amount,
        String accountBank,
        String accountNumber,
        String transactionId,
        LocalDateTime approvedAt,
        LocalDateTime canceledAt,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PaymentCancelResponse from(Payment payment) {
        return new PaymentCancelResponse(
                payment.getId(),
                payment.getUser().getId(),
                payment.getAmount(),
                payment.getUser().getAccountBank(),
                payment.getUser().getAccountNumber(),
                payment.getTransactionId(),
                payment.getApprovedAt(),
                payment.getCanceledAt(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
