package com.olivejua.payservice.service;

import com.olivejua.payservice.domain.Payment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PaymentAgencyHandler {

    /**
     * 결제 요청후 응답이 5초이내 오지 않으면 timeout 예외를 던진다.
     * TODO Response 별도 생성할지는 고려해보기
     */
    public Payment requestPaymentFromAgency(Payment payment) {
        String transactionId = String.valueOf(UUID.randomUUID().getLeastSignificantBits());
        LocalDateTime approvedAt = LocalDateTime.now();

        return payment.approve(transactionId, approvedAt);
    }

    public LocalDateTime requestCancellationFromAgency() {
        return LocalDateTime.now();
    }
}
