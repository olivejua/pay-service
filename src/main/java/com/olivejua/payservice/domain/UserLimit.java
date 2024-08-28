package com.olivejua.payservice.domain;

import com.olivejua.payservice.error.ApplicationException;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    public static UserLimit createDefaultSettings(User user) {
        return UserLimit.builder()
                .user(user)
                .maxBalance(10_000_000L)
                .singlePaymentLimit(1_000_000L)
                .dailyPaymentLimit(3_000_000L)
                .monthlyPaymentLimit(7_000_000L)
                .build();
    }

    public void validateIfPaymentAmountDoesNotExceed(long amount, List<Payment> paymentsForThisMonth) {
        validateIfExceedSinglePaymentLimit(amount);
        validateIfExceedDailyPaymentLimit(amount, paymentsForThisMonth);
        validateIfExceedMonthlyPaymentLimit(amount, paymentsForThisMonth);
        validateIfExceedMaxBalance(amount);
    }

    private void validateIfExceedSinglePaymentLimit(long amount) {
        if (singlePaymentLimit < amount) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "SINGLE_PAYMENT_LIMIT_EXCEEDED", "Single payment limit exceeded.");
        }
    }

    private void validateIfExceedDailyPaymentLimit(long amount, List<Payment> paymentsForThisMonth) {
        final LocalDate today = LocalDate.now();
        final long totalAmountForToday = paymentsForThisMonth.stream()
                .filter(payment -> payment.getCreatedAt().toLocalDate().isEqual(today))
                .map(Payment::getAmount)
                .reduce(0L, Long::sum);

        if (dailyPaymentLimit < (totalAmountForToday + amount)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "DAILY_LIMIT_EXCEEDED", "Daily payment limit exceeded.");
        }
    }

    private void validateIfExceedMonthlyPaymentLimit(long amount, List<Payment> paymentsForThisMonth) {
        final long totalAmountForThisMonth = paymentsForThisMonth.stream()
                .map(Payment::getAmount)
                .reduce(0L, Long::sum);
        if (monthlyPaymentLimit < (totalAmountForThisMonth + amount)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "MONTHLY_LIMIT_EXCEEDED", "Monthly payment limit exceeded.");
        }
    }

    private void validateIfExceedMaxBalance(long amount) {
        if (maxBalance < (user.getCurrentBalance() + amount)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "MAX_BALANCE_EXCEEDED", "Payment amount exceeds the user's maximum allowed balance.");
        }
    }
}
