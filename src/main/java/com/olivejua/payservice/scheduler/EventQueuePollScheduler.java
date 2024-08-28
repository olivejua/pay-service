package com.olivejua.payservice.scheduler;

import com.olivejua.payservice.queue.EventQueues;
import com.olivejua.payservice.service.PaybackService;
import com.olivejua.payservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EventQueuePollScheduler {
    private final PaymentService paymentService;
    private final PaybackService paybackService;

    /**
     * TODO payment에서 하나 가져와서 실행 (하나씩 하지 않고 배치로 동시실행하면 성능을 높일 수 있다.)
     * TODO 실패케이스에 대한 대비를 해야한다. FAIL LOG
     */

    @Scheduled(fixedDelay = 1000)
    void pollAllPaymentsWithPending() {
        if (EventQueues.PAYMENT_PROCESSING_QUEUE.isEmpty()) {
            return;
        }

        final Long paymentId = EventQueues.PAYMENT_PROCESSING_QUEUE.remove();
        paymentService.approvePayment(paymentId);
    }

    @Scheduled(fixedDelay = 1000)
    void pollAllPaymentsWithCancelPending() {
        if (EventQueues.PAYMENT_CANCELLATION_QUEUE.isEmpty()) {
            return;
        }

        final Long paymentId = EventQueues.PAYMENT_CANCELLATION_QUEUE.remove();
        paymentService.cancelPayment(paymentId);
    }

    @Scheduled(fixedDelay = 1000)
    void pollAllPaybackWithPending() {
        if (EventQueues.PAYBACK_PROCESSING_QUEUE.isEmpty()) {
            return;
        }

        final Long paymentId = EventQueues.PAYBACK_PROCESSING_QUEUE.remove();
        paybackService.createPayback(paymentId);
    }

    @Scheduled(fixedDelay = 1000)
    void pollAllPaybackWithCancelPending() {
        if (EventQueues.PAYBACK_CANCELLATION_QUEUE.isEmpty()) {
            return;
        }

        final Long paymentId = EventQueues.PAYBACK_CANCELLATION_QUEUE.remove();
        paybackService.cancelPayback(paymentId);
    }
}
