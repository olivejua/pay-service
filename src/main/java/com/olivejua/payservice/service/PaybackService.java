package com.olivejua.payservice.service;

import com.olivejua.payservice.controller.response.PaybackCancelResponse;
import com.olivejua.payservice.controller.response.PaybackCreateResponse;
import com.olivejua.payservice.database.entity.PaybackEntity;
import com.olivejua.payservice.database.entity.PaybackPolicyEntity;
import com.olivejua.payservice.database.repository.PaybackJpaRepository;
import com.olivejua.payservice.database.repository.PaybackPolicyJpaRepository;
import com.olivejua.payservice.domain.Payback;
import com.olivejua.payservice.domain.PaybackPolicy;
import com.olivejua.payservice.domain.Payment;
import com.olivejua.payservice.domain.type.PaybackStatus;
import com.olivejua.payservice.error.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@RequiredArgsConstructor
@Service
public class PaybackService {
    private final PaymentService paymentService;
    private final UserService userService;
    private final PaybackJpaRepository paybackRepository;
    private final PaybackPolicyJpaRepository paybackPolicyJpaRepository;
    private final PaybackJpaRepository paybackJpaRepository;

    /**
     * 페이백 지급
     */
    public Optional<PaybackCreateResponse> createPayback(Long paymentId) {
        final Payment payment = paymentService.getById(paymentId);
        if (paybackRepository.existsByPaymentId(payment.getId())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "PAYBACK_ALREADY_EXISTS", "Payback for this payment already exists.");
        }

        List<PaybackPolicyEntity> policies = paybackPolicyJpaRepository.findAllByIsActive(true);

        if (policies.isEmpty()) {
            return Optional.empty();
        }

        // 적용할 정책의 우선순위가 들어가면 좋음
        PaybackPolicy policy = policies.get(0).toModel();

        Payback payback = Payback.from(policy, payment);
        payback = paybackJpaRepository.save(PaybackEntity.from(payback)).toModel();
        userService.addCurrentBalance(payment.getUser().getId(), payback.getAmount());

        return Optional.of(PaybackCreateResponse.from(payback));
    }

    /**
     * 페이백 회수
     */
    public Optional<PaybackCancelResponse> cancelPayback(Long paymentId) {
        final Payment payment = paymentService.getById(paymentId);

        Optional<Payback> paybackOptional = paybackRepository.findByPaymentId(paymentId).map(PaybackEntity::toModel);

        if (paybackOptional.isEmpty()) {
            return Optional.empty();
        }

        Payback payback = paybackOptional.get();
        if (payback.hasStatusOf(PaybackStatus.CANCELED)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "ALREADY_CANCELED", "The payback has already been canceled.");
        }

        payback = payback.cancel();
        payback = paybackRepository.save(PaybackEntity.from(payback)).toModel();
        userService.subtractCurrentBalance(payment.getUser().getId(), payback.getAmount());

        return Optional.of(PaybackCancelResponse.from(payback));
    }
}
