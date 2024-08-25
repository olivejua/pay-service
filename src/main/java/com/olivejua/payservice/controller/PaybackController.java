package com.olivejua.payservice.controller;

import com.olivejua.payservice.controller.request.PaybackCancelRequest;
import com.olivejua.payservice.controller.request.PaybackCreateRequest;
import com.olivejua.payservice.controller.response.PaybackCreateResponse;
import com.olivejua.payservice.service.PaybackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/pay/paybacks")
@RestController
public class PaybackController {
    private final PaybackService paybackService;

    //TODO 페이백 요청은 비동기 요청
    @PostMapping
    public ResponseEntity<PaybackCreateResponse> createPayback(@RequestBody PaybackCreateRequest request) {
        PaybackCreateResponse response = paybackService.createPayback(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{paybackId}/cancel")
    public ResponseEntity<Object> cancelPayback(@PathVariable Long paybackId, @RequestBody PaybackCancelRequest request) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }
}
