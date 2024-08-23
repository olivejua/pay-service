package com.olivejua.payservice.controller.request;

public record PaymentCreateRequest(
        Long userId,
        Long amount
) {
}
