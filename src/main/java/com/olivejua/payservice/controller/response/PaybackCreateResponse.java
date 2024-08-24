package com.olivejua.payservice.controller.response;

import com.olivejua.payservice.domain.type.PaybackStatus;

import java.time.LocalDateTime;

public record PaybackCreateResponse(
        Long paymentId,
        Long amount,
        PaybackStatus status,
        LocalDateTime createdAt
) {
}
