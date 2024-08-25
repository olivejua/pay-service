package com.olivejua.payservice.service;

import com.olivejua.payservice.controller.request.PaybackCreateRequest;
import com.olivejua.payservice.controller.response.PaybackCreateResponse;
import com.olivejua.payservice.database.entity.UserEntity;
import com.olivejua.payservice.database.repository.PaybackJpaRepository;
import com.olivejua.payservice.database.repository.UserJpaRepository;
import com.olivejua.payservice.domain.Payment;
import com.olivejua.payservice.domain.User;
import com.olivejua.payservice.error.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaybackService {
    private final UserJpaRepository userRepository;
    private final PaymentService paymentService;
    private final PaybackJpaRepository paybackRepository;


    public PaybackCreateResponse createPayback(PaybackCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .map(UserEntity::toModel)
                .filter(User::hasActiveStatus)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND_OR_WITHDRAWN", "User does not exist or is in a withdrawn state."));

        Payment payment = paymentService.getById(request.paymentId());
        if (paybackRepository.existsByPaymentId(payment.getId())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "PAYBACK_ALREADY_EXISTS", "Payback for this payment already exists.");
        }



        return null;
    }
}
