package com.olivejua.payservice.controller.request;

public record PaybackCancelRequest(
        Long userId,
        Long paymentId,
        Long cancelAmount
) {
}
