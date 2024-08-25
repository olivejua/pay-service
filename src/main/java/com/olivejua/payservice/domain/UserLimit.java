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

    //TODO Default Setting 다시한번 검토해보기
    public static UserLimit createDefaultSettings(User user) {
        return UserLimit.builder()
                .user(user)
                .maxBalance(10_000_000L)
                .singlePaymentLimit(1_000_000L)
                .dailyPaymentLimit(3_000_000L)
                .monthlyPaymentLimit(7_000_000L)
                .build();
    }

    public boolean exceedSinglePaymentLimit(long amount) {
        return singlePaymentLimit < amount;
    }

    public boolean exceedDailyPaymentLimit(long amount) {
        return dailyPaymentLimit < amount;
    }

    public boolean exceedMonthlyPaymentLimit(long amount) {
        return monthlyPaymentLimit < amount;
    }

    public boolean exceedMaxBalance(long amount) {
        return maxBalance < amount;
    }
}
