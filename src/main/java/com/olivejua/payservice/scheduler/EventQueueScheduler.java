package com.olivejua.payservice.scheduler;

import com.olivejua.payservice.queue.EventQueues;
import com.olivejua.payservice.service.PaybackService;
import com.olivejua.payservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventQueueScheduler {
    private final PaymentService paymentService;
    private final PaybackService paybackService;

    @Scheduled(fixedDelay = 1000)
    void pollAllPaymentsWithPending() {
        if (EventQueues.PAYMENT_PROCESSING_QUEUE.isEmpty()) {
            return;
        }

        try {
            final Long paymentId = EventQueues.PAYMENT_PROCESSING_QUEUE.remove();
            paymentService.approvePayment(paymentId);
        } catch (Exception e) {
            //이슈를 추적하기 위해 실패에 대한 로그를 DB 또는 로그서버에 저장하거나 실패에 대한 대비 케이스를 구현하는것이 좋음
            log.error("Unexpected Error", e);
        }
    }

    @Scheduled(fixedDelay = 1000)
    void pollAllPaymentsWithCancelPending() {
        if (EventQueues.PAYMENT_CANCELLATION_QUEUE.isEmpty()) {
            return;
        }

        try {
            final Long paymentId = EventQueues.PAYMENT_CANCELLATION_QUEUE.remove();
            paymentService.cancelPayment(paymentId);
        } catch (Exception e) {
            log.error("Unexpected Error", e);
        }
    }

    @Scheduled(fixedDelay = 1000)
    void pollAllPaybackWithPending() {
        if (EventQueues.PAYBACK_PROCESSING_QUEUE.isEmpty()) {
            return;
        }

        try {
            final Long paymentId = EventQueues.PAYBACK_PROCESSING_QUEUE.remove();
            paybackService.createPayback(paymentId);
        } catch (Exception e) {
            log.error("Unexpected Error", e);
        }
    }

    @Scheduled(fixedDelay = 1000)
    void pollAllPaybackWithCancelPending() {
        if (EventQueues.PAYBACK_CANCELLATION_QUEUE.isEmpty()) {
            return;
        }

        try {
            final Long paymentId = EventQueues.PAYBACK_CANCELLATION_QUEUE.remove();
            paybackService.cancelPayback(paymentId);
        } catch (Exception e) {
            log.error("Unexpected Error", e);
        }
    }
}
