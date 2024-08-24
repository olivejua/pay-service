package com.olivejua.payservice.controller;

import com.olivejua.payservice.controller.request.PaybackCancelRequest;
import com.olivejua.payservice.controller.request.PaybackCreateRequest;
import com.olivejua.payservice.controller.response.PaybackCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/pay/paybacks")
@RestController
public class PaybackController {

    @PostMapping
    public ResponseEntity<PaybackCreateResponse> createPayback(@RequestBody PaybackCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(null);
    }

    @PostMapping("/{paybackId}/cancel")
    public ResponseEntity<Object> cancelPayback(@PathVariable Long paybackId, @RequestBody PaybackCancelRequest request) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }
}
