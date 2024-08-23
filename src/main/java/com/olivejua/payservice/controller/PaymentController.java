package com.olivejua.payservice.controller;

import com.olivejua.payservice.controller.request.PaymentCancelRequest;
import com.olivejua.payservice.controller.request.PaymentCreateRequest;
import com.olivejua.payservice.controller.response.PaymentCancelResponse;
import com.olivejua.payservice.controller.response.PaymentCreateResponse;
import com.olivejua.payservice.error.ApplicationException;
import com.olivejua.payservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/pay/payments")
@RestController
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    ResponseEntity<PaymentCreateResponse> createPayment(@RequestBody PaymentCreateRequest request) {
        PaymentCreateResponse response = paymentService.createPayment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{paymentId}/cancel")
    ResponseEntity<PaymentCancelResponse> cancelPayment(@PathVariable("paymentId") Long paymentId, @RequestBody PaymentCancelRequest request) {
        //FIXME 비즈니스 로직 구현 후 제거하기
        if (request.userId().equals(2L)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND_OR_WITHDRAWN", "User does not exist or is in a withdrawn state.");
        }

        if (request.userId().equals(3L)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "UNAUTHORIZED_CANCELLATION", "The requester is not authorized to cancel this payment.");
        }

        if (paymentId.equals(2L)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "PAYMENT_NOT_FOUND", "Payment information not found.");
        }

        if (paymentId.equals(3L)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "ALREADY_CANCELED", "The payment has already been canceled.");
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }
}
