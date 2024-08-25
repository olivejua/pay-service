package com.olivejua.payservice.controller.response;

import com.olivejua.payservice.domain.Payment;
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
    public static PaymentCreateResponse from(Payment payment) {
        return new PaymentCreateResponse(
                payment.getId(),
                payment.getUser().getId(),
                payment.getAmount(),
                payment.getUser().getAccountBank(),
                payment.getUser().getAccountNumber(),
                payment.getTransactionId(),
                payment.getApprovedAt(),
                payment.getStatus(),
                payment.getCreatedAt());
    }
}
