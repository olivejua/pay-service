package com.olivejua.payservice.service;

import com.olivejua.payservice.controller.request.PaymentCancelRequest;
import com.olivejua.payservice.controller.request.PaymentCreateRequest;
import com.olivejua.payservice.controller.response.PaymentCancelResponse;
import com.olivejua.payservice.controller.response.PaymentCreateResponse;
import com.olivejua.payservice.database.entity.PaymentEntity;
import com.olivejua.payservice.database.entity.UserEntity;
import com.olivejua.payservice.database.entity.UserLimitEntity;
import com.olivejua.payservice.database.repository.UserJpaRepository;
import com.olivejua.payservice.database.repository.UserLimitJpaRepository;
import com.olivejua.payservice.domain.Payment;
import com.olivejua.payservice.domain.User;
import com.olivejua.payservice.domain.UserLimit;
import com.olivejua.payservice.domain.type.PaymentStatus;
import com.olivejua.payservice.domain.type.UserStatus;
import com.olivejua.payservice.error.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PaymentService {
    private final UserJpaRepository userRepository;
    private final UserLimitJpaRepository userLimitRepository;

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

    private final long todayTransactionAmount = 2_100_000;
    private final long thisMonthTransactionAmount = 6_300_000;

    public PaymentCreateResponse createPayment(PaymentCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .map(UserEntity::toModel)
                .filter(User::hasActiveStatus)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND_OR_WITHDRAWN", "User does not exist or is in a withdrawn state."));

        UserLimit userLimit = userLimitRepository.findByUserId(user.getId())
                .map(UserLimitEntity::toModel)
                .orElse(UserLimit.createDefaultSettings(user));

        final LocalDate today = LocalDate.now();

        //TODO 구현방법 맞는지 검토해보기
        //TODO 유저의 결제 금액이 유효성검증해서 결제할 수 있는 금액인지 검증하는 기능을 분리하기
        long todayTransactionAmount = paymentRepository.sumOfAmountsBetween(today, today);
        long thisMonthTransactionAmount = paymentRepository.sumOfAmountsBetween(LocalDate.of(today.getYear(), today.getMonth(), 1), LocalDate.of(today.getYear(), today.getMonth(), today.lengthOfMonth()));

        //유저의 한도금액을 넘지 않았는지
        if (userLimit.exceedSinglePaymentLimit(request.amount())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "SINGLE_PAYMENT_LIMIT_EXCEEDED", "Single payment limit exceeded.");
        }

        if (userLimit.exceedDailyPaymentLimit(todayTransactionAmount + request.amount())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "DAILY_LIMIT_EXCEEDED", "Daily payment limit exceeded.");
        }

        if (userLimit.exceedMonthlyPaymentLimit(thisMonthTransactionAmount + request.amount())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "MONTHLY_LIMIT_EXCEEDED", "Monthly payment limit exceeded.");
        }

        if (userLimit.exceedMaxBalance(dummyUser.getCurrentBalance() + request.amount())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "MAX_BALANCE_EXCEEDED", "Payment amount exceeds the user's maximum allowed balance.");
        }

        //결제대행사 transactionId, approvedAt 받아오기
        String transactionId = paymentAgencyHandler.requestPaymentFromAgency();

        //status 두개밖에 없는데 필요한지 고민해보기
        LocalDateTime now = LocalDateTime.now();
        Payment payment = Payment.builder()
                .user(user)
                .amount(request.amount())
                .status(PaymentStatus.DONE)
                .transactionId(transactionId)
                .createdAt(now)
                .updatedAt(now)
                .approvedAt(now)
                .build();
        payment = paymentRepository.save(PaymentEntity.from(payment)).toModel();

        return PaymentCreateResponse.from(payment);
    }


    public PaymentCancelResponse cancelPayment(Long id, PaymentCancelRequest request) {
        //FIXME 비즈니스 로직 구현 후 제거하기
        User user = userRepository.findById(request.userId())
                .map(UserEntity::toModel)
                .filter(User::hasActiveStatus)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND_OR_WITHDRAWN", "User does not exist or is in a withdrawn state."));

        Payment payment = paymentRepository.findById()
                .map(PaymentEntity::toModel)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "PAYMENT_NOT_FOUND", "Payment information not found."));

        if (payment.hasDifferentPayerFrom(user)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "UNAUTHORIZED_CANCELLATION", "The requester is not authorized to cancel this payment.");
        }

        if (payment.hasStatusOf(PaymentStatus.CANCELED)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "ALREADY_CANCELED", "The payment has already been canceled.");
        }

        LocalDateTime canceledAt = paymentAgencyHandler.requestPaymentCancelationFromAgency();

        payment = payment.cancel(canceledAt);
        payment = paymentRepository.save(PaymentEntity.from(payment)).toModel();

        return PaymentCancelResponse.from(payment);
    }
}
