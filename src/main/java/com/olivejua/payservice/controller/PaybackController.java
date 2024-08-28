package com.olivejua.payservice.controller;

import com.olivejua.payservice.controller.request.PaybackCancelRequest;
import com.olivejua.payservice.controller.request.PaybackCreateRequest;
import com.olivejua.payservice.controller.response.PaybackCancelResponse;
import com.olivejua.payservice.controller.response.PaybackCreateResponse;
import com.olivejua.payservice.service.PaybackService;
import com.olivejua.payservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RequiredArgsConstructor
@RequestMapping("/pay/paybacks")
@RestController
public class PaybackController {
    private final PaybackService paybackService;
    private final UserService userService; // 인증기능이 있다면 인증유저를 가지고 있기 때문에 필요없음

    @PostMapping
    public ResponseEntity<PaybackCreateResponse> createPayback(@RequestBody PaybackCreateRequest request) {
        userService.validateIfUserIsActive(request.userId());

        Optional<PaybackCreateResponse> responseOptional = paybackService.createPayback(request.paymentId());

        return responseOptional
                .map(response ->
                        ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(response))
                .orElseGet(() ->
                        ResponseEntity
                        .noContent()
                        .build());
    }

    @PostMapping("/cancel")
    public ResponseEntity<PaybackCancelResponse> cancelPayback(@RequestBody PaybackCancelRequest request) {
        userService.validateIfUserIsActive(request.userId());

        Optional<PaybackCancelResponse> responseOptional = paybackService.cancelPayback(request.paymentId());

        return responseOptional
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.OK)
                                .body(response))
                .orElseGet(() ->
                        ResponseEntity
                                .noContent()
                                .build());
    }
}
