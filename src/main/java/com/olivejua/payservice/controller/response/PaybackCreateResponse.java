package com.olivejua.payservice.controller.response;

import com.olivejua.payservice.domain.Payback;
import com.olivejua.payservice.domain.type.PaybackStatus;

import java.time.LocalDateTime;

public record PaybackCreateResponse(
        Long paymentId,
        Long amount,
        PaybackStatus status,
        LocalDateTime createdAt
) {
    public static PaybackCreateResponse from(Payback payback) {
        return new PaybackCreateResponse(payback.getPayment().getId(), payback.getAmount(), payback.getStatus(), payback.getCreatedAt());
    }
}
