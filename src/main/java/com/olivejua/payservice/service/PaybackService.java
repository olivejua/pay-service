package com.olivejua.payservice.service;

import com.olivejua.payservice.controller.request.PaybackCancelRequest;
import com.olivejua.payservice.controller.request.PaybackCreateRequest;
import com.olivejua.payservice.controller.response.PaybackCancelResponse;
import com.olivejua.payservice.controller.response.PaybackCreateResponse;
import com.olivejua.payservice.database.entity.PaybackEntity;
import com.olivejua.payservice.database.entity.PaybackPolicyEntity;
import com.olivejua.payservice.database.entity.UserEntity;
import com.olivejua.payservice.database.repository.PaybackJpaRepository;
import com.olivejua.payservice.database.repository.PaybackPolicyJpaRepository;
import com.olivejua.payservice.database.repository.UserJpaRepository;
import com.olivejua.payservice.domain.Payback;
import com.olivejua.payservice.domain.PaybackPolicy;
import com.olivejua.payservice.domain.Payment;
import com.olivejua.payservice.domain.User;
import com.olivejua.payservice.domain.type.PaybackStatus;
import com.olivejua.payservice.error.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PaybackService {
    private final UserJpaRepository userRepository;
    private final PaymentService paymentService;
    private final PaybackJpaRepository paybackRepository;
    private final PaybackPolicyJpaRepository paybackPolicyJpaRepository;
    private final PaybackJpaRepository paybackJpaRepository;

    /**
     * TODO 고려사항이 비즈니스에 대한 부분일까 아니면 이슈가 발생할만 곳들을 짚을 수 있는 능력일까?
     * TODO 검증 순서도 맞는지 한번 더 검토해보기
     */
    public Optional<PaybackCreateResponse> createPayback(PaybackCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .map(UserEntity::toModel)
                .filter(User::hasActiveStatus)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND_OR_WITHDRAWN", "User does not exist or is in a withdrawn state."));

        Payment payment = paymentService.getById(request.paymentId());
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

        return Optional.of(PaybackCreateResponse.from(payback));
    }

    public Optional<PaybackCancelResponse> cancelPayback(PaybackCancelRequest request) {
        User user = userRepository.findById(request.userId())
                .map(UserEntity::toModel)
                .filter(User::hasActiveStatus)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND_OR_WITHDRAWN", "User does not exist or is in a withdrawn state."));

        Optional<Payback> paybackOptional = paybackRepository.findByPaymentId(request.paymentId()).map(PaybackEntity::toModel);

        if (paybackOptional.isEmpty()) {
            return Optional.empty();
        }

        Payback payback = paybackOptional.get();
        if (payback.hasStatusOf(PaybackStatus.CANCELED)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "ALREADY_CANCELED", "The payback has already been canceled.");
        }

        payback = payback.cancel();
        payback = paybackRepository.save(PaybackEntity.from(payback)).toModel();

        return Optional.of(PaybackCancelResponse.from(payback));
    }
}
