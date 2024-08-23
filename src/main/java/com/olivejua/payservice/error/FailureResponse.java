package com.olivejua.payservice.error;

public record FailureResponse(
        String code,
        String message
) {
}
