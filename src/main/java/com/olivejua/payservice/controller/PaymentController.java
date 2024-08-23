package com.olivejua.payservice.controller;

import com.olivejua.payservice.controller.request.PaymentCancelRequest;
import com.olivejua.payservice.controller.request.PaymentCreateRequest;
import com.olivejua.payservice.controller.response.PaymentCancelResponse;
import com.olivejua.payservice.controller.response.PaymentCreateResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/pay/payments")
@RestController
public class PaymentController {

    @PostMapping
    ResponseEntity<PaymentCreateResponse> createPayment(@RequestBody PaymentCreateRequest request) {


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(null);
    }

    @PostMapping("/{paymentId}/cancel")
    ResponseEntity<PaymentCancelResponse> cancelPayment(@PathVariable Long paymentId, @RequestBody PaymentCancelRequest request) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }
}
