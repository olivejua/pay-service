package com.olivejua.payservice.controller;

import com.olivejua.payservice.controller.request.PaybackCancelRequest;
import com.olivejua.payservice.controller.request.PaybackCreateRequest;
import com.olivejua.payservice.controller.response.PaybackCancelResponse;
import com.olivejua.payservice.controller.response.PaybackCreateResponse;
import com.olivejua.payservice.service.PaybackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequiredArgsConstructor
@RequestMapping("/pay/paybacks")
@RestController
public class PaybackController {
    private final PaybackService paybackService;

    //TODO 페이백 요청은 비동기 요청
    @PostMapping
    public ResponseEntity<PaybackCreateResponse> createPayback(@RequestBody PaybackCreateRequest request) {
        Optional<PaybackCreateResponse> responseOptional = paybackService.createPayback(request);

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
        Optional<PaybackCancelResponse> responseOptional = paybackService.cancelPayback(request);

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
