package com.olivejua.payservice.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PaymentAgencyHandler {

    public String requestPaymentFromAgency() {
        return String.valueOf(UUID.randomUUID().getLeastSignificantBits());
    }

    public LocalDateTime requestCancellationFromAgency() {
        return LocalDateTime.now();
    }
}
