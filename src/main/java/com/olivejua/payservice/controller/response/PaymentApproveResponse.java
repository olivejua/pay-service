package com.olivejua.payservice.controller.response;

import com.olivejua.payservice.domain.Payment;
import com.olivejua.payservice.domain.type.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentApproveResponse(
        Long amount,
        String accountBank,
        String accountNumber, // 마스킹해야한다
        String transactionId,
        LocalDateTime approvedAt,
        PaymentStatus status,
        LocalDateTime updatedAt
) {

    public static PaymentApproveResponse from(Payment payment) {
        return new PaymentApproveResponse(
                payment.getAmount(),
                payment.getUser().getAccountBank(),
                payment.getUser().getAccountNumber(),
                payment.getTransactionId(),
                payment.getApprovedAt(),
                payment.getStatus(),
                payment.getUpdatedAt());
    }

}
