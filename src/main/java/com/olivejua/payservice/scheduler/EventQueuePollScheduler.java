package com.olivejua.payservice.scheduler;

import com.olivejua.payservice.database.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EventQueuePollScheduler {
    private final PaymentJpaRepository paymentJpaRepository;

    void pollAllPaymentsWithPending() {
    }
}
