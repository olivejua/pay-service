package com.olivejua.payservice.service;

import com.olivejua.payservice.controller.response.PaymentApproveResponse;
import com.olivejua.payservice.controller.response.PaymentCancelResponse;
import com.olivejua.payservice.controller.response.PaymentCreateResponse;
import com.olivejua.payservice.database.entity.PaymentEntity;
import com.olivejua.payservice.database.entity.UserLimitEntity;
import com.olivejua.payservice.database.repository.PaymentJpaRepository;
import com.olivejua.payservice.database.repository.UserLimitJpaRepository;
import com.olivejua.payservice.domain.Payment;
import com.olivejua.payservice.domain.User;
import com.olivejua.payservice.domain.UserLimit;
import com.olivejua.payservice.domain.type.PaymentStatus;
import com.olivejua.payservice.error.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

//TRNASACTION
@RequiredArgsConstructor
@Service
public class PaymentService {
    private final UserLimitJpaRepository userLimitRepository;
    private final PaymentJpaRepository paymentRepository;
    private final PaymentAgencyHandler paymentAgencyHandler;

    public PaymentCreateResponse createPayment(User user, long amount) {
        final LocalDateTime requestDateTime = LocalDateTime.now();

        validateUserLimit(user, amount, requestDateTime);

        Payment payment = Payment.createWithPending(user, amount, requestDateTime);
        payment = paymentRepository.save(PaymentEntity.from(payment)).toModel();

        return PaymentCreateResponse.from(payment);
    }

    private void validateUserLimit(User user, long amount, LocalDateTime requestDateTime) {
        UserLimit userLimit = userLimitRepository.findByUserId(user.getId())
                .map(UserLimitEntity::toModel)
                .orElse(UserLimit.createDefaultSettings(user));

        final LocalDate today = requestDateTime.toLocalDate();

        //TODO 구현방법 맞는지 검토해보기
        //TODO 유저의 결제 금액이 유효성검증해서 결제할 수 있는 금액인지 검증하는 기능을 분리하기
        long todayTransactionAmount = paymentRepository.findTotalAmountByUserIdAndStatusAndCreatedAtBetween(user.getId(), PaymentStatus.COMPLETED, today.atStartOfDay(), today.atTime(23, 59, 59));
        long thisMonthTransactionAmount = paymentRepository.findTotalAmountByUserIdAndStatusAndCreatedAtBetween(user.getId(), PaymentStatus.COMPLETED, LocalDate.of(today.getYear(), today.getMonth(), 1).atStartOfDay(), LocalDate.of(today.getYear(), today.getMonth(), today.lengthOfMonth()).atTime(23, 59, 59));

        //유저의 한도금액을 넘지 않았는지
        if (userLimit.exceedSinglePaymentLimit(amount)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "SINGLE_PAYMENT_LIMIT_EXCEEDED", "Single payment limit exceeded.");
        }

        if (userLimit.exceedDailyPaymentLimit(todayTransactionAmount + amount)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "DAILY_LIMIT_EXCEEDED", "Daily payment limit exceeded.");
        }

        if (userLimit.exceedMonthlyPaymentLimit(thisMonthTransactionAmount + amount)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "MONTHLY_LIMIT_EXCEEDED", "Monthly payment limit exceeded.");
        }

        if (userLimit.exceedMaxBalance(user.getCurrentBalance() + amount)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "MAX_BALANCE_EXCEEDED", "Payment amount exceeds the user's maximum allowed balance.");
        }
    }

    /**
     * 결제 대행사 요청 후 처리
     * - Payment 조회했을 때 존재하지 않으면 예외를 던진다.
     * - 상태가 대기중이 아니라면 처리하지 않고 종료한다.
     * - 결제 요청 후 응답이 5초이내 오지 않으면 timeout 예외를 던진다.
     * - 결제 완료하면 상태 업데이트하고, 응답을 반환한다.
     */
    public Optional<PaymentApproveResponse> approvePayment(Long paymentId) {
        Payment payment = getById(paymentId);

        if (!payment.hasStatusOf(PaymentStatus.PENDING)) {
            return Optional.empty();
        }

        payment = paymentAgencyHandler.requestPaymentFromAgency(payment);
        return Optional.of(PaymentApproveResponse.from(payment));
    }

    public PaymentCancelResponse cancelPayment(User user, Long id) {
        Payment payment = getById(id);

        if (payment.hasDifferentPayerFrom(user)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "UNAUTHORIZED_CANCELLATION", "The requester is not authorized to cancel this payment.");
        }

        if (payment.hasStatusOf(PaymentStatus.CANCELED)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "ALREADY_CANCELED", "The payment has already been canceled.");
        }

        LocalDateTime canceledAt = paymentAgencyHandler.requestCancellationFromAgency();

        payment = payment.cancel(canceledAt);
        payment = paymentRepository.save(PaymentEntity.from(payment)).toModel();

        return PaymentCancelResponse.from(payment);
    }

    public Payment getById(Long id) {
        return paymentRepository.findById(id)
                .map(PaymentEntity::toModel)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "PAYMENT_NOT_FOUND", "Payment information not found."));
    }
}
