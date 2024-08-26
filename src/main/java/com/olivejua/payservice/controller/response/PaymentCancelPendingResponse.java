package com.olivejua.payservice.controller.response;

import com.olivejua.payservice.domain.Payment;
import com.olivejua.payservice.domain.type.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentCancelPendingResponse(
        Long id,
        Long userId,
        Long amount,
        String accountBank,
        String accountNumber,
        String transactionId,
        LocalDateTime approvedAt,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PaymentCancelPendingResponse from(Payment payment) {
        return new PaymentCancelPendingResponse(
                payment.getId(),
                payment.getUser().getId(),
                payment.getAmount(),
                payment.getUser().getAccountBank(),
                payment.getUser().getAccountNumber(),
                payment.getTransactionId(),
                payment.getApprovedAt(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
