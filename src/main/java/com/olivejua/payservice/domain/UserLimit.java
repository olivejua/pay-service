package com.olivejua.payservice.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserLimit {
    private final Long id;
    private final User user;
    private final long maxBalance;
    private final long singlePaymentLimit;
    private final long dailyPaymentLimit;
    private final long monthlyPaymentLimit;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public UserLimit(Long id, User user, Long maxBalance, Long singlePaymentLimit, Long dailyPaymentLimit, Long monthlyPaymentLimit, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.maxBalance = maxBalance;
        this.singlePaymentLimit = singlePaymentLimit;
        this.dailyPaymentLimit = dailyPaymentLimit;
        this.monthlyPaymentLimit = monthlyPaymentLimit;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
