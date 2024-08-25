package com.olivejua.payservice.controller.request;

//TODO 금액에 맞는 데이터타입
public record PaybackCreateRequest(
        Long paymentId
) {
}
