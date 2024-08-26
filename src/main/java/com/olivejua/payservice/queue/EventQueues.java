package com.olivejua.payservice.queue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventQueues {
    private static final Queue<Long> PAYMENT_PROCESSING_QUEUE = new ConcurrentLinkedQueue<>();
    private static final Queue<Long> PAYMENT_CANCELLATION_QUEUE = new ConcurrentLinkedQueue<>();
    private static final Queue<Long> PAYBACK_PROCESSING_QUEUE = new ConcurrentLinkedQueue<>();
    private static final Queue<Long> PAYBACK_CANCELLATION_QUEUE = new ConcurrentLinkedQueue<>();



}
