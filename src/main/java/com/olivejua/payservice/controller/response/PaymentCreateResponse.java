package com.olivejua.payservice.controller.response;

import com.olivejua.payservice.domain.type.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentCreateResponse(
        Long id,
        Long userId,
        Long amount,
        String accountBank,
        String accountNumber,
        String transactionId,
        LocalDateTime approvedAt,
        PaymentStatus status,
        LocalDateTime createdAt
) {
}
