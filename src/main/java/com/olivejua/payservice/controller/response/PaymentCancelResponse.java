package com.olivejua.payservice.controller.response;

import com.olivejua.payservice.domain.type.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentCancelResponse(
        Long id,
        Long userId,
        Long amount,
        String accountBank,
        String accountNumber,
        Long transactionId,
        LocalDateTime approvedAt,
        LocalDateTime canceledAt,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
