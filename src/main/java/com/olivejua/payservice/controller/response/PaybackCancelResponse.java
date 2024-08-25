package com.olivejua.payservice.controller.response;

import com.olivejua.payservice.domain.Payback;
import com.olivejua.payservice.domain.type.PaybackStatus;

import java.time.LocalDateTime;

public record PaybackCancelResponse(
        Long id,
        Long paymentId,
        Long amount,
        PaybackStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime canceledAt
) {
    public static PaybackCancelResponse from(Payback payback) {
        return new PaybackCancelResponse(
                payback.getId(),
                payback.getPayment().getId(),
                payback.getAmount(),
                payback.getStatus(),
                payback.getCreatedAt(),
                payback.getUpdatedAt(),
                payback.getCanceledAt());
    }
}
