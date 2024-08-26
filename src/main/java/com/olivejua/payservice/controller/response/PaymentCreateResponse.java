package com.olivejua.payservice.controller.response;

import com.olivejua.payservice.domain.Payment;
import com.olivejua.payservice.domain.type.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentCreateResponse(
        Long id,
        Long amount,
        String accountBank,
        String accountNumber, // 마스킹해야한다 아니면 계좌에 대한 명칭
        PaymentStatus status,
        LocalDateTime createdAt
) {
    public static PaymentCreateResponse from(Payment payment) {
        return new PaymentCreateResponse(
                payment.getId(),
                payment.getAmount(),
                payment.getUser().getAccountBank(),
                payment.getUser().getAccountNumber(),
                payment.getStatus(),
                payment.getCreatedAt());
    }
}
