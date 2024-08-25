package com.olivejua.payservice.controller;

import com.olivejua.payservice.controller.request.PaymentCancelRequest;
import com.olivejua.payservice.controller.request.PaymentCreateRequest;
import com.olivejua.payservice.controller.response.PaymentCancelResponse;
import com.olivejua.payservice.controller.response.PaymentCreateResponse;
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
        final PaymentCreateResponse response = paymentService.createPayment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{paymentId}/cancel")
    ResponseEntity<PaymentCancelResponse> cancelPayment(@PathVariable("paymentId") Long paymentId, @RequestBody PaymentCancelRequest request) {
        final PaymentCancelResponse response = paymentService.cancelPayment(paymentId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
