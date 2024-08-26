package com.olivejua.payservice.controller;

import com.olivejua.payservice.controller.request.PaymentCancelRequest;
import com.olivejua.payservice.controller.request.PaymentCreateRequest;
import com.olivejua.payservice.controller.response.PaymentCancelResponse;
import com.olivejua.payservice.controller.response.PaymentCreateResponse;
import com.olivejua.payservice.domain.User;
import com.olivejua.payservice.service.PaymentService;
import com.olivejua.payservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/pay/payments")
@RestController
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService; // 인증기능이 있다면 인증유저를 가지고 있기 때문에 필요없음

    @PostMapping
    ResponseEntity<PaymentCreateResponse> createPayment(@RequestBody PaymentCreateRequest request) {
        final User activeUser = userService.getActiveUser(request.userId());
        final PaymentCreateResponse response = paymentService.createPayment(activeUser, request.amount());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{paymentId}/cancel")
    ResponseEntity<PaymentCancelResponse> cancelPayment(@PathVariable("paymentId") Long paymentId, @RequestBody PaymentCancelRequest request) {
        final User activeUser = userService.getActiveUser(request.userId());
        final PaymentCancelResponse response = paymentService.cancelPayment(activeUser, paymentId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
