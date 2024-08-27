package com.olivejua.payservice.service;

import com.olivejua.payservice.controller.response.PaymentApproveResponse;
import com.olivejua.payservice.controller.response.PaymentCancelPendingResponse;
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
import com.olivejua.payservice.queue.EventQueues;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Transactional
@RequiredArgsConstructor
@Service
public class PaymentService {
    private final UserLimitJpaRepository userLimitRepository;
    private final PaymentJpaRepository paymentRepository;
    private final PaymentAgencyHandler paymentAgencyHandler;

    /**
     * 결제 요청
     */
    public PaymentCreateResponse createPayment(User requestUser, long amount) {
        final LocalDateTime requestDateTime = LocalDateTime.now();

        validateUserLimit(requestUser, amount, requestDateTime);

        Payment payment = Payment.createWithPending(requestUser, amount, requestDateTime);
        payment = paymentRepository.save(PaymentEntity.from(payment)).toModel();

        final PaymentCreateResponse response = PaymentCreateResponse.from(payment);

        EventQueues.PAYMENT_PROCESSING_QUEUE.add(response.id());

        return response;
    }

    private void validateUserLimit(User requestUser, long amount, LocalDateTime requestDateTime) {
        final UserLimit userLimit = userLimitRepository.findByUserId(requestUser.getId())
                .map(UserLimitEntity::toModel)
                .orElse(UserLimit.createDefaultSettings(requestUser));

        final LocalDate today = requestDateTime.toLocalDate();
        final List<Payment> paymentsForThisMonth = findMonthlyPayments(requestUser, today); // TODO 캐시 고려

        userLimit.validateIfPaymentAmountDoesNotExceed(amount, paymentsForThisMonth);
    }

    private List<Payment> findMonthlyPayments(User user, LocalDate targetDate) {
        final LocalDateTime startOfMonth = targetDate.withDayOfMonth(1).atStartOfDay();
        final LocalDateTime endOfMonth = targetDate.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
        return paymentRepository.findAllByUserIdAndStatusAndCreatedAtBetween(user.getId(), PaymentStatus.COMPLETED, startOfMonth, endOfMonth)
                .stream()
                .map(PaymentEntity::toModel)
                .toList();
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

        if (payment.doesNotHaveStatus(PaymentStatus.PENDING)) {
            return Optional.empty();
        }

        payment = paymentAgencyHandler.requestPaymentFromAgency(payment);
        final PaymentApproveResponse response = PaymentApproveResponse.from(payment);

        EventQueues.PAYBACK_PROCESSING_QUEUE.add(paymentId);

        return Optional.of(response);
    }

    /**
     * 취소 요청하면 취소접수 상태가 된다.
     */
    public PaymentCancelPendingResponse cancelRequestPayment(User requestUser, Long id) {
        Payment payment = getById(id);

        payment.validateIfValidUser(requestUser);
        payment = payment.cancelPending();
        payment = paymentRepository.save(PaymentEntity.from(payment)).toModel();

        final PaymentCancelPendingResponse response = PaymentCancelPendingResponse.from(payment);

        EventQueues.PAYMENT_CANCELLATION_QUEUE.add(response.id());

        return response;
    }

    /**
     * 결제대행사 취소 요청 후 처리
     */
    public Optional<PaymentCancelResponse> cancelPayment(Long paymentId) {
        Payment payment = getById(paymentId);

        if (payment.doesNotHaveStatus(PaymentStatus.CANCEL_PENDING)) {
            return Optional.empty();
        }

        payment = paymentAgencyHandler.requestCancellationFromAgency(payment);
        final PaymentCancelResponse response = PaymentCancelResponse.from(payment);

        EventQueues.PAYBACK_CANCELLATION_QUEUE.add(paymentId);

        return Optional.of(response);
    }

    public Payment getById(Long id) {
        return paymentRepository.findById(id)
                .map(PaymentEntity::toModel)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "PAYMENT_NOT_FOUND", "Payment information not found."));
    }
}
