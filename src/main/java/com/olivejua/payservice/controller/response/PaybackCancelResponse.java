package com.olivejua.payservice.controller.response;

public record PaybackCancelResponse(
        Long userId,
        Long canceledAmount
) {
}
