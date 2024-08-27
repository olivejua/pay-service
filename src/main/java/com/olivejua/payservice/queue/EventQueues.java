package com.olivejua.payservice.queue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventQueues {
    /**
     * 결제대행사에 결제 요청 : PENDING
     */
    public static final Queue<Long> PAYMENT_PROCESSING_QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * 결제대행사에 취소 요청 : CANCEL_PENDING
     */
    public static final Queue<Long> PAYMENT_CANCELLATION_QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * 결제 성공 후 페이백 지급
     */
    public static final Queue<Long> PAYBACK_PROCESSING_QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * 결제취소 후 페이백 회수
     */
    public static final Queue<Long> PAYBACK_CANCELLATION_QUEUE = new ConcurrentLinkedQueue<>();
}
