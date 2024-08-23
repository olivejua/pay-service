package com.olivejua.payservice.service;

import com.olivejua.payservice.controller.request.PaymentCreateRequest;
import com.olivejua.payservice.controller.response.PaymentCreateResponse;
import com.olivejua.payservice.domain.User;
import com.olivejua.payservice.domain.UserLimit;
import com.olivejua.payservice.domain.type.PaymentStatus;
import com.olivejua.payservice.domain.type.UserStatus;
import com.olivejua.payservice.error.ApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {
    private final User dummyUser = User.builder()
            .id(1L)
            .name("user1")
            .email("user1@gmail.com")
            .status(UserStatus.ACTIVE)
            .currentBalance(9_500_000L)
            .accountBank("NH")
            .accountNumber("123-12-123456-12")
            .createdAt(LocalDateTime.of(2024, 8, 31, 12, 0, 0))
            .updatedAt(LocalDateTime.of(2024, 9, 1, 12, 0, 0))
            .build();

    private final UserLimit dummyUserLimit = UserLimit.builder()
            .id(1L)
            .user(dummyUser)
            .maxBalance(10_000_000L)
            .singlePaymentLimit(1_000_000L)
            .dailyPaymentLimit(3_000_000L)
            .monthlyPaymentLimit(7_000_000L)
            .build();

    private final long todayTransactionAmount = 2_100_000;
    private final long thisMonthTransactionAmount = 6_300_000;

    public PaymentCreateResponse createPayment(PaymentCreateRequest request) {
        //FIXME 비즈니스 로직 구현 후 제거하기
        if (!request.userId().equals(dummyUser.getId())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND_OR_WITHDRAWN", "User does not exist or is in a withdrawn state.");
        }

        if (dummyUserLimit.getSinglePaymentLimit() < request.amount()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "SINGLE_PAYMENT_LIMIT_EXCEEDED", "Single payment limit exceeded.");
        }

        if (dummyUserLimit.getDailyPaymentLimit() < todayTransactionAmount + request.amount()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "DAILY_LIMIT_EXCEEDED", "Daily payment limit exceeded.");
        }

        if (dummyUserLimit.getMonthlyPaymentLimit() < thisMonthTransactionAmount + request.amount()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "MONTHLY_LIMIT_EXCEEDED", "Monthly payment limit exceeded.");
        }

        if (dummyUserLimit.getMaxBalance() < dummyUser.getCurrentBalance() + request.amount()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "MAX_BALANCE_EXCEEDED", "Payment amount exceeds the user's maximum allowed balance.");
        }

        return new PaymentCreateResponse(1L, request.userId(), request.amount(), "NH", "123-12-123456-12", UUID.randomUUID().toString(), LocalDateTime.now(), PaymentStatus.DONE, LocalDateTime.now());
    }
}
